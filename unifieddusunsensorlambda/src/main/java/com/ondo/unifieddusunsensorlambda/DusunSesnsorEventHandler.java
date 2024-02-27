/*  INTELLECTUAL PROPERTY RIGHTS
*
*  Copyright (c) 2023 ONDO System Inc.  All Rights Reserved.
*
*  NOTICE:  All title and intellectual property rights in and to this software
*  product (the “Software”) (including but not limited to any images,
*  photographs, animations, video, audio, music, text, and any other media
*  incorporated into the Software), the accompanying printed materials, and
*  any copies of the Software are owned by ONDO System Inc. or its suppliers. The
*  intellectual and technical concepts contained in the Software are covered
*  by one or more U.S. and foreign patents as well as trade secrets and
*  copyright law.  All title and intellectual property rights in and to the
*  content that is not contained in the Software, but may be accessed through
*  use of the Software, is the property of the respective content owners and
*  may be protected by applicable copyright or other intellectual property
*  laws and treaties.
*
*  The Software may be used only in accordance with the terms of the license
*  agreement between ONDO System and its authorized licensees.  Dissemination or
*  reproduction of this Software or of the information contained herein is
*  strictly forbidden unless prior written permission is obtained from ONDO System
*  Inc.
*
*  EXCEPT AS MAY OTHERWISE BE EXPRESSLY SET FORTH IN A SPECIFIC LICENSE
*  AGREEMENT, WITH RESPECT TO THE SOFTWARE AND THE USE THEREOF, ONDO System MAKES
*  NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, STATUTORY OR
*  OTHERWISE, INCLUDING BUT NOT LIMITED TO, ANY WARRANTY OF MERCHANTABILITY,
*  SATISFACTORY QUALITY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
*  ONDO System SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, PUNITIVE
*  OR CONSEQUENTIAL DAMAGES, OR FOR ANY LOSS OF PROFITS, LOSS OF DATA, LOSS OF
*  BUSINESS OR LOSS OF OPPORTUNITY (WHETHER DIRECT OR INDIRECT) ARISING UNDER
*  THIS SOFTWARE LICENSE, AND REGARDLESS OF WHETHER THE POSSIBILITY OF ANY
*  SUCH LOSSES OR DAMAGES WAS FORSEEABLE OR OTHERWISE DISCLOSED TO ONDO System.
*  ONDO System SHALL HAVE NO LIABILITY OR OBLIGATION FOR ANY DAMAGES THAT ARISE
*  FROM THE USE OF A PRODUCT AS PART OF OR IN COMBINATION WITH ANY DEVICES,
*  PARTS OR THIRD PARTY PRODUCTS THAT ARE NOT PROVIDED BY ONDO System AND ARE
*  INCONSISTENT WITH THE DESIGNED PURPOSE OF THE ONDO System PRODUCT.
*
*  ************************************************************************/

