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

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.persistence.mindsdb.http.MindsDBHTTPClient;
import com.minsait.onesait.platform.persistence.mindsdb.model.MindsDBQuery;

public abstract class MindsDBPredictorManager {

	@Autowired
	private MindsDBHTTPClient mindsDBHTTPClient;

	public static final String RETRAIN_QUERY="RETRAIN mindsdb.";
	public static final String DROP_CONN="DROP datasource ";

	public abstract String createPredictor(PredictorDTO predictor);

	public void retrainPredictor(String predictorName) {
		mindsDBHTTPClient.sendQuery(new MindsDBQuery(RETRAIN_QUERY + predictorName));
	}

	public void removePredictor(String predictor, String connName) {
		mindsDBHTTPClient.deleteMindsDBPredictor(predictor);
		mindsDBHTTPClient.deleteMindsDBDatasource(predictor);
		mindsDBHTTPClient.sendQuery(new MindsDBQuery(DROP_CONN + connName));
	}

	public String getPredictorsConnName(String name) {
		final JsonNode ds = mindsDBHTTPClient.getDatasource(name);
		if(ds != null && !ds.path("source_type").isMissingNode()) {
			return ds.get("source_type").asText();
		}else {
			return null;
		}
	}

	public JsonNode getPredictor(String name) {
		return mindsDBHTTPClient.getPredictor(name);
	}

	public abstract boolean supports(Ontology ontology);

	public abstract boolean supportsByConnName(String connection);

	public abstract void createDatasource(OntologyVirtualDatasource ovds);


}
