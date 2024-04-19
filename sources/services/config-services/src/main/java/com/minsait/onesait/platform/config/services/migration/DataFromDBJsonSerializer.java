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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataFromDBJsonSerializer extends StdSerializer<DataFromDB> {

	private static final long serialVersionUID = 1L;

	public DataFromDBJsonSerializer() {
		this(DataFromDB.class);
	}

	protected DataFromDBJsonSerializer(Class<DataFromDB> t) {
		super(t);
	}

	@Override
	public void serialize(DataFromDB value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Set<Class<?>> classes = value.data.keySet();

		gen.writeStartObject();
		gen.writeArrayFieldStart("allData");
		for (Class<?> clazz : classes) {
			gen.writeStartObject();
			gen.writeStringField("class", clazz.getName());
			gen.writeArrayFieldStart("instances");
			log.debug("********* SERIALIZE			: " + clazz.getName());

			Map<Serializable, Map<String, Object>> instance = value.data.get(clazz);
			Set<Serializable> ids = instance.keySet();
			for (Serializable id : ids) {
				gen.writeStartObject();
				gen.writeObjectField("id", id);
				gen.writeObjectFieldStart("data");
				Map<String, Object> data = instance.get(id);
				Set<String> fields = data.keySet();
				log.debug("********* ID			: " + id);
				for (String field : fields) {
					log.debug("********* FIELD			: " + field);
					Object object = data.get(field);
					log.debug("                 value			: " + (object != null ? object.getClass() : null));
					gen.writeObjectField(field, object);
				}
				gen.writeEndObject();
				gen.writeEndObject();
			}
			gen.writeEndArray();
			gen.writeEndObject();
		}
		gen.writeEndArray();
		gen.writeEndObject();

	}

}
