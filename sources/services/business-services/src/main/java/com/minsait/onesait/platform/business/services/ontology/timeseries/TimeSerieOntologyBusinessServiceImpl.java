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
package com.minsait.onesait.platform.business.services.ontology.timeseries;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationServiceImpl;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.TimescaleContinuousAggregateRequest;
import com.minsait.onesait.platform.persistence.timescaledb.TimescaleDBManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TimeSerieOntologyBusinessServiceImpl implements TimeSeriesOntologyBusinessService {

	@Autowired
	private OntologyTimeSeriesService ontologyTimeSeriesService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private KafkaAuthorizationServiceImpl kafkaAuthorizationService;
	@Autowired
	private TimescaleDBManageDBRepository timescaleManageRepository;

	@Override
	public Ontology createOntology(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, OntologyConfiguration config,
			boolean parseProperties, boolean parseWindow) throws TimeSerieOntologyBusinessServiceException {
		Ontology createdOnt = null;
		try {
			// create ontology
			createdOnt = ontologyTimeSeriesService.createOntologyTimeSeries(ontologyTimeSeriesDTO, config, true, true);

			if (createdOnt.getRtdbDatasource().equals(Ontology.RtdbDatasource.TIMESCALE)) {
				// create table
				timescaleManageRepository.createTable4Ontology(createdOnt.getIdentification(), null, null);
			}
			return createdOnt;
		} catch (Exception e) {
			ontologyBusinessService.deleteOntology(createdOnt.getId(), createdOnt.getUser().getUserId());
			throw new TimeSerieOntologyBusinessServiceException(e.getMessage());
		}

	}

	@Override
	public void updateOntology(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config, boolean hasDocuments) throws TimeSerieOntologyBusinessServiceException {

		final Ontology ontology = ontologyService
				.getOntologyByIdentification(ontologyTimeSeriesDTO.getIdentification(), sessionUserId);
		if (ontologyTimeSeriesDTO.getRtdbDatasource().equals(RtdbDatasource.TIMESCALE.toString())) {
			// If TImescaleDB we need to alter the table first
			final OntologyTimeSeries ontologyTimeserie = ontologyTimeSeriesService.getOntologyByOntology(ontology);
			final Map<String, String> newFields = new HashMap<>();
			// Compare fields definitions

			for (int i = 0; i < ontologyTimeSeriesDTO.getFieldnames().length; i++) {
				String currentName = ontologyTimeSeriesDTO.getFieldnames()[i];
				long coincidences = ontologyTimeserie.getTimeSeriesProperties().stream()
						.filter(prop -> prop.getPropertyType() == PropertyType.SERIE_FIELD
								&& prop.getPropertyName().equalsIgnoreCase(currentName))
						.count();

				if (coincidences == 0) {
					newFields.put(ontologyTimeSeriesDTO.getFieldnames()[i], ontologyTimeSeriesDTO.getFieldtypes()[i]);
				}

			}

			timescaleManageRepository.updateTable4Ontology(ontologyTimeSeriesDTO.getIdentification(),
					ontologyTimeSeriesDTO.getJsonSchema(), newFields);
		}
		ontologyTimeSeriesService.updateOntologyTimeSeries(ontologyTimeSeriesDTO, sessionUserId, config);
 		
		kafkaAuthorizationService.checkOntologyAclAfterUpdate(ontology);
	}

	@Override
	public void deleteOntology(String id, String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional
	public void createContinuousAggregate(String ontologyIdentification, String sessionUser,
			TimescaleContinuousAggregateRequest request) throws TimeSerieOntologyBusinessServiceException {

		Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification, sessionUser);
		if (ontology != null) {

			OntologyTimeSeries timeSeriesOntology = ontologyTimeSeriesService.getOntologyByOntology(ontology);
			OntologyTimeseriesTimescaleAggregates aggregate = ontologyTimeSeriesService
					.createContinuousAggregate(timeSeriesOntology, sessionUser, request);
			timescaleManageRepository.createContinuousAggregate(aggregate);

		} else {
			log.error("Error while creating TimescaleDB aggregate. Ontology {} does not exist.",
					ontologyIdentification);
			throw new TimeSerieOntologyBusinessServiceException("Ontology does not exist.");
		}

	}

	@Override
	@Transactional
	public void deleteContinuousAggregate(String ontologyIdentification, String sessionUser, String name)
			throws TimeSerieOntologyBusinessServiceException {
		Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification, sessionUser);
		if (ontology != null) {
			OntologyTimeSeries timeSeriesOntology = ontologyTimeSeriesService.getOntologyByOntology(ontology);
			ontologyTimeSeriesService.deleteContinuousAggregate(timeSeriesOntology, sessionUser, name);
			timescaleManageRepository.deleteContinuousAggregate(timeSeriesOntology, name);
		} else {
			log.error("Error while deleting TimescaleDB aggregate. Ontology {} does not exist.",
					ontologyIdentification);
			throw new TimeSerieOntologyBusinessServiceException("Ontology does not exist.");
		}

	}

}
