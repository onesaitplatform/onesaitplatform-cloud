/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
public class GadgetMixConfDTO {
	/**
	 * JSON for gadget
	 * "{\"legend\":{\"display\":false,\"fullWidth\":false,\"labels\":{\"padding\":10,\"fontSize\":11,
	 * \"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,
	 * \"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,
	 * \"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\
	 * "ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":10},\
	 * "gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\
	 * "ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\
	 * "labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",
	 * \"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",
	 * \"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}"
	 */

	private Boolean legendDisplay = true;
	private Boolean legendFullWidth = false;
	private Integer legendLabelsPadding = 10;
	private Integer legendLabelsFontSize = 11;
	private Boolean legendLabelsUsePointStyle = false;
	private Integer legendLabelsBoxWidth = 2;

	private String scalesYAxesId = "#0";
	private Boolean scalesYAxesDisplay = true;
	private String scalesYAxesType = "linear";
	private String scalesYAxesPosition = "left";
	private String scalesYAxesScaleLabelLabelString = "";
	private Boolean scalesYAxesScaleLabelDisplay = true;
	private String scalesYAxesScaleLabelFontFamily = "Soho";
	private Integer scalesYAxesScaleLabelPadding = 4;
	private Boolean scalesYAxesStacked = false;
	private Boolean scalesYAxesSort = true;
	private String scalesYAxesTicksSuggestedMin = "0";
	private String scalesYAxesTicksSuggestedMax = "1000";
	private String scalesYAxesTicksMaxTicksLimit = "10";
	private Boolean scalesYAxesGridLinesDisplay = false;

	private Boolean scalesXAxesStacked = false;
	private Boolean scalesXAxesSort = true;
	private String scalesXAxesTicksFontFamily = "Soho";
	private String scalesXAxesScaleLabelLabelString = "";
	private Boolean scalesXAxesScaleLabelDisplay = true;
	private String scalesXAxesHideLabel = "2";
	private Integer scalesXAxesScaleLabelPadding = 4;
	private Boolean scalesXAxesGridLinesDisplay = true;
	private Integer[] scalesXAxesGridLinesBorderDash = new Integer[] { 2, 4 };
	private String scalesXAxesGridLinesColor = "#CCC";
	private Integer[] scalesXAxesGridLinesZeroLineBorderDash = new Integer[] { 2, 4 };
	private String scalesXAxesGridLinesZeroLineColor = "transparent";
}
