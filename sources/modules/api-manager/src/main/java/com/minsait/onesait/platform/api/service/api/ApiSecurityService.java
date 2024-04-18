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
package com.minsait.onesait.platform.api.service.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiSecurityService {

	@Autowired
	ApiManagerService apiManagerService;

	@Autowired
	ApiServiceRest apiServiceRest;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private UserService userService;

	@Autowired
	private ConfigDBDetailsService detailsService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;

	public boolean isAdmin(final User user) {
		return user.isAdmin();
	}

	public boolean isCol(final User user) {
		return (Role.Type.ROLE_OPERATIONS.name().equalsIgnoreCase(user.getRole().getId()));
	}

	public boolean isUser(final User user) {
		return (Role.Type.ROLE_USER.name().equalsIgnoreCase(user.getRole().getId()));
	}

	public User getUser(String userId) {
		return userService.getUser(userId);
	}

	public User getUserByApiToken(String token) {
		detailsService.loadUserByUserToken(token);
		return userService.getUserByToken(token);
	}

	public UserToken getUserToken(User userId, String token) {
		return userService.getUserToken(userId.getUserId(), token);
	}

	public boolean authorized(Api api, String tokenUsuario) {
		final User user = getUserByApiToken(tokenUsuario);
		return checkUserApiPermission(api, user);
	}

	public boolean checkOntologyOperationPermission() {
		return true;
	}

	public boolean checkUserApiPermission(Api api, User user) {

		if (api == null || user == null)
			return false;

		boolean autorizado = false;

		// is administrator, then true
		if (user.isAdmin()) {// Rol administrador
			autorizado = true;

		} else if (api.getUser().getUserId() != null && api.getUser().getUserId().equals(user.getUserId())) {
			// owner
			autorizado = true;
		} else {
			// No administrador, no owner but subscripted
			UserApi suscriptionApi = null;
			try {
				suscriptionApi = apiServiceRest.findApiSuscriptions(api, user);
			} catch (final Exception e) {
				log.error("Something failed ", e);
			}

			if (suscriptionApi != null) {
				autorizado = true;
			} else {
				autorizado = resourceService.hasAccess(user.getUserId(), api.getId(), ResourceAccessType.VIEW);
			}
		}

		return autorizado;
	}

	public boolean checkApiAvailable(Api api, User user) {

		if (api == null || user == null)
			return false;

		boolean can = api.getState().name().equalsIgnoreCase(Api.ApiStates.CREATED.name())
				&& ((api.getUser().getUserId().equals(user.getUserId()) || user.isAdmin()));
		if (can)
			return true;
		else {
			final String state = api.getState().name();
			can = (state.equalsIgnoreCase(Api.ApiStates.PUBLISHED.name())
					|| state.equalsIgnoreCase(Api.ApiStates.DEPRECATED.name())
					|| state.equalsIgnoreCase(Api.ApiStates.DEVELOPMENT.name()));
			return can;
		}

	}

	public boolean checkApiIsPublic(Api api) {
		return api.isPublic();
	}

	private boolean checkOntologyAccesses(Ontology ontology, List<OntologyUserAccess> uo, boolean insert) {

		for (final OntologyUserAccess oua : uo) {

			if (oua.getOntology().getId().equals(ontology.getId()) && oua.getOntologyUserAccessType() != null) {
				if (OntologyUserAccessType.Type.ALL.name()
						.equalsIgnoreCase(oua.getOntologyUserAccessType().getName())) {
					return true;

				} else if (OntologyUserAccessType.Type.INSERT.name()
						.equalsIgnoreCase(oua.getOntologyUserAccessType().getName())) {
					if (insert) {
						return true;

					}
				} else if (OntologyUserAccessType.Type.QUERY.name()
						.equalsIgnoreCase(oua.getOntologyUserAccessType().getName())) {
					return !insert;

				}
			}
		}
		return false;

	}

	public Boolean checkRole(User user, Ontology ontology, boolean insert) {

		Boolean authorize = false;
		// If the role is Manager always allows the operation
		if (user.isAdmin()) {// Rol administrador
			authorize = true;

		} else {

			if (ontology.getUser().getUserId().equals(user.getUserId())) {// Si es el propietario
				return true;
			}
			if (ontology.isPublic()) {
				return true;
			}

			// If other role, it checks whether the user is associated with ontology
			final List<OntologyUserAccess> uo = ontologyUserAccessRepository.findByUser(user);
			authorize = checkOntologyAccesses(ontology, uo, insert);

			if (!authorize) {
				if (insert)
					authorize = resourceService.hasAccess(user.getUserId(), ontology.getId(),
							ResourceAccessType.MANAGE);
				else
					authorize = resourceService.hasAccess(user.getUserId(), ontology.getId(), ResourceAccessType.VIEW);
			}

		}
		return authorize;
	}

	public User getUserOauth(String tokenOauth) {
		try {
			final Authentication auth = jwtService.getAuthentication(tokenOauth);
			if (auth != null) {
				if (auth.getPrincipal() instanceof UserPrincipal) {
					final UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
					MultitenancyContextHolder.setTenantName(principal.getTenant());
					MultitenancyContextHolder.setVerticalSchema(principal.getVerticalSchema());
				}
				detailsService.loadUserByUsername(auth.getName());
//				SecurityContextHolder.getContext().setAuthentication(auth);
				
				// To avoid concurrency problem where getUser returns null: https://docs.spring.io/spring-security/site/docs/5.2.11.RELEASE/reference/html/overall-architecture.html#:~:text=concurrent%20requests%20in%20a%20single%20session
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(auth);
				SecurityContextHolder.setContext(context);
				
				return (userService.getUser(auth.getName()));
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}
