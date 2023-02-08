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
package com.minsait.onesait.platform.config.services.predictor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.OntologyAI;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyAIRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class PredictorServiceImpl implements PredictorService {

	@Autowired
	private OntologyAIRepository ontologyAIRepository;
	@Autowired
	private UserService userService;

	@Override
	public List<PredictorDTO> predictors(String userId) {
		final User user = userService.getUser(userId);
		List<OntologyAI> ontologies = null;
		if (userService.isUserAdministrator(user)) {
			ontologies = ontologyAIRepository.findAll();
		} else {
			ontologies = ontologyAIRepository.findByUser(user);
		}
		return ontologies.stream()
				.map(oai -> PredictorDTO.builder().ontology(oai.getSourceEntity())
						.user(oai.getOntology().getUser().getUserId()).targetFields(oai.getTargetProperties())
						.inputFields(oai.getInputProperties()).name(oai.getPredictor()).aiTable(oai.getOntology().getIdentification()).build())
				.collect(Collectors.toList());
	}

}
