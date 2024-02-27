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
package com.ondo.unifieddusunsensorlambda;

public class BridgeRedisDTO {

	private String bridgeId;

	private String facilityId;

	private String bleMacId;

	private String bridgeName;

	private Long lastHeartBeatTime;

	public String getBridgeId() {
		return bridgeId;
	}

	public void setBridgeId(String bridgeId) {
		this.bridgeId = bridgeId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getBleMacId() {
		return bleMacId;
	}

	public void setBleMacId(String bleMacId) {
		this.bleMacId = bleMacId;
	}

	public String getBridgeName() {
		return bridgeName;
	}

	public void setBridgeName(String bridgeName) {
		this.bridgeName = bridgeName;
	}

	public Long getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}

	public void setLastHeartBeatTime(Long lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}

}
