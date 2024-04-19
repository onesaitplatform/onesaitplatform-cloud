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
	private static final String DATA_PROPERTY = "data";

	@Getter
	@Setter
	private long count;

	@Getter
	@Setter
	private List<String> ids;

	@Getter
	@Setter
	private String strIds;

	@Override
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
		
		
		if (obj.has(DATA_PROPERTY) && obj.get(DATA_PROPERTY) instanceof JSONArray) {
			//TIMESERIES
			
			List<String> ids = new ArrayList<>();
			JSONArray data = obj.getJSONArray(DATA_PROPERTY);
			int len = data.length();
			
			result.setCount(len);
			
			result.setIds(ids);
		} else {
			//NON TIMESERIES
			if (obj.has(DATA_PROPERTY) && obj.getJSONObject(DATA_PROPERTY).has(IDS_PROPERTY)
					&& null != obj.getJSONObject(DATA_PROPERTY).get(IDS_PROPERTY)) {
				if (obj.getJSONObject(DATA_PROPERTY).get(COUNT_PROPERTY) instanceof Integer) {
					result.setCount((Integer) obj.getJSONObject(DATA_PROPERTY).get(COUNT_PROPERTY));
				} else if ((obj.getJSONObject(DATA_PROPERTY).get(COUNT_PROPERTY) instanceof Long)) {
					result.setCount((Long) obj.getJSONObject(DATA_PROPERTY).get(COUNT_PROPERTY));
				}
			} else if (!obj.has(DATA_PROPERTY) && obj.has(IDS_PROPERTY) && null != obj.get(IDS_PROPERTY)) {
				if (obj.get(COUNT_PROPERTY) instanceof Integer) {
					result.setCount((Integer) obj.get(COUNT_PROPERTY));
				} else if ((obj.get(COUNT_PROPERTY) instanceof Long)) {
					result.setCount((Long) obj.get(COUNT_PROPERTY));
				}
			}
	
			if (obj.has(DATA_PROPERTY) && obj.getJSONObject(DATA_PROPERTY).has(IDS_PROPERTY)
					&& null != obj.getJSONObject(DATA_PROPERTY).get(IDS_PROPERTY)) {
				ArrayList<String> ids = new ArrayList<>();
	
				JSONArray lIds = (JSONArray) obj.getJSONObject(DATA_PROPERTY).get(IDS_PROPERTY);
				for (int i = 0; i < lIds.length(); i++) {
					ids.add(lIds.getString(i));
				}
				result.setIds(ids);
	
				result.setStrIds(obj.getJSONObject(DATA_PROPERTY).get(IDS_PROPERTY).toString());
			} else if (!obj.has(DATA_PROPERTY) && obj.has(IDS_PROPERTY) && null != obj.get(IDS_PROPERTY)) {
				ArrayList<String> ids = new ArrayList<>();
	
				JSONArray lIds = (JSONArray) obj.get(IDS_PROPERTY);
				for (int i = 0; i < lIds.length(); i++) {
					ids.add(lIds.getString(i));
				}
				result.setIds(ids);
	
				result.setStrIds(obj.get(IDS_PROPERTY).toString());
			}
	
			if (!obj.has(DATA_PROPERTY) && !obj.has(IDS_PROPERTY)) {
				if (obj.get(COUNT_PROPERTY) instanceof Integer) {
					result.setCount((Integer) obj.get(COUNT_PROPERTY));
				} else if ((obj.get(COUNT_PROPERTY) instanceof Long)) {
					result.setCount((Long) obj.get(COUNT_PROPERTY));
				}
			}
		}
		return result;
	}

	public static MultiDocumentOperationResult fromString(String data) {
		JSONObject obj = new JSONObject(data);
		return fromJSONObject(obj);
	}

}
