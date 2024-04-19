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
package com.minsait.onesait.platform.router.service.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.services.subscription.SubscriptionService;
import com.minsait.onesait.platform.libraries.nodered.auth.NoderedAuthenticationService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.router.service.app.model.AdviceNotificationModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceNotificationService;
import com.minsait.onesait.platform.router.service.app.service.KafkaTopicKsqlNotificationService;
import com.minsait.onesait.platform.router.service.app.service.KafkaTopicOntologyNotificationService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;
import com.minsait.onesait.platform.router.service.app.service.RulesEngineNotificationService;
import com.minsait.onesait.platform.router.service.app.service.advice.AdviceServiceImpl;
import com.minsait.onesait.platform.router.subscription.SubscriptionManager;

import lombok.extern.slf4j.Slf4j;

@Service("routerFlowManagerService")
@Slf4j
public class RouterFlowManagerService {

	@Value("${onesaitplatform.router.notifications.pool.nodered:10}")
	private int noderedThreadPool;

	@Value("${onesaitplatform.router.notifications.pool.queue.nodered:-1}")
	private int noderedThreadPoolQueue;

	@Value("${onesaitplatform.router.notifications.pool.kafka:10}")
	private int kafkaThreadPool;

	@Value("${onesaitplatform.router.notifications.pool.queue.kafka:-1}")
	private int kafkaThreadPoolQueue;

	@Value("${onesaitplatform.router.notifications.pool.rulesengine:10}")
	private int rulesEngineThreadPool;

	@Value("${onesaitplatform.router.notifications.pool.queue.rulesengine:-1}")
	private int rulesEngineThreadPoolQueue;

	@Value("${onesaitplatform.router.notifications.pool.subscription:10}")
	private int subscriptionThreadPool;

	@Value("${onesaitplatform.router.notifications.pool.queue.subscription:-1}")
	private int subscriptionThreadPoolQueue;

	@Autowired
	private RouterCrudService routerCrudService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private AdviceServiceImpl adviceServiceImpl;

	@Autowired
	private RulesEngineNotificationService rulesEngineNotificationService;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private SubscriptionManager subscriptorManager;

	@Autowired
	private NoderedAuthenticationService noderedAthService;

	@Autowired
	@Qualifier("notificationAdviceNodeRED")
	private IQueue<NotificationCompositeModel> notificationAdviceNodeRED;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private ExecutorService noderedNotificatorExecutor;
	private ExecutorService kafkaNotificatorExecutor;
	private ExecutorService rulesEngineNotificatorExecutor;
	private ExecutorService subscriptionNotificatorExecutor;
	private ExecutorService noderedNotificatorProcessor;
	
	private Map<String, AdviceNotificationService> adviceNotificationServiceBeans;

	private static final String ERROR_MESSAGE_STR = "{ \"error\" : { \"message\" : \"";

	@PostConstruct
	public void init() {
		if (noderedThreadPoolQueue < 0) {
			noderedNotificatorExecutor = Executors.newFixedThreadPool(noderedThreadPool);
		} else {
			final BlockingQueue<Runnable> q = new ArrayBlockingQueue<>(noderedThreadPoolQueue);
			noderedNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}

		if (kafkaThreadPoolQueue < 0) {
			kafkaNotificatorExecutor = Executors.newFixedThreadPool(kafkaThreadPool);
		} else {
			final BlockingQueue<Runnable> q = new ArrayBlockingQueue<>(kafkaThreadPoolQueue);
			kafkaNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}
		if (rulesEngineThreadPoolQueue < 0) {
			rulesEngineNotificatorExecutor = Executors.newFixedThreadPool(rulesEngineThreadPool);
		} else {
			final BlockingQueue<Runnable> q = new ArrayBlockingQueue<>(rulesEngineThreadPoolQueue);
			rulesEngineNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}
		if (subscriptionThreadPoolQueue < 0) {
			subscriptionNotificatorExecutor = Executors.newFixedThreadPool(subscriptionThreadPool);
		} else {
			final BlockingQueue<Runnable> q = new ArrayBlockingQueue<>(subscriptionThreadPoolQueue);
			subscriptionNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}

		noderedNotificatorProcessor = Executors.newSingleThreadExecutor();
		noderedNotificatorProcessor.execute(() -> {
			while (true) {
				try {
					final NotificationCompositeModel notificationModel = notificationAdviceNodeRED.take();
					log.debug("Notification evaluatioin. Ontology: {}, type: {}, message: {}",
							notificationModel.getNotificationModel().getOperationModel().getOntologyName(),
							notificationModel.getNotificationModel().getOperationModel().getOperationType(),
							notificationModel.getNotificationModel().getOperationModel().getBody());
					// process notificationModel

					notifyNoderedNotificationNodes(notificationModel);
				} catch (final Exception e1) {
					log.error("Interrupted Disconnected Clients Queue listening", e1);
				}
			}
		});
		
		adviceNotificationServiceBeans = applicationContext
				.getBeansOfType(AdviceNotificationService.class);
	}

