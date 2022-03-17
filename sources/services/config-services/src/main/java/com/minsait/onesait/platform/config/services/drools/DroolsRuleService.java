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
package com.minsait.onesait.platform.config.services.drools;

import java.util.List;

import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRule.TableExtension;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface DroolsRuleService {

	List<DroolsRule> getRulesForOntology(Ontology ontology);

	List<DroolsRule> getRulesForOntology(String ontology);

	List<DroolsRule> getAllRules();

	List<DroolsRule> getAllRules(String user);

	List<DroolsRule> getAllRules(User user);

	List<DroolsRuleDomain> getActiveDomains();

	List<DroolsRuleDomain> getAllDomains(String user);

	List<DroolsRuleDomain> getAllDomains();

	DroolsRuleDomain getUserDomain(String user);

	DroolsRuleDomain getUserDomain(User user);

	List<DroolsRule> getAllRulesToLoad();

	List<DroolsRule> getRulesForInputOntology(String ontology);

	DroolsRule getRule(String identification);

	DroolsRuleDomain getDomain(String id);

	DroolsRuleDomain createDomain(String user);

	int countRules(String user);

	DroolsRuleDomain changeDomainState(String id);

	void changeDomainState(String userId, boolean active);

	void updateDRL(String identification, String drl);

	void deleteRule(String identification);

	void updateActive(String identification);

	void updateActive(String identification, boolean active);

	DroolsRule create(DroolsRule rule, String userId);

	DroolsRule update(String identification, DroolsRule rule);

	boolean hasUserEditPermission(String identification, String userId);

	boolean hasUserPermissionOnDomain(String id, String userId);

	void updateDecisionTable(String identification, byte[] decisionTable, TableExtension extension);
}
