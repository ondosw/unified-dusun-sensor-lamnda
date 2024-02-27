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

	static Integer bandRangeEnd = null;

	static Integer bandRangeStart = null;

	static Integer tagRangeEnd = null;

	static Integer tagRangeStart = null;
	static String tenant = null;

	@SuppressWarnings("unchecked")
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent input, Context context) {
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
		

		response.setRecords(recordList);
		logger.log("Lambda END @ " + new Date());
		return response;
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

}
