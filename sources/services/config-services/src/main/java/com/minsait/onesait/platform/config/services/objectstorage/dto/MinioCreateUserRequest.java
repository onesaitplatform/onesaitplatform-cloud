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
package com.minsait.onesait.platform.config.services.objectstorage.dto;

import lombok.Data;

@Data
public class MinioCreateUserRequest {
	
	public MinioCreateUserRequest(String accessKey, String secretKey, String[] groups, String[] policies) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.groups = groups;
		this.policies = policies;
	}
	
	private String accessKey;
	private String secretKey;
	private String[] groups;
	private String[] policies;

}