	@PreDestroy
	public void destroy() {
		noderedNotificatorExecutor.shutdown();
		kafkaNotificatorExecutor.shutdown();
		rulesEngineNotificatorExecutor.shutdown();
		subscriptionNotificatorExecutor.shutdown();
		noderedNotificatorProcessor.shutdown();
	}

	ObjectMapper mapper = new ObjectMapper();

	public OperationResultModel startBrokerFlow(NotificationModel model) {
		log.debug("startBrokerFlow: Notification Model arrived");
		final NotificationCompositeModel compositeModel = new NotificationCompositeModel();
		compositeModel.setNotificationModel(model);

		try {
			final NotificationCompositeModel result = executeCrudOperations(compositeModel);
			if (result.getOperationResultModel().isStatus()) {
				log.info("Check if there are any subscriptor to this ontology that should be notified.");
			}

			// if OK notify NodeRED scripts and Kafka subscriptors
			if (compositeModel.getOperationResultModel().isStatus()) {
				notifyScriptsAndNodereds(compositeModel);
				notifyRulesEngine(compositeModel);
				notifyKafkaTopics(result);
				notifySubscriptors(model);
			}

			return result.getOperationResultModel();
		} catch (final Exception e) {
			log.error("Error processing message", e);
			final OperationResultModel output = new OperationResultModel();
			output.setResult("ERROR");
			output.setStatus(false);
			if (e.getCause() != null) {
				// If the Exception is caused by persistence....
				if (e.getCause() instanceof DBPersistenceException) {

					final DBPersistenceException dbex = (DBPersistenceException) e.getCause();
					if (!dbex.getErrorsResult().isEmpty())
						output.setMessage(ERROR_MESSAGE_STR + dbex.getDetailedMessage() + "\", \"errors\" : "
								+ (new Gson()).toJson(dbex.getErrorsResult()) + " }}");
					else if (dbex.getCause() != null)
						output.setMessage(ERROR_MESSAGE_STR + dbex.getDetailedMessage() + "\", \"errors\" : ["
								+ (new Gson()).toJson(new ErrorResult(dbex.getCause().getMessage())) + "] }}");
					else
						output.setMessage(ERROR_MESSAGE_STR + dbex.getDetailedMessage() + "\", \"errors\" : [] }}");

				} else {
					output.setMessage(e.getCause().getMessage());
				}
			} else {
				output.setMessage(e.getMessage());
			}
			return output;
		}

	}

	public OperationResultModel notifyModules(NotificationModel model) {
		// TO-DO es necesario el result?

		final NotificationCompositeModel compositeModel = new NotificationCompositeModel();
		compositeModel.setNotificationModel(model);
		log.debug("startNotifyModules: advice model arrived");
		notifyScriptsAndNodereds(compositeModel);
		notifyRulesEngine(compositeModel);
		notifyKafkaTopics(compositeModel);
		notifySubscriptors(model);

		return new OperationResultModel();
	}

