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
package com.minsait.onesait.platform.config.services.migration;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaFromDB {

	Map<String, Map<String, String>> schema = new HashMap<>();

	void addClass(Class<?> clazz) {

		// if previous information about a class exists, it is overridden.
		schema.put(clazz.getName(), new HashMap<>());

		Map<String, Field> fields = MigrationUtils.getAllFields(clazz);

		if (!fields.isEmpty()) {
			Map<String, String> classInfo = schema.get(clazz.getName());
			for (String fieldName : fields.keySet()) {
				Type genericType = fields.get(fieldName).getGenericType();
				classInfo.put(fieldName, genericType.getTypeName());
			}
		}
	}

	void addClass(String className, HashMap<String, String> map) {
		schema.put(className, map);
	}

	Set<String> getClasses() {
		return schema.keySet();
	}

	Map<String, String> getFields(String className) {
		return schema.get(className);
	}

	public boolean hasClazz(String className) {
		return schema.containsKey(className);
	}
}
