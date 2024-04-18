/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRule.TableExtension;
import com.minsait.onesait.platform.config.model.DroolsRule.Type;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DroolsRuleDomainRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class DroolsRuleServiceImpl implements DroolsRuleService {

	@Autowired
	private DroolsRuleRepository droolsRuleRepository;
	@Autowired
	private DroolsRuleDomainRepository droolsRuleDomainRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyRepository ontologyRepository;
	private static final String DOMAIN_SUFFIX = "-rules-domain";

	@Override
	public List<DroolsRule> getRulesForOntology(Ontology ontology) {
		return droolsRuleRepository.findBySourceOntologyAndActiveTrue(ontology);
	}

	@Override
	public List<DroolsRule> getRulesForOntology(String ontology) {
		return droolsRuleRepository.findBySourceOntologyIdentificationAndActiveTrue(ontology);
	}

	@Override
	public List<DroolsRule> getAllRules() {
		return droolsRuleRepository.findAll();
	}

	@Override
	public List<DroolsRule> getAllRules(String user) {
		return this.getAllRules(userService.getUser(user));
	}

	@Override
	public List<DroolsRule> getAllRules(User user) {
		return droolsRuleRepository.findByUser(user);
	}

	@Override
	public List<DroolsRuleDomain> getActiveDomains() {
		return droolsRuleDomainRepository.findByActiveTrue();
	}

	@Override
	public List<DroolsRuleDomain> getAllDomains(String user) {

		if (userService.getUser(user).isAdmin()) {
			return droolsRuleDomainRepository.findAll();
		} else {
			return new ArrayList<>(Arrays.asList(this.getUserDomain(user)));
		}
	}

	@Override
	public DroolsRuleDomain getUserDomain(String user) {
		return this.getUserDomain(userService.getUser(user));
	}

	@Override
	public DroolsRuleDomain getUserDomain(User user) {
		return droolsRuleDomainRepository.findByUser(user);
	}

	@Override
	public List<DroolsRule> getAllRulesToLoad() {
		final List<DroolsRuleDomain> activeDomains = droolsRuleDomainRepository.findByActiveTrue();

		final List<DroolsRule> rules = new ArrayList<>();

		activeDomains.forEach(d -> rules.addAll(droolsRuleRepository.findByUser(d.getUser())));

		return rules;

	}

	@Override
	public List<DroolsRule> getRulesForInputOntology(String ontology) {
		final List<DroolsRuleDomain> domains = getActiveDomains();
		return this.getRulesForOntology(ontology).stream()
				.filter(dr -> domains.stream().anyMatch(d -> d.getUser().equals(dr.getUser())))
				.collect(Collectors.toList());

	}

	@Override
	public DroolsRule getRule(String identification) {
		return droolsRuleRepository.findByIdentification(identification);
	}

	@Override
	public DroolsRuleDomain getDomain(String id) {
		return droolsRuleDomainRepository.findById(id).orElse(null);
	}

	@Override
	public DroolsRuleDomain createDomain(String user) {
		final DroolsRuleDomain domain = new DroolsRuleDomain();
		domain.setActive(true);
		domain.setUser(userService.getUser(user));
		domain.setIdentification(user.concat(DOMAIN_SUFFIX));
		return droolsRuleDomainRepository.save(domain);
	}

	@Override
	public int countRules(String user) {
		return droolsRuleRepository.countByUser(userService.getUser(user));
	}

	@Override
	public DroolsRuleDomain changeDomainState(String id) {
		final DroolsRuleDomain domain = getDomain(id);
		if (domain != null) {
			domain.setActive(!domain.isActive());
			return droolsRuleDomainRepository.save(domain);
		}
		return null;
	}

	@Override
	public void updateDRL(String identification, String drl) {
		droolsRuleRepository.updateDRLByIdentification(drl, identification);
	}

	@Override
	public void updateDecisionTable(String identification, byte[] decisionTable, TableExtension extension) {
		droolsRuleRepository.updateDecisionTableByIdentification(decisionTable, extension, identification);
	}

	@Override
	public void deleteRule(String identification) {
		droolsRuleRepository.deleteByIdentification(identification);
	}

	@Override
	@Transactional
	public void updateActive(String identification) {
		final DroolsRule rule = droolsRuleRepository.findByIdentification(identification);
		droolsRuleRepository.updateActiveByIdentification(!rule.isActive(), identification);

	}

	@Override
	@Transactional
	public void updateActive(String identification, boolean active) {
		droolsRuleRepository.updateActiveByIdentification(active, identification);

	}

	@Override
	public DroolsRule create(DroolsRule rule, String userId) {
		if (rule.getType().equals(Type.ONTOLOGY)) {

			rule.setSourceOntology(
					ontologyRepository.findByIdentification(rule.getSourceOntology().getIdentification()));
			rule.setTargetOntology(
					ontologyRepository.findByIdentification(rule.getTargetOntology().getIdentification()));
			if (!droolsRuleRepository.findBySourceOntologyAndUser(rule.getSourceOntology().getIdentification(), userId)
					.isEmpty())
				throw new GenericRuntimeOPException(
						"Only one Rule entity is allowed per user for the SAME source ontology. Add rules for this ontology to the main .drl file instead.");
			if (rule.getSourceOntology().getIdentification().equals(rule.getTargetOntology().getIdentification()))
				throw new GenericRuntimeOPException("Source and Target ontology must be different");

		} else {
			rule.setSourceOntology(null);
			rule.setTargetOntology(null);
		}
		rule.setActive(true);
		rule.setUser(userService.getUser(userId));
		return droolsRuleRepository.save(rule);
	}

	@Override
	public DroolsRule update(String identification, DroolsRule rule) {
		final DroolsRule ruleDb = droolsRuleRepository.findByIdentification(identification);
		rule.setType(ruleDb.getType());
		ruleDb.setDRL(rule.getDRL());
		ruleDb.setDecisionTable(rule.getDecisionTable());
		if (ruleDb.getType().equals(Type.ONTOLOGY)) {
			final String previousSource = ruleDb.getSourceOntology().getIdentification();
			if (!previousSource.equals(rule.getSourceOntology().getIdentification())
					&& !droolsRuleRepository.findBySourceOntologyAndUser(rule.getSourceOntology().getIdentification(),
							ruleDb.getUser().getUserId()).isEmpty())
				throw new GenericRuntimeOPException(
						"Only one Rule entity is allowed per user for the SAME source ontology. Add rules for this ontology to the main .drl file instead.");
			ruleDb.setSourceOntology(
					ontologyRepository.findByIdentification(rule.getSourceOntology().getIdentification()));
			ruleDb.setTargetOntology(
					ontologyRepository.findByIdentification(rule.getTargetOntology().getIdentification()));

		}
		return droolsRuleRepository.save(ruleDb);
	}

	@Override
	public boolean hasUserEditPermission(String identification, String userId) {
		final DroolsRule ruleDb = droolsRuleRepository.findByIdentification(identification);
		final User user = userService.getUser(userId);
		return (userService.isUserAdministrator(user) || user.equals(ruleDb.getUser()));
	}

	@Override
	public boolean hasUserPermissionOnDomain(String id, String userId) {
		final DroolsRuleDomain domain = getDomain(id);
		final User user = userService.getUser(userId);
		return (userService.isUserAdministrator(user) || user.equals(domain.getUser()));
	}

	@Override
	public List<DroolsRuleDomain> getAllDomains() {
		return droolsRuleDomainRepository.findAll();
	}

	@Override
	@Transactional
	public void changeDomainState(String userId, boolean active) {
		droolsRuleDomainRepository.updateActiveByUserId(active, userId);
	}
}
