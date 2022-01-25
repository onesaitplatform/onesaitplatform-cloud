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

import java.util.List;
import java.util.Map;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
	@Autowired
	@Qualifier("apiManagerService")
	private com.minsait.onesait.platform.api.service.api.ApiManagerService apiService;

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Api api = (Api) data.get(Constants.API);
		return api != null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY);
	}

	@Action
	public void checkOperationIsAvailable(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Api api = (Api) data.get(Constants.API);
		final String method = (String) data.get(Constants.METHOD);
		final String pathInfo = (String) data.get(Constants.PATH_INFO);
		final ApiOperation customSQL = apiService.getCustomSQL(pathInfo, api, method);
		final String objectId = apiService.getObjectidFromPathQuery(pathInfo, customSQL);
		final List<ApiOperation> operations = apiManagerService.getOperationsByMethod(api, Type.valueOf(method));
		ApiOperation operation = null;
		if (!StringUtils.isEmpty(objectId)) {
			operation = operations.stream().filter(a -> a.getPath().equals("/{id}")).findAny().orElse(null);
		} else if (customSQL != null) {
			operation = customSQL;
		} else {
			operation = operations.stream().filter(a -> StringUtils.isEmpty(a.getPath()) || "/".equals(a.getPath()))
					.findAny().orElse(null);
		}

		if (operation == null) {
			stopAllNextRules(facts, "There are no operations allowed for this API with HTTP method " + method,

					DefaultRuleBase.ReasonType.GENERAL, HttpStatus.NOT_FOUND);
		}
	}

}
