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
package com.minsait.onesait.platform.commons.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

public class InsertResult {

	public static final String TYPE_PROPERTY = "type";
	public static final String DATA_PROPERTY = "data";

	@Getter
	@Setter
	private ComplexWriteResultType type;

	@Getter
	@Setter
	private Object data;

	@Override
	public String toString() {
		JSONObject instanceJson = new JSONObject();
		instanceJson.put(TYPE_PROPERTY, type.name());
		if (data instanceof MultiDocumentOperationResult) {
			instanceJson.put(DATA_PROPERTY, ((MultiDocumentOperationResult) data).toJSONObject());
		} else if (data instanceof List) {
			JSONArray lData = new JSONArray();
			((List<TimeSeriesResult>) data).forEach(partialResult -> {
				lData.put(partialResult.toJSONObject());
			});
			instanceJson.put(DATA_PROPERTY, lData);
		}

		return instanceJson.toString();
	}

}
