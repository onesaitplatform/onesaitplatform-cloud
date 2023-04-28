/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.router.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.repository.SubscriptionRepository;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.subscription.model.SubscriptorClient;
import com.minsait.onesait.platform.router.subscription.notificator.Notificator;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubscriptionManagerImpl implements SubscriptionManager {

	private static final String MAP_SUBSCRIPTION = "mapSubscription";
	private static final String ERROR_STR = "ERROR";
	private static final String EQUAL = "igual";
	private static final String DISTINCT = "distinto";
	private static final String LESS = "menor";
	private static final String HIGH = "mayor";
	private static final String HIGH_EQUAL = "mayor-igual";
	private static final String LESS_EQUAL = "menor-igual";

	@Value("${onesaitplatform.router.notifications.pool.subscription.notificator:10}")
	private int subscriptionNotificatorThreadPool;

	@Value("${onesaitplatform.router.notifications.pool.queue.subscription.notificator:-1}")
	private int subscriptionNotificatorThreadPoolQueue;

	@Autowired
	@Qualifier("globalCache")
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Notificator notificator;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	private ExecutorService subscriptionNotificatorExecutor;

	@PostConstruct
	public void init() {
		if (subscriptionNotificatorThreadPoolQueue < 0) {
			subscriptionNotificatorExecutor = Executors.newFixedThreadPool(subscriptionNotificatorThreadPool);
		} else {
			final BlockingQueue q = new ArrayBlockingQueue(subscriptionNotificatorThreadPoolQueue);
			subscriptionNotificatorExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
		}

		List<Subscriptor> subscriptors = subscriptorRepository.findAll();
		for (Subscriptor client : subscriptors) {
			IMap<String, List<SubscriptorClient>> map = hazelcastInstance
					.getMap(MAP_SUBSCRIPTION + client.getSubscription().getIdentification());
			if (map == null) {
				log.debug("Map for subscription {} doen't exist.", client.getSubscription().getIdentification());
				new MultiMapConfig().setName(MAP_SUBSCRIPTION + client.getSubscription().getIdentification())
						.setValueCollectionType("LIST").setBinary(false);
				map = hazelcastInstance.getMap(MAP_SUBSCRIPTION + client.getSubscription().getIdentification());
				log.debug("Map for subscription {} created.", client.getSubscription().getIdentification());
			}
			if (map.containsKey(client.getQueryValue())) {
				map.lock(client.getQueryValue());
				List<SubscriptorClient> clients = map.get(client.getQueryValue());
				clients.add(new SubscriptorClient(client.getSubscriptionGW(), client.getSubscriptionId(),
						client.getClientId(), client.getCallbackEndpoint(), null));
				map.put(client.getQueryValue(), clients, 0, TimeUnit.SECONDS);
				map.unlock(client.getQueryValue());
			} else {
				List<SubscriptorClient> clients = new ArrayList<>();
				clients.add(new SubscriptorClient(client.getSubscriptionGW(), client.getSubscriptionId(),
						client.getClientId(), client.getCallbackEndpoint(), null));
				map.put(client.getQueryValue(), clients, 0, TimeUnit.SECONDS);
			}

			log.info("Add new client {} to subscription {}", client.getSubscriptionGW(),
					client.getSubscription().getIdentification());
		}
	}

	@PreDestroy
	public void destroy() {
		subscriptionNotificatorExecutor.shutdown();
	}

	@Override
	public OperationResultModel subscription(SubscriptionModel model) {
		OperationResultModel result = new OperationResultModel();
		result.setOperation(model.getOperationType().name());
		try {
			log.debug("Check if the subscription {} exist.", model.getSubscription());
			List<Subscription> subscriptions = subscriptionRepository.findByIdentification(model.getSubscription());
			if (subscriptions.isEmpty()) {
				log.error("The Subscription {} doesn't exist.", model.getSubscription());
				result.setStatus(false);
				result.setMessage("The Subscription" + model.getSubscription() + " doesn't exist.");
				result.setResult(ERROR_STR);
				result.setErrorCode("404");
				return result;
			} else {
				log.debug("Check if  exist a MultiMap in hazelcast for subscription {}", model.getSubscription());
				IMap<String, List<SubscriptorClient>> map = hazelcastInstance
						.getMap(MAP_SUBSCRIPTION + model.getSubscription());
				if (map == null) {
					log.debug("Map for subscription {} doen't exist.", model.getSubscription());
					new MultiMapConfig().setName(MAP_SUBSCRIPTION + model.getSubscription())
							.setValueCollectionType("LIST").setBinary(false);
					map = hazelcastInstance.getMap(MAP_SUBSCRIPTION + model.getSubscription());
					log.debug("Map for subscription {} created.", model.getSubscription());
				}
				if (map.containsKey(model.getQueryValue())) {
					map.lock(model.getQueryValue());
					List<SubscriptorClient> clients = map.get(model.getQueryValue());
					clients.add(new SubscriptorClient(model.getSubscriptionGW(), model.getSuscriptionId(),
							model.getClientId(), model.getCallback(), model.getSessionKey()));
					map.put(model.getQueryValue(), clients, 0, TimeUnit.SECONDS);
					map.unlock(model.getQueryValue());
				} else {
					List<SubscriptorClient> clients = new ArrayList<>();
					clients.add(new SubscriptorClient(model.getSubscriptionGW(), model.getSuscriptionId(),
							model.getClientId(), model.getCallback(), model.getSessionKey()));
					map.put(model.getQueryValue(), clients, 0, TimeUnit.SECONDS);
				}

				Subscriptor subscriptor = new Subscriptor();
				subscriptor.setSubscriptionGW(model.getSubscriptionGW());
				subscriptor.setSubscriptionId(model.getSuscriptionId());
				subscriptor.setSubscription(subscriptions.get(0));
				subscriptor.setQueryValue(model.getQueryValue());
				subscriptor.setClientId(model.getClientId());

				if (model.getCallback() != null) {
					subscriptor.setCallbackEndpoint(model.getCallback());
				} else {
					subscriptor.setClientId(model.getClientId());
				}

				subscriptorRepository.save(subscriptor);

				log.info("Add new client {} to subscription {}", model.getClientId(), model.getSubscription());

				result.setStatus(true);
				result.setResult("{\"subscriptionId\" : \"" + model.getSuscriptionId() + "\"}");
				return result;
			}
		} catch (Exception e) {
			log.error("Error subscribing client to subscription {}. {}", model.getSubscription(), e);
			result.setResult(ERROR_STR + e.getMessage());
			result.setStatus(false);
			result.setMessage(e.getMessage());
			result.setErrorCode(ERROR_STR);
			return result;
		}

	}

	@Override
	public OperationResultModel unsubscription(SubscriptionModel model) {
		OperationResultModel result = new OperationResultModel();
		result.setOperation(model.getOperationType().name());
		try {
			log.debug("Check if the subscriptor with id  {} exist.", model.getSuscriptionId());

			Subscriptor subscriptor = subscriptorRepository.findBySubscriptionId(model.getSuscriptionId());
			if (subscriptor != null) {
				IMap<String, List<SubscriptorClient>> map = hazelcastInstance
						.getMap(MAP_SUBSCRIPTION + subscriptor.getSubscription().getIdentification());
				List<SubscriptorClient> clients = map.get(subscriptor.getQueryValue());
				SubscriptorClient clientToRemove = null;
				for (SubscriptorClient client : clients) {
					if (client.getClientId().equalsIgnoreCase(subscriptor.getSubscriptionId())
							|| client.getClientId().equalsIgnoreCase(subscriptor.getClientId())) {
						clientToRemove = client;
					}
				}
				if (clientToRemove != null) {
					clients.remove(clientToRemove);
					map.put(subscriptor.getQueryValue(), clients, 0, TimeUnit.SECONDS);
					subscriptorRepository.delete(subscriptor);
				}
			}
			result.setStatus(true);
			result.setResult("OK");
			result.setMessage("Client unsuscribe successfully");
			return result;

		} catch (Exception e) {
			log.error("Error unsubscribing client with subscriptionId {}. {}", model.getSuscriptionId(), e);
			result.setResult(ERROR_STR + e.getMessage());
			result.setStatus(false);
			result.setMessage(e.getMessage());
			result.setErrorCode(ERROR_STR);
			return result;
		}
	}

	@Override
	public void notifySubscriptors(Subscription subscription, JsonNode instance) {
		IMap<String, List<SubscriptorClient>> map = hazelcastInstance
				.getMap(MAP_SUBSCRIPTION + subscription.getIdentification());
		if (!map.isEmpty()) {
			log.debug("There are subscritors in the map {}", MAP_SUBSCRIPTION + subscription.getIdentification());

			if (instance.isArray()) {
				for (final JsonNode objNode : instance) {
					this.checkNotify(objNode, subscription, map);
				}
			} else {
				this.checkNotify(instance, subscription, map);
			}
		} else {
			log.debug("There are NOT subscritors in the map {}", MAP_SUBSCRIPTION + subscription.getIdentification());
		}
	}

	private void checkNotify(JsonNode instance, Subscription subscription, IMap<String, List<SubscriptorClient>> map) {

		try {
			log.info("Execute JsonPath: {} on instance : {}", subscription.getProjection(),
					objectMapper.writeValueAsString(instance));
			Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
					.options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();
			String dataToNotify = JsonPath.using(conf).parse(objectMapper.writeValueAsString(instance))
					.read(subscription.getProjection()).toString();
			if (dataToNotify != null && !dataToNotify.equals("[]")) {
				ArrayNode queryField = JsonPath.using(conf).parse(objectMapper.writeValueAsString(instance))
						.read(subscription.getQueryField());
				String field = null;
				if (queryField.size() == 1 && queryField.get(0).toString().startsWith("\"")) {
					field = queryField.get(0).toString().substring(1, queryField.get(0).toString().length() - 1);
				} else {
					field = queryField.get(0).toString();
				}
				if (subscription.getQueryOperator().equalsIgnoreCase(EQUAL)) {
					if (map.containsKey(field)) {
						this.notify(map, field, dataToNotify);
					}
				} else {
					Set<String> keys = map.keySet();
					for (String key : keys) {
						if (isNumeric(key)
								&& (subscription.getQueryOperator().equalsIgnoreCase(HIGH)
										&& Double.parseDouble(field) > Double.parseDouble(key))
								|| (subscription.getQueryOperator().equalsIgnoreCase(LESS)
										&& Double.parseDouble(field) < Double.parseDouble(key))
								|| (subscription.getQueryOperator().equalsIgnoreCase(HIGH_EQUAL)
										&& Double.parseDouble(field) >= Double.parseDouble(key))
								|| (subscription.getQueryOperator().equalsIgnoreCase(LESS_EQUAL)
										&& Double.parseDouble(field) <= Double.parseDouble(key))
								|| (subscription.getQueryOperator().equalsIgnoreCase(DISTINCT)
										&& Double.parseDouble(field) != Double.parseDouble(key))) {

							this.notify(map, key, dataToNotify);

						}
					}
				}
			}

		} catch (JsonProcessingException e) {
			log.error("Error parsing ontology instance. {}", e);
		}
	}

	private void notify(IMap<String, List<SubscriptorClient>> map, String key, String dataToNotify) {
		final String vertical = MultitenancyContextHolder.getVerticalSchema();
		final String tenant = MultitenancyContextHolder.getTenantName();
		List<SubscriptorClient> clients = map.get(key);
		if (!clients.isEmpty()) {
			for (SubscriptorClient client : clients) {
				subscriptionNotificatorExecutor.execute(() -> {
					try {
						MultitenancyContextHolder.setTenantName(tenant);
						MultitenancyContextHolder.setVerticalSchema(vertical);
						log.debug("notifySubscriptors: Begin");
						Boolean isOk = notificator.notify(dataToNotify, client);
						log.debug("Notify sent.");
						if (!isOk) {
							log.info("Notification failed. Removing client subscriptor.");
							clients.remove(client);
							map.put(key, clients, 0, TimeUnit.SECONDS);
							Subscriptor subscriptor = subscriptorRepository.findBySubscriptionId(client.getClientId());
							subscriptorRepository.delete(subscriptor);
						}

						MultitenancyContextHolder.clear();
						log.debug("notifySubscriptors: END");
					} catch (final Exception e) {
						log.error("Error Notifing suscriptor Event. {}", e);
					}

				});
			}
		}

	}

	private static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
