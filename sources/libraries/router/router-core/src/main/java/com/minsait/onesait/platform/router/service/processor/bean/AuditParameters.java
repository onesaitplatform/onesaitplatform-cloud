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
package com.minsait.onesait.platform.router.service.processor.bean;

import lombok.Getter;
import lombok.Setter;

public class AuditParameters {
	
	@Getter
	@Setter
	private String user;
	
	@Getter
	@Setter
	private String eventType;
	
	@Getter
	@Setter
	private String operationType;

	public AuditParameters(String user, String eventType, String operationType) {
		super();
		this.user = user;
		this.eventType = eventType;
		this.operationType = operationType;
	}

}
