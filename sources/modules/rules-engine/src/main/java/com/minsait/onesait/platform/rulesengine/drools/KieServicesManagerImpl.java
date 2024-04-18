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
package com.minsait.onesait.platform.rulesengine.drools;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.rulesengine.service.impl.RulesEngineServiceImpl;

@Service
@EnableScheduling
public class KieServicesManagerImpl implements KieServicesManager {

	private final Map<String, KieFileSystem> fileSystems = new ConcurrentHashMap<>();
	private final Map<String, KieServices> kieServicesMap = new ConcurrentHashMap<>();

	private static final String PATH_TO_RULES = "src/main/resources/rules/";
	private static final String GROUP_ID = "com.minsait.onesait.platform";
	private static final String ARTIFACT_ID = "onesaitplatform-rules-engine";
	private static final String VERSION = "1.0";

	@Autowired
	private DroolsRuleService droolsRuleService;
	@Autowired
	private MultitenancyService masterUserService;

	private Map<String, KieContainer> cacheKieContainer;

	private KieServices getNewKieServices(String user) {
		if (kieServicesMap.get(user) != null)
			kieServicesMap.remove(user);
		try {
			final KieServices ks = (KieServices) Class.forName("org.drools.compiler.kie.builder.impl.KieServicesImpl")
					.newInstance();
			kieServicesMap.put(user, ks);
			return ks;
		} catch (final Exception e) {
			throw new GenericRuntimeOPException("Unable to instance KieServices", e);
		}
	}

	@PostConstruct
	void loadRules() {
		cacheKieContainer = new ConcurrentHashMap<>();
		masterUserService.getAllVerticals().forEach(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			final List<DroolsRuleDomain> activeDomains = droolsRuleService.getActiveDomains();
			activeDomains.forEach(rd -> initializeRuleEngineDomain(rd.getUser().getUserId()));
			MultitenancyContextHolder.clear();
		});

	}

	@Scheduled(fixedDelay = 120000)
	private void synchronizeDomains() {
		final List<String> allUsers = new ArrayList<>();
		masterUserService.getAllVerticals().forEach(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			final List<String> activeDomainUsers = droolsRuleService.getActiveDomains().stream()
					.map(d -> d.getUser().getUserId()).collect(Collectors.toList());
			allUsers.addAll(activeDomainUsers);
			activeDomainUsers.forEach(u -> {
				if (!kieServicesMap.containsKey(u)) {
					initializeRuleEngineDomain(u);
				}

			});
			MultitenancyContextHolder.clear();
		});
		kieServicesMap.entrySet().removeIf(e -> !allUsers.contains(e.getKey()));
	}

	@Override
	public void initializeRuleEngineDomain(String user) {
		final KieServices kieServices = getNewKieServices(user);

		final KieFileSystem kfs = kieServices.newKieFileSystem();

		if (fileSystems.get(user) != null)
			fileSystems.remove(user);
		kfs.generateAndWritePomXML(getReleaseId(user));
		fileSystems.put(user, kfs);
		loadRulesForUser(user);

	}

	@Override
	public void initializeRuleEngineDomain(String user, DroolsRule rule) {
		final KieServices kieServices = getNewKieServices(user);

		final KieFileSystem kfs = kieServices.newKieFileSystem();

		if (fileSystems.get(user) != null)
			fileSystems.remove(user);
		kfs.generateAndWritePomXML(getReleaseId(user));
		fileSystems.put(user, kfs);
		addRule(user, rule.getDRL(), rule.getIdentification());

	}

	@Override
	public void removeServices(String user) {
		kieServicesMap.remove(user);
		fileSystems.remove(user);
		cacheKieContainer.remove(user);
	}

	@Override
	public KieSession getKieSession(String user) {
		if (kieServicesMap.get(user) == null)
			throw new GenericRuntimeOPException("User does not have any binded kie services");
		final KieServices ks = kieServicesMap.get(user);

		if (cacheKieContainer.containsKey(user)) {
			return cacheKieContainer.get(user).newKieSession();
		} else {
			final KieContainer container = ks.newKieContainer(getReleaseId(user));
			cacheKieContainer.put(user, container);
			return container.newKieSession();
		}

	}

	@Override
	public Results addRule(String user, String ruleDRL, String ruleName) {
		if (fileSystems.get(user) == null)
			throw new GenericRuntimeOPException("User does not have any binded kie file system");
		final KieFileSystem kfs = fileSystems.get(user);
		kfs.write(PATH_TO_RULES + user + "/" + ruleName + ".drl",
				ResourceFactory.newReaderResource(new StringReader(ruleDRL)));
		final KieServices ks = kieServicesMap.get(user);
		return ks.newKieBuilder(kfs).buildAll().getResults();
	}

	@Override
	public void removeRule(String user, String ruleName) {
		if (fileSystems.get(user) == null)
			throw new GenericRuntimeOPException("User does not have any binded kie file system");
		final KieFileSystem kfs = fileSystems.get(user);
		kfs.delete(PATH_TO_RULES + user + "/" + ruleName + ".drl");
		final KieServices ks = kieServicesMap.get(user);
		ks.newKieBuilder(kfs).buildAll();
		cacheKieContainer.remove(user);
	}

	@Override
	public boolean isRuleEngineDomainActive(String user) {
		return kieServicesMap.containsKey(user);
	}

	private void loadRulesForUser(String user) {
		final List<DroolsRule> rules = droolsRuleService.getAllRules(user);
		rules.forEach(dr -> addRule(dr.getUser().getUserId(), dr.getDRL(), dr.getIdentification()));
	}

	private ReleaseId getReleaseId(String user) {
		return kieServicesMap.get(user).newReleaseId(GROUP_ID, ARTIFACT_ID + "-" + user, VERSION);
	}

	@Override
	public Results updateRule(String user, String ruleName, String drl) {
		removeRule(user, ruleName);
		removeServices(RulesEngineServiceImpl.TEMP_JAR_PREFIX + ruleName);
		return addRule(user, drl, ruleName);
	}
}
