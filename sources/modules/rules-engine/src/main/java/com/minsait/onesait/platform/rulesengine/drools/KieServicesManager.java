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
package com.minsait.onesait.platform.rulesengine.drools;

import org.kie.api.builder.Results;
import org.kie.api.runtime.KieSession;

import com.minsait.onesait.platform.config.model.DroolsRule;

public interface KieServicesManager {

	void initializeRuleEngineDomain(String user);

	void initializeRuleEngineDomain(String user, DroolsRule rule);

	void removeServices(String user);

	Results addRule(String user, String ruleDRL, String ruleName, byte[] decisionTable, String extension);

	KieSession getKieSession(String user);

	boolean isRuleEngineDomainActive(String user);

	Results updateRule(String user, String ruleName, String drl, byte[] decisionTable, String extension);

	void removeRule(String user, String ruleName, String extension);
}
