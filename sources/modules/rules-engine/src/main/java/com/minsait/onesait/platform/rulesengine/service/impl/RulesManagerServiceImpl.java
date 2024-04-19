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
package com.minsait.onesait.platform.rulesengine.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hazelcast.topic.ITopic;
import com.minsait.onesait.platform.commons.model.HazelcastMessageNotification;
import com.minsait.onesait.platform.commons.model.HazelcastRuleDomainObject;
import com.minsait.onesait.platform.commons.model.HazelcastRuleObject;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.rulesengine.drools.KieServicesManager;
import com.minsait.onesait.platform.rulesengine.service.RulesManagerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RulesManagerServiceImpl implements RulesManagerService {

	@Autowired
	private KieServicesManager kieServicesManager;
	@Autowired
	private DroolsRuleService droolsRuleService;
	@Autowired
	@Qualifier("topicAsyncComm")
	private ITopic<String> topicAsyncComm;

	@Override
	public void manageRule(HazelcastRuleObject rule) {
		log.debug("Managing changes for rule {} of user {}", rule.getIdentification(), rule.getUserId());
		final DroolsRule droolsRule = droolsRuleService.getRule(rule.getIdentification());
		if (droolsRule == null && StringUtils.isEmpty(rule.getDRL()) && rule.getDecisionTable() == null)
			kieServicesManager.removeRule(rule.getUserId(), rule.getIdentification(), rule.getExtension());
		else {
			final Results results = kieServicesManager.updateRule(rule.getUserId(), rule.getIdentification(),
					rule.getDRL(), rule.getDecisionTable(), rule.getExtension());
			if (results.hasMessages(Level.ERROR)) {
				HazelcastMessageNotification.builder().rule(rule.getIdentification()).message(results.toString())
						.build().toJson().ifPresent(m -> topicAsyncComm.publish(m));

			} else {
				HazelcastMessageNotification.builder().rule(rule.getIdentification())
						.message(HazelcastMessageNotification.OK).build().toJson()
						.ifPresent(m -> topicAsyncComm.publish(m));
			}
		}
	}

	@Override
	public void manageDomain(HazelcastRuleDomainObject domain) {
		log.debug("Manging changes for rule domain of user {}", domain.getUserId());
		final DroolsRuleDomain droolsDomain = droolsRuleService.getDomain(domain.getId());
		if (droolsDomain == null || !droolsDomain.isActive())
			kieServicesManager.removeServices(domain.getUserId());
		else {
			if (!kieServicesManager.isRuleEngineDomainActive(domain.getUserId()))
				kieServicesManager.initializeRuleEngineDomain(domain.getUserId());
		}

	}

}
