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

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class SynopticElementDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String aggregationType;

	@Getter
	@Setter
	private String aggregationField;

	@Getter
	@Setter
	private String projectionField;

	@Getter
	@Setter
	private ArrayList<WhereDTO> where;

	@Getter
	@Setter
	private String classType;

	@Getter
	@Setter
	private ColorDTO color;

	@Getter
	@Setter
	private ConditionDTO condition;

	@Getter
	@Setter
	private String typeElement;

	@Getter
	@Setter
	private String unitsOfMeasure;

	@Getter
	@Setter
	private String classTypeList[];

	@Getter
	@Setter
	private String aggregationList[];

	@Getter
	@Setter
	private String eventsList[];

	@Getter
	@Setter
	private ArrayList<EventDTO> events;

	@Getter
	@Setter
	private String limit;

	@Getter
	@Setter
	private ArrayList<OrderByDTO> orderBy;

}
