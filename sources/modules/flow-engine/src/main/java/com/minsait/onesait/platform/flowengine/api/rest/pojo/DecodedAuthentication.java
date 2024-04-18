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
package com.minsait.onesait.platform.flowengine.api.rest.pojo;

import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DecodedAuthentication {

	@Getter
	@Setter
	@JsonProperty("id_usuario")
	private String userId;

	@Getter
	@Setter
	private String credentials;

	@Getter
	@Setter
	private String verticalSchema;

	private static final String SPLITTER = ":";

	public DecodedAuthentication(String authentication) {
		final String auth = new String(Base64.getDecoder().decode(authentication));
		userId = auth.split(SPLITTER)[0];
		credentials = auth.split(SPLITTER)[1];
		try {
			verticalSchema = auth.split(SPLITTER)[2];
		} catch (final Exception e) {
			log.debug("no vertical in authentication");
		}
	}
}
