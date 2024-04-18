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

import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;

import lombok.Getter;
import lombok.Setter;

public class InformationDTO {

	@Getter
	@Setter
	private String dashboard;

	@Getter
	@Setter
	private DashboardType dashboardType;

	@Getter
	@Setter
	private String gadgetId;

	@Getter
	@Setter
	private String gadgetName;

	@Getter
	@Setter
	private String dashboardDescription;

	@Getter
	@Setter
	private String dashboardStyle;

	@Getter
	@Setter
	private String gadgetType;

	@Getter
	@Setter
	private String refresh;
	@Getter
	@Setter
	private String ontology;

	@Getter
	@Setter
	private String dataSource;

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private AxesDTO axes;

	@Getter
	@Setter
	private GadgetConfDTO gadgetConf;

	@Getter
	@Setter
	private ArrayList<MeasureDTO> columns;

	@Getter
	@Setter
	private MapConfDTO mapConf;

	@Getter
	@Setter
	private FilterDTO[] filters;

	@Getter
	@Setter
	private CustomMenuOptionsDTO[] customMenuOptions;

	@Getter
	@Setter
	private SetupLayout setupLayout;

	@Getter
	@Setter
	private boolean merge = false;

	@Getter
	@Setter
	private String[] assetsID;

	@Getter
	@Setter
	private SynopticElementDTO synopticElement;

}
