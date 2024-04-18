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

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiSecurityService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.User;

@Component
@Rule
public class SecurityRule extends DefaultRuleBase {

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return request != null && canExecuteRule(facts);
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);

		final User user = (User) data.get(Constants.USER);
		final Api api = (Api) data.get(Constants.API);

		boolean published = false;

		final boolean available = apiSecurityService.checkApiAvailable(api, user);
		final boolean checkUser = apiSecurityService.checkUserApiPermission(api, user);
		published = apiSecurityService.checkApiIsPublic(api);

		if (!available) {
			stopAllNextRules(facts, "API is not Available", DefaultRuleBase.ReasonType.SECURITY,
					HttpStatus.NOT_ACCEPTABLE);
		}
		if (!checkUser && !published) {
			stopAllNextRules(facts, "User has no permission to use API", DefaultRuleBase.ReasonType.SECURITY,
					HttpStatus.FORBIDDEN);
		}

	}

}
