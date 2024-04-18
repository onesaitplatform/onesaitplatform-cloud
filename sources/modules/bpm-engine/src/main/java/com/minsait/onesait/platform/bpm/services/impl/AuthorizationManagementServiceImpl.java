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
package com.minsait.onesait.platform.bpm.services.impl;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.bpm.services.AuthorizationManagementService;
import com.minsait.onesait.platform.config.components.AuthorizationLevel;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthorizationManagementServiceImpl implements AuthorizationManagementService {

	private static final String ASIGNEE_FILTER = "My Tasks";
	private static final String GROUP_TASKS_FILTER = "Group Tasks";
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ProcessEngine processEngine;

	private IdentityServiceImpl identityService;
	private AuthorizationService authorizationService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static final String ROOT_NODE_REALMS = "realms";

	@PostConstruct
	public void postInit() {
		identityService = (IdentityServiceImpl) processEngine.getIdentityService();
		authorizationService = processEngine.getAuthorizationService();
		// TO-DO multiple tenants
//		syncRealmMappings();
	}

	@Override
	public void syncRealmMappings() {
		try {
			final JsonNode config = getConfigMap();
			if (config != null) {
				config.get(ROOT_NODE_REALMS).fields().forEachRemaining(r -> {
					final JsonNode mappings = r.getValue();
					mappings.fields().forEachRemaining(m -> {
						final String roleName = m.getKey();
						final String authorizationLevel = m.getValue().asText();
						createGroup(roleName, authorizationLevel);
					});
				});
			}
		} catch (final Exception e) {
			log.error("Could not parse BPM_ROLE_MAPPING config", e);
		}

	}

	@Override
	public void createGroup(String groupName, String authorizationLevel) {
		try {
			createGroup(groupName, AuthorizationLevel.valueOf(authorizationLevel));
		} catch (final Exception e) {
			log.error("Invalid authorization level, allowed values are: {}", AuthorizationLevel.values(), e);
		}

	}

	@Override
	public void createGroup(String groupName, AuthorizationLevel authorizationLevel) {
		if (groupExists(groupName)) {
			if (log.isDebugEnabled()) {
				log.debug("Group {} already exists", groupName);
			}
			return;
		}
		switch (authorizationLevel) {
		case ADMIN:
			createAdminGroup(groupName);
			break;
		case OPERATOR:
			createDeveloperGroup(groupName);
			break;
		case OPERATOR_READ:
			createOperatorReadGroup(groupName);
			break;
		case TASK_WORKER:
		default:
			createUserGroup(groupName);
			break;
		}

	}

	private void createAdminGroup(String groupName) {
		final Group adminGroup = identityService.newGroup(groupName);
		adminGroup.setName(groupName);
		adminGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_SYSTEM);
		identityService.saveGroup(adminGroup);
		EnumSet.allOf(Resources.class).forEach(r -> {
			final Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId(ANY);
			authorization.setResource(r);
			authorizationService.saveAuthorization(authorization);
		});

	}

	private void createOperatorReadGroup(String groupName) {
		final Group developerGroup = identityService.newGroup(groupName);
		developerGroup.setName(groupName);
		developerGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
		identityService.saveGroup(developerGroup);
		Authorization authorization;
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceId("tasklist").count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ACCESS);
			authorization.setResourceId("tasklist");
			authorization.setResource(APPLICATION);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceId("cockpit").count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.setResourceId("cockpit");
			authorization.setResource(APPLICATION);
			authorization.addPermission(ACCESS);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.DEPLOYMENT)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.setResource(Resources.DEPLOYMENT);
			authorization.setResourceId(ANY);
			authorization.addPermission(READ);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.FILTER)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(READ);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName)
				.resourceType(Resources.PROCESS_DEFINITION).count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(READ);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.PROCESS_DEFINITION);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName)
				.resourceType(Resources.PROCESS_INSTANCE).count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(READ);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.PROCESS_INSTANCE);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.TASK)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(READ);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.TASK);
			authorizationService.saveAuthorization(authorization);
		}

	}

	private void createDeveloperGroup(String groupName) {
		final Group developerGroup = identityService.newGroup(groupName);
		developerGroup.setName(groupName);
		developerGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
		identityService.saveGroup(developerGroup);
		Authorization authorization;
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceId("tasklist").count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId("tasklist");
			authorization.setResource(APPLICATION);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceId("cockpit").count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.setResourceId("cockpit");
			authorization.setResource(APPLICATION);
			authorization.addPermission(ALL);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.DEPLOYMENT)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.setResource(Resources.DEPLOYMENT);
			authorization.setResourceId(ANY);
			authorization.addPermission(ALL);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.FILTER)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName)
				.resourceType(Resources.PROCESS_DEFINITION).count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.PROCESS_DEFINITION);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName)
				.resourceType(Resources.PROCESS_INSTANCE).count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.PROCESS_INSTANCE);
			authorizationService.saveAuthorization(authorization);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.TASK)
				.count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId(ANY);
			authorization.setResource(Resources.TASK);
			authorizationService.saveAuthorization(authorization);
		}
	}

	private void createUserGroup(String groupName) {
		final Group userGroup = identityService.newGroup(groupName);
		userGroup.setName(groupName);
		userGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
		identityService.saveGroup(userGroup);
		Authorization authorization;
		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceId("tasklist").count() == 0) {
			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			authorization.setGroupId(groupName);
			authorization.addPermission(ALL);
			authorization.setResourceId("tasklist");
			authorization.setResource(APPLICATION);
			authorizationService.saveAuthorization(authorization);
		}
		if (processEngine.getFilterService().createFilterQuery().filterName(GROUP_TASKS_FILTER).count() == 0) {
			final Map<String, Object> props = new HashMap<>();
			props.put("refresh", true);
			TaskQuery tq = processEngine.getTaskService().createTaskQuery()
					.taskCandidateGroupInExpression("${ currentUserGroups() }").active();
			Filter filter = processEngine.getFilterService().newTaskFilter();
			filter.setName(GROUP_TASKS_FILTER);
			filter.setQuery(tq);
			filter.setProperties(props);
			filter = processEngine.getFilterService().saveFilter(filter);

			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
			authorization.setUserId(ANY);
			authorization.setResourceId(filter.getId());
			authorization.addPermission(READ);
			authorization.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(authorization);

			tq = processEngine.getTaskService().createTaskQuery().taskAssigneeExpression("${ currentUser() }");
			filter = processEngine.getFilterService().newTaskFilter();
			filter.setName(ASIGNEE_FILTER);
			filter.setQuery(tq);
			filter.setProperties(props);
			filter = processEngine.getFilterService().saveFilter(filter);

			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
			authorization.setUserId(ANY);
			authorization.setResourceId(filter.getId());
			authorization.addPermission(READ);
			authorization.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(authorization);
		}

