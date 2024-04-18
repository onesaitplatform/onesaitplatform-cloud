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

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

public class SchemaFromDBJsonDeserializer extends StdDeserializer<SchemaFromDB> {

	private static final long serialVersionUID = 1L;

	public SchemaFromDBJsonDeserializer() {
		this(SchemaFromDB.class);
	}

	protected SchemaFromDBJsonDeserializer(Class<SchemaFromDB> t) {
		super(t);
	}

	@Override
	public SchemaFromDB deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		TypeFactory typeFactory = mapper.getTypeFactory();
		MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

		JsonNode jsonAllData = mapper.readTree(p);
		SchemaFromDB schema = new SchemaFromDB();
		JsonNode jsonClasses = jsonAllData.get("schema");
		if (jsonClasses.isArray()) {
			for (final JsonNode jsonClazz : jsonClasses) {
				String className = jsonClazz.get("class").asText();
				JsonNode fields = jsonClazz.get("fields");
				HashMap<String, String> map = mapper.readerFor(mapType).readValue(fields);
				schema.addClass(className, map);
			}
		} else {
			throw new GenericRuntimeOPException("Error processing json: it should be an array");

		}

		return schema;
	}
}
