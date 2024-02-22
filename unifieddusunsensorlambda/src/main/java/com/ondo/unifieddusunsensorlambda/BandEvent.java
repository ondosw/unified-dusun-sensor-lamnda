package com.ondo.unifieddusunsensorlambda;

public class BandEvent {

	private String recordId;

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	private String rawData;
	private String bandId; //
	private Float curTemp;//
	private Float ambTemp; //
	private Float battery; //
	private Integer accValue; //

	private Integer fwVersion; //
	private Integer rssi;
	private String gatewayBLEMacId;

	private BridgeEvent bridgeEvent;

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
