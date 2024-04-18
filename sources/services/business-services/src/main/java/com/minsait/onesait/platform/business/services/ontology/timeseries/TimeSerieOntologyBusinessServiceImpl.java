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
package com.minsait.onesait.platform.business.services.ontology.timeseries;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationServiceImpl;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.TimescaleContinuousAggregateRequest;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
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
	@Autowired
	private ManageDBRepositoryFactory manageDBPersistence;

	@Override
	public Ontology createOntology(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, OntologyConfiguration config,
			boolean parseProperties, boolean parseWindow)
			throws TimeSerieOntologyBusinessServiceException, JsonProcessingException {
		Ontology createdOnt = null;
		try {
			// create ontology
			createdOnt = ontologyTimeSeriesService.createOntologyTimeSeries(ontologyTimeSeriesDTO, config, true, true);

			if (createdOnt.getRtdbDatasource().equals(Ontology.RtdbDatasource.TIMESCALE)) {
				// create table
				timescaleManageRepository.createTable4Ontology(createdOnt.getIdentification(), null, null);
			} else {
				// create index for timeserie
				// create index
				StringBuilder index = new StringBuilder().append("db.")
						.append(ontologyTimeSeriesDTO.getIdentification()).append(".createIndex({");
				index.append("\"TimeSerie.timestamp\":-1");
				for (String tag : ontologyTimeSeriesDTO.getTags()) {
					for (String tagElement : tag.split("-")) {
						final String[] tagElementParsed = tagElement.split(":");
						if (tagElement != null && !tagElement.isEmpty() && tagElementParsed[0].trim().equals("name")) {
							index.append(",\"TimeSerie.").append(tagElementParsed[1].trim()).append("\":-1");
						}
					}
				}
				index.append(",\"TimeSerie.propertyName\":-1");
				index.append(",\"TimeSerie.windowType\":-1");
				index.append(",\"TimeSerie.windowFrecuency\":-1");
				index.append(",\"TimeSerie.windowFrecuencyUnit\":-1");
				index.append("},{\"unique\":true,\"name\":\"").append(ontologyTimeSeriesDTO.getIdentification())
						.append("_UNIQ_IDX\"})");
				manageDBPersistence.getInstance(createdOnt.getRtdbDatasource()).createIndex(index.toString());
			}
			return createdOnt;
		} catch (Exception e) {
			if (createdOnt != null) {
				ontologyBusinessService.deleteOntology(createdOnt.getId(), createdOnt.getUser().getUserId());
			}
			throw new TimeSerieOntologyBusinessServiceException(e.getMessage());
		}

	}

	@Override
	public void updateOntology(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config, boolean hasDocuments) throws TimeSerieOntologyBusinessServiceException {

		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyTimeSeriesDTO.getIdentification(),
				sessionUserId);
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

			try {
				updateCompressionPolicy(ontologyTimeSeriesDTO, ontologyTimeserie);
				updateRetentionPolicy(ontologyTimeSeriesDTO, ontologyTimeserie);
			} catch (Exception e) {
				throw new TimeSerieOntologyBusinessServiceException(e.getMessage());
			}
		}
		ontologyTimeSeriesService.updateOntologyTimeSeries(ontologyTimeSeriesDTO, sessionUserId, config);

		kafkaAuthorizationService.checkOntologyAclAfterUpdate(ontology);
	}

	private void updateCompressionPolicy(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO,
			final OntologyTimeSeries ontologyTimeserie) {
		final OntologyTimeseriesTimescaleProperties ontologyTimeseriesTimescaleProperties = ontologyTimeSeriesDTO
				.getTimescaleProperties();
		// if compression policy changes to false: deactivate
		if (!ontologyTimeseriesTimescaleProperties.isCompressionActive()
				&& ontologyTimeserie.getTimeSeriesTimescaleProperties().isCompressionActive()) {
			timescaleManageRepository.deactivateCompressionPolicy(ontologyTimeSeriesDTO.getIdentification());
		}
		// if compression policy changes to true: activate
		else if (ontologyTimeseriesTimescaleProperties.isCompressionActive()
				&& !ontologyTimeserie.getTimeSeriesTimescaleProperties().isCompressionActive()) {
			timescaleManageRepository.activateCompressionPolicy(ontologyTimeSeriesDTO.getIdentification(),
					ontologyTimeseriesTimescaleProperties);
		}
		// if compression policy changes frequency or query: deactivate and activate
		// with new
		// policy
		else if (ontologyTimeseriesTimescaleProperties.isCompressionActive()
				&& ontologyTimeserie.getTimeSeriesTimescaleProperties().isCompressionActive()
				&& (!ontologyTimeseriesTimescaleProperties.getCompressionUnit()
						.equals(ontologyTimeserie.getTimeSeriesTimescaleProperties().getCompressionUnit())
						|| !ontologyTimeseriesTimescaleProperties.getCompressionAfter()
								.equals(ontologyTimeserie.getTimeSeriesTimescaleProperties().getCompressionAfter())
						|| !ontologyTimeseriesTimescaleProperties.getCompressionQuery()
								.equals(ontologyTimeserie.getTimeSeriesTimescaleProperties().getCompressionQuery()))) {
			timescaleManageRepository.deactivateCompressionPolicy(ontologyTimeSeriesDTO.getIdentification());
			timescaleManageRepository.activateCompressionPolicy(ontologyTimeSeriesDTO.getIdentification(),
					ontologyTimeseriesTimescaleProperties);
		}
	}

	private void updateRetentionPolicy(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO,
			final OntologyTimeSeries ontologyTimeserie) {
		final OntologyTimeseriesTimescaleProperties ontologyTimeseriesTimescaleProperties = ontologyTimeSeriesDTO
				.getTimescaleProperties();
		// if retention policy changes to false: deactivate
		if (!ontologyTimeseriesTimescaleProperties.isRetentionActive()
				&& ontologyTimeserie.getTimeSeriesTimescaleProperties().isRetentionActive()) {
			timescaleManageRepository.deactivateRetentionPolicy(ontologyTimeSeriesDTO.getIdentification());
		}
		// if retention policy changes to true: activate
		else if (ontologyTimeseriesTimescaleProperties.isRetentionActive()
				&& !ontologyTimeserie.getTimeSeriesTimescaleProperties().isRetentionActive()) {
			timescaleManageRepository.activateRetentionPolicy(ontologyTimeSeriesDTO.getIdentification(),
					ontologyTimeseriesTimescaleProperties);
		}
		// if retention policy changes frequency: deactivate and activate with new
		// policy
		else if (ontologyTimeseriesTimescaleProperties.isRetentionActive()
				&& ontologyTimeserie.getTimeSeriesTimescaleProperties().isRetentionActive()
				&& (!ontologyTimeseriesTimescaleProperties.getRetentionUnit()
						.equals(ontologyTimeserie.getTimeSeriesTimescaleProperties().getRetentionUnit())
						|| !ontologyTimeseriesTimescaleProperties.getRetentionBefore()
								.equals(ontologyTimeserie.getTimeSeriesTimescaleProperties().getRetentionBefore()))) {
			timescaleManageRepository.deactivateRetentionPolicy(ontologyTimeSeriesDTO.getIdentification());
			timescaleManageRepository.activateRetentionPolicy(ontologyTimeSeriesDTO.getIdentification(),
					ontologyTimeseriesTimescaleProperties);
		}
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
