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

import com.fasterxml.jackson.annotation.JsonProperty;
 
public class WearerInfo {
	
	@JsonProperty("fwVersion")
	private Integer fwVersion;
	@JsonProperty("alertThreshold")
	private Float alertThreshold;
	@JsonProperty("wearerId")
	private String wearerId;
	
	@JsonProperty("firstName")
	private String firstName;

	@JsonProperty("lastName")
	private String lastName;	

	@JsonProperty("facilityId")
	private String facilityId;
	
	@JsonProperty("bandId")
	private String bandId;

	@JsonProperty("wearerGroupId")
	private String wearerGroupId;

	@JsonProperty("alertThresholdId")
	private Float alertThresholdId;

	@JsonProperty("alertSpike")
	private String alertSpike;

	@JsonProperty("localId")
	private String localId;
	
	@JsonProperty("baseLine")
	private String baseLine;
	
	@JsonProperty("createdDate")
	private String createdDate;

	@JsonProperty("pNEligible")
	private Integer pNEligible;

	@JsonProperty("pNSentTime")
	private Long pNSentTime;
	
	@JsonProperty("lastWarningTempTime")
	private Long lastWarningTempTime;
	
	@JsonProperty("lastWarningTemp")
	private Float lastWarningTemp;
	
	@JsonProperty("lastTempTime")
	private Long lastTempTime;
	
	@JsonProperty("lastTemp")
	private Float lastTemp;
	
	@JsonProperty("location")
	private boolean location;
	
	@JsonProperty("locationAlert")
	private Boolean locationAlert;
	
	@JsonProperty("gracePeriod")
	private Integer gracePeriod;
	
	@JsonProperty("locationWarningSentTime")
	private Long locationWarningSentTime;

	public boolean isLocation() {
		return location;
	}

	public void setLocation(boolean location) {
		this.location = location;
	}

	public String getWearerId() {
		return wearerId;
	}

	public void setWearerId(String wearerId) {
		this.wearerId = wearerId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getBandId() {
		return bandId;
	}

	public void setBandId(String bandId) {
		this.bandId = bandId;
	}

	public String getWearerGroupId() {
		return wearerGroupId;
	}

	public void setWearerGroupId(String wearerGroupId) {
		this.wearerGroupId = wearerGroupId;
	}

	public Float getAlertThresholdId() {
		return alertThresholdId;
	}

	public void setAlertThresholdId(Float alertThresholdId) {
		this.alertThresholdId = alertThresholdId;
	}

	public String getAlertSpike() {
		return alertSpike;
	}

	public void setAlertSpike(String alertSpike) {
		this.alertSpike = alertSpike;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getBaseLine() {
		return baseLine;
	}

	public void setBaseLine(String baseLine) {
		this.baseLine = baseLine;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getpNEligible() {
		return pNEligible;
	}
	
	public Integer getPNEligible() {
		return pNEligible;
	}


	public void setpNEligible(Integer pNEligible) {
		this.pNEligible = pNEligible;
	}
	public void setPNEligible(Integer pNEligible) {
		this.pNEligible = pNEligible;
	}
	

	public Long getpNSentTime() {
		return pNSentTime;
	}
	public Long getPNSentTime() {
		return pNSentTime;
	}
	
	

	public void setpNSentTime(Long pNSentTime) {
		this.pNSentTime = pNSentTime;
	}
	public void setPNSentTime(Long pNSentTime) {
		this.pNSentTime = pNSentTime;
	}
	

	public Long getLastWarningTempTime() {
		return lastWarningTempTime;
	}

	public void setLastWarningTempTime(Long lastWarningTempTime) {
		this.lastWarningTempTime = lastWarningTempTime;
	}

	public Float getLastWarningTemp() {
		return lastWarningTemp;
	}

	public void setLastWarningTemp(Float lastWarningTemp) {
		this.lastWarningTemp = lastWarningTemp;
	}

	public Long getLastTempTime() {
		return lastTempTime;
	}

	public void setLastTempTime(Long lastTempTime) {
		this.lastTempTime = lastTempTime;
	}

	public Float getLastTemp() {
		return lastTemp;
	}

	public void setLastTemp(Float lastTemp) {
		this.lastTemp = lastTemp;
	}


	public Boolean getLocationAlert() {
		return locationAlert;
	}

	public void setLocationAlert(Boolean locationAlert) {
		this.locationAlert = locationAlert;
	}

	public Integer getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(Integer gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	
	public Long getLocationWarningSentTime() {
		return locationWarningSentTime;
	}

	public void setLocationWarningSentTime(Long locationWarningSentTime) {
		this.locationWarningSentTime = locationWarningSentTime;
	}

	public Integer getFwVersion() {
		return fwVersion;
	}

	public void setFwVersion(Integer fwVersion) {
		this.fwVersion = fwVersion;
	}

	public Float getAlertThreshold() {
		return alertThreshold;
	}

	public void setAlertThreshold(Float alertThreshold) {
		this.alertThreshold = alertThreshold;
	}

	 
	
	
	

}