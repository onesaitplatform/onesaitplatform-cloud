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
package com.minsait.onesait.platform.security.dashboard.engine.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputMessage {

	private String dashboard;
	private String ds;
	private List<String> group;
	private List<FilterStt> filter;
	private List<ProjectStt> project;
	private List<OrderByStt> sort;
	private long offset;
	private long limit;
	private List<ParamStt> param;
	private String query;
	private String user;
	private String rol;
	private String ontology;
	private String token;

	public InputMessage() {
	};

	public InputMessage(String dashboard, String ds, List<String> group, List<FilterStt> filter,
			List<ProjectStt> project, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param,
			String query, String token) {
		this.dashboard = dashboard;
		this.ds = ds;
		this.group = group;
		this.filter = filter;
		this.project = project;
		this.sort = sort;
		this.offset = offset;
		this.limit = limit;
		this.param = param;
		this.query = query;
		this.token = token;
	}

	public InputMessage(String dashboard, String ds, List<String> group, List<FilterStt> filter,
			List<ProjectStt> project, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param,
			boolean debug, String query, String token) {
		this.dashboard = dashboard;
		this.ds = ds;
		this.group = group;
		this.filter = filter;
		this.project = project;
		this.sort = sort;
		this.offset = offset;
		this.limit = limit;
		this.param = param;
		this.token = token;
	}
}
