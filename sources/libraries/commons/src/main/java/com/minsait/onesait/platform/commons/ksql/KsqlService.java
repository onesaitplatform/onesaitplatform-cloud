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
package com.minsait.onesait.platform.commons.ksql;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import io.confluent.ksql.api.client.QueryInfo;
import io.confluent.ksql.rest.client.RestResponse;
import io.confluent.ksql.rest.entity.KsqlEntity;
import io.confluent.ksql.rest.entity.KsqlEntityList;
import io.confluent.ksql.rest.entity.KsqlErrorMessage;
import io.confluent.ksql.rest.entity.Queries;
import io.confluent.ksql.rest.entity.RunningQuery;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.kafka.ksql", name = "enable", havingValue = "true")
@Slf4j
@Service
public class KsqlService {

	private static final String KSQL_ERROR_CODE_CAUSE = "Ksql error code = {}. Cause = {}.";

	@Value("${onesaitplatform.ksql.server.url:http://localhost:8088}")
	private String ksqlServerUrl;
	@Value("${onesaitplatform.ksql.server.hostname:localhost}")
	private String ksqlHost;
	@Value("${onesaitplatform.ksql.server.port:8088}")
	private int ksqlHostPort;
	private Client ksqlClient;


	@PostConstruct
	public void init() {
		log.info("Starting KSQL Client...");
		ClientOptions options = ClientOptions.create()
		        .setHost(ksqlHost)
		        .setPort(ksqlHostPort);
		    ksqlClient = Client.create(options);
		log.info("KSQL Client started !");
	}
	
	@PreDestroy
	public void end() {
		ksqlClient.close();
	}

	public void executeKsqlRequest(String request) throws KsqlExecutionException {
		try {
			ksqlClient.executeStatement(request).get();
		} catch ( Exception e ) {
			log.error(KSQL_ERROR_CODE_CAUSE, e.getMessage(), e.getCause());
			throw new KsqlExecutionException(e.getMessage());
		}
		
/*
		
		
		final RestResponse<KsqlEntityList> response = ksqlClient.makeKsqlRequest(request);
		if (response.get() instanceof KsqlErrorMessage) {
			final KsqlErrorMessage error = (KsqlErrorMessage) response.get();
			log.error(KSQL_ERROR_CODE_CAUSE, error.getErrorCode(), error.getMessage());
			throw new KsqlExecutionException(error.getMessage());
		}
		final KsqlEntityList list = response.getResponse();
		if (list.get(0) instanceof CommandStatusEntity) {
			final CommandStatusEntity status = (CommandStatusEntity) list.get(0);
			if (status.getCommandStatus().getStatus() == CommandStatus.Status.ERROR) {

				log.error("Ksql error code = {}. ", status.getCommandStatus().getMessage());
				throw new KsqlExecutionException(status.getCommandStatus().getMessage());
			}
		}*/
	}

	public void deleteQueriesFromSink(String sinkIdentification) throws KsqlExecutionException {
		try {
			List<QueryInfo> queries = ksqlClient.listQueries().get();
			for (QueryInfo query : queries) {
				processDeleteQueriesFromConcreteSink(query, sinkIdentification);
			}
		} catch(Exception e) {
			//TODO: CATCH
			log.error("Unable to retrieve queries from KSQL Server.");
			log.error(KSQL_ERROR_CODE_CAUSE, e.getMessage(), e.getCause());
			throw new KsqlExecutionException(e.getMessage());
		}
		
		/*
		final RestResponse<KsqlEntityList> response = ksqlClient.makeKsqlRequest("show queries;");
		if (response.get() instanceof KsqlErrorMessage) {
			final KsqlErrorMessage error = (KsqlErrorMessage) response.get();
			log.error("Unable to retrieve queries from KSQL Server.");
			log.error(KSQL_ERROR_CODE_CAUSE, error.getErrorCode(), error.getMessage());
			throw new KsqlExecutionException(error.getMessage());
		}
		try {
			final KsqlEntityList queriesResponse = response.getResponse();
			for (final KsqlEntity queries : queriesResponse) {
				for (final RunningQuery query : ((Queries) queries).getQueries()) {
					processDeleteQueriesFromConcreteSink(query, sinkIdentification);
				}
			}
		} catch (final Exception e) {
			log.error("Error while deleting queries for sink = {}.", sinkIdentification, e);
			throw new KsqlExecutionException(e.getMessage());
		}*/
	}
	private void processDeleteQueriesFromConcreteSink(QueryInfo query, String sinkIdentification) throws KsqlExecutionException {
		if(query.getSink().isPresent() && query.getSink().get().equals(sinkIdentification)) {
			try {
				ksqlClient.executeStatement("TERMINATE " + query.getId() + ";").get();
				log.info("Ksql query = {} deleted.", query.getId());
			} catch (Exception e) {
				log.error("Unable to DELETE query= {} from KSQL Server.", query.getId());
				log.error(KSQL_ERROR_CODE_CAUSE, e.getMessage(), e.getCause());
				throw new KsqlExecutionException(e.getMessage());
			}
		}
	}

