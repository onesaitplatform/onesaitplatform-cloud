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
package com.minsait.onesait.platform.router.service.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.business.services.user.UserOperationsService;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;
import com.minsait.onesait.platform.router.service.processor.bean.AuditParameters;

import lombok.extern.slf4j.Slf4j;

@Component("auditFlowManagerService")
@Slf4j
public class AuditFlowManagerService {

	@Autowired
	private RouterCrudService routerCrudService;

	@Autowired
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserOperationsService userOperationsService;
	@Autowired
	private IntegrationResourcesService resourcesServices;
	@Autowired
	@Qualifier("auditQueue")
	private IQueue<String> auditQueue;

	private ExecutorService auditLogger;

	@Value("${onesaitplatform.router.audit.extractor.pool:10}")
	private int auditThreadPoolSize;

	@Value("${onesaitplatform.router.audit.process.events:true}")
	private boolean processAuditEvents;

	private static final String USER_KEY = "user";
	private static final String EVENT_TYPE_KEY = "type";
	private static final String OPERATION_TYPE_KEY = "operationType";
	private static final String AUDIT_EVENT_ERROR = "Error processing Audit event";

	@PostConstruct
	public void init() {
		if (!ignoreAudit() && processAuditEvents) {
			auditLogger = Executors.newFixedThreadPool(auditThreadPoolSize);
			for (int i = 0; i < auditThreadPoolSize; i++) {
				auditLogger.execute(() -> {
					while (true) {
						try {
							final String event = auditQueue.take();

							try {
								audit(event);
								log.debug("Procesa auditoria: " + event);
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

	@PreDestroy
	public void destroy() {
		auditLogger.shutdownNow();
	}

	public void audit(String item) throws JSONException, RouterCrudServiceException {
		log.info("executeAuditOperations: begin");
		try {
			final JSONObject jsonObj = new JSONObject(item);
			final AuditParameters commonParams = getAuditParameters(jsonObj);
			if (commonParams.getUser() != null) {
				if (!AuditConst.ANONYMOUS_USER.equals(commonParams.getUser())) {
					final User user = userService.getUser(commonParams.getUser());
					if (user == null) {
						log.info("The user {} does not exists so change to anonymous user",commonParams.getUser());
						commonParams.setUser(AuditConst.ANONYMOUS_USER);
					}
				}

				final String ontology = ServiceUtils.getAuditCollectionName(commonParams.getUser());
				// Create ontology audit for user or anonymous if not exists
				if (commonParams.getUser().equals(AuditConst.ANONYMOUS_USER)) {
					if (ontologyService.getOntologyByIdentification(ontology, AuditConst.ADMIN_USER) == null) {
						log.info("Creating audit ontology for user{}", commonParams.getUser());
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
				if (EventType.valueOf(commonParams.getEventType()).equals(EventType.SECURITY)) {
					operation = OperationModel.Source.AUDIT;
				} else if (EventType.valueOf(commonParams.getEventType()).equals(EventType.IOTBROKER)) {
					operation = OperationModel.Source.IOTBROKER;
				} else {
					operation = OperationModel.Source.AUDIT;
				}
				final OperationModel model = OperationModel
						.builder(ontology, OperationType.INSERT, commonParams.getUser(), operation).body(item)
						.queryType(com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType.NONE)
						.cacheable(false).build();

				routerCrudService.insertWithNoAudit(model);
			}

		} catch (final Exception e) {
			log.error("executeAuditOperations", e);
		}
	}

	private AuditParameters getAuditParameters(JSONObject jsonObj) throws JSONException {
		final String user = !(jsonObj.isNull(USER_KEY)) ? jsonObj.getString(USER_KEY) : null;
		final String eventType = jsonObj.getString(EVENT_TYPE_KEY);
		final String operationType = jsonObj.getString(OPERATION_TYPE_KEY);
		return new AuditParameters(user, eventType, operationType);
	}

	private boolean ignoreAudit() {
		boolean b = false;
		try {
			b = ((Boolean) resourcesServices.getGlobalConfiguration().getEnv().getAudit().get("ignore")).booleanValue();
		} catch (final RuntimeException e) {
			log.error("Could not find property ignore-audit, returning false as default");
		}
		return b;
	}

}
