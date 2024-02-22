package com.ondo.unifieddusunsensorlambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LambdaUtil {
	static float compTemp = 0;
	static final float lowerThreshold = (float) 30.0;
	static final float middleThreshold = (float) 34.5;
	static final float higherThreshold = (float) 36.5;
	static final float minimumCompensation = (float) 1.5;
	static final float middleCompensation = (float) 2.5;
	static final float maximumCompensation = (float) 6.5;
	// Any change here must be reflected in OndoServiceUtil
	static final double indeterminateCondition = 0.7f;

	static Long currentTime() {
		return new Date().getTime();
	}

	static String convertBandId(String batteryVal) {
		return String.valueOf(Integer.parseInt(batteryVal, 16));
	}

	static BandEvent parseRawDataString(final String rawDataString) {
		BandEvent bandEvent = new BandEvent();
		bandEvent.setRawData(rawDataString);

		bandEvent.setFwVersion(Integer.parseInt(rawDataString.substring(8, 10), 16));
		// bandEvent.setRssi(Integer.parseInt((fields[3])));
		// bandEvent.setGatewayBLEMacId(fields[2]);

		bandEvent.setBandId(convertBandId(rawDataString.substring(10, 18)));
		bandEvent.setCurTemp(convertSkinTemp(rawDataString.substring(18, 21)));
		bandEvent.setAmbTemp(convertAmbientTemp(rawDataString.substring(21, 24)));
		bandEvent.setBattery(convertBatteryVal(rawDataString.substring(24, 25)));

		bandEvent.setAccValue(Integer.parseInt(rawDataString.substring(25, 28), 16));

		BridgeEvent bridgeEvent = new BridgeEvent();

		// bridgeEvent.setMacId(fields[2]);

		bridgeEvent.setHeartBeatTime(new Date().getTime());

		bandEvent.setBridgeEvent(bridgeEvent);

		return bandEvent;
	}

	static Float convertBatteryVal(String batteryVal) {
		int num = Integer.parseInt(batteryVal, 16);
		return (3 - (float) num / 10);
	}

	static Float convertAmbientTemp(String ambTemp) {
		return Float.valueOf(Integer.parseInt(ambTemp.substring(0, 2), 16))
				+ (Float.valueOf(ambTemp.substring(2, 3)) / 10);
	}

	static Float convertSkinTemp(String skinTemp) {
		return Float.valueOf(Integer.parseInt(skinTemp.substring(0, 2), 16))
				+ (Float.valueOf(skinTemp.substring(2, 3)) / 10);
	}

	public static float compensateTemperatureV2(float currentTemp) {

		if (currentTemp == lowerThreshold) {
			compTemp = maximumCompensation;
		} else if (currentTemp > lowerThreshold && currentTemp <= middleThreshold) {
			compTemp = (float) (maximumCompensation
					- ((maximumCompensation - middleCompensation) / (middleThreshold - lowerThreshold))
							* (currentTemp - lowerThreshold));
		} else if (currentTemp > middleThreshold && currentTemp <= higherThreshold) {
			compTemp = (float) (middleCompensation
					- ((middleCompensation - minimumCompensation) / (higherThreshold - middleThreshold))
							* (currentTemp - middleThreshold));
		} else if (currentTemp > higherThreshold) {
			compTemp = minimumCompensation;
		}
		return compTemp <= 0 ? 0 : compTemp;

	}

	public static float celsiusToFahrenheit(float celsiusTemp) {
		return (celsiusTemp * 9 / 5) + 32;
	}
	/*
	 * { "data": [
	 * "$GPRP,FA71BEF0EAB7,C1D8ACFD33AB,-51,0DFF5900060000816F1861621FFF0A094F4E444F5F42414E44,1623741675"
	 * ] } ondo/band June 15, 2021, 12:51:09 (UTC+0530) { "data": [
	 * "$HBRP,EE74EF551F28,EE74EF551F28,-127,00000000,1623741668" ] }
	 *
	 */

	public static BridgeEvent parseBridgeRawDataString(final String rawDataString) {
		BridgeEvent bridgeEvent = new BridgeEvent();
		bridgeEvent.setRawData(rawDataString);
		// System.out.println("Before parsing " + rawDataString);
		String[] fields = rawDataString.split(",");

		// System.out.println("Bridge fields 5 " + fields[5].toString());
		bridgeEvent.setMacId(fields[2]);
		if (fields.length > 5) {
			String timePlusSth = fields[5].toString();
			// System.out.println("timePlusSth " + timePlusSth);
			String timeBridge = timePlusSth.substring(0, timePlusSth.length() - 4);
			// System.out.println("timeBridge " + timeBridge);
			Long bridgeTime = Long.parseLong(timeBridge);
			// System.out.println("bridgeTime " + bridgeTime);
			bridgeEvent.setHeartBeatTime(bridgeTime);
		} else
			bridgeEvent.setHeartBeatTime(new Date().getTime());

		return bridgeEvent;
	}

	/*
	 * Notable state �indeterminate� takes place when
	 * 
	 * Wrist temperature meets the alert criteria (by threshold or baseline
	 * deviation) &&
	 * 
	 * WAD <= 0.7 �C
	 */
	// Below logic is USED BY API , any logic change here must also be done at
	// OndoServiceUtil
	public static boolean isDeterminateTemperature(Double curTemp, Double ambTemp, Double alertThreshold) {

		DecimalFormat numberFormat = new DecimalFormat("#.#");

		System.out.println("cur Temp " + curTemp + "amb Temp " + ambTemp);

		if (curTemp != null && ambTemp != null && alertThreshold != null) {

			if (curTemp < alertThreshold) {
				return true; // determinate
			} else {
				// double diff = Math.abs(curTemp - ambTemp);

				if (Double.valueOf(numberFormat.format(Math.abs(curTemp - ambTemp))) <= indeterminateCondition) {
					return false; // Not determinate
				} else {
					return true; // determinate
				}
			}
		} else
			return false;
	}

	// User by Lambda for Warning Temperature
	public static boolean isDeterminateTemperatureFloat(Float curTemp, Float ambTemp, Float alertThreshold) {

		DecimalFormat numberFormat = new DecimalFormat("#.#");

		if (curTemp != null && ambTemp != null) {

			int indeterminateCondition2 = Float.compare(ambTemp, alertThreshold);

			if (indeterminateCondition2 >= 0) {

				return false; // Not determinate
			}

			if (Double.valueOf(numberFormat.format(Math.abs(curTemp - ambTemp))) <= indeterminateCondition) {
				return false; // Not determinate
			} else {
				return true; // determinate
			}

		} else
			return false;
	}

	public static String compress(String str, String inEncoding) {
		if (str == null || str.length() == 0) {
			return str;
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(inEncoding));
			gzip.close();
			return URLEncoder.encode(out.toString("ISO-8859-1"), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decompress(String str, String outEncoding) {
		if (str == null || str.length() == 0) {
			return str;
		}

		try {
			String decode = URLDecoder.decode(str, "UTF-8");

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayInputStream in = new ByteArrayInputStream(decode.getBytes("ISO-8859-1"));
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			return out.toString(outEncoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
