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
package com.minsait.onesait.platform.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HazelcastRuleObject {
	private String identification;
	private String userId;
	private String DRL;
	private byte[] decisionTable;
	private String extension;

	private static final ObjectMapper mapper = new ObjectMapper();

	public Optional<String> toJson() {
		try {
			return Optional.of(mapper.writeValueAsString(this));
		} catch (final Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<HazelcastRuleObject> fromJson(String json) {
		try {
			return Optional.of(mapper.readValue(json, HazelcastRuleObject.class));

		} catch (final Exception e) {
			return Optional.empty();
		}
	}
}
