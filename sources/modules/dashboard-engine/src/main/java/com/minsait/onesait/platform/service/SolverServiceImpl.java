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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.aop.DashboardEngineAuditable;
import com.minsait.onesait.platform.bean.AccessType;
import com.minsait.onesait.platform.bean.DashboardCache;
import com.minsait.onesait.platform.business.services.datasources.dto.FilterStt;
import com.minsait.onesait.platform.business.services.datasources.dto.InputMessage;
import com.minsait.onesait.platform.business.services.datasources.dto.OrderByStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ParamStt;
import com.minsait.onesait.platform.business.services.datasources.dto.ProjectStt;
import com.minsait.onesait.platform.business.services.datasources.exception.DashboardEngineException;
import com.minsait.onesait.platform.business.services.datasources.service.DatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
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
import com.minsait.onesait.platform.security.AppWebUtils;
import com.minsait.onesait.platform.security.dashboard.engine.ValidationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SolverServiceImpl implements SolverService {

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
	private DatasourceService datasourceService;

	@Autowired
	private DashboardCache dashboardCache;

	@Autowired
	OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private OPResourceService resourceService;

	@Autowired(required = false)
	private ValidationService validationService;

	@Override
	@DashboardEngineAuditable
	public String solveDatasource(InputMessage im)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException {
		String error;

		if (getDashboardUserSecurity(im.getDashboard())) {

			final GadgetDatasource gd = datasourceService.getGadgetDatasourceFromIdentification(im.getDs(),
					utils.getUserId());

			if (externalValidation(im, gd)) {

				// if dashboard is null (edit mode), we use authenticated user instead of
				// datasource user
				final String executeAs = "".equals(im.getDashboard()) || im.getDashboard() == null ? utils.getUserId()
						: gd.getUser().getUserId();

				final Ontology ont = datasourceService.getOntologyFromDatasource(gd, executeAs);

				return datasourceService.solveDatasource(im, ont, gd, executeAs);

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
							.map(f -> new FilterStt(f.getField(), f.getOp(), f.getExp())).collect(Collectors.toList()));
				}

				im.setGroup(message.getGroup());
				im.setLimit(message.getLimit());
				im.setOffset(message.getOffset());

				if (message.getParam() != null && message.getParam().size() > 0) {
					final List<ParamStt> param = new ArrayList<>();
					for (final Object element : message.getParam()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt pastt = (com.minsait.onesait.platform.security.dashboard.engine.dto.ParamStt) element;
						param.add(new ParamStt(pastt.getField(), pastt.getValue()));
					}
					im.setParam(param);
				}

				if (message.getProject() != null && message.getProject().size() > 0) {
					final List<ProjectStt> project = new ArrayList<>();
					for (final Object element : message.getProject()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt projectStt = (com.minsait.onesait.platform.security.dashboard.engine.dto.ProjectStt) element;
						project.add(
								new ProjectStt(projectStt.getField(), projectStt.getOp(), null, projectStt.getAlias()));
					}
					im.setProject(project);
				}

				if (message.getSort() != null && message.getSort().size() > 0) {
					final List<OrderByStt> sort = new ArrayList<>();
					for (final Object element : message.getSort()) {
						final com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt orderByStt = (com.minsait.onesait.platform.security.dashboard.engine.dto.OrderByStt) element;
						sort.add(new OrderByStt(orderByStt.getField(), orderByStt.isAsc()));
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
	public String explainDatasource(InputMessage im)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException {
		String error;

		if (getDashboardUserSecurity(im.getDashboard())) {
			final GadgetDatasource gd = datasourceService.getGadgetDatasourceFromIdentification(im.getDs(),
					utils.getUserId());

			if (externalValidation(im, gd)) {
				// explain only works with same user for ontology
				final String executeAs = utils.getUserId();

				final Ontology ont = datasourceService.getOntologyFromDatasource(gd, executeAs);
				return datasourceService.solveDatasource(im, ont, gd, executeAs);

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

}