	public NotificationCompositeModel executeCrudOperations(NotificationCompositeModel compositeModel)
			throws GenericOPException, RouterCrudServiceException {
		log.debug("executeCrudOperations: Begin");

		final OperationModel model = compositeModel.getNotificationModel().getOperationModel();
		final String METHOD = model.getOperationType().name();

		final OperationResultModel fallback = new OperationResultModel();
		fallback.setResult("NO_RESULT");
		fallback.setStatus(false);
		fallback.setMessage("Operation Not Executed due to lack of OperationType");
		compositeModel.setOperationResultModel(fallback);

		try {
			if (METHOD.equalsIgnoreCase(ApiOperation.Type.GET.name())
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.QUERY.name())) {
				final OperationResultModel result = routerCrudService.query(model);
				compositeModel.setOperationResultModel(result);
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.POST.name())
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.INSERT.name())) {
				final OperationResultModel result = routerCrudService.insert(model);
				compositeModel.setOperationResultModel(result);
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.PUT.name())
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.UPDATE.name())) {
				final OperationResultModel result = routerCrudService.update(model);
				compositeModel.setOperationResultModel(result);
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.DELETE.name())
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.DELETE.name())) {
				final OperationResultModel result = routerCrudService.delete(model);
				compositeModel.setOperationResultModel(result);
			} else {
				throw new IllegalArgumentException("Operation not soported: " + METHOD);
			}

		} catch (final RouterCrudServiceException ex) {
			log.error("executeCrudOperations: Exception " + ex.getMessage(), ex);
			compositeModel.setOperationResultModel(ex.getResult());
			throw ex;
		} catch (final Exception e) {
			log.error("executeCrudOperations: Exception " + e.getMessage(), e);
			throw e;
		}

		log.debug("executeCrudOperations: End");
		return compositeModel;

	}

	private List<AdviceNotificationModel> getScriptsAndNodereds(NotificationCompositeModel compositeModel) {
		log.debug("getScriptsAndNodereds: Begin");
		final OperationModel model = compositeModel.getNotificationModel().getOperationModel();

		final String ontologyName = model.getOntologyName();
		String messageType = model.getOperationType().name();
		if (model.getQueryType() != null && model.getQueryType().equals(QueryType.SQL) && model.getBody() != null) {
			if (model.getBody().trim().toUpperCase().startsWith(OperationType.DELETE.name()))
				messageType = OperationType.DELETE.name();
			if (model.getBody().trim().toUpperCase().startsWith(OperationType.UPDATE.name()))
				messageType = OperationType.UPDATE.name();
		}

		final List<AdviceNotificationModel> listNotifications = new ArrayList<>();

		final Iterator<Entry<String, AdviceNotificationService>> iterator = adviceNotificationServiceBeans.entrySet().iterator();
		try {
			while (iterator.hasNext()) {
				final Entry<String, AdviceNotificationService> item = iterator.next();
				final AdviceNotificationService service = item.getValue();
				final List<AdviceNotificationModel> list = service.getAdviceNotificationModel(ontologyName,
						messageType);
				if (list != null && !list.isEmpty())
					listNotifications.addAll(list);
			}
		} catch (final Exception e) {
			log.error("Error while obtaining node red notifications {}", e.getMessage());
		}

		log.debug("getScriptsAndNodereds: End");

		return listNotifications;

	}

	private void notifySubscriptors(NotificationModel model) {
		final List<Subscription> subscriptions = subscriptionService
				.findByOntology(model.getOperationModel().getOntologyName());
		if (!subscriptions.isEmpty()) {
			log.info("There are {} subscriptions for the ontology {}", subscriptions.size(),
					model.getOperationModel().getOntologyName());
			final String vertical = MultitenancyContextHolder.getVerticalSchema();
			final String tenant = MultitenancyContextHolder.getTenantName();
			for (final Subscription subscription : subscriptions) {
				subscriptionNotificatorExecutor.execute(() -> {
					try {
						MultitenancyContextHolder.setTenantName(tenant);
						MultitenancyContextHolder.setVerticalSchema(vertical);
						log.debug("notifySubscriptors: Begin");

						subscriptorManager.notifySubscriptors(subscription,
								objectMapper.readTree(model.getOperationModel().getBody()));

						MultitenancyContextHolder.clear();
						log.debug("notifySubscriptors: END");
					} catch (final Exception e) {
						log.error("Error Notifing suscriptor Event. {}", e);
					}

				});
			}
		}
	}

	private void notifyNoderedNotificationNodes(NotificationCompositeModel compositeModel) {
		
		// check if necesary to send
		if (Boolean.TRUE.equals(compositeModel.getDiscardAfterElapsedTimeEnabled())) {
			// compare timestamps
			Date now = new Date();
			Date notifCreationTS = compositeModel.getOriginalNotificationTimestamp();
			Integer timeElapsed = Math.toIntExact((now.getTime() - notifCreationTS.getTime()) / 1000);
			if (timeElapsed > compositeModel.getMaxRetryElapsedTime()) {
				// discard message
				log.debug(
						"Notification message wil be discarted. Elapsed time:{}, Max time allowed: {}, Ontology: {}, Type: {}, Message: {}",
						timeElapsed, compositeModel.getMaxRetryElapsedTime(),
						compositeModel.getNotificationModel().getOperationModel().getOntologyName(),
						compositeModel.getNotificationModel().getOperationModel().getOperationType(),
						compositeModel.getNotificationModel().getOperationModel().getBody());
				return;
			}
		}
		// try to send notification model
		try {
			// Check NodeRED Authentication
			compositeModel.setHeaderAuthValue(noderedAthService.getNoderedAuthAccessToken(
					compositeModel.getDomainOwner(), compositeModel.getDomainIdentification()));
			adviceServiceImpl.execute(compositeModel);
		} catch (Exception e) {
			// If not successfully, requeue
			log.error("Error sending NodeRED Notification");
			if(Boolean.TRUE.equals(compositeModel.getRetryOnFaialureEnabled())) {
				compositeModel.setRetriedNotification(true);
				notificationAdviceNodeRED.add(compositeModel);
			}
		}

	}

	private void notifyScriptsAndNodereds(NotificationCompositeModel compositeModel) {

		// Notification to Scripts in NodeRED
		final List<AdviceNotificationModel> lendpoints = getScriptsAndNodereds(compositeModel);
		final String vertical = MultitenancyContextHolder.getVerticalSchema();
		final String tenant = MultitenancyContextHolder.getTenantName();
		for (final AdviceNotificationModel entity : lendpoints) {
			noderedNotificatorExecutor.execute(() -> {
				try {
					MultitenancyContextHolder.setTenantName(tenant);
					MultitenancyContextHolder.setVerticalSchema(vertical);
					log.debug("adviceScriptsAndNodereds: Begin");

					final NotificationCompositeModel compositeModelTemp = new NotificationCompositeModel();
					compositeModelTemp.setNotificationModel(compositeModel.getNotificationModel());

					compositeModelTemp.setUrl(entity.getUrl());
					compositeModelTemp.setNotificationEntityId(entity.getEntityId());

					final SuscriptionModel model = entity.getSuscriptionModel();
					if (model != null) {
						final OperationModel operationModel = OperationModel.builder(model.getOntologyName(),
								com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.QUERY,
								model.getUser(), Source.INTERNAL_ROUTER)
								.body(appendOIDForSQL(model.getQuery(),
										compositeModel.getOperationResultModel().getResult()))
								.queryType(QueryType.valueOf(model.getQueryType().name())).build();

						OperationResultModel result = null;

						result = routerCrudService.execute(operationModel);

						if (result != null) {
							compositeModelTemp.setOperationResultModel(result);
						}

					}
					compositeModelTemp.setHeaderAuthKey(entity.getUrlAuthkey());
					compositeModelTemp.setHeaderAuthValue(entity.getUrlAuthValue());
					Date originalNotificationTimestamp = new Date();// current date
					compositeModelTemp.setOriginalNotificationTimestamp(originalNotificationTimestamp);
					compositeModelTemp.setMaxRetryElapsedTime(entity.getMaxRetryElapsedTime());
					compositeModelTemp.setRetryOnFaialureEnabled(entity.getRetryOnFaialureEnabled());
					compositeModelTemp.setDiscardAfterElapsedTimeEnabled(entity.getDiscardAfterElapsedTimeEnabled());
					compositeModelTemp.setDomainIdentification(entity.getDomainIdentification());
					compositeModelTemp.setDomainOwner(entity.getDomainOwner());
					compositeModelTemp.setRetriedNotification(false);
					// set to the queue
					notificationAdviceNodeRED.add(compositeModelTemp);
					MultitenancyContextHolder.clear();
					log.debug("adviceScriptsAndNodereds: END");
				} catch (final Exception e) {
					log.error("Error processing Script Event", e);
				}

			});
		}
	}

	private String appendOIDForSQL(String query, String objectId) {
		if (query.toUpperCase().contains("SELECT")) {
			if (query.toUpperCase().contains("WHERE")) {
				return query + " AND _id = OID(\"" + objectId + "\")";
			} else {
				return query + " WHERE _id = OID(\"" + objectId + "\")";
			}
		} else {
			return "";
		}
	}

	public void notifyKafkaTopics(NotificationCompositeModel compositeModel) {
		log.debug("getKafkaTopicToNotify: Begin");
		final OperationModel model = compositeModel.getNotificationModel().getOperationModel();

		final String ontologyName = model.getOntologyName();
		final OperationType messageType = model.getOperationType();
		final String payload = model.getBody();

		if (messageType == OperationType.POST || messageType == OperationType.INSERT) {
						
			// KSQL Notification to ORIGINS
			notifyKafkaKsqlTopics(ontologyName, payload);

			// Kafka notification to Ontology's Notification Topic
			notifyKafkaOntologyTopics(ontologyName, payload);
		}

		log.debug("getKafkaTopicToNotify: End");

	}

	private void notifyKafkaKsqlTopics(String ontologyName, String payload) {
		final Map<String, KafkaTopicKsqlNotificationService> ksqlMap = applicationContext
				.getBeansOfType(KafkaTopicKsqlNotificationService.class);

		final Iterator<Entry<String, KafkaTopicKsqlNotificationService>> ksqlIterator = ksqlMap.entrySet().iterator();

		while (ksqlIterator.hasNext()) {
			final Entry<String, KafkaTopicKsqlNotificationService> item = ksqlIterator.next();

			kafkaNotificatorExecutor.execute(() -> {
				final KafkaTopicKsqlNotificationService service = item.getValue();
				final List<String> list = service.getKafkaTopicKsqlNotification(ontologyName);
				if (list != null && !list.isEmpty()) {
					for (final String kafkaTopic : list) {
						kafkaTemplate.send(kafkaTopic, payload);
					}
				}

			});
		}

	}

	private void notifyKafkaOntologyTopics(String ontologyName, String payload) {

		final Map<String, KafkaTopicOntologyNotificationService> kafkaMap = applicationContext
				.getBeansOfType(KafkaTopicOntologyNotificationService.class);

		final Iterator<Entry<String, KafkaTopicOntologyNotificationService>> kafkaIterator = kafkaMap.entrySet()
				.iterator();

		while (kafkaIterator.hasNext()) {
			final Entry<String, KafkaTopicOntologyNotificationService> item = kafkaIterator.next();
			final String vertical = MultitenancyContextHolder.getVerticalSchema();
			final String tenant = MultitenancyContextHolder.getTenantName();
			kafkaNotificatorExecutor.execute(() -> {
				MultitenancyContextHolder.setTenantName(tenant);
				MultitenancyContextHolder.setVerticalSchema(vertical);
				final KafkaTopicOntologyNotificationService service = item.getValue();
				final String kafkaTopic = service.getKafkaTopicOntologyNotification(ontologyName);
				MultitenancyContextHolder.clear();
				if (kafkaTopic != null) {
					kafkaTemplate.send(kafkaTopic, payload);
				}

			});

		}
	}

	public void notifyRulesEngine(NotificationCompositeModel compositeModel) {
		log.debug("rulesEngineNotifications: Begin");
		final OperationModel model = compositeModel.getNotificationModel().getOperationModel();

		final String ontology = model.getOntologyName();
		final OperationType messageType = model.getOperationType();
		final String payload = model.getBody();
		final Source source = compositeModel.getNotificationModel().getOperationModel().getSource();
		if ((messageType == OperationType.POST || messageType == OperationType.INSERT)
				&& rulesEngineNotificationService.notifyToEngine(ontology) && !Source.RULES_ENGINE.equals(source)) {
			final String vertical = MultitenancyContextHolder.getVerticalSchema();
			final String tenant = MultitenancyContextHolder.getTenantName();
			rulesEngineNotificatorExecutor.execute(() -> {
				MultitenancyContextHolder.setTenantName(tenant);
				MultitenancyContextHolder.setVerticalSchema(vertical);
				rulesEngineNotificationService.notify(ontology, payload);
				MultitenancyContextHolder.clear();
			});
		}

		log.debug("rulesEngineNotifications: End");

	}

}
