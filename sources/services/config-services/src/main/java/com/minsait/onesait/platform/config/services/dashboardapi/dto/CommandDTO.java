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

public class CommandDTO {

//	 {"command":"newGadget", 
//	 "information":{
//		 "dashboard":"identificationDashboard",
//		 "gadgetType":"trend", "refresh":20, "ontology":"ontologyTest",
//		 "nameX":"","nameY":"",
//		 "measuresY":[{"name":"a","path":"ontologyTest.ontologyTest.a"},{"name":"b","path":"ontologyTest.ontologyTest.b"}],
//		 "measuresX":[{"name":"time","path":"ontologyTest.ontologyTest.time"}]
//		 }
//	 }

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String command;
	
	@Getter
	@Setter
	private Boolean isPublic;

	@Getter
	@Setter
	private String authorization;

	@Getter
	@Setter
	private InformationDTO information;

}
