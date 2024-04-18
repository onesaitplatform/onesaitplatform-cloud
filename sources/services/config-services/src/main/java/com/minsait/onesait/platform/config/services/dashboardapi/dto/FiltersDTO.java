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

import lombok.Getter;
import lombok.Setter;

public class FiltersDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	private String field;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String op;

	@Getter
	@Setter
	private String typeAction;
	@Getter
	@Setter
	private boolean initialFilter = false;
	@Getter
	@Setter
	private boolean useLastValue = false;
	@Getter
	@Setter
	private boolean filterChaining = false;

	@Getter
	@Setter
	private DataDTO data;

	@Getter
	@Setter
	private TargetDTO targetList[];

	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private boolean hide = false;;

}
