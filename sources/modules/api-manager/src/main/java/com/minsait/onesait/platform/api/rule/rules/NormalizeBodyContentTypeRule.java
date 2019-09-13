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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Component
@Rule
public class NormalizeBodyContentTypeRule extends DefaultRuleBase {

	@Priority
	public int getPriority() {
		return 1;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		Map<String, Object> data = facts.get(RuleManager.FACTS);
		Object body = data.get(Constants.BODY);
		return body != null;
	}

	@Action
	public void setFirstDerivedData(Facts facts) {

		Map<String, Object> data = facts.get(RuleManager.FACTS);

		String body = (String) data.get(Constants.BODY);
		String contentTypeInput = (String) data.get(Constants.CONTENT_TYPE_INPUT);

		if (!"".equals(body) && contentTypeInput != null && contentTypeInput.equals(MediaType.APPLICATION_ATOM_XML)) {
			try {
				JSONObject xmlJSONObj = XML.toJSONObject(body);
				data.put(Constants.BODY, xmlJSONObj.toString());
			} catch (Exception e) {

				stopAllNextRules(facts, "BODY IS NOT JSON PARSEABLE : " + e.getMessage(),
						DefaultRuleBase.ReasonType.GENERAL);
			}
		}

	}

	public boolean isValidJSON(String toTestStr) {
		JSONObject jsonObj = toJSONObject(toTestStr);
		JSONArray jsonArray = toJSONArray(toTestStr);

		return jsonObj != null || jsonArray != null;
	}

	private JSONObject toJSONObject(String input) {
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(input);
		} catch (JSONException e) {
			return null;
		}
		return jsonObj;
	}

	private JSONArray toJSONArray(String input) {
		JSONArray jsonObj = null;
		try {
			jsonObj = new JSONArray(input);
		} catch (JSONException e) {
			return null;
		}
		return jsonObj;
	}

	public boolean isValidJSONtoMongo(String body) {
		try {
			DBObject dbObject = (DBObject) JSON.parse(body);
			return dbObject != null;
		} catch (Exception e) {
			return false;
		}
	}

	public String depureJSON(String body) {
		DBObject dbObject = null;
		try {
			dbObject = (DBObject) JSON.parse(body);
			if (dbObject == null)
				return null;
			else {
				return dbObject.toString();
			}
		} catch (Exception e) {
			return null;
		}

	}
}