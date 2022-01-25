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
package com.minsait.onesait.platform.config.services.ontology;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;

public interface OntologyTimeSeriesService {

	public Ontology createOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontology, OntologyConfiguration config,
			boolean parseProperties, boolean parseWindow)
			throws OntologyServiceException, OntologyDataJsonProblemException;

	public ResponseEntity<?> updateOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config) throws OntologyServiceException, OntologyDataJsonProblemException;

	public OntologyTimeSeriesServiceDTO generateOntologyTimeSeriesDTO(Ontology ontology);

	public OntologyTimeSeries getOntologyByOntology(Ontology ontology);

	public List<OntologyTimeSeriesProperty> getTimeSeriesPropertiesByOntologyTimeSeries(
			OntologyTimeSeries ontologyTimeSeries);

	public List<OntologyTimeSeriesWindow> getTimeSeriesWindowByOntologyTimeSeries(OntologyTimeSeries ontologyTimeSeries);
	
	public void cloneOntologyTimeSeries(String identification, Ontology ontology, User user, OntologyConfiguration config)
			throws OntologyServiceException, OntologyDataJsonProblemException;

	ResponseEntity<?> updateOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config, boolean cleanProperties, boolean cleanWindow);

}
