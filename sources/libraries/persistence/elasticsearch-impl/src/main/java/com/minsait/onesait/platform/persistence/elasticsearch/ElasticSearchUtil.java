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
package com.minsait.onesait.platform.persistence.elasticsearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ElasticSearchUtil {

	public String parseElastiSearchResult(String response, boolean addId) throws JSONException {

		JSONArray hitsArray = null;
		JSONObject hits = null;
		JSONObject aggregations = null;
		JSONObject source = null;
		JSONObject json = null;

		final JSONArray jsonArray = new JSONArray();
		
		json = new JSONObject(response);
		hits = json.getJSONObject("hits");
		hitsArray = hits.getJSONArray("hits");

		try {


			aggregations = json.getJSONObject("aggregations");
			try {
				return aggregations.getJSONObject("gender").getJSONArray("buckets").toString();
			} catch (final JSONException e) {
				return aggregations.toString();
			}
		} catch (final JSONException e) {
		    log.error("" + e);
		}
		if (hitsArray.length() > 0) {
			for (int i = 0; i < hitsArray.length(); i++) {
				final JSONObject h = hitsArray.getJSONObject(i);
				source = h.getJSONObject("_source");
				if (addId)
					source.put("_id", h.getString("_id"));
				jsonArray.put(source);
			}
		} else if (hits.length() > 0) {
			jsonArray.put(hits);
		}

		return jsonArray.toString();

	}

}
