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
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class DusunSesnsorEventHandler
		implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent input, Context context) {
		LambdaLogger logger = context.getLogger();
		KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();
		if (System.getenv("jedis_url") == null) {
			logger.log("jedis_url Configuration Is Required ");
			return response;
		}
		if (System.getenv("bandRangeEnd") == null) {
			logger.log("bandRangeEnd Configuration Is Required ");
			return response;
		}
		if (System.getenv("bandRangeStart") == null) {
			logger.log("bandRangeStart Configuration Is Required ");
		}

		Integer bandRangeEnd = Integer.valueOf(System.getenv("bandRangeEnd"));

		Integer bandRangeStart = Integer.valueOf(System.getenv("bandRangeStart"));

		Jedis jedisObj = new Jedis(System.getenv("jedis_url"));

		logger.log("Lambda START @ " + new Date());
		if (input.getRecords() == null || input.getRecords().isEmpty()) {
			logger.log("Its Empty List - No records found! Lambda Ends here! @ " + new Date());
			jedisObj.close();
			return response;
		}
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
		}
		logger.log("TOTAL EVENT MSG=" + input.getRecords().size() + " Parsed Record=" + dataList.size());
		Set<String> filteredDataList = new HashSet<>();
		for (Map<String, Object> map : dataList) {
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			Map<String, Object> data_value = (Map<String, Object>) data.get("value");

			List<Map<String, Object>> device_list = (List<Map<String, Object>>) data_value.get("device_list");
			for (Map<String, Object> map2 : device_list) {
				if (map2.get("connectable") != null) {
					if ("1".equalsIgnoreCase(map2.get("connectable").toString()) == false) {
						filteredDataList.add(map2.get("data").toString());
					}
				}
			}
		}
		System.out.println("TOTALconnectable RECORD=" + filteredDataList.size());
		for (String map : filteredDataList) {
			logger.log("DATA ::" + map);
		}
		List<BandEvent> allBandEvents = new ArrayList<>();
		for (String rawDataString : filteredDataList) {

			BandEvent bandEvent = LambdaUtil.parseRawDataString(rawDataString);
			if (Long.valueOf(bandEvent.getBandId()) >= bandRangeStart
					&& Long.valueOf(bandEvent.getBandId()) <= bandRangeEnd) {
				allBandEvents.add(bandEvent);
			} else {
				logger.log(bandEvent.getBandId() + " Is In Invalid Range =(" + bandRangeStart + " - " + bandRangeEnd
						+ ")");
			}

		}
		for (BandEvent bandEvent : allBandEvents) {
			logger.log(bandEvent.toString());
		}
		logger.log("Lambda END @ " + new Date());
		jedisObj.close();
		return response;
	}

}
