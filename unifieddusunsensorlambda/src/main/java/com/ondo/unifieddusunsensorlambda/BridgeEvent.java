package com.ondo.unifieddusunsensorlambda;

public class BridgeEvent {

	private String rawData;
	private String macId; //
	private long heartBeatTime;
	private String recordId;

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

	public String getMacId() {
		return macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}

	public long getHeartBeatTime() {
		return heartBeatTime;
	}

	public void setHeartBeatTime(long heartBeatTime) {
		this.heartBeatTime = heartBeatTime;
	}

	@Override
	public String toString() {
		return "BridgeEvent [rawData=" + rawData + ", macId=" + macId + ", heartBeatTime=" + heartBeatTime
				+ ", recordId=" + recordId + "]";
	}

}
