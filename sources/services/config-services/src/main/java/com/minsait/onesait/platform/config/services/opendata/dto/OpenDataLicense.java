/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.config.services.opendata.dto;

import lombok.Getter;
import lombok.Setter;

public class OpenDataLicense {
	
	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String title;	
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private String status;
	
	@Getter
	@Setter
	private String is_generic;
	
	@Getter
	@Setter
	private String maintainer;
	
	@Getter
	@Setter
	private String od_conformance;
	
	@Getter
	@Setter
	private String osd_conformance;
	
	@Getter
	@Setter
	private String family;
	
	@Getter
	@Setter
	private String domain_data;
	
	@Getter
	@Setter
	private boolean is_okd_compliant;
	
	@Getter
	@Setter
	private boolean is_osi_compliant;
	
	@Getter
	@Setter
	private String domain_content;
	
	@Getter
	@Setter
	private String domain_software;
	

}
