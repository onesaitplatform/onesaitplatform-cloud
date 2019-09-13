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

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;

@Component
@Rule
public class ApiOperationAvailableRule extends DefaultRuleBase {

	@Autowired
	private ApiManagerService apiManagerService;

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Api api = (Api) data.get(Constants.API);
		return(api != null && !api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON));
	}

	@Action
	public void checkOperationIsAvailable(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Api api = (Api) data.get(Constants.API);
		final String method = (String) data.get(Constants.METHOD);
		final ApiOperation operation = apiManagerService.getOperationsByMethod(api, Type.valueOf(method)).stream()
				.findAny().orElse(null);
		if (operation == null)
			stopAllNextRules(facts, "There are no operations allowed for this API with HTTP method " + method,

					DefaultRuleBase.ReasonType.GENERAL);
	}

}
