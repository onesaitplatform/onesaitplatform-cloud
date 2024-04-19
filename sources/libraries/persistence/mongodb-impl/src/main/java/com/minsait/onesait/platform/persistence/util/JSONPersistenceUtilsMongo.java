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
package com.minsait.onesait.platform.persistence.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONPersistenceUtilsMongo {

	private JSONPersistenceUtilsMongo() {

	}

	private static final String PROPERTIES_STR = "properties";

	public static List<String> getGeoIndexes(String schemaString) {
		JSONObject jsonObj = new JSONObject(schemaString);
		List<String> output = new ArrayList<>();

		JSONObject properties = jsonObj.getJSONObject(PROPERTIES_STR);
		Iterator<String> it = properties.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			JSONObject o = (JSONObject) properties.get(key);
			Object type = o.get("type");
			if (type instanceof String && "object".equalsIgnoreCase((String) type)) {
				try {
					JSONObject coordinates = o.getJSONObject(PROPERTIES_STR).getJSONObject("coordinates");
					JSONObject theType = o.getJSONObject(PROPERTIES_STR).getJSONObject("type");
					if (theType != null && coordinates != null)
						output.add(key);
				} catch (Exception e) {
					log.error("Error getting properties", e);
					throw new JSONException(e);
				}
			}

		}
		return output;
	}
}
