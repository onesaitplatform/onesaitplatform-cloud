/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesPropertyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesWindowRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesDTO;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class OntologyTimeSeriesServiceImpl implements OntologyTimeSeriesService {

	private static final String STATUS_STR = "status";
	private static final String CAUSE_STR = "cause";
	private static final String ERROR_STR = "error";
	private static final String STATS_STR = "_stats";

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

	@Override
	@Transactional
	public ResponseEntity<Map<String, String>> createOntologyTimeSeries(OntologyTimeSeriesDTO ontologyTimeSeriesDTO,
			HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (ontologyService.existsOntology(ontologyTimeSeriesDTO.getIdentification())) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, "Exists another ontology with this identification");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

		}

		final Ontology ontology = new Ontology();
		ontologyTimeSeriesDTO.setTimeSeriesProperties();
		ontologyTimeSeriesDTO.setTimeSeriesWindow();
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

		final OntologyConfiguration config = new OntologyConfiguration(request);

		ontologyService.createOntology(ontology, config);

		final OntologyTimeSeries oTS = new OntologyTimeSeries();
		oTS.setOntology(ontology);
		oTS.setId(ontology.getId());
		ontologyTimeSeriesRepository.save(oTS);

		for (final OntologyTimeSeriesProperty oTSP : ontologyTimeSeriesDTO.getTimeSeriesProperties()) {
			oTSP.setOntologyTimeSeries(oTS);
			ontologyTimeSeriesPropertyRepository.save(oTSP);
		}
		for (final OntologyTimeSeriesWindow oTSW : ontologyTimeSeriesDTO.getTimeSeriesWindow()) {
			oTSW.setOntologyTimeSeries(oTS);
			ontologyTimeSeriesWindowRepository.save(oTSW);
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
			final OntologyConfiguration config2 = new OntologyConfiguration(request);

			ontologyService.createOntology(stats, config2);
		}
		response.put("redirect", "/controlpanel/ontologies/list");
		response.put(STATUS_STR, "ok");
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// @Override
	@Override
	@Transactional
	public ResponseEntity<?> updateOntologyTimeSeries(OntologyTimeSeriesDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config) {
		final Map<String, String> response = new HashMap<>();

		final Ontology ontologyDb = ontologyRepository.findById(ontologyTimeSeriesDTO.getId());
		final User sessionUser = userService.getUser(sessionUserId);

		ontologyTimeSeriesDTO.setTimeSeriesProperties();
		ontologyTimeSeriesDTO.setTimeSeriesWindow();

		if (ontologyDb != null) {
			ontologyDb.setJsonSchema(ontologyTimeSeriesDTO.getJsonSchema());
			ontologyDb.setActive(ontologyTimeSeriesDTO.isActive());
			ontologyDb.setPublic(ontologyTimeSeriesDTO.isPublic());
			ontologyDb.setAllowsCypherFields(ontologyTimeSeriesDTO.isAllowsCypherFields());
			ontologyDb.setDescription(ontologyTimeSeriesDTO.getDescription());
			ontologyDb.setMetainf(ontologyTimeSeriesDTO.getMetainf());
			if (ontologyService.hasUserPermisionForChangeOntology(sessionUser, ontologyDb)) {
				ontologyRepository.save(ontologyDb);

			} else {
				throw new OntologyServiceException("The user is not authorized");
			}
		} else
			throw new OntologyServiceException("Ontology does not exist");

		final OntologyTimeSeries oTS = ontologyTimeSeriesRepository.findById(ontologyTimeSeriesDTO.getId());
		Set<OntologyTimeSeriesProperty> ontologyTSPDb = ontologyTimeSeriesPropertyRepository
				.findByOntologyTimeSeries(oTS);
		Set<OntologyTimeSeriesWindow> ontologyTSWDb = ontologyTimeSeriesWindowRepository.findByOntologyTimeSeries(oTS);

		boolean flag = true;
		// Check and add new
		for (final OntologyTimeSeriesProperty oTSP : ontologyTimeSeriesDTO.getTimeSeriesProperties()) {
			for (final OntologyTimeSeriesProperty oTSPDB : ontologyTSPDb) {
				if (oTSP.getPropertyType().equals(oTSPDB.getPropertyType())
						&& oTSP.getPropertyDataType().equals(oTSPDB.getPropertyDataType())
						&& oTSP.getPropertyName().equals(oTSPDB.getPropertyName())) {
					flag = false;
				}
			}
			oTSP.setOntologyTimeSeries(oTS);
			if (flag) {
				ontologyTimeSeriesPropertyRepository.save(oTSP);
			}
			flag = true;
		}

		ontologyTSPDb = ontologyTimeSeriesPropertyRepository.findByOntologyTimeSeries(oTS);
		// Check and remove old
		for (final OntologyTimeSeriesProperty oTSPDB : ontologyTSPDb) {
			for (final OntologyTimeSeriesProperty oTSP : ontologyTimeSeriesDTO.getTimeSeriesProperties()) {
				if (oTSP.getPropertyType().equals(oTSPDB.getPropertyType())
						&& oTSP.getPropertyDataType().equals(oTSPDB.getPropertyDataType())
						&& oTSP.getPropertyName().equals(oTSPDB.getPropertyName())) {
					flag = false;
				}
			}
			if (flag) {
				ontologyTimeSeriesPropertyRepository.deleteByMyId(oTSPDB.getId());
			}
			flag = true;
		}

		// Check and add new
		for (final OntologyTimeSeriesWindow oTSW : ontologyTimeSeriesDTO.getTimeSeriesWindow()) {
			for (final OntologyTimeSeriesWindow oTSWDB : ontologyTSWDb) {
				if (oTSW.getWindowType().equals(oTSWDB.getWindowType())
						&& oTSW.getFrecuencyUnit().equals(oTSWDB.getFrecuencyUnit())
						&& oTSW.getFrecuency().equals(oTSWDB.getFrecuency())
						&& oTSW.getAggregationFunction().equals(oTSWDB.getAggregationFunction())
						&& oTSW.isBdh() == oTSWDB.isBdh()) {
					flag = false;
				}
			}
			if (flag) {
				oTSW.setOntologyTimeSeries(oTS);
				ontologyTimeSeriesWindowRepository.save(oTSW);
			}
			flag = true;
		}

		ontologyTSWDb = ontologyTimeSeriesWindowRepository.findByOntologyTimeSeries(oTS);
		// Check and remove old
		for (final OntologyTimeSeriesWindow oTSWDB : ontologyTSWDb) {
			for (final OntologyTimeSeriesWindow oTSW : ontologyTimeSeriesDTO.getTimeSeriesWindow()) {
				if (oTSW.getWindowType().equals(oTSWDB.getWindowType())
						&& oTSW.getFrecuencyUnit().equals(oTSWDB.getFrecuencyUnit())
						&& oTSW.getFrecuency().equals(oTSWDB.getFrecuency())
						&& oTSW.getAggregationFunction().equals(oTSWDB.getAggregationFunction())
						&& oTSW.isBdh() == oTSWDB.isBdh()) {
					flag = false;
				}
			}
			if (flag) {
				ontologyTimeSeriesWindowRepository.deleteByMyId(oTSWDB.getId());
			}
			flag = true;
		}

		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
	}

	// @Override
	@Override
	public OntologyTimeSeriesDTO generateOntologyTimeSeriesDTO(Ontology ontology) {
		final OntologyTimeSeries ontologyTimeSeries = ontologyTimeSeriesRepository.findByOntology(ontology).get(0);
		final OntologyTimeSeriesDTO otsDTO = new OntologyTimeSeriesDTO();
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
		otsDTO.setUser(ontology.getUser());

		otsDTO.setTimeSeriesProperties(
				ontologyTimeSeriesPropertyRepository.findByOntologyTimeSeries(ontologyTimeSeries));
		otsDTO.setTimeSeriesWindow(ontologyTimeSeriesWindowRepository.findByOntologyTimeSeries(ontologyTimeSeries));
		return otsDTO;
	}
}
