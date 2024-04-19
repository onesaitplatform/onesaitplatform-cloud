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
package com.minsait.onesait.platform.controlpanel.services.rules;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.minsait.onesait.platform.business.services.interceptor.MultitenancyInterceptor;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.HazelcastMessageNotification;
import com.minsait.onesait.platform.commons.model.HazelcastRuleDomainObject;
import com.minsait.onesait.platform.commons.model.HazelcastRuleObject;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRule.TableExtension;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.rules.DroolsRuleDTO;
import com.minsait.onesait.platform.controlpanel.controller.rules.RuleDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BusinessRuleServiceImpl implements BusinessRuleService {

	@Autowired
	@Qualifier("topicChangedRules")
	private ITopic<String> topicRules;

	@Autowired
	@Qualifier("topicAsyncComm")
	private ITopic<String> topicAsyncComm;

	@Autowired
	@Qualifier("topicChangedDomains")
	private ITopic<String> topicDomains;

	@Autowired
	private DroolsRuleService droolsRuleService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private UserService userService;

	@Autowired
	private OntologyService ontologyService;

	private RestTemplate restTemplate;

	@PostConstruct
	void setup() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {

				request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + utils.getCurrentUserOauthToken());

				return execution.execute(request, body);
			}
		});
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void save(DroolsRuleDTO rule, String userId) throws GenericOPException, IOException {
		DroolsRule droolsRule = convertDTOtoRule(rule);
		droolsRuleService.create(droolsRule, userId);
		try {
			publishAndHandleHzNotification(droolsRule.getIdentification(), droolsRule.getDRL(),
					droolsRule.getDecisionTable(),
					droolsRule.getExtension() != null ? droolsRule.getExtension().name() : null);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("save:", e);
			throw new GenericOPException(e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void update(DroolsRuleDTO rule, String userId, String identification)
			throws GenericOPException, IOException {
		DroolsRule droolsRule = convertDTOtoRule(rule);
		droolsRuleService.update(identification, droolsRule);
		try {
			publishAndHandleHzNotification(droolsRule.getIdentification(), droolsRule.getDRL(),
					droolsRule.getDecisionTable(),
					droolsRule.getExtension() != null ? droolsRule.getExtension().name() : null);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("update:", e);
			throw new GenericOPException(e);
		}
	}

	@Override
	public void delete(String identification) {
		final User user = droolsRuleService.getRule(identification).getUser();
		droolsRuleService.deleteRule(identification);
		publishHzRuleNotification(identification, user, null);
	}

	@Override
	public String test(String identification, String input) {
		final HttpHeaders headers = new HttpHeaders();
		MultitenancyInterceptor.addMultitenantHeaders(headers);
		return restTemplate.postForObject(
				resourcesService.getUrl(Module.RULES_ENGINE, ServiceUrl.BASE) + "/execute/rule/" + identification,
				new HttpEntity<>(input, headers), String.class);
	}

	@Override
	public void updateActive(String identification) {
		droolsRuleService.updateActive(identification);
		final DroolsRule rule = droolsRuleService.getRule(identification);
		publishHzRuleNotification(identification, rule.getDRL(), rule.getDecisionTable(),
				rule.getExtension() != null ? rule.getExtension().name() : null);
	}

	@Override
	public void updateActive(String identification, boolean active) {
		droolsRuleService.updateActive(identification, active);
		final DroolsRule rule = droolsRuleService.getRule(identification);
		publishHzRuleNotification(identification, rule.getDRL(), rule.getDecisionTable(),
				rule.getExtension() != null ? rule.getExtension().name() : null);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateDRL(String identification, String newDRL) throws GenericOPException {
		droolsRuleService.updateDRL(identification, newDRL);
		try {
			publishAndHandleHzNotification(identification, newDRL, null, null);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("updateURL:", e);
			throw new GenericOPException(e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateDecisionTable(String identification, MultipartFile decisionTable)
			throws GenericOPException, IOException {
		TableExtension extension = TableExtension
				.valueOf(FilenameUtils.getExtension(decisionTable.getOriginalFilename()).toUpperCase());
		droolsRuleService.updateDecisionTable(identification, decisionTable.getBytes(), extension);
		try {
			publishAndHandleHzNotification(identification, null, decisionTable.getBytes(), extension.name());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("updateURL:", e);
			throw new GenericOPException(e);
		}
	}

	private DroolsRule convertDTOtoRule(DroolsRuleDTO rule) throws IOException {
		DroolsRule droolsRule = new DroolsRule();

		if (!rule.isDecisionTable()) {
			droolsRule.setDRL(rule.getDRL());
		} else {
			droolsRule.setDecisionTable(rule.getTable().getBytes());
			droolsRule.setExtension(TableExtension
					.valueOf(FilenameUtils.getExtension(rule.getTable().getOriginalFilename()).toUpperCase()));
		}
		droolsRule.setActive(rule.isActive());
		droolsRule.setIdentification(rule.getIdentification());
		droolsRule.setSourceOntology(rule.getSourceOntology());
		droolsRule.setTargetOntology(rule.getTargetOntology());
		droolsRule.setType(rule.getType());
		return droolsRule;
	}

	private void publishHzRuleNotification(String identification, User user, String drl) {
		final DroolsRule rule = droolsRuleService.getRule(identification);
		final HazelcastRuleObject ruleObj = HazelcastRuleObject.builder().identification(identification)
				.userId(rule == null ? user.getUserId() : rule.getUser().getUserId()).DRL(drl).build();
		ruleObj.toJson().ifPresent(s -> topicRules.publish(s));
	}

	private void publishHzRuleNotification(String identification, String drl, byte[] decisionTable,
			String tableExtension) {
		final DroolsRule rule = droolsRuleService.getRule(identification);
		final HazelcastRuleObject ruleObj = HazelcastRuleObject.builder().identification(identification)
				.userId(rule.getUser().getUserId()).DRL(drl).decisionTable(decisionTable).extension(tableExtension)
				.build();
		ruleObj.toJson().ifPresent(s -> topicRules.publish(s));
	}

	private void publishAndHandleHzNotification(String identification, String drl, byte[] decisionTable,
			String tableExtension) throws InterruptedException, ExecutionException, TimeoutException {
		final HazelcastListener listener = new HazelcastListener(identification);
		final UUID registerId = topicAsyncComm.addMessageListener(listener);
		publishHzRuleNotification(identification, drl, decisionTable, tableExtension);
		final String results = listener.getResults().get(30, TimeUnit.SECONDS);
		if (!results.equalsIgnoreCase(HazelcastMessageNotification.OK))
			throw new GenericRuntimeOPException(results);
		topicAsyncComm.removeMessageListener(registerId);
	}

	@Getter
	@Setter
	public class HazelcastListener implements MessageListener<String> {

		private boolean messageReceived;
		private String rule;
		private String message;

		public HazelcastListener(String rule) {
			this.rule = rule;
		}

		@Override
		public void onMessage(Message<String> message) {
			HazelcastMessageNotification.fromJson(message.getMessageObject()).ifPresent(h -> {
				if (rule.equals(h.getRule())) {
					messageReceived = true;
					this.message = h.getMessage();
				}
			});
		}

		public Future<String> getResults() {
			return Executors.newSingleThreadExecutor().submit(() -> {
				while (!messageReceived) {
					Thread.sleep(500);
				}
				return message;

			});
		}

	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void save(RuleDTO rule, String userId) throws GenericOPException {
		final DroolsRule r = convertFromDTO(rule, userId);
		droolsRuleService.create(r, userId);
//		try {
//			publishAndHandleHzNotification(rule.getIdentification(), r.getDRL(), r.getDecisionTable(),
//					rule.getExtension());
//		} catch (InterruptedException | ExecutionException | TimeoutException e) {
//			log.error("save:", e);
//			throw new GenericOPException(e);
//		}

	}

	private DroolsRule convertFromDTO(RuleDTO r, String userId) {
		final DroolsRule dr = new DroolsRule();
		dr.setActive(true);
		dr.setDRL(r.getDrl());
		dr.setIdentification(r.getIdentification());
		dr.setType(r.getType());
		dr.setUser(userService.getUser(userId));
		dr.setSourceOntology(ontologyService.getOntologyByIdentification(r.getInputOntology()));
		dr.setTargetOntology(ontologyService.getOntologyByIdentification(r.getOutputOntology()));
		return dr;
	}

	@Override
	public DroolsRuleDomain changeDomainState(String id) {
		final DroolsRuleDomain domain = droolsRuleService.changeDomainState(id);
		if (domain != null)
			HazelcastRuleDomainObject.builder().id(domain.getId()).userId(domain.getUser().getUserId()).build().toJson()
					.ifPresent(s -> topicDomains.publish(s));
		return domain;
	}

	@Override
	public DroolsRuleDomain createDomain(String user) {
		final DroolsRuleDomain domain = droolsRuleService.createDomain(user);
		HazelcastRuleDomainObject.builder().id(domain.getId()).userId(domain.getUser().getUserId()).build().toJson()
				.ifPresent(s -> topicDomains.publish(s));
		return domain;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void update(RuleDTO rule, String userId, String identification) throws GenericOPException {
		final DroolsRule dr = convertFromDTO(rule, userId);
		droolsRuleService.update(identification, dr);
//		try {
//			publishAndHandleHzNotification(rule.getIdentification(), dr.getDRL(), dr.getDecisionTable(),
//					rule.getExtension());
//		} catch (InterruptedException | ExecutionException | TimeoutException e) {
//			log.error("update:", e);
//			throw new GenericOPException(e);
//		}

	}

	@Override
	public void changeDomainState(String userId, boolean active) {
		final DroolsRuleDomain drd = droolsRuleService.getUserDomain(userId);
		if (drd != null) {
			droolsRuleService.changeDomainState(userId, active);
			HazelcastRuleDomainObject.builder().id(drd.getId()).userId(drd.getUser().getUserId()).build().toJson()
					.ifPresent(s -> topicDomains.publish(s));
		}

	}

	@Override
	public void changeDomainStates(boolean active) {
		droolsRuleService.getAllDomains().forEach(drd -> {
			droolsRuleService.changeDomainState(drd.getUser().getUserId(), active);
			HazelcastRuleDomainObject.builder().id(drd.getId()).userId(drd.getUser().getUserId()).build().toJson()
					.ifPresent(s -> topicDomains.publish(s));
		});

	}

	@Override
	public File createFolder(String path) throws GenericRuntimeOPException {
		final File file = new File(path);
		if (!file.exists()) {
			final Boolean success = file.mkdirs();
			if (!success) {
				throw new GenericRuntimeOPException("Creating tmp file fot Decision Table failed");
			}
		}
		return file;
	}

	@Override
	public File uploadFileToFolder(byte[] bytes, String path, String ruleName, String extension) {

		try {

			final InputStream is = new ByteArrayInputStream(bytes);

			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + File.separator + ruleName + "." + extension.toLowerCase();
			File outputFile = new File(fullPath);
			final OutputStream os = new FileOutputStream(outputFile);

			IOUtils.copy(is, os);
			is.close();
			os.close();
			return outputFile;
		} catch (final IOException e) {
			throw new GenericRuntimeOPException("Error uploading files " + e);
		} catch (Exception e) {
			throw new GenericRuntimeOPException("Error uploading files " + e);
		}
	}

	@Override
	public boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

}
