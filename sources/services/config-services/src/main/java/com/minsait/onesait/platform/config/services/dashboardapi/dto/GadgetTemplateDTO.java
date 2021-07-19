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

public class GadgetTemplateDTO {

	@Getter
	@Setter
	private String identification;
	@Getter
	@Setter
	private String id;
	@Getter
	@Setter
	private String user;
	@Getter
	@Setter
	private String description;
	@Getter
	@Setter
	private boolean isPublic;
	@Getter
	@Setter
	private String template;
	@Getter
	@Setter
	private String templateJS;
	@Getter
	@Setter
	private String type;
	@Getter
	@Setter
	private String headerlibs;

}
