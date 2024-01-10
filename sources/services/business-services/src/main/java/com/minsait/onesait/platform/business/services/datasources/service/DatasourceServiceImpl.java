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
package com.minsait.onesait.platform.business.services.datasources.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.business.services.crud.CrudService;
import com.minsait.onesait.platform.business.services.datasources.dto.InputMessage;
import com.minsait.onesait.platform.business.services.datasources.exception.DatasourceException;
import com.minsait.onesait.platform.business.services.datasources.solver.SolverInterface;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatasourceServiceImpl implements DatasourceService {

	private static final String SIMPLE_MODE = "simpleMode";
	private static final String ERROR_TRUE = "{\"error\":\"true\"}";
	@Autowired
	GadgetDatasourceRepository gdr;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	@Qualifier("QuasarSolver")
	SolverInterface quasarSolver;

	@Autowired
	@Qualifier("SQLSolver")
	SolverInterface sqlSolver;

	@Autowired
	@Qualifier("SQLServerSolver")
	SolverInterface sqlServerSolver;

	@Autowired
	@Qualifier("OracleSolver")
	SolverInterface oracleSolver;

	@Autowired
	@Qualifier("OracleSolver11")
	SolverInterface oracleSolver11;

	@Autowired
	@Qualifier("NebulaGraphSolver")
	SolverInterface nebulaGraphSolver;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private CrudService crudService;

	public SolverInterface getSolverByDatasource(RtdbDatasource datasource, String ontology) {
		switch (datasource) {
		case ELASTIC_SEARCH:
		case OPEN_SEARCH:
			return sqlSolver;
		case VIRTUAL:
			final OntologyVirtualDatasource ontologyDatasource = ontologyVirtualRepository
					.findOntologyVirtualDatasourceByOntologyIdentification(ontology);
			switch (ontologyDatasource.getSgdb()) {
			case ORACLE:
				return oracleSolver;
			case ORACLE11:
				return oracleSolver11;
			case SQLSERVER:
				return sqlServerSolver;
			default:
				return sqlSolver;
			}
		case NEBULA_GRAPH:
			return nebulaGraphSolver;
		default:
			return quasarSolver;
		}
	}

	public GadgetDatasource getGadgetDatasourceFromIdentification(String gds, String userId) {
		final GadgetDatasource gd = gdr.findByIdentification(gds);

		if (gd == null) {
			final String error = "Not found datasource: 403 for user " + userId + " datasource: " + gds;
			log.error(error);
			throw new DatasourceException(DatasourceException.Error.NOT_FOUND, error);
		}

		return gd;
	}

	public Ontology getOntologyFromDatasource(GadgetDatasource gd, String userId) {
		String ontology = "";
		if (gd.getOntology() == null || gd.getOntology().getIdentification() == null) {
			ontology = getOntologyFromDatasource(gd.getQuery());
		} else {
			ontology = gd.getOntology().getIdentification();
		}

		final Ontology ont = ontologyService.getOntologyByIdentification(ontology, userId);

		if (ont == null) {
			final String error = "Not found ontology: 403 for user " + userId + " datasource: "
					+ gd.getIdentification();
			log.error(error);
			throw new DatasourceException(DatasourceException.Error.NOT_FOUND, error);
		}

		return ont;
	}

	@Override
	public String solveDatasource(InputMessage im, Ontology ont, GadgetDatasource gd, String userId)
			throws DatasourceException, OntologyDataUnauthorizedException, GenericOPException {

		if (gd == null) {
			gd = getGadgetDatasourceFromIdentification(im.getDs(), userId);
		}
		boolean isSimpleMode = isDatasourceSimpleMode(gd);

		// if dashboard is null (edit mode), we use authenticated user instead of
		// datasource user

		if (ont == null) {
			ont = getOntologyFromDatasource(gd, userId);
		}

		return getSolverByDatasource(ont.getRtdbDatasource(), ont.getIdentification()).buildQueryAndSolve(gd.getQuery(),
				gd.getMaxvalues(), im.getFilter(), im.getProject(), im.getGroup(), im.getSort(), im.getOffset(),
				im.getLimit(), im.getParam(), im.isDebug(), userId, ont.getIdentification(), isSimpleMode);

	}

	@Override
	public String getDataById(String entityId, String oid, String userId) {
		return crudService.findById(entityId, oid, userId);
	}

	@Override
	public String deleteDataById(String entityId, String oid, String userId) {
		try {
			ontologyService.getOntologyByIdInsert(entityId, userId);
			return crudService.processQuery("", entityId, ApiOperation.Type.DELETE, "", oid, userId);
		} catch (final Exception e) {
			throw new DatasourceException(DatasourceException.Error.GENERIC_EXCEPTION, e);
		}
	}

	@Override
	public String insertData(String entityId, String userId, String data) {
		try {
			ontologyService.getOntologyByIdInsert(entityId, userId);
			return crudService.processQuery("", entityId, ApiOperation.Type.POST, data, "", userId);
		} catch (final Exception e) {
			throw new DatasourceException(DatasourceException.Error.GENERIC_EXCEPTION, e);
		}
	}

	@Override
	public String update(String entityId, String oid, String userId, String data) {
		try {
			ontologyService.getOntologyByIdInsert(entityId, userId);
			return crudService.processQuery("", entityId, ApiOperation.Type.PUT, data, oid, userId);
		} catch (final Exception e) {
			throw new DatasourceException(DatasourceException.Error.GENERIC_EXCEPTION, e);
		}
	}

	// get config and map for get if is simple mode or complex
	// if return true then is simple mode
	// if return false then work normaly

	private boolean isDatasourceSimpleMode(GadgetDatasource gd) {
		String config = gd.getConfig();
		if (config != null && config.trim().length() > 0) {
			try {
				JSONObject configJson = new JSONObject(config);
				boolean isSimpleMode = configJson.getBoolean(SIMPLE_MODE);
				return isSimpleMode;
			} catch (Exception e) {
				return false;
			}

		}

		return false;
	}

	private static String getOntologyFromDatasource(String datasource) {
		datasource = datasource.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ");
		datasource = datasource.trim().replaceAll(" +", " ");
		String[] list = datasource.split("from ");
		if (list.length == 1) {
			list = datasource.split("FROM ");
		}
		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				if (!list[i].startsWith("(")) {
					int indexOf = list[i].toLowerCase().indexOf(" ", 0);
					final int indexOfCloseBracket = list[i].toLowerCase().indexOf(')', 0);
					indexOf = indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf ? indexOfCloseBracket
							: indexOf;
					if (indexOf == -1) {
						indexOf = list[i].length();
					}
					return list[i].substring(0, indexOf).trim();
				}
			}
		}
		return "";
	}

}
