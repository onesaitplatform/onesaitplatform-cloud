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
package com.minsait.onesait.platform.business.services.predictor;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyAI;
import com.minsait.onesait.platform.config.repository.OntologyAIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.predictor.PredictorService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.mindsdb.service.MindsDBPredictorManagerFacade;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PredictorBusinessServiceImpl implements PredictorBusinessService {

	@Autowired
	private PredictorService predictorService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyAIRepository ontologyAIRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private MindsDBPredictorManagerFacade mindsDBPredictorManagerFacade;

	@Override
	public void create(PredictorDTO predictor) {
		final Ontology source = ontologyService.getOntologyByIdentification(predictor.getOntology());
		final Ontology ontology = createOntology(predictor, source);
		try {
			final String connName = MindsDBPredictorManagerFacade.getInstance(source).createPredictor(predictor);
			final OntologyAI ontologyAI = createOntologyAI(predictor, source, ontology, connName);
			ontologyRepository.save(ontology);
			ontologyAIRepository.save(ontologyAI);
		} catch (final Exception e) {
			throw e;
		}
	}

	@Override
	public void retrain(String predictorName) {
		// TO-DO check permissions
		final OntologyAI oAI = ontologyAIRepository.findByPredictor(predictorName);
		if (oAI != null) {
			final Ontology source = ontologyRepository.findByIdentification(oAI.getSourceEntity());
			MindsDBPredictorManagerFacade.getInstance(source).retrainPredictor(predictorName);
		}

	}

	private OntologyAI createOntologyAI(PredictorDTO predictor, Ontology source, Ontology ontology,
			String connectionName) {
		final OntologyAI oAI = new OntologyAI();
		oAI.setInputProperties(predictor.getInputFields());
		oAI.setTargetProperties(predictor.getTargetFields());
		oAI.setOntology(ontology);
		oAI.setSourceEntity(source.getIdentification());
		oAI.setOriginalDatasource(source.getRtdbDatasource());
		oAI.setPredictor(predictor.getName());
		oAI.setConnectionName(connectionName);
		return oAI;

	}

	private Ontology createOntology(PredictorDTO predictor, Ontology source) {

		if (source != null) {
			final Ontology oNew = new Ontology();
			oNew.setActive(true);
			oNew.setIdentification(predictor.getName());
			oNew.setRtdbDatasource(RtdbDatasource.AI_MINDS_DB);
			oNew.setUser(userService.getUser(predictor.getUser()));
			oNew.setJsonSchema(source.getJsonSchema());
			oNew.setDataModel(source.getDataModel());
			return oNew;
		} else {
			throw new RuntimeException("Source Ontology does not exist");
		}
	}

	@Override
	public List<PredictorDTO> predictors(String userId) {
		final List<PredictorDTO> predictors = predictorService.predictors(userId);
		predictors.forEach(p -> {
			final JsonNode pn = MindsDBPredictorManagerFacade.getDefaultInstance().getPredictor(p.getName());
			p.setAccuracy(pn.path("accuracy").isMissingNode() ? null : pn.get("accuracy").asDouble());
			p.setStatus(pn.path("status").isMissingNode() ? null : pn.get("status").asText());
			p.setWinningAlgorithm(getWinningAlgorithm(pn));
			p.setConnName(MindsDBPredictorManagerFacade.getDefaultInstance().getPredictorsConnName(p.getName()));
		});
		return predictors;
	}

	@Override
	public JsonNode predictorInfo(String predictor) {
		return MindsDBPredictorManagerFacade.getDefaultInstance().getPredictor(predictor);
	}

	@Override
	public String code(String predictor) {
		final JsonNode pInfo = predictorInfo(predictor);
		if (pInfo.path("code").isMissingNode()) {
			log.warn("No code available on predictor {}", predictor);
			return null;
		} else {
			return pInfo.path("code").asText();
		}
	}

	private String getWinningAlgorithm(JsonNode predictorNode) {
		String winningAlgorithm = null;
		if (predictorNode.has("submodel_data")) {
			for (final JsonNode sd : predictorNode.path("submodel_data")) {
				if (sd.path("is_best").asBoolean()) {
					winningAlgorithm = sd.path("name").asText();
				}
			}
		}
		return winningAlgorithm;

	}

	@Override
	@Transactional
	public void deletePredictor(String predictorName, String connName) {
		MindsDBPredictorManagerFacade.getDefaultInstance().removePredictor(predictorName, connName);
		ontologyAIRepository.deleteByPredictor(predictorName);
		ontologyRepository.deleteByIdentification(predictorName);

	}

}
