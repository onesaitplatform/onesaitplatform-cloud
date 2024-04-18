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
package com.minsait.onesait.platform.router.service.processor;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.crypto.Cipher;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.collection.IQueue;
import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.business.services.user.UserOperationsService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;
import com.minsait.onesait.platform.router.service.processor.bean.AuditParameters;

import lombok.extern.slf4j.Slf4j;

@Component("auditFlowManagerService")
@Lazy(false)
@Slf4j
public class AuditFlowManagerService {

	@Autowired
	private RouterCrudService routerCrudService;

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserOperationsService userOperationsService;
	@Autowired
	private IntegrationResourcesService resourcesServices;
	@Autowired
	private MultitenancyService masterUserService;
	@Autowired
	@Qualifier("auditQueue")
	private IQueue<String> auditQueue;

	@Value("${onesaitplatform.router.audit.extractor.pool:10}")
	private int auditThreadPoolSize;
	private ExecutorService auditLogger;
	private static ExecutorService auditLoggerRest;

	@Value("${onesaitplatform.router.audit.process.events:true}")
	private boolean processAuditEvents;

	private final ObjectMapper mapper = new ObjectMapper();

	private static final String USER_KEY = "user";
	private static final String EVENT_TYPE_KEY = "type";
	private static final String OPERATION_TYPE_KEY = "operationType";
	private static final String AUDIT_EVENT_ERROR = "Error processing Audit event";
	private static final String CIPHER_DATA = "cipherData";

	@PostConstruct
	public void init() {
		if (!ignoreAudit() && processAuditEvents) {
			auditLoggerRest = Executors.newFixedThreadPool(auditThreadPoolSize);
			auditLogger = Executors.newFixedThreadPool(auditThreadPoolSize);
			for (int i = 0; i < auditThreadPoolSize; i++) {
				auditLogger.execute(() -> {
					while (true) {
						try {
							String event = auditQueue.take();
							if (isCipher()) {
								event = signData(event);
							}
							try {
								audit(event);
								if (log.isDebugEnabled()) {
									log.debug("Procesa auditoria: {}", event);
								}
							} catch (final JSONException e2) {
								log.error(AUDIT_EVENT_ERROR, e2);
							} catch (final RouterCrudServiceException e3) {
								log.error(AUDIT_EVENT_ERROR, e3);
							} catch (final Exception e4) {
								log.error(AUDIT_EVENT_ERROR, e4);
							}

						} catch (final Exception e1) {
							log.error("Interrupted Audit Queue listening", e1);
						}
					}
				});
			}
		} else {
			log.info("This Instace will not process Adit Events");
		}

	}

	@RestController
	@RequestMapping("audit")
	public class AuditController {

		@PostMapping
		public ResponseEntity<String> auditRest(@RequestBody String payload) {
			if (!ignoreAudit() && processAuditEvents) {
				auditLoggerRest.submit(() -> {
					try {
						audit(payload);
					} catch (JSONException | RouterCrudServiceException e) {
						log.error("Error while auditing REST", e);
					}
				});
			}
			return ResponseEntity.ok().build();
		}
	}

	@PreDestroy
	public void destroy() {
		auditLogger.shutdownNow();
	}

