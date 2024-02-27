package com.ondo.unifieddusunsensorlambda;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class LambdaLogic {
	static final int smalllerTTLForDashboard = 25;
	static final int SixMonthsTTLForTempLookup = 5000;

	@SuppressWarnings("unchecked")
	public static void doLogic(List<Map<String, Object>> dataList,  ObjectMapper objectMapper,
			Map<String, Long> bridgeMacIdsAndHeartBeatTime, Jedis jedisObj, Integer bandRangeStart,
			Integer bandRangeEnd, Integer tagRangeStart, Integer tagRangeEnd, String tenant,
			List<KinesisAnalyticsInputPreprocessingResponse.Record> recordList, AmazonDynamoDB ddb) {
		List<Map<String, Object>> filteredDataList = new ArrayList<>();
		Map<String, List<Map<String, Object>>> bleMacIdsAndfilteredDataList = new HashMap<>();
		for (Map<String, Object> map : dataList) {

			Map<String, Object> data = (Map<String, Object>) map.get("data");
			if (data.get("attribute") != null && data.get("mac") != null
					&& data.get("attribute").toString().equalsIgnoreCase("gateway.heartbeat")) {
				String macId = map.get("mac").toString().replace(":", "");
				Map<String, Object> data_value = (Map<String, Object>) data.get("value");
				bridgeMacIdsAndHeartBeatTime.put(macId,
						Long.valueOf(data_value.get("current_time").toString() + "000"));
				System.out.println("heartbeat DATA");

			}

			else {
				Map<String, Object> data_value = (Map<String, Object>) data.get("value");

				List<Map<String, Object>> device_list = (List<Map<String, Object>>) data_value.get("device_list");
				for (Map<String, Object> map2 : device_list) {
					if (map2.get("connectable") != null) {
						if ("1".equalsIgnoreCase(map2.get("connectable").toString()) == false) {
							filteredDataList.add(map2);
							System.out.println("SENSOR DATA");
						}
					}
				}
				bleMacIdsAndfilteredDataList.put(map.get("ble_addr").toString().replace(":", ""), filteredDataList);
			}

		}
		System.out.println("TOTALconnectable RECORD=" + filteredDataList.size());

		List<BandEvent> allBandEvents = new ArrayList<>();
		Set<String> bandIds = new HashSet<>();
		List<String> errorListRedis = new ArrayList<>();
		for (String bleMqacId : bleMacIdsAndfilteredDataList.keySet()) {
			for (Map<String, Object> map2 : bleMacIdsAndfilteredDataList.get(bleMqacId)) {

				try {
					BandEvent bandEvent = LambdaUtil.parseRawDataString(map2, jedisObj, objectMapper, 
							errorListRedis, bleMqacId);
					if (Long.valueOf(bandEvent.getBandId()) >= bandRangeStart
							&& Long.valueOf(bandEvent.getBandId()) <= bandRangeEnd) {
						allBandEvents.add(bandEvent);
						bandIds.add(bandEvent.getBandId());
					} else {
						System.out.println(bandEvent.getBandId() + " Is In Invalid Range =(" + bandRangeStart + " - "
								+ bandRangeEnd + ")");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

		}

		Set<String> bandIdsUpdate = new HashSet<>();
		for (String bandid : bandIds) {

			if (Integer.valueOf(bandid) >= tagRangeStart && Integer.valueOf(bandid) <= tagRangeEnd) {
				bandIdsUpdate.add(tenant + ".tagId." + bandid);
			} else {
				bandIdsUpdate.add(tenant + ".bandId." + bandid);
			}

		}

		Map<String, WearerInfo> wearerInfoMap = new HashMap<>();
		try {
			wearerInfoMap = getBandDetails(bandIdsUpdate, objectMapper, jedisObj);
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("wearerInfoMap=" + wearerInfoMap);
		try {
			Map<String, BridgeInfo> bridgeInfoMap = getBridgeDetails(bridgeMacIdsAndHeartBeatTime.keySet(), jedisObj,
					objectMapper);

			for (String mid : bridgeInfoMap.keySet()) {
				BridgeInfo bridgeInfo = bridgeInfoMap.get(mid);
				BridgeEvent bridgeEvent = new BridgeEvent();
				bridgeEvent.setMacId(mid);
				bridgeEvent.setHeartBeatTime(bridgeMacIdsAndHeartBeatTime.get(mid));
				bridgeEvent.setBridgeId(bridgeInfo.getBridgeId());
				updateHeartBeat(bridgeInfo, bridgeEvent, jedisObj, objectMapper);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		List<DBRecord> dbRecords = new ArrayList<DBRecord>();

		// Stopwatch bandStopWatch = new Stopwatch();
		// bandStopWatch.StartTiming();

		try {
			System.out.println("##############UNIFIED + Before PrepareDBRecord ");

			dbRecords = prepareDBRecord(wearerInfoMap, allBandEvents, objectMapper, jedisObj, tenant,

					bandRangeStart, bandRangeEnd, tagRangeStart, tagRangeEnd);
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		List<String> failRecordIdList = prepareToInsertIntoDB(dbRecords, bandRangeStart, bandRangeEnd, tagRangeStart,
				tagRangeEnd, ddb

		);

		for (String string : failRecordIdList) {
			for (KinesisAnalyticsInputPreprocessingResponse.Record txRecord : recordList) {
				if (txRecord.getRecordId().equalsIgnoreCase(string)) {
					txRecord.setResult(Result.ProcessingFailed);
				}
			}
		}
		failRecordIdList = prepareToInsertIntoTempLookupTable(dbRecords, ddb);
		for (String string : failRecordIdList) {
			for (KinesisAnalyticsInputPreprocessingResponse.Record txRecord : recordList) {
				if (txRecord.getRecordId().equalsIgnoreCase(string)) {
					txRecord.setResult(Result.ProcessingFailed);
				}
			}
		}

	}

	private static List<String> prepareToInsertIntoTempLookupTable(List<DBRecord> dbRecords, AmazonDynamoDB ddb) {
		List<String> failRecord = new ArrayList<>();
		int j = 1;
		Map<PutRequest, String> idtorcrodId = new HashMap<>();
		List<String> allRecid = new ArrayList<String>();
		for (int i = 0; i < dbRecords.size() /* && i < 25 */; i++) {

			List<WriteRequest> writeRequests = new ArrayList<>(dbRecords.size());

			for (j = 1; j <= 25 && i < dbRecords.size(); j++, i++) {
				DBRecord dbRecord = dbRecords.get(i);
				PutRequest putRequest = new PutRequest();
				putRequest.addItemEntry("id", new AttributeValue().withS(dbRecord.getId()));

				putRequest.addItemEntry("bandId", new AttributeValue().withS(dbRecord.getBandId()));
				putRequest.addItemEntry("batVolt", new AttributeValue().withN(String.valueOf(dbRecord.getBatVolt())));
				putRequest.addItemEntry("curTemp", new AttributeValue().withN(String.valueOf(dbRecord.getCurTemp())));
				putRequest.addItemEntry("ambTemp", new AttributeValue().withN(String.valueOf(dbRecord.getAmbTemp())));
				putRequest.addItemEntry("accVal", new AttributeValue().withN(String.valueOf(dbRecord.getAccValue())));

				putRequest.addItemEntry("fcltyId", new AttributeValue().withS(dbRecord.getFcltyId()));
				putRequest.addItemEntry("curTime", new AttributeValue().withN(String.valueOf(dbRecord.getCurTime())));
				putRequest.addItemEntry("ttl",
						new AttributeValue().withN("" + EpochTime.epochTTL(SixMonthsTTLForTempLookup)));
				putRequest.addItemEntry("wearerId", new AttributeValue().withS(String.valueOf(dbRecord.getWearerId())));
				putRequest.addItemEntry("rssi", new AttributeValue().withN(String.valueOf(dbRecord.getRssi())));
				putRequest.addItemEntry("fwVersion",
						new AttributeValue().withN(String.valueOf(dbRecord.getFwVersion())));
				putRequest.addItemEntry("gatewayBLEMacId", new AttributeValue().withS(dbRecord.getGatewayBLEMacId()));
				WriteRequest writeRequest = new WriteRequest(putRequest);
				idtorcrodId.put(putRequest, dbRecord.getRecordId());
				allRecid.add(dbRecord.getRecordId());
				writeRequests.add(writeRequest);
			}

			// Do batching
			BatchWriteItemRequest request = new BatchWriteItemRequest();
			request.addRequestItemsEntry("ondoreport", writeRequests);

			// System.out.println("#########JUST BEFORE INSERT " + writeRequests.size());

			i--;
			try {
				System.out.println("Saving In ondoreport" + request);
				BatchWriteItemResult batchWriteItemResult = ddb.batchWriteItem(request);

				if (batchWriteItemResult.getUnprocessedItems() != null) {
					List<WriteRequest> unprocessedItems = batchWriteItemResult.getUnprocessedItems().get("ondoreport");
					if (unprocessedItems != null) {
						for (WriteRequest writeRequest : unprocessedItems) {
							failRecord.add(idtorcrodId.get(writeRequest.getPutRequest()));
							System.out.println("FAIL @ ondoreport " + writeRequest.getPutRequest());
						}
					}

				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.getMessage();
				failRecord.addAll(allRecid);
			}

			// Reinitialize j for anothr batch of 25 records or less
			j = 1;
		}
		return failRecord;

	}

	@SuppressWarnings("unchecked")
	private static Map<String, WearerInfo> getBandDetails(Set<String> bandIdArray, ObjectMapper objectMapper,
			Jedis jedis) throws Exception {
		Map<String, WearerInfo> returnMap = new HashMap<>();
		if (bandIdArray != null && bandIdArray.size() > 0) {
			for (String string : bandIdArray) {
				String json = jedis.get(string);
				if (json != null) {
					Map<String, Object> dat = new HashMap<>();
					dat = objectMapper.readValue(json, dat.getClass());

					WearerInfo wearerInfo = new WearerInfo();
					System.out.println(dat);
					if (dat.containsKey("AssetId")) {
						wearerInfo.setWearerId(dat.get("AssetId").toString());
					} else if (dat.containsKey("wearerId")) {
						wearerInfo.setWearerId(dat.get("wearerId").toString());
					}
					wearerInfo.setFacilityId(dat.get("facilityId").toString());
					wearerInfo.setBandId(string.split("\\.")[2]);

					returnMap.put(wearerInfo.getBandId(), wearerInfo);
				}

			}

		}

		return returnMap;
	}

	private static Map<String, BridgeInfo> getBridgeDetails(Set<String> macIds, Jedis jedis, ObjectMapper objectMapper)
			throws Exception {
		Map<String, BridgeInfo> ret = new HashMap<>();

		if (macIds == null || macIds.size() == 0) {

		} else {

			for (String string : macIds) {
				String val = jedis.get(string);

				if (val != null) {
					BridgeInfo bridgeInfo = objectMapper.readValue(val, BridgeInfo.class);
					ret.put(string, bridgeInfo);
				}

			}

		}
		return ret;
	}

	private static void updateHeartBeat(BridgeInfo bridgeInfo, BridgeEvent bridgeEvent, Jedis jedis,
			ObjectMapper objectMapper) {
		if (bridgeEvent.getMacId() != null) {
			String vals = jedis.get(bridgeEvent.getMacId());
			try {
				if (vals == null) {

					BridgeRedisDTO bridgeRedisObject = new BridgeRedisDTO();
					bridgeRedisObject.setLastHeartBeatTime(new Date().getTime());
					bridgeRedisObject.setBleMacId(bridgeEvent.getMacId());
					bridgeRedisObject.setFacilityId(bridgeInfo.getFacilityId());
					bridgeRedisObject.setBridgeName(bridgeInfo.getBridgeName());
					bridgeRedisObject.setBridgeId(bridgeInfo.getBridgeId());
					jedis.set(bridgeEvent.getMacId(), objectMapper.writeValueAsString(bridgeRedisObject));

				} else {

					BridgeRedisDTO bridgeRedisObject = objectMapper.readValue(vals, BridgeRedisDTO.class);
					bridgeRedisObject.setLastHeartBeatTime(new Date().getTime());
					jedis.set(bridgeEvent.getMacId(), objectMapper.writeValueAsString(bridgeRedisObject));

				}
			} catch (Exception e) {
				System.out.print(e.getMessage());
			}
		}

	}

	static final Long deduptime = 1000L * 60;

	@SuppressWarnings("unchecked")
	private static Boolean recordLastTempAndTime(WearerInfo wInfo, BandEvent bandEvent, ObjectMapper objectMapper,
			Jedis jedisObj, String tenants, Integer bandRangeStart, Integer bandRangeEnd, Integer tagRangeStart,
			Integer tagRangeEnd) throws Exception {
		try {
			Integer bandid = Integer.valueOf(wInfo.getBandId());
			if (bandid >= bandRangeStart && bandid <= bandRangeEnd) {
				WearerInfo wearerInfo = objectMapper.readValue(jedisObj.get(tenants + ".bandId." + wInfo.getBandId()),
						WearerInfo.class);
				wearerInfo.setLastTemp(bandEvent.getCurTemp());
				wearerInfo.setLastTempTime(new Date().getTime());
				wearerInfo.setFwVersion(bandEvent.getFwVersion());
				jedisObj.set(tenants + ".bandId." + wInfo.getBandId(), objectMapper.writeValueAsString(wearerInfo));
			} else if (bandid >= tagRangeStart && bandid <= tagRangeEnd) {
				Map<String, Object> tagData = new HashMap<>();
				tagData = objectMapper.readValue(jedisObj.get(tenants + ".tagId." + wInfo.getBandId()),
						tagData.getClass());
				if (tagData == null) {
					tagData = new HashMap<>();
					tagData.put("tagId", bandid);
				}
				tagData.put("lastTemp", bandEvent.getCurTemp());
				tagData.put("lastTempTime", new Date().getTime());
				tagData.put("fwVersion", bandEvent.getFwVersion());
				jedisObj.set(tenants + ".tagId." + wInfo.getBandId(), objectMapper.writeValueAsString(tagData));
				return false;
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return true;
	}

	private static List<String> prepareToInsertIntoDB(List<DBRecord> dbRecords, Integer bandRangeStart,
			Integer bandRangeEnd, Integer tagRangeStart, Integer tagRangeEnd, AmazonDynamoDB ddb) {

		List<String> failRecord = new ArrayList<>();
		int j = 1;
		for (int i = 0; i < dbRecords.size() /* && i < 25 */; i++) {
			List<String> allRecid = new ArrayList<String>();
			List<WriteRequest> writeRequests = new ArrayList<>(dbRecords.size());
			Map<PutRequest, String> idtorcrodId = new HashMap<>();
			for (j = 1; j <= 25 && i < dbRecords.size(); j++, i++) {

				DBRecord dbRecord = dbRecords.get(i);

				PutRequest putRequest = new PutRequest();
				putRequest.addItemEntry("id", new AttributeValue().withS(dbRecord.getId()));

				putRequest.addItemEntry("bandId", new AttributeValue().withS(dbRecord.getBandId()));

				Integer bandid = Integer.valueOf(dbRecord.getBandId());
				if (bandid >= bandRangeStart && bandid <= bandRangeEnd) {
					putRequest.addItemEntry("batVolt",
							new AttributeValue().withN(String.valueOf(dbRecord.getBatVolt())));
					putRequest.addItemEntry("curTemp",
							new AttributeValue().withN(String.valueOf(dbRecord.getCurTemp())));
					putRequest.addItemEntry("ambTemp",
							new AttributeValue().withN(String.valueOf(dbRecord.getAmbTemp())));
					putRequest.addItemEntry("accVal",
							new AttributeValue().withN(String.valueOf(dbRecord.getAccValue())));
					putRequest.addItemEntry("displayName",
							new AttributeValue().withS(dbRecord.getFName() + " ---->> " + dbRecord.getLName()));
					putRequest.addItemEntry("fcltyId", new AttributeValue().withS(dbRecord.getFcltyId()));

					putRequest.addItemEntry("curTime",
							new AttributeValue().withN(String.valueOf(dbRecord.getCurTime())));
					putRequest.addItemEntry("ttl",
							new AttributeValue().withN("" + EpochTime.epochTTL(smalllerTTLForDashboard)));
					putRequest.addItemEntry("wearerId",
							new AttributeValue().withS(String.valueOf(dbRecord.getWearerId())));
					WriteRequest writeRequest = new WriteRequest(putRequest);
					writeRequests.add(writeRequest);
				}

			}

			// Do batching
			BatchWriteItemRequest request = new BatchWriteItemRequest();
			request.addRequestItemsEntry("unified-dashboard", writeRequests);

			// System.out.println("#########JUST BEFORE INSERT " + writeRequests.size());

			i--;
			try {
				System.out.println("Saving In dashboard" + request);
				BatchWriteItemResult batchWriteItemResult = ddb.batchWriteItem(request);

				if (batchWriteItemResult.getUnprocessedItems() != null) {
					List<WriteRequest> unprocessedItems = batchWriteItemResult.getUnprocessedItems()
							.get("unified-dashboard");
					if (unprocessedItems != null) {
						for (WriteRequest writeRequest : unprocessedItems) {
							System.out.println("Fail @ Dashboard " + writeRequest.getPutRequest());
							failRecord.add(idtorcrodId.get(writeRequest.getPutRequest()));
						}
					}

				}
			} catch (Exception e) {
				failRecord.addAll(allRecid);
				System.err.println(e.getMessage());
				e.getMessage();
			}

			j = 1;
		}
		return failRecord;
	}

	private static List<DBRecord> prepareDBRecord(Map<String, WearerInfo> wearerInfoMap, List<BandEvent> bandEvents,
			ObjectMapper objectMapper, Jedis jedis, String tenant, Integer bandRangeStart, Integer bandRangeEnd,
			Integer tagRangeStart, Integer tagRangeEnd

	) throws Exception {
		List<DBRecord> dbRecords = new ArrayList<>();
		for (BandEvent bandEvent : bandEvents) {

			WearerInfo wearerInfo = wearerInfoMap.get(bandEvent.getBandId());

			Long timeDiff = 0L;

			if ((null != wearerInfo) && (null != wearerInfo.getLastTempTime())) {
				timeDiff = LambdaUtil.currentTime() - wearerInfo.getLastTempTime();

			}

			if ((null != wearerInfo) && (timeDiff >= deduptime)
					|| (null != wearerInfo) && (null == wearerInfo.getLastTempTime())
					|| (null != wearerInfo) && bandEvent.getAccValue() == 0) // ODR

			{
				System.out.println("#########UNIFIED " + "Inside Dedup check");
				WearerInfo wInfo = wearerInfoMap.get(bandEvent.getBandId());
				Boolean band = recordLastTempAndTime(wInfo, bandEvent, objectMapper, jedis, tenant, bandRangeStart,
						bandRangeEnd, tagRangeStart, tagRangeEnd);
				if (band) {
					DBRecord dbRecord = mapToDBRecord(wearerInfoMap.get(bandEvent.getBandId()), bandEvent);
					System.out.println("dbRecord==================>>>" + dbRecord.getWearerId());
					dbRecord.setRecordId(bandEvent.getRecordId());
					dbRecords.add(dbRecord);
				}

			}
		}
		return dbRecords;
	}

	private static DBRecord mapToDBRecord(WearerInfo wearerInfo, BandEvent bandEvent)
			throws NumberFormatException, Exception {

		DBRecord dbRecord = DBRecord.builder().setId(UUID.randomUUID().toString()).setBandId(wearerInfo.getBandId())
				.setWearerId(wearerInfo.getWearerId()).setFcltyId(wearerInfo.getFacilityId())
				.setfName(wearerInfo.getFirstName()).setlName(wearerInfo.getLastName())
				.setGrpId(wearerInfo.getWearerGroupId())
				.setAltTH(wearerInfo.getAlertThreshold() != null
						? Double.valueOf(String.valueOf(wearerInfo.getAlertThreshold()))
						: null)
				.setAmbTemp(bandEvent.getAmbTemp()).setBatVolt(Double.valueOf(String.valueOf(bandEvent.getBattery())))
				.setCurTemp(Double.valueOf(String.valueOf(bandEvent.getCurTemp()))).setAccValue(bandEvent.getAccValue())
				.setCurTime(new Date().getTime()).setFwVersion(bandEvent.getFwVersion()).setRssi(bandEvent.getRssi())
				.setGatewayBLEMacId(bandEvent.getGatewayBLEMacId()).build();
		return dbRecord;
	}

}
