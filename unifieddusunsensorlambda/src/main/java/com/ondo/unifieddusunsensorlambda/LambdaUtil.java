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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class LambdaUtil {
	static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
	static Long currentTime() {
		return new Date().getTime();
	}
	@SuppressWarnings("unchecked")
	static BandEvent parseRawDataString(Map<String, Object> map2, Jedis jedis, ObjectMapper objectMapper,
			  List<String> errorListRedis, String bleMacId) throws Exception {

		BandEvent bandEvent = new BandEvent();
		bandEvent.setRawData(map2.get("data").toString());

		String rawDataString = map2.get("data").toString();

		bandEvent.setFwVersion(Integer.valueOf(rawDataString.substring(8, 10), 16));
		 
		bandEvent.setGatewayBLEMacId(bleMacId);

		String valuefromRedis = jedis.get(bleMacId);
		BridgeEvent bridgeEvent = new BridgeEvent();
		Map<String, Object> mapDataFromRedis = new HashMap<String, Object>();
		String facilityId = null;
		if (valuefromRedis == null) {
			System.out.println(" Value is null from Redis for GatewayBLEMacId= " + bleMacId);
			return null;
		} else {

			mapDataFromRedis = objectMapper.readValue(valuefromRedis, mapDataFromRedis.getClass());
			bridgeEvent.setBridgeId(mapDataFromRedis.get("bridgeId").toString());
			if (mapDataFromRedis.containsKey("facilityId") == false) {
				return null;
			}
			facilityId = mapDataFromRedis.get("facilityId").toString();
		}

		String bandId = Long.valueOf(convertBandId(rawDataString.substring(10, 18))).toString();

		System.out.println("Band id:: from MSG" + bandId);
		String bandDataJSON = null;
		String erroBandKy = "";
		if (Integer.valueOf(bandId) >= DusunSesnsorEventHandler.tagRangeStart
				&& Integer.valueOf(bandId) <= DusunSesnsorEventHandler.tagRangeEnd) {
			bandDataJSON = jedis.get(DusunSesnsorEventHandler.tenant + ".tagId." + bandId);
			erroBandKy = DusunSesnsorEventHandler.tenant + ".tagId." + bandId;
		} else {
			bandDataJSON = jedis.get(DusunSesnsorEventHandler.tenant + ".bandId." + bandId);
			erroBandKy = DusunSesnsorEventHandler.tenant + ".bandId." + bandId;
		}

		if (bandDataJSON == null) {
			errorListRedis.add(DusunSesnsorEventHandler.tenant + ".bandId." + bandId);
			return null;
		}
		mapDataFromRedis = objectMapper.readValue(bandDataJSON, mapDataFromRedis.getClass());

		if (mapDataFromRedis.containsKey("facilityId") == false
				|| mapDataFromRedis.get("facilityId").toString().equalsIgnoreCase(facilityId) == false) {
			System.out.println("this is error case as facilityId DIFFRENT IN KEY  " + bandEvent.getGatewayBLEMacId()
					+ " AND  KEY " + (erroBandKy) + mapDataFromRedis + "\n" + rawDataString);
			return null;
		}
		bandEvent.setBandId(bandId);
		bandEvent.setCurTemp(convertSkinTemp(rawDataString.substring(18, 21)));
		bandEvent.setAmbTemp(convertAmbientTemp(rawDataString.substring(21, 24)));
		bandEvent.setBattery(convertBatteryVal(rawDataString.substring(24, 25)));
		bandEvent.setAccValueX(Integer.parseInt(rawDataString.substring(25, 26), 16));
		 
		bridgeEvent.setMacId(bleMacId);

		bridgeEvent.setHeartBeatTime(Long.valueOf(map2.get("scan_time").toString() + "000"));

		bandEvent.setBridgeEvent(bridgeEvent);

		return bandEvent;

	}

	static String convertBandId(String batteryVal) {
		return String.valueOf(Long.valueOf(batteryVal, 16));
	}

	static Float convertSkinTemp(String skinTemp) {
		return Float.valueOf(Long.valueOf(skinTemp.substring(0, 2), 16))
				+ (Float.valueOf(skinTemp.substring(2, 3)) / 10);
	}

	static Float convertBatteryVal(String batteryVal) {
		int num = Integer.valueOf(batteryVal, 16);
		return (3 - (float) num / 10);
	}

	static Float convertAmbientTemp(String ambTemp) {
		return Float.valueOf(Long.valueOf(ambTemp.substring(0, 2), 16)) + (Float.valueOf(ambTemp.substring(2, 3)) / 10);
	}

 

}
