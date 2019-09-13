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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

public class MultiDocumentOperationResult {

	private static final String COUNT_PROPERTY = "count";
	private static final String IDS_PROPERTY = "ids";

	@Getter
	@Setter
	private long count;

	@Getter
	@Setter
	private List<String> ids;

	@Getter
	@Setter
	private String strIds;

	public String toString() {
		return this.toJSONObject().toString();
	}

	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(COUNT_PROPERTY, count);
		if (null != ids) {
			jsonObject.put(IDS_PROPERTY, ids);
		}

		return jsonObject;
	}

	public static MultiDocumentOperationResult fromJSONObject(JSONObject obj) {
		MultiDocumentOperationResult result = new MultiDocumentOperationResult();

		if (obj.get(COUNT_PROPERTY) instanceof Integer) {
			result.setCount((Integer) obj.get(COUNT_PROPERTY));
		} else if ((obj.get(COUNT_PROPERTY) instanceof Long)) {
			result.setCount((Long) obj.get(COUNT_PROPERTY));
		}

		if (obj.has(IDS_PROPERTY) && null != obj.get(IDS_PROPERTY)) {
			ArrayList<String> ids = new ArrayList<>();

			JSONArray lIds = (JSONArray) obj.get(IDS_PROPERTY);
			for (int i = 0; i < lIds.length(); i++) {
				ids.add(lIds.getString(i));
			}
			result.setIds(ids);

			result.setStrIds(obj.get(IDS_PROPERTY).toString());
		}

		return result;
	}

	public static MultiDocumentOperationResult fromString(String data) {
		JSONObject obj = new JSONObject(data);
		return fromJSONObject(obj);
	}

}
