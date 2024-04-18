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
package com.minsait.onesait.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.bean.AccessType;
import com.minsait.onesait.platform.bean.DashboardCache;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.dto.socket.InputMessage;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.security.AppWebUtils;
import com.minsait.onesait.platform.solver.SolverInterface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SolverServiceImpl implements SolverService {

	private static final String ELASTIC_DATASOURCE_TYPE = "ELASTIC_SEARCH";
	private static final String VIRTUAL_DATASOURCE_TYPE = "VIRTUAL";

	@Autowired
	GadgetDatasourceRepository gdr;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	AppWebUtils utils;

	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Qualifier("QuasarSolver")
	SolverInterface quasarSolver;

	@Autowired
	@Qualifier("ElasticSolver")
	SolverInterface elasticSolver;

	@Autowired
	@Qualifier("OracleSolver")
	SolverInterface oracleSolver;

	@Autowired
	private DashboardCache dashboardCache;

	@Autowired
	OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private OPResourceService resourceService;

	@Override
	public String solveDatasource(InputMessage im)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		log.debug("solveDatasource");
		if (getDashboardUserSecurity(im.getDashboard())) {
			final GadgetDatasource gd = gdr.findByIdentification(im.getDs());

			if (gd == null) {
				return "Not found: 403 for user " + utils.getUserId() + " datasource: " + im.getDs();
			}

			// if dashboard is null (edit mode), we use authenticated user instead of
			// datasource user
			final String executeAs = ("".equals(im.getDashboard()) || im.getDashboard() == null) ? utils.getUserId()
					: gd.getUser().getUserId();

			String ontology = "";
			if (gd.getOntology() == null || gd.getOntology().getIdentification() == null) {
				ontology = getOntologyFromDatasource(gd.getQuery());
			} else {
				ontology = gd.getOntology().getIdentification();
			}

			final Ontology ont = ontologyService.getOntologyByIdentification(ontology, executeAs);

			if (ont == null) {
				return "Not found: 403 for user " + utils.getUserId() + " ontology: " + ontology;
			}

			String dsName = ont.getRtdbDatasource().name();
			if (dsName.equals(ELASTIC_DATASOURCE_TYPE)) {
				return elasticSolver.buildQueryAndSolve(gd.getQuery(), gd.getMaxvalues(), im.getFilter(),
						im.getProject(), im.getGroup(), executeAs, ontology);
			} else if (dsName.equals(VIRTUAL_DATASOURCE_TYPE)) {
				OntologyVirtualDatasource ontologyDatasource = ontologyVirtualRepository
						.findOntologyVirtualDatasourceByOntologyIdentification(ontology);

				switch (ontologyDatasource.getSgdb()) {
				case ORACLE:
					return oracleSolver.buildQueryAndSolve(gd.getQuery(), gd.getMaxvalues(), im.getFilter(),
							im.getProject(), im.getGroup(), executeAs, ontology);
				case POSTGRESQL:
					return quasarSolver.buildQueryAndSolve(gd.getQuery(), gd.getMaxvalues(), im.getFilter(),
							im.getProject(), im.getGroup(), executeAs, ontology);
				default:
					throw new GenericRuntimeOPException("Not supported SGBD");
				}

			} else {
				return quasarSolver.buildQueryAndSolve(gd.getQuery(), gd.getMaxvalues(), im.getFilter(),
						im.getProject(), im.getGroup(), executeAs, ontology);
			}
		}

		return "User " + utils.getUserId() + " can't access to dashboard";
	}

	// This method return null when used can't access the dashboard, in the way
	// return same user or another with permision over ontologies
	private boolean getDashboardUserSecurity(String dashboardId) {

		if ("".equals(dashboardId) || dashboardId == null || utils.isAdministrator()) {// Gadget edit mode dashboard is
																						// null
			return true;
		}

		final AccessType access = dashboardCache.getAccess();

		if (access == AccessType.NOCHECKED) {
			final Dashboard d = dashboardRepository.findById(dashboardId);
			if (d.isPublic() || d.getUser().getUserId().equals(utils.getUserId())) {
				dashboardCache.setAccess(AccessType.ALLOW);
				return true;
			} else {

				final DashboardUserAccess dua = dashboardUserAccessRepository.findByDashboardAndUser(d,
						userRepository.findByUserId(utils.getUserId()));
				if (dua != null) {// Read or write can resolve datasource in dsengine
					dashboardCache.setAccess(AccessType.ALLOW);
					return true;
				} else {
					if (resourceService.hasAccess(utils.getUserId(), dashboardId, ResourceAccessType.VIEW)) {
						dashboardCache.setAccess(AccessType.ALLOW);
						return true;
					} else {
						dashboardCache.setAccess(AccessType.DENY);
						return false;
					}

				}
			}
		} else {
			return dashboardCache.getAccess() == AccessType.ALLOW;
		}
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
					indexOf = (indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf) ? indexOfCloseBracket
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
