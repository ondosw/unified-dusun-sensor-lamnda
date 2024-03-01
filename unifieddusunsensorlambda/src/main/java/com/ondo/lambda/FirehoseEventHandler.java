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

package com.ondo.lambda;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent.Record;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class FirehoseEventHandler
		implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {


	static Integer bandRangeEnd = null;

	static Integer bandRangeStart = null;

	static Integer tagRangeEnd = null;

	static Integer tagRangeStart = null;
	static String tenant = null;
	static AmazonDynamoDB ddb = null;


	@SuppressWarnings("unchecked")
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent input, Context context) {
		ddb = AmazonDynamoDBClientBuilder.defaultClient();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<KinesisAnalyticsInputPreprocessingResponse.Record> recordList = new ArrayList<KinesisAnalyticsInputPreprocessingResponse.Record>();
		 
		System.out.println("Lambda START @ " + new Date());
		KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();
		if (System.getenv("jedis_url") == null) {
			System.out.println("jedis_url Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tenant") == null) {
			System.out.println("tenant Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("bandRangeEnd") == null) {
			System.out.println("bandRangeEnd Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("bandRangeStart") == null) {
			System.out.println("bandRangeStart Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tagRangeEnd") == null) {
			System.out.println("tagRangeEnd Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}
		if (System.getenv("tagRangeStart") == null) {
			System.out.println("tagRangeStart Configuration Is Required ");
			System.out.println("Lambda END @ " + new Date());
			return response;
		}

		bandRangeEnd = Integer.valueOf(System.getenv("bandRangeEnd"));

		bandRangeStart = Integer.valueOf(System.getenv("bandRangeStart"));

		tagRangeEnd = Integer.valueOf(System.getenv("tagRangeEnd"));

		tagRangeStart = Integer.valueOf(System.getenv("tagRangeStart"));
		tenant = System.getenv("tenant");

		if (input.getRecords() == null || input.getRecords().isEmpty()) {
			System.out.println("Its Empty List - No records found! Lambda Ends here!");
			return response;
		}
		Integer bandRangeEnd = Integer.valueOf(System.getenv("bandRangeEnd"));

		Integer bandRangeStart = Integer.valueOf(System.getenv("bandRangeStart"));

		Jedis jedisObj = new Jedis(System.getenv("jedis_url"));
		System.out.println("Initial Record Size =  " + input.getRecords().size());
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

		System.out.println("TOTAL EVENT MSG=" + input.getRecords().size() + " Parsed Record=" + dataList.size());
		LambdaLogic.doLogic(dataList,   objectMapper, bridgeMacIdsAndHeartBeatTime, jedisObj, bandRangeStart,
				bandRangeEnd, tagRangeStart, tagRangeEnd, tenant,recordList,  ddb);

		System.out.println("Lambda END @ " + new Date());
		response.setRecords(recordList);
		return response;
	}

	
}
