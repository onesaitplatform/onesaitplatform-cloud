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
package com.minsait.onesait.platform.config.services.generic.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.SecurityServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SecurityServiceImpl implements SecurityService {

	private static final String ENTITY_USER_ACCESS = "%sUserAccess";
	private static final String USERACCESSREPOSITORYGETBYUSER = "findByUser";
	private static final String GETELEMENTFROMUSERACCESS = "get%s";
	private static final String GETELEMENTUSERACCESSFROMUSERACCESS = "get%sUserAccessType";
	private static final String GETIDFROMELEMENT = "getId";
	private static final String GETUSERFROMELEMENT = "getUser";
	private static final String GETNAMEFROMUSERACCESS = "getName";
	private static final String GETPUBLICFROMELEMENT = "isPublic";
	private static final String SETELEMENTUSERACCESSTYPE = "setAccessType";
	private static final String EDITSTR = "EDIT";
	private static final String VIEWSTR = "VIEW";
	private static final String RUNSTR = "RUN";
	private static final String METHOD_NOT_FOUND_ERROR = "Security service for {}, user: {}, method not found";
	private static final String METHOD_ILLEGAL_ACCESS_ERROR = "Security service for {}, user: {}, invoke method illegal access";
	private static final String METHOD_ILLEGAL_ARGUMENT_ERROR = "Security service for %s, user: %s, invoke method illegal argument";
	private static final String METHOD_INVOCATION_TARGET_ERROR = "Security service for %s, user: %s, invoke method invocation target";
	private static final String METHOD_ACCESS_DENIED_ERROR = "Security service for %s, user: %s, can't access to method";

	// On Ontologies we manage differentes permissions
	private static final String ONT_AUTH_ALL = "ALL";
	private static final String ONT_AUTH_QUERY = "QUERY";
	private static final String ONT_AUTH_INSERT = "INSERT";

	@Autowired
	@Lazy
	private OPResourceService resourceService;

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	@PersistenceContext
	EntityManager entityManager;

	private Class<?> getEntityClass(String entityName) {
		for (final EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
			if (entityName.equals(entity.getName())) {
				return entity.getJavaType();
			}
		}
		return null;
	}

	private void generateUserAccessMap(List<?> listEntitiesUserAccess, String type, Map<String, String> elemAccessMap)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		final Object entitiesUserAccess = listEntitiesUserAccess.get(0);
		final Method getRepositoryElementMethod = entitiesUserAccess.getClass()
				.getMethod(String.format(GETELEMENTFROMUSERACCESS, type));
		final Method getRepositoryAccessTypeMethod = entitiesUserAccess.getClass()
				.getMethod(String.format(GETELEMENTUSERACCESSFROMUSERACCESS, type));
		Object entity = getRepositoryElementMethod.invoke(entitiesUserAccess);
		Object entityUserAccess = getRepositoryAccessTypeMethod.invoke(entitiesUserAccess);
		final Method getEntityIdStr = entity.getClass().getMethod(GETIDFROMELEMENT);
		final Method getUserAccessTypeStr = entityUserAccess.getClass().getMethod(GETNAMEFROMUSERACCESS);

		for (final Object o : listEntitiesUserAccess) {
			entity = getRepositoryElementMethod.invoke(o);
			entityUserAccess = getRepositoryAccessTypeMethod.invoke(o);
			final String entityId = (String) getEntityIdStr.invoke(entity);
			final String accessType = (String) getUserAccessTypeStr.invoke(entityUserAccess);
			elemAccessMap.put(entityId, accessType);
		}
	}

	// Get the map element access type, if there isn't this entity return empty map
	private Map<String, String> getElementAccessTypeMapByUser(String type, User user) throws SecurityServiceException {
		final Map<String, String> elemAccessMap = new HashMap<>();
		final Class<?> entityUserAccessClass = getEntityClass(String.format(ENTITY_USER_ACCESS, type));
		if (entityUserAccessClass == null) {// Not entity UserAccess Found for element security so we skip this
			if (log.isDebugEnabled()) {
				log.debug("Not entity UserAccess found: {}, skipping this security control", String.format(ENTITY_USER_ACCESS, type));
			}			
		} else {
			final Repositories repositories = new Repositories(listableBeanFactory);
			Object repositoryUserAccessBean;
			try {
				final Optional<Object> optI = repositories.getRepositoryFor(entityUserAccessClass);
				if (!optI.isPresent())
					throw new SecurityServiceException(
							"No Repository found for " + entityUserAccessClass.getCanonicalName());
				repositoryUserAccessBean = optI.get();

				final Method method = repositoryUserAccessBean.getClass().getMethod(USERACCESSREPOSITORYGETBYUSER,
						User.class);
				final Object listEntitiesUserAccessObject = method.invoke(repositoryUserAccessBean, user);
				List<?> listEntitiesUserAccess;
				if (listEntitiesUserAccessObject instanceof List<?>) {
					listEntitiesUserAccess = (List<?>) listEntitiesUserAccessObject;
				} else {
					throw new SecurityServiceException(SecurityServiceException.Error.WRONG_INPUT_LIST,
							"Security service, wrong input list");
				}
				if (!listEntitiesUserAccess.isEmpty()) {
					generateUserAccessMap(listEntitiesUserAccess, type, elemAccessMap);
				}
			} catch (final NoSuchMethodException e) {
				log.error(METHOD_NOT_FOUND_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.METHOD_NOT_FOUND, e);
			} catch (final IllegalAccessException e) {
				log.error(METHOD_ILLEGAL_ACCESS_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			} catch (final IllegalArgumentException e) {
				log.error(METHOD_ILLEGAL_ARGUMENT_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			} catch (final InvocationTargetException e) {
				log.error(METHOD_INVOCATION_TARGET_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			}
		}
		return elemAccessMap;
	}

	private String getAccessTypeFromAccessMap(Map<String, String> meat, Map<String, ResourceAccessType> mrat,
			Object entity, User testUser, Method getUserFromElementMethod, Method getElementId,
			Method getRepositoryIsPublicMethod)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		final User elementUser = (User) getUserFromElementMethod.invoke(entity);
		final String id = (String) getElementId.invoke(entity);

		if (elementUser.getUserId().equals(testUser.getUserId())) {
			return EDITSTR;
		} else {
			final String meatAccessType = meat.get(id);
			final ResourceAccessType mratAccessType = mrat.get(id);

			if (meatAccessType != null) {
				switch (meatAccessType) {
				case EDITSTR:
					return EDITSTR;
				// On Ontologies authorizations calls ALL, INSERT and QUERY and not EDIT and
				// VIEW
				case ONT_AUTH_ALL:
					return ONT_AUTH_ALL;
				case ONT_AUTH_INSERT:
					return ONT_AUTH_INSERT;
				case VIEWSTR:
				default:
					break;
				}
			}
			if (mratAccessType != null && mratAccessType == ProjectResourceAccess.ResourceAccessType.MANAGE) {
				return EDITSTR;
			} else if (meatAccessType != null && meatAccessType.equals(RUNSTR)) {
				return RUNSTR;
			} else if ((mratAccessType != null && mratAccessType == ProjectResourceAccess.ResourceAccessType.VIEW)
					|| (meatAccessType != null && meatAccessType.equals(VIEWSTR))) {
				return VIEWSTR;
			}
			if (getRepositoryIsPublicMethod != null) {
				final Boolean isPublic = (Boolean) getRepositoryIsPublicMethod.invoke(entity);
				if (isPublic) {
					return VIEWSTR;
				}
			}
			if (meatAccessType != null) {
				return ONT_AUTH_QUERY;
			}
			return null;
		}
	}

	private List<String> generateIdsFromInputRawList(List<?> inputRawList, Method getElementId)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final List<String> lIds = new LinkedList<>();
		for (final Object resource : inputRawList) {
			lIds.add((String) getElementId.invoke(resource));
		}
		return lIds;
	}

	@Override
	public void setSecurityToInputList(List<?> inputRawList, User user, String type) throws SecurityServiceException {
		if (!inputRawList.isEmpty()) {
			// Get first element for getting set accesstype
			final Object felement = inputRawList.get(0);
			try {
				final Method setElementAccessType = felement.getClass().getMethod(SETELEMENTUSERACCESSTYPE,
						String.class);
				final Method getElementId = felement.getClass().getMethod(GETIDFROMELEMENT);
				final Method getUserFromElementMethod = felement.getClass().getMethod(GETUSERFROMELEMENT);
				Method getRepositoryIsPublicMethod = null;
				try {
					getRepositoryIsPublicMethod = felement.getClass()
							.getMethod(String.format(GETPUBLICFROMELEMENT, type));
				} catch (final NoSuchMethodException e) {
				}

				// Get all Project Resource access with map por contant access for all resources
				final Map<String, ResourceAccessType> mrat = resourceService
						.getResourcesAccessMapByUserAndResourceIdList(user,
								generateIdsFromInputRawList(inputRawList, getElementId));

				// Get all Element Authorization
				final Map<String, String> meat = getElementAccessTypeMapByUser(type, user);

				for (final Object o : inputRawList) {
					setElementAccessType.invoke(o, getAccessTypeFromAccessMap(meat, mrat, o, user,
							getUserFromElementMethod, getElementId, getRepositoryIsPublicMethod));
				}
			} catch (final NoSuchMethodException e) {
				log.error(METHOD_NOT_FOUND_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.METHOD_NOT_FOUND, e);
			} catch (final SecurityException e) {
				log.error(METHOD_ACCESS_DENIED_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.SECURITY_ERROR, e);
			} catch (final IllegalAccessException e) {
				log.error(METHOD_ILLEGAL_ACCESS_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			} catch (final IllegalArgumentException e) {
				log.error(METHOD_ILLEGAL_ARGUMENT_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			} catch (final InvocationTargetException e) {
				log.error(METHOD_INVOCATION_TARGET_ERROR, type, user.getUserId());
				throw new SecurityServiceException(SecurityServiceException.Error.INVOKE_METHOD_ERROR, e);
			}

		}
	}
}