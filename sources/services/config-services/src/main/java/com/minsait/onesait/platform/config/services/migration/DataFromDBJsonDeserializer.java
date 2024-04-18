/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataFromDBJsonDeserializer extends StdDeserializer<DataFromDB> {

	private static final long serialVersionUID = 1L;

	public DataFromDBJsonDeserializer() {
		this(DataFromDB.class);
	}

	protected DataFromDBJsonDeserializer(Class<DataFromDB> t) {
		super(t);
	}

	@Override
	public DataFromDB deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		TypeFactory typeFactory = mapper.getTypeFactory();
		MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);

		JsonNode jsonAllData = mapper.readTree(p);
		DataFromDB data = new DataFromDB();
		JsonNode jsonClasses = jsonAllData.get("allData");
		if (jsonClasses.isArray()) {

			try {
				for (final JsonNode jsonClazz : jsonClasses) {
					String clazzName = jsonClazz.get("class").asText();
					Class<?> clazz = Class.forName(clazzName);
					data.addClass(clazz);
					JsonNode instances = jsonClazz.get("instances");
					processInstances(mapper, mapType, data, clazz, instances);
				}
			} catch (ClassNotFoundException e) {
				log.error("deserialize", e);
				throw new GenericRuntimeOPException("Error obtaining classes", e);
			} catch (Exception e) {
				log.error("deserialize", e);
				throw new GenericRuntimeOPException("Error", e);
			}
		} else {
			throw new GenericRuntimeOPException("Error processing json: it should be an array");
		}

		return data;
	}

	private void processInstances(ObjectMapper mapper, MapType mapType, DataFromDB data, Class<?> clazz,
			JsonNode instances) throws IOException, GenericOPException {

		if (instances.isArray()) {
			for (final JsonNode instance : instances) {
				String id = instance.get("id").asText();
				JsonNode instanceData = instance.get("data");
				HashMap<String, Object> map = mapper.readerFor(mapType).readValue(instanceData);
				data.addInstance(clazz, id, map);
			}
		} else {
			throw new GenericOPException("Error processing json: instances field should be an array");
		}

	}

}
