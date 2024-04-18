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
package com.minsait.onesait.platform.persistence.mindsdb.service;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.mindsdb.http.MindsDBHTTPClient;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorDatasource;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorDatasource.Source;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorPayload;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBDatasourcesManager;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBSQLGenericUtil;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBSQLGenericUtil.SERVER_PART;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MindsDBPredictorMongoDB extends MindsDBPredictorManager {

	public static final String MONGODB_CONN_NAME = "mongo_onesaitplatform";
	public static final String PREDICTORS_COLLECTION = "predictors";


	@Autowired
	private MindsDBDatasourcesManager mindsDBDatasourcesManager;
	@Autowired
	private MindsDBHTTPClient mindsDBHTTPClient;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private Environment env;


	@PostConstruct
	public void setUpConnections() {
		createDefaultDS();
	}

	@Override
	public String createPredictor(PredictorDTO predictor) {
		createDefaultDS();
		final MindsDBPredictorDatasource ds = new MindsDBPredictorDatasource();
		ds.setName(predictor.getName());
		ds.setSourceType(MONGODB_CONN_NAME);
		final Source s = new Source();
		s.setCollection(predictor.getOntology());
		s.setDatabase(Tenant2SchemaMapper.getRtdbSchema());
		s.setFind(new HashMap<>());
		ds.setSource(s);
		mindsDBHTTPClient.createMindsDBDatasource(ds);
		mindsDBHTTPClient.createMindsDBPredictor(MindsDBPredictorPayload.builder().datasourceName(ds.getName())
				.retrain(true).toPredict(predictor.getTargetFields()).build());
		return MONGODB_CONN_NAME;
	}

	@Override
	public boolean supports(Ontology ontology) {
		// DEFAULT PredictorManager
		if (ontology == null) {
			return true;
		}
		return ontology.getRtdbDatasource().equals(RtdbDatasource.MONGO);
	}

	@Override
	public boolean supportsByConnName(String connection) {
		return connection == null || connection.startsWith("mongo");
	}

	@Override
	public void createDatasource(OntologyVirtualDatasource ovds) {
		// NO-OP
	}


	private void createDefaultDS() {
		final String mongodbHost = MindsDBSQLGenericUtil.getMongoDB(
				(String) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("mongodb-servers"),
				SERVER_PART.HOST);
		final String mongodbPort = MindsDBSQLGenericUtil.getMongoDB(
				(String) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("mongodb-servers"),
				SERVER_PART.PORT);
		final String user = env.getProperty("REALTIMEDBUSER") == null ? "" : env.getProperty("REALTIMEDBUSER");
		final String pass = env.getProperty("REALTIMEDBPASS") == null ? "" : env.getProperty("REALTIMEDBPASS");
		mindsDBDatasourcesManager.createDatasource(MONGODB_CONN_NAME, "mongodb", mongodbHost, mongodbPort, user, pass);
	}
}
