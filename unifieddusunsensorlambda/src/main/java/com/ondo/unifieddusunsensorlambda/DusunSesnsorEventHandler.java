package com.ondo.unifieddusunsensorlambda;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent.Record;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DusunSesnsorEventHandler
		implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent input, Context context) {
		LambdaLogger logger = context.getLogger();
		KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();
		logger.log("Lambda START @ " + new Date());
		if (input.getRecords() == null || input.getRecords().isEmpty()) {
			logger.log("Its Empty List - No records found! Lambda Ends here! @ " + new Date());
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
		List<Map<String, Object>> filteredDataList = new ArrayList<>();
		for (Map<String, Object> map : dataList) {
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			Map<String, Object> data_value = (Map<String, Object>) data.get("value");

			List<Map<String, Object>> device_list = (List<Map<String, Object>>) data_value.get("device_list");
			for (Map<String, Object> map2 : device_list) {
				if (map2.get("connectable") != null) {
					if ("1".equalsIgnoreCase(map2.get("connectable").toString())) {
						filteredDataList.add(map2);
					}
				}
			}
		}
		logger.log("TOTALconnectable RECORD=" + filteredDataList.size());
		logger.log("Lambda END @ " + new Date());
		return null;
	}

}
