/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
public class GadgetPIEConfDTO {

	/**
	 * JSON for gadget JSON "legend": { "display": true, "fullWidth": false,
	 * "position": "left", "labels": { "padding": 10, "fontSize": 11,
	 * "usePointStyle": false, "boxWidth": 1 } },"elements": { "arc": {
	 * "borderWidth": 1, "borderColor": "#fff" } }, "maintainAspectRatio":false,
	 * "responsive":true, "responsiveAnimationDuration":500,
	 * "circumference":"6.283185307179586", "rotation":"6.283185307179586",
	 * "charType":"doughnut" }
	 */

	private Boolean legendDisplay = true;

	private Boolean legendFullWidth = false;

	private String legendPosition = "left";

	private Integer legendLabelsPadding = 10;

	private Integer legendLabelsFontSize = 11;

	private Boolean legendLabelsUsePointStyle = false;

	private Integer legendLabelsBoxWidth = 1;

	private Integer elementsArcBorderWidth = 1;

	private String elementsArcBorderColor = "#fff";

	private Boolean maintainAspectRatio = false;

	private Boolean responsive = false;

	private Integer responsiveAnimationDuration = 500;

	private Double circumference = 6.283185307179586;

	private String charType = "doughnut";

}
