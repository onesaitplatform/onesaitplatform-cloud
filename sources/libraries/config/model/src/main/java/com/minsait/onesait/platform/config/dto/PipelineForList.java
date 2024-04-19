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
package com.minsait.onesait.platform.config.dto;

import com.minsait.onesait.platform.config.model.User;
import lombok.Data;

@Data
public class PipelineForList {

	private String id;
	private String identification;
	private String idstreamsets;
	private User user;
	private boolean isPublic;
	private String accessType;

	public PipelineForList(String id, String identification, String idstreamsets, User user, boolean isPublic, String accessType) {
		this.id = id;
		this.identification = identification;
		this.idstreamsets = idstreamsets;
		this.user = user;
		this.isPublic = isPublic;
		this.accessType = accessType;
	}

}