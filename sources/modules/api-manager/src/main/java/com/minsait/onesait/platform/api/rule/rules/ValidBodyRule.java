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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.Map;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.mongodb.BasicDBObject;


@Component
@Rule
public class ValidBodyRule extends DefaultRuleBase {

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Object body = data.get(ApiServiceInterface.BODY);
		final Api api = (Api) data.get(ApiServiceInterface.API);
		return body != null && canExecuteRule(facts) && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY);
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);

		final byte[] requestBody = (byte[]) data.get(ApiServiceInterface.BODY);
		final String body = new String(requestBody);

		if (!"".equals(body)) {
			final boolean valid = isValidJSON(body);
			
            if(!valid){
				stopAllNextRules(facts, "BODY IS NOT JSON PARSEABLE ", DefaultRuleBase.ReasonType.GENERAL,
						HttpStatus.BAD_REQUEST);
            }
			
		}

	}

	public boolean isValidJSON(String toTestStr) {
		final JSONObject jsonObj = toJSONObject(toTestStr);
		final JSONArray jsonArray = toJSONArray(toTestStr);

		return jsonObj != null || jsonArray != null;
	}

	private JSONObject toJSONObject(String input) {
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	private JSONArray toJSONArray(String input) {
		JSONArray jsonObj = null;
		try {
			jsonObj = new JSONArray(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	public boolean isValidJSONtoMongo(String body) {
		try {
			final BasicDBObject dbObject = BasicDBObject.parse(body);

			return dbObject != null;
		} catch (final Exception e) {
			return false;
		}
	}

	public String depureJSON(String body) {
		BasicDBObject dbObject = null;
		try {
			dbObject = BasicDBObject.parse(body);
			if (dbObject == null) {
				return null;
			} else {
				return dbObject.toString();
			}
		} catch (final Exception e) {
			return null;
		}

	}
}
