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
package com.minsait.onesait.platform.controlpanel.controller.gis.layer;

import lombok.Getter;
import lombok.Setter;

public class LayerDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String size;

	@Getter
	@Setter
	private Boolean isPublic;

	@Getter
	@Setter
	private Boolean isFilter;

	@Getter
	@Setter
	private String ontology;

	@Getter
	@Setter
	private String query;
	@Getter
	@Setter
	private String queryParams;

	@Getter
	@Setter
	private String geometryField;

	@Getter
	@Setter
	private String geometryType;

	@Getter
	@Setter
	private String innerColor;

	@Getter
	@Setter
	private String outerColor;

	@Getter
	@Setter
	private String outerThin;

	@Getter
	@Setter
	private String url;

	@Getter
	@Setter
	private String layerTypeWms;

	@Getter
	@Setter
	private String externalType;

	@Getter
	@Setter
	private String infoBox;

	@Getter
	@Setter
	private String filters;

	@Getter
	@Setter
	private Integer refreshTime;

	@Getter
	@Setter
	private String weightField;

	@Getter
	@Setter
	private String heatMapMin;

	@Getter
	@Setter
	private String heatMapMax;

	@Getter
	@Setter
	private String heatMapRadius;

	@Getter
	@Setter
	private Boolean isHeatMap;

	@Getter
	@Setter
	private String west;

	@Getter
	@Setter
	private String east;

	@Getter
	@Setter
	private String south;

	@Getter
	@Setter
	private String north;

	@Getter
	@Setter
	private Boolean isVirtual;

}
