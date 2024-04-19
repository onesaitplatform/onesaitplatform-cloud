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
package com.minsait.onesait.platform.persistence.mongodb.template.multitenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.services.MultitenantConfigurationService;
import com.minsait.onesait.platform.multitenant.pojo.RTDBConfiguration;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.mongodb.config.MongoDbCredentials;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MongoDBClientManager {

	enum Mode {
		VERTICAL, TENANT, DEFAULT
	}

	@Autowired
	private MultitenantConfigurationService multitenantConfigurationService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private MongoDbCredentials defaultCredentials;

	private static final String DEFAULT = "default";
	private Mode mode = Mode.DEFAULT;

	private RTDBConfiguration configuration;

	private String defaultServers;
	private int socketTimeout;
	private int connectTimeout;
	private int maxWaitTime;
	private int poolSize;
	private boolean readFromSecondaries;
	private String readPreference;
	private String writeConcern;
	private boolean sslEnabled;

	@Getter
	private final Map<String, MongoClient> clients = new HashMap<>();

	private MongoClient defaultClient;

	@PostConstruct
	private void loadConfiguration() {
		configuration = multitenantConfigurationService.getMultitenantRTDBConfiguration();
		if (configuration != null && configuration.getDatabases().getMongodb() != null) {
			Assert.notNull(configuration.getDatabases().getMongodb().getMap().get(DEFAULT),
					"default server must be configured in the Master configuration (MongoDB)");
			mode = Mode.valueOf(configuration.getIsolationLevel().name());
			loadMongoDBClients();
		}
		loadCentralMongoConfig();
	}

	@PreDestroy
	private void destroy() {
		if (defaultClient != null) {
			defaultClient.close();
			defaultClient = null;
		}
		clients.entrySet().forEach(e -> e.getValue().close());
		clients.clear();
	}

	public void loadMongoDBClients() {
		configuration.getDatabases().getMongodb().getMap().entrySet().forEach(e -> newClientToMap(e.getKey()));
	}

	public MongoClient electClient() {
		switch (mode) {
		case VERTICAL:
			return getClientInternal(
					Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()));
		case TENANT:
			return getClientInternal(MultitenancyContextHolder.getTenantName());
		case DEFAULT:
		default:
			return defaultClient;
		}
	}

	private void newClientToMap(String key) {
		final MongoClient client = configureMongoDbClient(configuration.getDatabases().getMongodb().getMap().get(key));
		clients.put(key, client);
	}

	private MongoClient getClientInternal(String key) {
		if (clients.containsKey(key)) {
			return clients.get(key);
		} else {
			return clients.get(DEFAULT);
		}
	}

	@Bean("mongo")
	@Primary
	public MongoClient defaultClient() {
		defaultClient = configureMongoDbClient(parseServers(defaultServers));
		return defaultClient;
	}

	private void loadCentralMongoConfig() {
		final Map<String, Object> databaseConfig = resourcesService.getGlobalConfiguration().getEnv().getDatabase();
		defaultServers = (String) databaseConfig.get("mongodb-servers");
		socketTimeout = ((Integer) databaseConfig.get("mongodb-socket-timeout")).intValue();
		connectTimeout = ((Integer) databaseConfig.get("mongodb-connect-timeout")).intValue();
		maxWaitTime = ((Integer) databaseConfig.get("mongodb-max-wait-time")).intValue();
		poolSize = ((Integer) databaseConfig.get("mongodb-pool-size")).intValue();
		readFromSecondaries = ((Boolean) databaseConfig.get("mongodb-read-from-secondaries")).booleanValue();
		try {
			readPreference = (String) databaseConfig.get("mongodb-read-preference");
		} catch (final Exception e) {
			log.warn("No read preference found on mongo configuration");
		}
		writeConcern = (String) databaseConfig.get("mongodb-write-concern");
		sslEnabled = ((Boolean) databaseConfig.get("mongodb-ssl-enabled")).booleanValue();
		defaultClient();
	}

	private List<ServerAddress> parseServers(String servers) throws DBPersistenceException {
		log.info("Parsing MongoDB servers property...");
		final List<ServerAddress> serverAddresses = new ArrayList<>();
		for (final String s : servers.split(",")) {
			final String[] splittedServerAddr = s.split(":");
			if (splittedServerAddr.length != 2) {
				final String errorMessage = String.format(
						"The MongoDB server address %s is malformed. The hostname and the port are required.", s);
				log.error(errorMessage);
				throw new DBPersistenceException(errorMessage);
			} else {
				log.info("Registering MongoDB server {}.", s);
			}
			final String host = splittedServerAddr[0].trim();
			final int port = Integer.valueOf(splittedServerAddr[1].trim());
			serverAddresses.add(new ServerAddress(host, port));
		}
		return serverAddresses;
	}

	private MongoClient configureMongoDbClient(String URI) {
		return MongoClients.create(URI);
	}

	private MongoClient configureMongoDbClient(List<ServerAddress> serverAddresses) {
		log.info("Configuring MongoDB client...");

		
		Builder clientBuilder= MongoClientSettings.builder()
	            .applyToClusterSettings(builder -> {
	            	builder.hosts(serverAddresses);
	            }).applyToSocketSettings(socket -> {
	            	socket.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
	            	socket.readTimeout(maxWaitTime, TimeUnit.MILLISECONDS);
	            }).applyToSslSettings(ssl -> {
	            	ssl.enabled(sslEnabled);
	            }).applyToConnectionPoolSettings(pool -> {
	            	pool.maxSize(poolSize);
	            });
		
		
		if (readFromSecondaries) {
			log.info("The MongoDB connector will forward the queries to the secondary nodes.");
			final ReadPreference preference = assignReadPreference();
			clientBuilder.readPreference(preference);
		}
		if (serverAddresses.size() == 1) {
			log.warn(
					"The MongoDB connector has been configured in single-server mode. The configured WriteConcern level ({}) will be ignored.",
					writeConcern);
			writeConcern = null;
		} else {
			log.info("The MongoDB connector has been configured in replica set mode. Using WriteConcern level {}.",
					writeConcern);
			final WriteConcern ackmode = WriteConcern.valueOf(writeConcern);
			clientBuilder.writeConcern(ackmode);
		}

		// MongoClientOptions options
		if (defaultCredentials.isEnableMongoDbAuthentication()) {
			final MongoCredential credential = MongoCredential.createCredential(defaultCredentials.getUsername(),
					defaultCredentials.getAuthenticationDatabase(), defaultCredentials.getPassword().toCharArray());

			clientBuilder.credential(credential);
		} 
		
		return MongoClients.create(clientBuilder.build());
	}

	private ReadPreference assignReadPreference() {
		ReadPreference preference = ReadPreference.secondary();
		if (readPreference != null) {
			if (readPreference.equalsIgnoreCase("primary")) {
				preference = ReadPreference.primary();
			}
			if (readPreference.equalsIgnoreCase("primarypreferred")) {
				preference = ReadPreference.primaryPreferred();
			}
			if (readPreference.equalsIgnoreCase("secondary")) {
				preference = ReadPreference.secondary();
			}
			if (readPreference.equalsIgnoreCase("secondarypreferred")) {
				preference = ReadPreference.secondaryPreferred();
			}
			if (readPreference.equalsIgnoreCase("nearest")) {
				preference = ReadPreference.nearest();
			}

		}
		return preference;
	}
}
