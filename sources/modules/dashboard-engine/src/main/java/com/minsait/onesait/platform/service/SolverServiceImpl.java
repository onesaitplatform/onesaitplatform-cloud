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
package com.minsait.onesait.platform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.aop.DashboardEngineAuditable;
import com.minsait.onesait.platform.bean.AccessType;
import com.minsait.onesait.platform.bean.DashboardCache;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.dto.socket.InputMessage;
import com.minsait.onesait.platform.dto.socket.querystt.OrderByStt;
import com.minsait.onesait.platform.dto.socket.querystt.ParamStt;
import com.minsait.onesait.platform.dto.socket.querystt.ProjectStt;
import com.minsait.onesait.platform.exception.DashboardEngineException;
import com.minsait.onesait.platform.security.AppWebUtils;
import com.minsait.onesait.platform.security.dashboard.engine.ValidationService;
import com.minsait.onesait.platform.solver.SolverInterface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SolverServiceImpl implements SolverService {

	private static final String SIMPLE_MODE = "simpleMode";

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
	private DashboardCache dashboardCache;

	@Autowired
	OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private OPResourceService resourceService;

	@Autowired(required = false)
	private ValidationService validationService;

	private SolverInterface getSolverByDatasource(RtdbDatasource datasource, String ontology) {
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

	private GadgetDatasource getGadgetDatasourceFromIdentification(String gds) {
		final GadgetDatasource gd = gdr.findByIdentification(gds);

		if (gd == null) {
			final String error = "Not found datasource: 403 for user " + utils.getUserId() + " datasource: " + gds;
			log.error(error);
			throw new DashboardEngineException(DashboardEngineException.Error.NOT_FOUND, error);
		}

		return gd;
	}

	private Ontology getOntologyFromDatasource(GadgetDatasource gd, String executeAs) {
		String ontology = "";
		if (gd.getOntology() == null || gd.getOntology().getIdentification() == null) {
			ontology = getOntologyFromDatasource(gd.getQuery());
		} else {
			ontology = gd.getOntology().getIdentification();
		}

		final Ontology ont = ontologyService.getOntologyByIdentification(ontology, executeAs);

		if (ont == null) {
			final String error = "Not found ontology: 403 for user " + utils.getUserId() + " datasource: "
					+ gd.getIdentification();
			log.error(error);
			throw new DashboardEngineException(DashboardEngineException.Error.NOT_FOUND, error);
		}

		return ont;
	}

	@Override
	@DashboardEngineAuditable
	public String solveDatasource(InputMessage im)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException {
		String error;

		if (getDashboardUserSecurity(im.getDashboard())) {

			final GadgetDatasource gd = getGadgetDatasourceFromIdentification(im.getDs());
			boolean isSimpleMode = isDatasourceSimpleMode(gd);
			if (externalValidation(im, gd)) {

				// if dashboard is null (edit mode), we use authenticated user instead of
				// datasource user
				final String executeAs = "".equals(im.getDashboard()) || im.getDashboard() == null ? utils.getUserId()
						: gd.getUser().getUserId();

				final Ontology ont = getOntologyFromDatasource(gd, executeAs);

				return getSolverByDatasource(ont.getRtdbDatasource(), ont.getIdentification()).buildQueryAndSolve(
						gd.getQuery(), gd.getMaxvalues(), im.getFilter(), im.getProject(), im.getGroup(), im.getSort(),
						im.getOffset(), im.getLimit(), im.getParam(), im.isDebug(), executeAs, ont.getIdentification(),
						isSimpleMode);
			} else {
				error = "User " + utils.getUserId()
				+ " cannot access the information due to restrictions defined in the security plugin";
				log.info(error);
				return "[]";
			}
		} else {
			error = "User " + utils.getUserId() + " can't access to dashboard";
			log.error(error);
			throw new DashboardEngineException(DashboardEngineException.Error.PERMISSION_DENIED,
					"User " + utils.getUserId() + " can't access to dashboard");
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

	private boolean externalValidation(InputMessage im, GadgetDatasource gd) {
		boolean externalValidation = true;
		if (validationService != null) {
			try {
				log.info("dashboard engine plugin active ");
				final com.minsait.onesait.platform.security.dashboard.engine.dto.InputMessage message = new com.minsait.onesait.platform.security.dashboard.engine.dto.InputMessage();
				// map to message
				message.setDashboard(im.getDashboard());
				message.setDs(im.getDs());
				if (im.getFilter() == null) {
					message.setFilter(new ArrayList<>());
				} else {
					message.setFilter(im.getFilter().stream().map(
							f -> new com.minsait.onesait.platform.security.dashboard.engine.dto.FilterStt(f.getField(),
									f.getOp(), f.getExp()))
							.collect(Collectors.toList()));
				}
				message.setGroup(im.getGroup());
				message.setLimit(im.getLimit());
				message.setOffset(im.getOffset());
				if (im.getParam() != null && im.getParam().size() > 0) {
					final List<com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt> param = new ArrayList<>();
					for (final Object element : im.getParam()) {
						final ParamStt pastt = (ParamStt) element;
						param.add(new com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt(
								pastt.getField(), pastt.getValue()));
					}
					message.setParam(param);
				}
				if (im.getProject() != null && im.getProject().size() > 0) {
					final List<com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt> project = new ArrayList<>();
					for (final Object element : im.getProject()) {
						final ProjectStt projectStt = (ProjectStt) element;
						project.add(new com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt(
								projectStt.getField(), projectStt.getOp(), projectStt.getAlias()));
					}

					message.setProject(project);
				}
				if (im.getSort() != null && im.getSort().size() > 0) {
					final List<com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt> sort = new ArrayList<>();
					for (final Object element : im.getSort()) {
						final OrderByStt orderByStt = (OrderByStt) element;
						sort.add(new com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt(
								orderByStt.getField(), orderByStt.isAsc()));
					}

					message.setSort(sort);
				}
				message.setQuery(gd.getQuery());
				message.setOntology(gd.getOntology().getIdentification());

				final User user = userRepository.findByUserId(utils.getUserId());
				final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				message.setToken((String) auth.getCredentials());
				message.setUser(user.getUserId());
				message.setRol(user.getRole().getName());

				externalValidation = validationService.validate(message);

				log.info("dashboard engine externalValidation ");
				// output map

				im.setDashboard(message.getDashboard());
				im.setDs(message.getDs());
				if (message.getFilter() == null) {
					im.setFilter(new ArrayList<>());
				} else {
					im.setFilter(message.getFilter().stream()
							.map(f -> new com.minsait.onesait.platform.dto.socket.querystt.FilterStt(f.getField(),
									f.getOp(), f.getExp()))
							.collect(Collectors.toList()));
				}

				im.setGroup(message.getGroup());
				im.setLimit(message.getLimit());
				im.setOffset(message.getOffset());

				if (message.getParam() != null && message.getParam().size() > 0) {
					final List<com.minsait.onesait.platform.dto.socket.querystt.ParamStt> param = new ArrayList<>();
					for (final Object element : message.getParam()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt pastt = (com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt) element;
						param.add(new com.minsait.onesait.platform.dto.socket.querystt.ParamStt(pastt.getField(),
								pastt.getValue()));
					}
					im.setParam(param);
				}

				if (message.getProject() != null && message.getProject().size() > 0) {
					final List<com.minsait.onesait.platform.dto.socket.querystt.ProjectStt> project = new ArrayList<>();
					for (final Object element : message.getProject()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt projectStt = (com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt) element;
						project.add(new com.minsait.onesait.platform.dto.socket.querystt.ProjectStt(
								projectStt.getField(), projectStt.getOp(), null, projectStt.getAlias()));
					}
					im.setProject(project);
				}

				if (message.getSort() != null && message.getSort().size() > 0) {
					final List<com.minsait.onesait.platform.dto.socket.querystt.OrderByStt> sort = new ArrayList<>();
					for (final Object element : message.getSort()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt orderByStt = (com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt) element;
						sort.add(new com.minsait.onesait.platform.dto.socket.querystt.OrderByStt(orderByStt.getField(),
								orderByStt.isAsc()));
					}

					im.setSort(sort);
				}

				gd.setQuery(message.getQuery());

			} catch (final Exception e) {
				externalValidation = false;
				log.error("external security plugin error", e);
			}
		}
		return externalValidation;
	}

	@Override
	public String explainDatasource(InputMessage im) {
		String error;

		if (getDashboardUserSecurity(im.getDashboard())) {
			final GadgetDatasource gd = getGadgetDatasourceFromIdentification(im.getDs());
			boolean isSimpleMode = isDatasourceSimpleMode(gd);
			if (externalValidation(im, gd)) {
				// explain only works with same user for ontology
				final String executeAs = utils.getUserId();

				final Ontology ont = getOntologyFromDatasource(gd, executeAs);

				return getSolverByDatasource(ont.getRtdbDatasource(), ont.getIdentification()).buildQuery(gd.getQuery(),
						gd.getMaxvalues(), im.getFilter(), im.getProject(), im.getGroup(), im.getSort(), im.getOffset(),
						im.getLimit(), im.getParam(), im.isDebug(), executeAs, ont.getIdentification(), isSimpleMode);
			} else {
				error = "User " + utils.getUserId()
				+ " cannot access the information due to restrictions defined in the security plugin";
				log.info(error);
				return "[]";
			}
		} else {
			error = "User " + utils.getUserId() + " can't access to dashboard";
			log.error(error);
			throw new DashboardEngineException(DashboardEngineException.Error.PERMISSION_DENIED,
					"User " + utils.getUserId() + " can't access to dashboard");
		}
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
			final Optional<Dashboard> opt = dashboardRepository.findById(dashboardId);
			if (!opt.isPresent()) {
				return false;
			}
			final Dashboard d = opt.get();
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
