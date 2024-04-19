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
package com.minsait.onesait.platform.config.services.opendata.typology;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.repository.ODTypologyDatasetRepository;
import com.minsait.onesait.platform.config.services.exceptions.ODTypologyDatasetServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TypologyDatasetServiceImpl implements TypologyDatasetService {

	@Autowired
	private ODTypologyDatasetRepository typologyDatasetRepository;
	@Autowired
	private TypologyService typologyService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private static final String TYPOLOGY_NOT_EXIST = "Typology does not exist in the database";

	@Override
	public List<String> getAllIds() {
		final List<ODTypologyDataset> typologies = typologyDatasetRepository.findAll();
		final List<String> identifications = new ArrayList<>();
		for (final ODTypologyDataset typology : typologies) {
			identifications.add(typology.getId());

		}
		return identifications;
	}

	@Override
	public List<ODTypologyDataset> getAllTypologiesDatasets() {
		return typologyDatasetRepository.findAll();
	}

	@Transactional
	@Override
	public void deleteTypologyDatasetById(String id) {
		final ODTypologyDataset typology = typologyDatasetRepository.findTypologyDatasetById(id);
		if (typology != null) {
			typologyDatasetRepository.delete(typology);
		} else {
			throw new ODTypologyDatasetServiceException("Cannot delete typology that does not exist");
		}
	}

	@Override
	public void saveTypologyDataset(String id, ODTypologyDataset typology) {

		final ODTypologyDataset typologyEnt = typologyDatasetRepository.findTypologyDatasetById(typology.getId());
		typologyEnt.setDatasetId(typology.getDatasetId());
		typologyEnt.setTypologyId(typology.getTypologyId());

		typologyDatasetRepository.save(typologyEnt);

	}

	@Override
	public ODTypologyDataset getTypologyById(String id) {
		return typologyDatasetRepository.findTypologyDatasetById(id);
	}

	private ODTypologyDataset getNewTypology(ODTypologyDataset typology) {
		log.debug("Typology no exist, creating...");
		final ODTypologyDataset d = new ODTypologyDataset();

		d.setDatasetId(typology.getDatasetId());
		d.setTypologyId(typology.getTypologyId());

		return typologyDatasetRepository.save(d);
	}

	@Override
	public String createNewTypologyDataset(ODTypologyDataset typology) {
		if (typology.getId() != null)
			throw new ODTypologyDatasetServiceException("Typology already exists in Database");

		final ODTypologyDataset dAux = getNewTypology(typology);

		return dAux.getId();

	}

	@Transactional
	@Override
	public String updateTypologyIdTypologyDataset(ODTypologyDataset typology) {
		final ODTypologyDataset d = typologyDatasetRepository.findTypologyDatasetByDatasetId(typology.getDatasetId());
		if (d == null) {
			throw new ODTypologyDatasetServiceException(TYPOLOGY_NOT_EXIST);
		} else {
			d.setTypologyId(typology.getTypologyId());
			return d.getId();
		}
	}

	@Override
	public ODTypologyDataset getTypologyByDatasetId(String datasetId) {
		return typologyDatasetRepository.findTypologyDatasetByDatasetId(datasetId);
	}
	
	@Override
	public List<ODTypologyDataset> getTypologyByTypologyID(String datasetId) {
		return typologyDatasetRepository.findTypologyDatasetByTypologyId(datasetId);
	}

	@Override
	public void deleteTypologyDatasetByDatasetId(String datasetId) {
		final ODTypologyDataset typology = typologyDatasetRepository.findTypologyDatasetByDatasetId(datasetId);
		if (typology != null) {
			typologyDatasetRepository.delete(typology);
		} 
	}
	
	@Override
	public String getTypologyIdentificationByDatasetId(String datasetId) {
		ODTypologyDataset typologyDataset = getTypologyByDatasetId(datasetId);
		if (typologyDataset != null) {
			ODTypology typology = typologyService.getTypologyById(typologyDataset.getTypologyId());
			if (typology != null) 
				return typology.getIdentification();
			else 
				throw new OpenDataServiceException("Typology does not exist");
		}
		return null;	
	}
	
	@Override
	public String getTypologyIdByDatasetId(String datasetId) {
		ODTypologyDataset typologyDataset = getTypologyByDatasetId(datasetId);
		if (typologyDataset != null) {
			ODTypology typology = typologyService.getTypologyById(typologyDataset.getTypologyId());
			if (typology != null) 
				return typology.getId();
			else 
				throw new OpenDataServiceException("Typology does not exist");
		}
		return null;	
	}
}
