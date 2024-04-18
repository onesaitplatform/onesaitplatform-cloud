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
package com.minsait.onesait.platform.config.services.ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesPropertyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesWindowRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeseriesTimescaleAggregatesRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeseriesTimescalePropetiesRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.TimescaleContinuousAggregateRequest;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class OntologyTimeSeriesServiceImpl implements OntologyTimeSeriesService {

	private static final String STATS_STR = "_stats";
	private static final String USER_NOT_AUTHORIZED = "The user is not authorized";
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserService userService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired
	private OntologyTimeSeriesPropertyRepository ontologyTimeSeriesPropertyRepository;
	@Autowired
	private OntologyTimeSeriesWindowRepository ontologyTimeSeriesWindowRepository;
	@Autowired
	private OntologyTimeseriesTimescalePropetiesRepository ontologyTimescalePropetiesRepository;
	@Autowired
	private OntologyTimeseriesTimescaleAggregatesRepository ontologyTimescaleAggregatesRepository;

	@Override
	public OntologyTimeSeries getOntologyByOntology(Ontology ontology) {
		return ontologyTimeSeriesRepository.findByOntologyIdentificaton(ontology.getIdentification());
	}

	@Override
	public List<OntologyTimeSeriesProperty> getTimeSeriesPropertiesByOntologyTimeSeries(
			OntologyTimeSeries ontologyTimeSeries) {
		final Set<OntologyTimeSeriesProperty> properties = ontologyTimeSeriesPropertyRepository
				.findByOntologyTimeSeries(ontologyTimeSeries);
		return properties.stream().collect(Collectors.toList());
	}

	@Override
	public List<OntologyTimeSeriesWindow> getTimeSeriesWindowByOntologyTimeSeries(
			OntologyTimeSeries ontologyTimeSeries) {
		final Set<OntologyTimeSeriesWindow> windows = ontologyTimeSeriesWindowRepository
				.findByOntologyTimeSeries(ontologyTimeSeries);
		return windows.stream().collect(Collectors.toList());
	}

	@Override
	@Transactional
	public Ontology createOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO,
			OntologyConfiguration config, boolean parseProperties, boolean parseWindow) {

		if (ontologyService.existsOntology(ontologyTimeSeriesDTO.getIdentification())) {
			throw new OntologyServiceException("Ontology already exists",
					OntologyServiceException.Error.EXISTING_ONTOLOGY);
		}

		final Ontology ontology = new Ontology();
		if (parseProperties) {
			ontologyTimeSeriesDTO.setTimeSeriesProperties();
		}
		if (parseWindow) {
			ontologyTimeSeriesDTO.setTimeSeriesWindow();
		}
		ontology.setJsonSchema(ontologyTimeSeriesDTO.getJsonSchema());
		ontology.setActive(ontologyTimeSeriesDTO.isActive());
		ontology.setPublic(ontologyTimeSeriesDTO.isPublic());
		ontology.setAllowsCypherFields(ontologyTimeSeriesDTO.isAllowsCypherFields());
		ontology.setDataModel(dataModelService.getDataModelByName("TimeSerie"));
		ontology.setDescription(ontologyTimeSeriesDTO.getDescription());
		ontology.setUser(ontologyTimeSeriesDTO.getUser());
		ontology.setMetainf(ontologyTimeSeriesDTO.getMetainf());
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(ontologyTimeSeriesDTO.getRtdbDatasource()));
		ontology.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
		ontology.setOntologyKPI(null);
		ontology.setIdentification(ontologyTimeSeriesDTO.getIdentification());
		ontology.setContextDataEnabled(ontologyTimeSeriesDTO.isContextDataEnabled());
		ontology.setSupportsJsonLd(ontologyTimeSeriesDTO.isSupportsJsonLd());
		ontology.setJsonLdContext(ontologyTimeSeriesDTO.getJsonLdContext());
		ontology.setEnableDataClass(ontologyTimeSeriesDTO.isEnableDataClass());
		
		ontologyService.createOntology(ontology, config);

		final OntologyTimeSeries oTS = new OntologyTimeSeries();
		oTS.setOntology(ontology);
		// oTS.setId(ontology.getId());
		ontologyTimeSeriesRepository.save(oTS);

		for (final OntologyTimeSeriesProperty oTSP : ontologyTimeSeriesDTO.getTimeSeriesProperties()) {
			oTSP.setOntologyTimeSeries(oTS);
			ontologyTimeSeriesPropertyRepository.save(oTSP);
			oTS.getTimeSeriesProperties().add(oTSP);
		}

		if (ontology.getRtdbDatasource() == RtdbDatasource.MONGO) {
			for (final OntologyTimeSeriesWindow oTSW : ontologyTimeSeriesDTO.getTimeSeriesWindow()) {
				oTSW.setOntologyTimeSeries(oTS);
				ontologyTimeSeriesWindowRepository.save(oTSW);
			}
		} else {
			// TImescaleDB:
			// Create ConfigDB TimescaleDB Properties
			final OntologyTimeseriesTimescaleProperties ontologyTimescaleProperties = ontologyTimeSeriesDTO
					.getTimescaleProperties();
			ontologyTimescaleProperties.setOntologyTimeSeries(oTS);
			ontologyTimescalePropetiesRepository.save(ontologyTimescaleProperties);
			oTS.setTimeSeriesTimescaleProperties(ontologyTimescaleProperties);
			ontologyTimeSeriesRepository.save(oTS);
			// TODO: Create TimescaleDB table + others
		}

		if (ontologyTimeSeriesDTO.isStats()) {
			final Ontology stats = new Ontology();
			stats.setJsonSchema("{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\""
					+ ontologyTimeSeriesDTO.getIdentification() + STATS_STR + "\",\"type\":\"object\",\"required\":[\""
					+ "Stats" + "\"],\"properties\":{\"" + ontologyTimeSeriesDTO.getIdentification() + STATS_STR
					+ "\":{\"type\":\"string\",\"$ref\":\"#/datos\"}},\"datos\":{\"description\":\"Properties for DataModel\",\"type\":\"object\",\"required\":[\"tag\",\"field\",\"lastValue\",\"windowType\",\"windowFreq\"],\"properties\":{\"tag\":{\"type\":\"array\", \"items\": [{\"type\":\"object\", \"properties\": {\"name\":{\"type\":\"string\"}, \"value\":{\"type\":\"object\"} }}], \"minItems\":1},\"field\":{\"type\":\"string\"},\"lastValue\":{\"type\":\"object\"},\"windowType\":{\"type\":\"string\"},\"windowFreq\":{\"type\":\"string\"}}},\"description\":\""
					+ "Stats for Ontology " + ontologyTimeSeriesDTO.getIdentification()
					+ "\",\"additionalProperties\":true}");
			stats.setActive(ontologyTimeSeriesDTO.isActive());
			stats.setPublic(ontologyTimeSeriesDTO.isPublic());
			stats.setAllowsCypherFields(ontologyTimeSeriesDTO.isAllowsCypherFields());
			stats.setDataModel(dataModelService.getDataModelByName("EmptyBase"));
			stats.setDescription("Stats for Ontology " + ontologyTimeSeriesDTO.getIdentification());
			stats.setUser(ontologyTimeSeriesDTO.getUser());
			stats.setMetainf("stats");
			stats.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(ontologyTimeSeriesDTO.getRtdbDatasource()));
			stats.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
			stats.setOntologyKPI(null);
			stats.setIdentification(ontologyTimeSeriesDTO.getIdentification() + STATS_STR);
	        stats.setSupportsJsonLd(ontologyTimeSeriesDTO.isSupportsJsonLd());
	        stats.setJsonLdContext(ontologyTimeSeriesDTO.getJsonLdContext());
	        stats.setEnableDataClass(ontologyTimeSeriesDTO.isEnableDataClass());

			ontologyService.createOntology(stats, config);
		}
		return ontology;
	}

	@Override
	public ResponseEntity<?> updateOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO,
			String sessionUserId, OntologyConfiguration config) {
		return updateOntologyTimeSeries(ontologyTimeSeriesDTO, sessionUserId, config, true, true);
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateOntologyTimeSeries(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO,
			String sessionUserId, OntologyConfiguration config, boolean cleanProperties, boolean cleanWindow) {
		final Map<String, String> response = new HashMap<>();

		final Ontology ontologyDb = ontologyRepository.findById(ontologyTimeSeriesDTO.getId()).orElse(null);
		final User sessionUser = userService.getUser(sessionUserId);

		if (cleanProperties)
			ontologyTimeSeriesDTO.setTimeSeriesProperties();
		if (cleanWindow)
			ontologyTimeSeriesDTO.setTimeSeriesWindow();

		if (ontologyDb != null) {
			ontologyDb.setJsonSchema(ontologyTimeSeriesDTO.getJsonSchema());
			ontologyDb.setActive(ontologyTimeSeriesDTO.isActive());
			ontologyDb.setPublic(ontologyTimeSeriesDTO.isPublic());
			ontologyDb.setAllowsCypherFields(ontologyTimeSeriesDTO.isAllowsCypherFields());
			ontologyDb.setDescription(ontologyTimeSeriesDTO.getDescription());
			ontologyDb.setMetainf(ontologyTimeSeriesDTO.getMetainf());
			ontologyDb.setContextDataEnabled(ontologyTimeSeriesDTO.isContextDataEnabled());
			ontologyDb.setAllowsCreateTopic(ontologyTimeSeriesDTO.isAllowsCreateTopic());
			ontologyDb.setAllowsCreateNotificationTopic(ontologyTimeSeriesDTO.isAllowsCreateNotificationTopic());
			ontologyDb.setSupportsJsonLd(ontologyTimeSeriesDTO.isSupportsJsonLd());
			ontologyDb.setJsonLdContext(ontologyTimeSeriesDTO.getJsonLdContext());
			ontologyDb.setEnableDataClass(ontologyTimeSeriesDTO.isEnableDataClass());
			if (ontologyService.hasUserPermisionForChangeOntology(sessionUser, ontologyDb)) {
				ontologyRepository.save(ontologyDb);

			} else {
				throw new OntologyServiceException(USER_NOT_AUTHORIZED);
			}
		} else
			throw new OntologyServiceException("Ontology does not exist");

		OntologyTimeSeries oTS = ontologyTimeSeriesRepository.findByOntology(ontologyDb).get(0);
		oTS.setTimeSeriesProperties(new HashSet<>());

		for (final OntologyTimeSeriesProperty oTSP : ontologyTimeSeriesDTO.getTimeSeriesProperties()) {
			oTSP.setOntologyTimeSeries(oTS);
			oTS.getTimeSeriesProperties().add(oTSP);
		}

		oTS.setTimeSeriesWindows(new HashSet<>());
		for (final OntologyTimeSeriesWindow oTSW : ontologyTimeSeriesDTO.getTimeSeriesWindow()) {
			oTSW.setOntologyTimeSeries(oTS);
			oTS.getTimeSeriesWindows().add(oTSW);
		}
		
		if (oTS.getTimeSeriesTimescaleProperties() != null && ontologyTimeSeriesDTO.getTimescaleProperties() != null) {
			oTS.getTimeSeriesTimescaleProperties().setCompressionActive(ontologyTimeSeriesDTO.getTimescaleProperties().isCompressionActive());
			oTS.getTimeSeriesTimescaleProperties().setCompressionUnit(ontologyTimeSeriesDTO.getTimescaleProperties().getCompressionUnit());
			oTS.getTimeSeriesTimescaleProperties().setCompressionAfter(ontologyTimeSeriesDTO.getTimescaleProperties().getCompressionAfter());
			oTS.getTimeSeriesTimescaleProperties().setCompressionQuery(ontologyTimeSeriesDTO.getTimescaleProperties().getCompressionQuery());
			oTS.getTimeSeriesTimescaleProperties().setRetentionActive(ontologyTimeSeriesDTO.getTimescaleProperties().isRetentionActive());
			oTS.getTimeSeriesTimescaleProperties().setRetentionUnit(ontologyTimeSeriesDTO.getTimescaleProperties().getRetentionUnit());
			oTS.getTimeSeriesTimescaleProperties().setRetentionBefore(ontologyTimeSeriesDTO.getTimescaleProperties().getRetentionBefore());		}
		
		ontologyTimeSeriesRepository.save(oTS);
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
	}

	// @Override
	@Override
	public OntologyTimeSeriesServiceDTO generateOntologyTimeSeriesDTO(Ontology ontology) {
		final List<OntologyTimeSeries> ontologyTimeSeriesList = ontologyTimeSeriesRepository.findByOntology(ontology);
		final OntologyTimeSeries ontologyTimeSeries;
		if(!ontologyTimeSeriesList.isEmpty()) {
		    ontologyTimeSeries = ontologyTimeSeriesList.get(0);
		} else {
            throw new OntologyServiceException("There is no time series ontology for the selected ontology", OntologyServiceException.Error.GENERIC_ERROR);
		}
		final OntologyTimeSeriesServiceDTO otsDTO = new OntologyTimeSeriesServiceDTO();
		otsDTO.setActive(ontology.isActive());
		otsDTO.setIdentification(ontology.getIdentification());
		otsDTO.setAllowsCypherFields(ontology.isAllowsCypherFields());
		otsDTO.setDescription(ontology.getDescription());
		otsDTO.setJsonSchema(ontology.getJsonSchema());
		otsDTO.setId(ontology.getId());
		otsDTO.setPublic(ontology.isPublic());
		otsDTO.setMetainf(ontology.getMetainf());
		otsDTO.setRtdbDatasource(ontology.getRtdbDatasource().toString());
		otsDTO.setDataModel(ontology.getDataModel());
		otsDTO.setOntologyKPI(ontology.getOntologyKPI());
		otsDTO.setRtdbClean(false);
		otsDTO.setRtdbToHdb(ontology.isRtdbToHdb());
		otsDTO.setAllowsCreateTopic(ontology.isAllowsCreateTopic());
		otsDTO.setAllowsCreateNotificationTopic(ontology.isAllowsCreateNotificationTopic());
		otsDTO.setUser(ontology.getUser());
		otsDTO.setContextDataEnabled(ontology.isContextDataEnabled());
		otsDTO.setTimeSeriesTimescaleProperties(ontologyTimeSeries.getTimeSeriesTimescaleProperties());
		otsDTO.setTimeSeriesTimescaleAggregates(ontologyTimeSeries.getTimeSeriesTimescaleAgregates());
		otsDTO.setTimeSeriesProperties(
				ontologyTimeSeriesPropertyRepository.findByOntologyTimeSeries(ontologyTimeSeries));
		otsDTO.setTimeSeriesWindow(ontologyTimeSeriesWindowRepository.findByOntologyTimeSeries(ontologyTimeSeries));
		otsDTO.setSupportsJsonLd(ontology.isSupportsJsonLd());
        otsDTO.setJsonLdContext(ontology.getJsonLdContext());
        otsDTO.setEnableDataClass(ontology.isEnableDataClass());
		return otsDTO;
	}

	@Override
	@Transactional
	public void cloneOntologyTimeSeries(String identification, Ontology ontology, User user,
			OntologyConfiguration config) {
		final OntologyTimeSeriesServiceDTO otsDTO = generateOntologyTimeSeriesDTO(ontology);
		final Ontology clone = new Ontology();

		clone.setIdentification(identification);
		clone.setUser(user);
		clone.setDescription(ontology.getDescription());
		clone.setActive(ontology.isActive());
		clone.setPublic(ontology.isPublic());
		clone.setDataModel(ontology.getDataModel());
		clone.setDataModelVersion(ontology.getDataModelVersion());
		clone.setJsonSchema(ontology.getJsonSchema());
		clone.setMetainf(ontology.getMetainf());
		clone.setRtdbToHdbStorage(ontology.getRtdbToHdbStorage());
		clone.setRtdbDatasource(ontology.getRtdbDatasource());
		clone.setAllowsCypherFields(ontology.isAllowsCypherFields());
        clone.setSupportsJsonLd(ontology.isSupportsJsonLd());
        clone.setJsonLdContext(ontology.getJsonLdContext());
        clone.setEnableDataClass(ontology.isEnableDataClass());

		ontologyService.createOntology(clone, config);

		OntologyTimeSeries oTS = new OntologyTimeSeries();
		oTS.setOntology(clone);
		oTS.setTimeSeriesProperties(new HashSet<>());
		oTS.setTimeSeriesWindows(new HashSet<>());

		for (final OntologyTimeSeriesProperty oTSP : otsDTO.getTimeSeriesProperties()) {
			OntologyTimeSeriesProperty oTSPClone = new OntologyTimeSeriesProperty();
			oTSPClone.setPropertyDataType(oTSP.getPropertyDataType());
			oTSPClone.setPropertyName(oTSP.getPropertyName());
			oTSPClone.setPropertyType(oTSP.getPropertyType());
			oTSPClone.setOntologyTimeSeries(oTS);
			oTS.getTimeSeriesProperties().add(oTSPClone);
		}

		for (final OntologyTimeSeriesWindow oTSW : otsDTO.getTimeSeriesWindow()) {
			OntologyTimeSeriesWindow oTSWClone = new OntologyTimeSeriesWindow();
			oTSWClone.setAggregationFunction(oTSW.getAggregationFunction());
			oTSWClone.setBdh(oTSW.isBdh());
			oTSWClone.setFrecuency(oTSW.getFrecuency());
			oTSWClone.setFrecuencyUnit(oTSW.getFrecuencyUnit());
			oTSWClone.setRetentionBefore(oTSW.getRetentionBefore());
			oTSWClone.setRetentionUnit(oTSW.getRetentionUnit());
			oTSWClone.setWindowType(oTSW.getWindowType());
			oTSWClone.setOntologyTimeSeries(oTS);
			oTS.getTimeSeriesWindows().add(oTSWClone);
		}
		ontologyTimeSeriesRepository.save(oTS);

	}

	@Override
	public OntologyTimeseriesTimescaleAggregates createContinuousAggregate(OntologyTimeSeries timeSerieOntology,
			String sessionUserId, TimescaleContinuousAggregateRequest request) {

		final User sessionUser = userService.getUser(sessionUserId);
		if (ontologyService.hasUserPermisionForChangeOntology(sessionUser, timeSerieOntology.getOntology())) {

			OntologyTimeseriesTimescaleAggregates existingAggregate = ontologyTimescaleAggregatesRepository
					.findByNameAndTimeSeriesOntologyId(request.getName(), timeSerieOntology.getId());
			if (existingAggregate != null) {
				Log.error("Continuous aggregates with name {} already exists for TimescaleDB ontology{}",
						request.getName(), timeSerieOntology.getOntology().getIdentification());
				throw new OntologyServiceException("Duplicated Aggregate name.", OntologyServiceException.Error.EXISTING_ONTOLOGY);
			}
			OntologyTimeseriesTimescaleAggregates aggregate = new OntologyTimeseriesTimescaleAggregates();
			aggregate.setName(request.getName());
			aggregate.setIdentification(timeSerieOntology.getOntology().getIdentification() + "_" + request.getName());
			aggregate.setOntologyTimeSeries(timeSerieOntology);
			aggregate.setAggregateQuery(request.getAggregateQuery());
			aggregate.setBucketFrequency(request.getBucketAggregation());
			aggregate.setBucketFrequencyUnit(FrecuencyUnit.valueOf(request.getBucketAggregationUnit().toUpperCase()));
			aggregate.setSchedulerFrequency(request.getSchedulingPolicy());
			aggregate.setSchedulerFrequencyUnit(FrecuencyUnit.valueOf(request.getSchedulingPolicyUnit().toUpperCase()));
			aggregate.setStartOffset(request.getStartOffset());
			aggregate.setStartOffsetUnit(FrecuencyUnit.valueOf(request.getStartOffsetUnit().toUpperCase()));
			aggregate.setEndOffset(request.getEndOffset());
			aggregate.setEndOffsetUnit(FrecuencyUnit.valueOf(request.getEndOffsetUnit().toUpperCase()));

			return ontologyTimescaleAggregatesRepository.save(aggregate);
		} else {
			Log.error("User {} is not authorized to create continuous aggregates over ontology {}", sessionUserId,
					timeSerieOntology.getOntology().getIdentification());
			throw new OntologyServiceException(USER_NOT_AUTHORIZED, OntologyServiceException.Error.USER_ACCESS_NOT_FOUND);
		}

	}

	@Override
	public void deleteContinuousAggregate(OntologyTimeSeries timeSerieOntology, String sessionUserId, String name) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (ontologyService.hasUserPermisionForChangeOntology(sessionUser, timeSerieOntology.getOntology())) {
			OntologyTimeseriesTimescaleAggregates aggregate = ontologyTimescaleAggregatesRepository
					.findByNameAndTimeSeriesOntologyId(name, timeSerieOntology.getId());
			ontologyTimescaleAggregatesRepository.deleteByMyId(aggregate.getId());
		} else {
			Log.error("User {} is not authorized to delete continuous aggregates over ontology {}", sessionUserId,
					timeSerieOntology.getOntology().getIdentification());
			throw new OntologyServiceException(USER_NOT_AUTHORIZED, OntologyServiceException.Error.USER_ACCESS_NOT_FOUND);
		}

	}

}
