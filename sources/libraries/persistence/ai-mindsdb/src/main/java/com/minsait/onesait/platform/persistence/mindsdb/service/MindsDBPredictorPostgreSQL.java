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

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyAI;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.repository.OntologyAIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.persistence.mindsdb.http.MindsDBHTTPClient;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorDatasource;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorDatasource.Source;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBPredictorPayload;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBDatasourcesManager;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBSQLGenericUtil;
import com.minsait.onesait.platform.persistence.mindsdb.util.MindsDBSQLGenericUtil.SERVER_PART;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MindsDBPredictorPostgreSQL extends MindsDBPredictorManager {

	public static final String PREDICTORS_COLLECTION = "predictors";

	@Autowired
	private MindsDBDatasourcesManager mindsDBDatasourcesManager;
	@Autowired
	private MindsDBHTTPClient mindsDBHTTPClient;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;
	@Autowired
	private OntologyAIRepository ontologyAIRepository;

	@PostConstruct
	public void setUpConnections() {
		final List<OntologyAI> predictors = ontologyAIRepository.findAll();
		predictors.forEach(p -> {
			final Ontology o = ontologyRepository.findByIdentification(p.getSourceEntity());
			final OntologyVirtual ov = ontologyVirtualRepository.findByOntologyId(o);
			if (ov != null && ov.getDatasourceId().getConnectionString().contains("postgresql")) {
				createDatasource(ov.getDatasourceId());
			}
		});
	}

	@Override
	public String createPredictor(PredictorDTO predictor) {
		final Ontology o = ontologyRepository.findByIdentification(predictor.getOntology());
		final OntologyVirtual ov = ontologyVirtualRepository.findByOntologyId(o);
		createDatasource(ov.getDatasourceId());
		final MindsDBPredictorDatasource ds = new MindsDBPredictorDatasource();
		ds.setName(predictor.getName());
		ds.setSourceType("psql_" + ov.getDatasourceId().getId().substring(0, 7));
		final Source s = new Source();
		s.setDatabase(ov.getDatasourceDatabase());
		s.setQuery("SELECT " + predictor.getInputFields() + " FROM " + ov.getDatasourceSchema() + "."
				+ ov.getDatasourceTableName());
		ds.setSource(s);
		mindsDBHTTPClient.createMindsDBDatasource(ds);
		mindsDBHTTPClient.createMindsDBPredictor(MindsDBPredictorPayload.builder().datasourceName(ds.getName())
				.retrain(true).toPredict(predictor.getTargetFields()).build());
		return ds.getSourceType();
	}

	@Override
	public boolean supports(Ontology ontology) {
		if (ontology != null && ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
			final OntologyVirtual ov = ontologyVirtualRepository.findByOntologyId(ontology);
			return ov.getDatasourceId().getConnectionString().contains("postgresql");
		}
		return false;
	}

	@Override
	public boolean supportsByConnName(String connection) {
		return connection != null && connection.startsWith("psql");
	}

	@Override
	public void createDatasource(OntologyVirtualDatasource ovds) {
		final String host = MindsDBSQLGenericUtil.getSQL(ovds.getConnectionString(), SERVER_PART.HOST);
		final String port = MindsDBSQLGenericUtil.getSQL(ovds.getConnectionString(), SERVER_PART.PORT);
		mindsDBDatasourcesManager.createDatasource("psql_" + ovds.getId().substring(0, 7), "postgres", host, port,
				ovds.getUserId(), JasyptConfig.getEncryptor().decrypt(ovds.getCredentials()));
	}
}