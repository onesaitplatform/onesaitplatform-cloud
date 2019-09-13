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
package com.minsait.onesait.platform.api.rule;

import org.jeasy.rules.api.Facts;

public class DefaultRuleBase {
	
	public enum ReasonType {
		INTERNAL, API_LIMIT, SECURITY, GENERAL, DEVELOPMENT;
	}
		
	protected void stopAllNextRules(Facts facts, String reason, ReasonType type) {
		facts.put(RuleManager.STOP_STATE, true);
		facts.put(RuleManager.REASON, reason);
		facts.put(RuleManager.REASON_TYPE, type.name());
	}
	
	protected boolean canExecuteRule(Facts facts) {
		Boolean bool = facts.get(RuleManager.STOP_STATE);
		if (bool == null) return true;
		else return !bool.booleanValue();		
	}
	
}