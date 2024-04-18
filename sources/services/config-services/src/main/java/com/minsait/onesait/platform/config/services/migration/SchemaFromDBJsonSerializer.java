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
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class SchemaFromDBJsonSerializer extends StdSerializer<SchemaFromDB>{

	private static final long serialVersionUID = 1L;

	public SchemaFromDBJsonSerializer() {
		this(SchemaFromDB.class);
	}
	
	protected SchemaFromDBJsonSerializer(Class<SchemaFromDB> t) {
		super(t);
	}

	@Override
	public void serialize(SchemaFromDB value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Set<String> classes = value.schema.keySet();
		
		gen.writeStartObject();
		gen.writeArrayFieldStart("schema");
		for (String className : classes) {
			gen.writeStartObject();
			gen.writeStringField("class", className);
			
			Map<String, String> fieldData = value.schema.get(className);
			Set<String> fields = fieldData.keySet();
			gen.writeObjectFieldStart("fields");
			for (String field  : fields) {
				gen.writeObjectField(field, fieldData.get(field));			
			}
			gen.writeEndObject();

			gen.writeEndObject();
		}
		gen.writeEndArray();
		gen.writeEndObject();
		
	}

}
