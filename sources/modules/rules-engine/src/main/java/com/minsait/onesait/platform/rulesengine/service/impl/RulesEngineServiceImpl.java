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
package com.minsait.onesait.platform.rulesengine.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.service.RouterService;
import com.minsait.onesait.platform.rulesengine.drools.KieServicesManager;
import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;
import com.minsait.onesait.platform.rulesengine.service.RulesEngineService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RulesEngineServiceImpl implements RulesEngineService {

	@Autowired
	private KieServicesManager kieServicesManager;
	@Autowired
	private DroolsRuleService droolsRuleService;

	private static final String GLOBAL_IDENTIFIER_INPUT = "input";
	private static final String GLOBAL_IDENTIFIER_OUTPUT = "output";
	private static final String TEMP_JAR_PREFIX = "temp";

	@Autowired
	private RouterService routerService;

	@Override
	public String executeRules(String ontology, String jsonInput, String user) {
		final OntologyJsonWrapper input = new OntologyJsonWrapper(jsonInput);
		final KieSession session = kieServicesManager.getKieSession(user);
		session.setGlobal(GLOBAL_IDENTIFIER_INPUT, input);
		final OntologyJsonWrapper output = new OntologyJsonWrapper();
		session.setGlobal(GLOBAL_IDENTIFIER_OUTPUT, output);
		try {
			final int n = session.fireAllRules();
			log.debug("Fired {} rules for ontology {} and user {}", n, ontology, user);
			session.dispose();
		} catch (final Exception e) {
			log.error("Exception while firing rules for user {} :  {}", user, e);
		}
		return output.toJson();

	}

	@Override
	@Async
	public List<Future<String>> executeRulesAsync(String ontology, String jsonInput) {
		final List<DroolsRule> rules = droolsRuleService.getRulesForInputOntology(ontology);
		return rules.stream().map(dr -> {
			final String output = executeRules(ontology, jsonInput, dr.getUser().getUserId());

			if (StringUtils.isEmpty(output) || output.equals("{}"))
				return new AsyncResult<>(output);
			final OperationModel model = OperationModel.builder(dr.getTargetOntology().getIdentification(),
					OperationType.POST, dr.getUser().getUserId(), OperationModel.Source.RULES_ENGINE).body(output)
					.build();
			final NotificationModel modelNotification = new NotificationModel();
			modelNotification.setOperationModel(model);
			try {
				routerService.insert(modelNotification);
			} catch (final Exception e) {
				log.error("Error while sending rule execution result for ontology: {}, user:{} ", ontology,
						dr.getUser().getUserId());
			}
			return new AsyncResult<>(output);

		}).collect(Collectors.toList());

	}

	@Override
	public String executeRestRule(String ruleIdentification, String jsonInput) throws GenericOPException {
		final DroolsRule rule = droolsRuleService.getRule(ruleIdentification);
		final String randomUUID = UUID.randomUUID().toString();
		kieServicesManager.initializeRuleEngineDomain(TEMP_JAR_PREFIX + randomUUID, rule);
		final KieSession session = kieServicesManager.getKieSession(TEMP_JAR_PREFIX + randomUUID);
		final OntologyJsonWrapper input = new OntologyJsonWrapper(jsonInput);
		session.setGlobal(GLOBAL_IDENTIFIER_INPUT, input);
		final OntologyJsonWrapper output = new OntologyJsonWrapper();
		session.setGlobal(GLOBAL_IDENTIFIER_OUTPUT, output);
		try {
			final int n = session.fireAllRules();
			log.debug("Fired {} rules for rule {}", n, ruleIdentification);
			session.dispose();
			return output.toJson();
		} catch (final Exception e) {
			log.error("Could not execute rule", e);
			throw new GenericOPException(e);
		} finally {
			kieServicesManager.removeServices(TEMP_JAR_PREFIX + randomUUID);
		}

	}

	@Override
	public boolean canUserExecuteRule(String ruleIdentification, String userId) {
		return droolsRuleService.hasUserEditPermission(ruleIdentification, userId);
	}

}
