/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.services.dashboardapi.dto;

import lombok.Data;

@Data
public class GadgetTableConfDTO {

	/**
	 * JSON for gadget TABLE
	 * "{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\","
	 * +
	 * "\"trHeightFooter\":\"40\",\"textColorTHead\":\"#060e14\",\"textColorBody\":\"#555555\",\"textColorFooter\":\"#555555\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,"
	 * +
	 * "\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}"
	 */

	private String tablePaginationLimit = "100";
	private Integer tablePaginationPage = 1;
	private Integer[] tablePaginationLimitOptions = new Integer[] { 5, 10, 20, 50, 100 };
	private String tablePaginationStyleBackGroundTHead = "#ffffff";
	private String tablePaginationStyleBackGroundTFooter = "#ffffff";
	private String tablePaginationStyleTrHeightHead = "40";
	private String tablePaginationStyleTrHeightBody = "40";
	private String tablePaginationStyleTrHeightFooter = "40";
	private String tablePaginationStyleTextColorTHead = "#060e14";
	private String tablePaginationStyleTextColorBody = "#555555";
	private String tablePaginationStyleTextColorFooter = "#555555";
	private Boolean tablePaginationOptionsRowSelection = false;
	private Boolean tablePaginationOptionsMultiSelect = false;
	private Boolean tablePaginationOptionsAutoSelect = false;
	private Boolean tablePaginationOptionsDecapitate = false;
	private Boolean tablePaginationOptionsLargeEditDialog = false;
	private Boolean tablePaginationOptionsBoundaryLinks = true;
	private Boolean tablePaginationOptionsLimitSelect = true;
	private Boolean tablePaginationOptionsPageSelect = true;

}
