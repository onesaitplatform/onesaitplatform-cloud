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

import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ApiOperation;
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
import com.minsait.onesait.platform.router.service.app.service.KafkaTopicNotificationService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;
import com.minsait.onesait.platform.router.service.app.service.RulesEngineNotificationService;
import com.minsait.onesait.platform.router.service.app.service.advice.AdviceServiceImpl;

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

	private ExecutorService noderedNotificatorExecutor;
	private ExecutorService kafkaNotificatorExecutor;
	private ExecutorService rulesEngineNotificatorExecutor;
	private static final String ERROR_MESSAGE_STR = "{ \"error\" : { \"message\" : \"";

	@PostConstruct
	public void init() {
		if (noderedThreadPoolQueue < 0) {
			noderedNotificatorExecutor = Executors.newFixedThreadPool(noderedThreadPool);
		} else {
			final BlockingQueue q = new ArrayBlockingQueue(noderedThreadPoolQueue);
			noderedNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}

		if (kafkaThreadPoolQueue < 0) {
			kafkaNotificatorExecutor = Executors.newFixedThreadPool(kafkaThreadPool);
		} else {
			final BlockingQueue q = new ArrayBlockingQueue(kafkaThreadPoolQueue);
			kafkaNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}
		if (rulesEngineThreadPoolQueue < 0) {
			rulesEngineNotificatorExecutor = Executors.newFixedThreadPool(rulesEngineThreadPool);
		} else {
			final BlockingQueue q = new ArrayBlockingQueue(rulesEngineThreadPoolQueue);
			rulesEngineNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}
	}

	@PreDestroy
	public void destroy() {
		noderedNotificatorExecutor.shutdown();
		kafkaNotificatorExecutor.shutdown();
		rulesEngineNotificatorExecutor.shutdown();
	}

	ObjectMapper mapper = new ObjectMapper();

	public OperationResultModel startBrokerFlow(NotificationModel model) {
		log.debug("startBrokerFlow: Notification Model arrived");
		final NotificationCompositeModel compositeModel = new NotificationCompositeModel();
		compositeModel.setNotificationModel(model);

		try {
			final NotificationCompositeModel result = executeCrudOperations(compositeModel);

			// if OK notify NodeRED scripts and Kafka subscriptors
			if (compositeModel.getOperationResultModel().isStatus()) {
				notifyScriptsAndNodereds(compositeModel);
				notifyRulesEngine(compositeModel);
				notifyKafkaTopics(result);
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

		final Map<String, AdviceNotificationService> map = applicationContext
				.getBeansOfType(AdviceNotificationService.class);

		final Iterator<Entry<String, AdviceNotificationService>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, AdviceNotificationService> item = iterator.next();
			final AdviceNotificationService service = item.getValue();
			final List<AdviceNotificationModel> list = service.getAdviceNotificationModel(ontologyName, messageType);
			if (list != null && !list.isEmpty())
				listNotifications.addAll(list);
		}

		log.debug("getScriptsAndNodereds: End");

		return listNotifications;

	}

	private void notifyScriptsAndNodereds(NotificationCompositeModel compositeModel) {

		// Notification to Scripts in NodeRED
		final List<AdviceNotificationModel> lendpoints = getScriptsAndNodereds(compositeModel);
		for (final AdviceNotificationModel entity : lendpoints) {
			noderedNotificatorExecutor.execute(() -> {
				try {
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
					adviceServiceImpl.execute(compositeModelTemp);

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

			final Map<String, KafkaTopicNotificationService> map = applicationContext
					.getBeansOfType(KafkaTopicNotificationService.class);

			final Iterator<Entry<String, KafkaTopicNotificationService>> iterator = map.entrySet().iterator();

			while (iterator.hasNext()) {
				final Entry<String, KafkaTopicNotificationService> item = iterator.next();

				kafkaNotificatorExecutor.execute(() -> {
					final KafkaTopicNotificationService service = item.getValue();
					final List<String> list = service.getKafkaTopicNotification(ontologyName);
					if (list != null && !list.isEmpty()) {
						for (final String kafkaTopic : list) {
							kafkaTemplate.send(kafkaTopic, payload);
						}
					}

				});

			}
		}

		log.debug("getKafkaTopicToNotify: End");

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
			rulesEngineNotificatorExecutor.execute(() -> rulesEngineNotificationService.notify(ontology, payload));
		}

		log.debug("rulesEngineNotifications: End");

	}

}