package com.ondo.unifieddusunsensorlambda;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent.Record;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class DusunSesnsorEventHandler
		implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {
 
	static final int smalllerTTLForDashboard = 25;
	static Integer bandRangeEnd = null;

	static Integer bandRangeStart = null;

	static Integer tagRangeEnd = null;

	static Integer tagRangeStart = null;
	static String tenant = null;
	static AmazonDynamoDB ddb=null;
	static final int SixMonthsTTLForTempLookup = 5000;

	@SuppressWarnings("unchecked")
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent input, Context context) {
		  ddb=  AmazonDynamoDBClientBuilder.defaultClient();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<KinesisAnalyticsInputPreprocessingResponse.Record> recordList = new ArrayList<KinesisAnalyticsInputPreprocessingResponse.Record>();
		LambdaLogger logger = context.getLogger();
		logger.log("Lambda START @ " + new Date());
		KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();
		if (System.getenv("jedis_url") == null) {
			logger.log("jedis_url Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tenant") == null) {
			logger.log("tenant Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("bandRangeEnd") == null) {
			logger.log("bandRangeEnd Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("bandRangeStart") == null) {
			logger.log("bandRangeStart Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tagRangeEnd") == null) {
			logger.log("tagRangeEnd Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tagRangeStart") == null) {
			logger.log("tagRangeStart Configuration Is Required ");
			logger.log("Lambda END @ " + new Date());
			return response;
		}

		bandRangeEnd = Integer.valueOf(System.getenv("bandRangeEnd"));

		bandRangeStart = Integer.valueOf(System.getenv("bandRangeStart"));

		tagRangeEnd = Integer.valueOf(System.getenv("tagRangeEnd"));

		tagRangeStart = Integer.valueOf(System.getenv("tagRangeStart"));
		tenant = System.getenv("tenant");

		if (input.getRecords() == null || input.getRecords().isEmpty()) {
			logger.log("Its Empty List - No records found! Lambda Ends here!");
			return response;
		}
		Integer bandRangeEnd = Integer.valueOf(System.getenv("bandRangeEnd"));

		Integer bandRangeStart = Integer.valueOf(System.getenv("bandRangeStart"));

		Jedis jedisObj = new Jedis(System.getenv("jedis_url"));
		logger.log("Initial Record Size =  " + input.getRecords().size());
		Map<String, Long> bridgeMacIdsAndHeartBeatTime = new HashMap<>();
		List<Map<String, Object>> dataList = new ArrayList<>();
		for (Record record : input.getRecords()) {

			KinesisAnalyticsInputPreprocessingResponse.Record responseRecord = new KinesisAnalyticsInputPreprocessingResponse.Record();
			responseRecord.setRecordId(record.getRecordId());
			responseRecord.setResult(Result.Ok);
			String rawDataString = new String(record.getData().array(), UTF_8);
			Map<String, Object> oneRecord = new HashMap<String, Object>();
			try {
				oneRecord = objectMapper.readValue(rawDataString, oneRecord.getClass());
				dataList.add(oneRecord);

			} catch (Exception e) {

				e.printStackTrace();
			}
			recordList.add(responseRecord);
		}
		logger.log("TOTAL EVENT MSG=" + input.getRecords().size() + " Parsed Record=" + dataList.size());
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

			}

			else {
				Map<String, Object> data_value = (Map<String, Object>) data.get("value");

				List<Map<String, Object>> device_list = (List<Map<String, Object>>) data_value.get("device_list");
				for (Map<String, Object> map2 : device_list) {
					if (map2.get("connectable") != null) {
						if ("1".equalsIgnoreCase(map2.get("connectable").toString()) == false) {
							filteredDataList.add(map2);
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
					BandEvent bandEvent = LambdaUtil.parseRawDataString(map2, jedisObj, objectMapper, logger,
							errorListRedis, bleMqacId);
					if (Long.valueOf(bandEvent.getBandId()) >= bandRangeStart
							&& Long.valueOf(bandEvent.getBandId()) <= bandRangeEnd) {
						allBandEvents.add(bandEvent);
						bandIds.add(bandEvent.getBandId());
					} else {
						logger.log(bandEvent.getBandId() + " Is In Invalid Range =(" + bandRangeStart + " - "
								+ bandRangeEnd + ")");
					}
				} catch (Exception e) {
					logger.log(e.getMessage());
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
		logger.log("wearerInfoMap=" + wearerInfoMap);
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
			logger.log(e.getMessage());
		}
		List<DBRecord> dbRecords = new ArrayList<DBRecord>();

		// Stopwatch bandStopWatch = new Stopwatch();
		// bandStopWatch.StartTiming();

		try {
			System.out.println("##############UNIFIED + Before PrepareDBRecord ");

			dbRecords = prepareDBRecord(wearerInfoMap,  allBandEvents,objectMapper,jedisObj,tenant );
		}   catch (Exception e1) {
			 
			e1.printStackTrace();
		}
		List<String> failRecordIdList = prepareToInsertIntoDB(dbRecords);

		for (String string : failRecordIdList) {
			for (KinesisAnalyticsInputPreprocessingResponse.Record txRecord : recordList) {
				if (txRecord.getRecordId().equalsIgnoreCase(string)) {
					txRecord.setResult(Result.ProcessingFailed);
				}
			}
		}
		failRecordIdList = prepareToInsertIntoTempLookupTable(dbRecords);
		for (String string : failRecordIdList) {
			for (KinesisAnalyticsInputPreprocessingResponse.Record txRecord : recordList) {
				if (txRecord.getRecordId().equalsIgnoreCase(string)) {
					txRecord.setResult(Result.ProcessingFailed);
				}
			}
		}
		response.setRecords(recordList);
		logger.log("Lambda END @ " + new Date());
		return response;
	}
	private List<String> prepareToInsertIntoTempLookupTable(List<DBRecord> dbRecords) {
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

	private void updateHeartBeat(BridgeInfo bridgeInfo, BridgeEvent bridgeEvent, Jedis jedis,
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

	@SuppressWarnings("unchecked")
	private Map<String, WearerInfo> getBandDetails(Set<String> bandIdArray, ObjectMapper objectMapper, Jedis jedis)
			throws Exception {
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

	private Map<String, BridgeInfo> getBridgeDetails(Set<String> macIds, Jedis jedis, ObjectMapper objectMapper)
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

	static final Long deduptime = 1000L * 60;

	@SuppressWarnings("unchecked")
	private Boolean recordLastTempAndTime(WearerInfo wInfo, BandEvent bandEvent, ObjectMapper objectMapper,
			Jedis jedisObj, String tenants) throws Exception {
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

	private List<String> prepareToInsertIntoDB(List<DBRecord> dbRecords) {

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

	private  List<DBRecord> prepareDBRecord(Map<String, WearerInfo> wearerInfoMap, List<BandEvent> bandEvents,
			ObjectMapper objectMapper, Jedis jedis, String tenant) throws Exception {
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
				Boolean band = recordLastTempAndTime(wInfo, bandEvent, objectMapper, jedis, tenant);
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
	private DBRecord mapToDBRecord(WearerInfo wearerInfo, BandEvent bandEvent) throws NumberFormatException, Exception {

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