	/*private void processDeleteQueriesFromConcreteSink(RunningQuery query, String sinkIdentification)
			throws KsqlExecutionException {
		for (final String sink : query.getSinks()) {
			if (sink.equalsIgnoreCase(sinkIdentification)) {
				// Delete Query
				final RestResponse<KsqlEntityList> response = ksqlClient
						.makeKsqlRequest("terminate " + query.getId().getId() + ";");
				if (response.get() instanceof KsqlErrorMessage) {
					final KsqlErrorMessage error = (KsqlErrorMessage) response.get();
					log.error("Unable to DELETE query= {} from KSQL Server.", query.getId().getId());
					log.error(KSQL_ERROR_CODE_CAUSE, error.getErrorCode(), error.getMessage());
					throw new KsqlExecutionException(error.getMessage());
				}
				log.info("Ksql query = {} deleted.", query.getId());
			}
		}
	}*/

	public void deleteQueriesByStatement(String statement) throws KsqlExecutionException {

		try {
			List<QueryInfo> queries = ksqlClient.listQueries().get();
			for (QueryInfo query : queries) {
				if (statement.equalsIgnoreCase(query.getSql())) {
					try {
						ksqlClient.executeStatement("TERMINATE " + query.getId() + ";").get();
						log.info("Ksql query = {} deleted.", query.getId());
					} catch (Exception e) {
						log.error("Unable to DELETE query= {} from KSQL Server.", query.getId());
						log.error(KSQL_ERROR_CODE_CAUSE, e.getMessage(), e.getCause());
						throw new KsqlExecutionException(e.getMessage());
					}
				}
			}
		} catch(Exception e) {
			log.error("Unable to retrieve queries from KSQL Server.");
			log.error(KSQL_ERROR_CODE_CAUSE, e.getMessage(), e.getCause());
			throw new KsqlExecutionException(e.getMessage());
		}
		
		/*
		RestResponse<KsqlEntityList> response = ksqlClient.makeKsqlRequest("show queries;");
		if (response.get() instanceof KsqlErrorMessage) {
			final KsqlErrorMessage error = (KsqlErrorMessage) response.get();
			log.error("Unable to retrieve queries from KSQL Server.");
			log.error(KSQL_ERROR_CODE_CAUSE, error.getErrorCode(), error.getMessage());
			throw new KsqlExecutionException(error.getMessage());
		}
		try {
			final KsqlEntityList queriesResponse = response.getResponse();
			for (final KsqlEntity queries : queriesResponse) {
				for (final RunningQuery query : ((Queries) queries).getQueries()) {
					if (statement.equalsIgnoreCase(query.getQueryString())) {
						// Delete Query
						response = ksqlClient.makeKsqlRequest("terminate " + query.getId().getId() + ";");
						if (response.get() instanceof KsqlErrorMessage) {
							final KsqlErrorMessage error = (KsqlErrorMessage) response.get();
							log.error("Unable to DELETE query= {} from KSQL Server.", query.getId().getId());
							log.error(KSQL_ERROR_CODE_CAUSE, error.getErrorCode(), error.getMessage());
							throw new KsqlExecutionException(error.getMessage());
						}
						log.info("Ksql query = {} deleted.", query.getId());
					}
				}
			}
		} catch (final Exception e) {
			log.error("Error while deleting queries from statement = {}.", statement, e);
			throw new KsqlExecutionException(e.getMessage());
		}*/
	}
}
