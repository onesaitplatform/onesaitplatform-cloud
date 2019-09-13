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
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.config.model.Api;

@Component
@Rule
public class MetricsRule extends DefaultRuleBase {

	private static final String RULE_AUDIT = "rule.audit.";

	@Autowired
	private CounterService counterService;

	@Priority
	public int getPriority() {
		return 100;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return ((request != null) && canExecuteRule(facts));
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		Map<String, Object> data = facts.get(RuleManager.FACTS);

		Api api = (Api) data.get(Constants.API);
		String method = (String) data.get(Constants.METHOD);
		String authenticationHeader = (String) data.get(Constants.AUTHENTICATION_HEADER);

		counterService.increment(RULE_AUDIT + api.getIdentification());
		counterService.increment(RULE_AUDIT + api.getIdentification() + "." + authenticationHeader);
		counterService.increment(RULE_AUDIT + api.getIdentification() + "." + method);

	}

}