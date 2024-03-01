/*  ****************************************************************************

 *  INTELLECTUAL PROPERTY RIGHTS
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

public class BandEvent {

	private String recordId;
	private String rawData;
	private String bandId;
	private Float curTemp;
	private Float ambTemp;
	private Float battery;
	private Integer accValue;
	private Integer sessionId;
	private Integer fwVersion;
	private Integer rssi;
	private String gatewayBLEMacId;

	private BridgeEvent bridgeEvent;
	private Integer accValueX;
	private Integer accValueY;
	private Integer accValueZ;
	

	public Integer getAccValueX() {
		return accValueX;
	}

	public void setAccValueX(Integer accValueX) {
		this.accValueX = accValueX;
	}

	public Integer getAccValueY() {
		return accValueY;
	}

	public void setAccValueY(Integer accValueY) {
		this.accValueY = accValueY;
	}

	public Integer getAccValueZ() {
		return accValueZ;
	}

	public void setAccValueZ(Integer accValueZ) {
		this.accValueZ = accValueZ;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public void setSessionId(Integer sessionId) {
		this.sessionId = sessionId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public String getBandId() {
		return bandId;
	}

	public void setBandId(String bandId) {
		this.bandId = bandId;
	}

	public Float getCurTemp() {
		return curTemp;
	}

	public void setCurTemp(Float curTemp) {
		this.curTemp = curTemp;
	}

	public Float getAmbTemp() {
		return ambTemp;
	}

	public void setAmbTemp(Float ambTemp) {
		this.ambTemp = ambTemp;
	}

	public Float getBattery() {
		return battery;
	}

	public void setBattery(Float battery) {
		this.battery = battery;
	}

	public Integer getAccValue() {
		return accValue;
	}

	public void setAccValue(Integer accValue) {
		this.accValue = accValue;
	}

	public Integer getFwVersion() {
		return fwVersion;
	}

	public void setFwVersion(Integer fwVersion) {
		this.fwVersion = fwVersion;
	}

	public Integer getRssi() {
		return rssi;
	}

	public void setRssi(Integer rssi) {
		this.rssi = rssi;
	}

	public String getGatewayBLEMacId() {
		return gatewayBLEMacId;
	}

	public void setGatewayBLEMacId(String gatewayBLEMacId) {
		this.gatewayBLEMacId = gatewayBLEMacId;
	}

	public BridgeEvent getBridgeEvent() {
		return bridgeEvent;
	}

	public void setBridgeEvent(BridgeEvent bridgeEvent) {
		this.bridgeEvent = bridgeEvent;
	}

	@Override
	public String toString() {
		return "BandEvent [recordId=" + recordId + ", rawData=" + rawData + ", bandId=" + bandId + ", curTemp="
				+ curTemp + ", ambTemp=" + ambTemp + ", battery=" + battery + ", accValue=" + accValue + ", fwVersion="
				+ fwVersion + ", rssi=" + rssi + ", gatewayBLEMacId=" + gatewayBLEMacId + ", bridgeEvent=" + bridgeEvent
				+ "]";
	}

}