	public String signData(String item) {

		try {
			log.debug("Audit data os going to be cipher.");
			mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

			final KeyStore keyStore = KeyStore.getInstance("PKCS12");
			if (log.isDebugEnabled()) {
				log.debug("Load keystore with key-store-path= {} and password={}.", getKeyStorePath(),getKeystorePassword());
			}
			keyStore.load(new FileInputStream(getKeyStorePath()), getKeystorePassword().toCharArray());
			final PrivateKey privateKey = (PrivateKey) keyStore.getKey("auditKeys",
					getKeystorePassword().toCharArray());

			final JsonNode json = mapper.readTree(item);
			final byte[] msg = item.getBytes();
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			final byte[] msgHash = md.digest(msg);

			if (isSigned()) {
				log.debug("Audit data os going to be sign.");
				final Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.ENCRYPT_MODE, privateKey);
				final byte[] digitalSignature = cipher.doFinal(msgHash);
				((ObjectNode) json).put(CIPHER_DATA, Base64.getEncoder().encodeToString(digitalSignature));
			} else {
				((ObjectNode) json).put(CIPHER_DATA, Base64.getEncoder().encodeToString(msgHash));
			}
			return mapper.writeValueAsString(json);

		} catch (final Exception e) {
			log.error("Error ciphering audit data. Data not ciphered.", e);
			return item;
		}
	}

	public void audit(String item) throws JSONException, RouterCrudServiceException {
		log.debug("executeAuditOperations: begin");
		try {
			final JSONObject jsonObj = new JSONObject(item);
			final String extraData = jsonObj.has("extraData") ? jsonObj.getJSONObject("extraData").toString() : "";
			jsonObj.put("extraData", extraData);
			item = jsonObj.toString();
			final AuditParameters commonParams = getAuditParameters(jsonObj);
			if (commonParams.getUser() != null) {
				if (!AuditConst.ANONYMOUS_USER.equals(commonParams.getUser())) {
					final Optional<MasterUser> user = masterUserService.findUser(commonParams.getUser());
					if (!user.isPresent()) {
						log.info("The user {} does not exists so change to anonymous user", commonParams.getUser());
						commonParams.setUser(AuditConst.ANONYMOUS_USER);
					} else {
						userDetailsService.loadUserByUsername(commonParams.getUser());
					}
				}

				final String ontology = ServiceUtils.getAuditCollectionName(commonParams.getUser());
				// Create ontology audit for user or anonymous if not exists
				if (commonParams.getUser().equals(AuditConst.ANONYMOUS_USER)) {
					if (ontologyService.getOntologyByIdentification(ontology, AuditConst.ADMIN_USER) == null) {
						log.info("Creating audit ontology for user {}", commonParams.getUser());
						try {
							userOperationsService.createAuditOntology(commonParams.getUser());
						} catch (final Exception e) {
							log.error("Error creating audit ontology for user {}, error: {}", commonParams.getUser(),
									e.getMessage());
						}
					}
				} else {
					if (ontologyService.getOntologyByIdentification(ontology, commonParams.getUser()) == null) {
						log.info("Creating audit ontology for user{}", commonParams.getUser());
						try {
							userOperationsService.createAuditOntology(commonParams.getUser());
						} catch (final Exception e) {
							log.error("Error creating audit ontology for user {}, error: {}", commonParams.getUser(),
									e.getMessage());
						}
					}
				}

				OperationModel.Source operation = null;
				try {
					final EventType eventType = EventType.valueOf(commonParams.getEventType());
					if (eventType.equals(EventType.SECURITY)) {
						operation = OperationModel.Source.AUDIT;
					} else if (eventType.equals(EventType.IOTBROKER)) {
						operation = OperationModel.Source.IOTBROKER;
					} else {
						operation = OperationModel.Source.AUDIT;
					}
				} catch (final Exception e) {
					operation = OperationModel.Source.AUDIT;

				}

				final OperationModel model = OperationModel
						.builder(ontology, OperationType.INSERT, commonParams.getUser(), operation).body(item)
						.queryType(com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType.NONE)
						.cacheable(false).build();

				routerCrudService.insertWithNoAudit(model);
			}

		} catch (final Exception e) {
			log.error("executeAuditOperations, error item: {}", item, e);
		}
	}

	private AuditParameters getAuditParameters(JSONObject jsonObj) throws JSONException {
		final String user = !jsonObj.isNull(USER_KEY) ? jsonObj.getString(USER_KEY) : null;
		final String eventType = jsonObj.getString(EVENT_TYPE_KEY);
		final String operationType = jsonObj.getString(OPERATION_TYPE_KEY);
		return new AuditParameters(user, eventType, operationType);
	}

	private boolean ignoreAudit() {
		boolean b = false;
		try {
			b = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getIgnore();
		} catch (final RuntimeException e) {
			log.error("Could not find property ignore-audit, returning false as default");
		}
		return b;
	}

	private boolean isCipher() {
		boolean b = false;
		try {
			b = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getHash();
		} catch (final RuntimeException e) {
			log.error("Could not find property hash-audit, returning false as default");
		}
		return b;
	}

	private boolean isSigned() {
		boolean b = false;
		try {
			b = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getSigned();
		} catch (final RuntimeException e) {
			log.error("Could not find property signed-audit, returning false as default");
		}
		return b;
	}

	private String getKeyStorePath() {
		String result = null;
		try {
			result = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getKeystorePath();
		} catch (final RuntimeException e) {
			log.error("Could not find property keystore-path, returning null as default");
		}
		return result;
	}

	private String getKeystorePassword() {
		String result = null;
		try {
			result = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getKeystorePassword();
		} catch (final RuntimeException e) {
			log.error("Could not find property keystore-password, returning null as default");
		}
		return result;
	}

}