//		if (authorizationService.createAuthorizationQuery().groupIdIn(groupName).resourceType(Resources.FILTER)
//				.count() == 0) {
//			authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
//			authorization.setGroupId(groupName);
//			authorization.addPermission(Permissions.READ);
//			authorization.setResourceId(ANY);
//			authorization.setResource(Resources.FILTER);
//			authorizationService.saveAuthorization(authorization);
//		}
	}

	private boolean groupExists(String groupName) {
		if (identityService.createGroupQuery().groupId(groupName).count() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public JsonNode getConfigMap() {
		final List<Configuration> configs = configurationService.getConfigurations(Type.BPM_ROLE_MAPPING);
		if (!CollectionUtils.isEmpty(configs)) {
			if (configs.size() > 1) {
				log.error("More than one configuration found for BPM_ROLE_MAPPING, please keep just one configuration");
			} else {
				try {
					return MAPPER.readValue(configs.get(0).getYmlConfig(), JsonNode.class);

				} catch (final JsonProcessingException e) {
					log.error("Could not parse BPM_ROLE_MAPPING config", e);
				}
			}
		} else {
			log.info("No configurations found of type BPM_ROLE_MAPPING");
		}
		return null;
	}

	@Override
	public List<String> getTenants(String userId) {
		return identityService.createTenantQuery().userMember(userId).list().stream().map(Tenant::getId).toList();
	}

	@Override
	public List<String> getAllTenants() {
		return identityService.createTenantQuery().list().stream().map(Tenant::getId).toList();
	}

	@Override
	public void authorizeUserOnTenat(String userId, String tenantId) {
		identityService.createTenantUserMembership(tenantId, userId);
	}

	@Override
	public List<String> getUsersInTenant(String tenantId) {
		return identityService.createUserQuery().memberOfTenant(tenantId).list().stream().map(User::getId).toList();
	}

}
