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
package com.minsait.onesait.platform.config.services.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class LoadEntityResult {

	@Getter
	private Map<Class<?>, Map<Serializable, Object>> entities;
	@Getter
	private MigrationErrors errors;

	public LoadEntityResult(Map<Class<?>, Map<Serializable, Object>> entities, MigrationErrors errors) {
		this.entities = entities;
		this.errors = errors;
	}

	public void append(LoadEntityResult other) {
		this.entities.putAll(other.entities);
		this.errors.addErrors(other.errors);
	}

	public List<Object> getAllObjects() {
		ArrayList<Object> objects = new ArrayList<>();
		Collection<Map<Serializable, Object>> values = entities.values();
		for (Map<Serializable, Object> value : values) {
			objects.addAll(value.values());
		}
		return objects;
	}
}
