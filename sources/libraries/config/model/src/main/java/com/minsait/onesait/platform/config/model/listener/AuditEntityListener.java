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
package com.minsait.onesait.platform.config.model.listener;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPPersistenceAuditEvent;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditEntityListener {

	private static final String BATCH_USER = "nouser_async";

	private static EventProducer eventProducer;

	private static final String SYS_ADMIN = "sysadmin";

	private final ObjectMapper mapper = new ObjectMapper();

	public static void initialize() {
		try {
			eventProducer = BeanUtil.getBean(EventProducer.class);
		} catch (final Exception e) {
			eventProducer = null;
		}
	}

	@PostRemove
	public void removeEntity(Object entity) {
		if (eventProducer != null) {
			final String className = entity.getClass().getSimpleName();
			final String user = getPrincipalName();
			log.trace("removed entity of class {} by user {}", className, user);

			final String message = "Removed entity of class " + className + " by user " + user;
			final Date today = new Date();
			final Table tab = entity.getClass().getAnnotation(Table.class);
			final String id = findIdFieldValue(entity.getClass(), entity);
			String entityPayload = null;
			try {
				entityPayload = mapper.writeValueAsString(entity);
			} catch (final Exception e) {
				//NO-OP
			}
			final OPPersistenceAuditEvent event = new OPPersistenceAuditEvent(message, UUID.randomUUID().toString(),
					EventType.SYSTEM, today.getTime(), null, SYS_ADMIN, null, OperationType.DELETE.name(),
					Module.PERSISTENCE, null, null, ResultOperationType.SUCCESS,
					MultitenancyContextHolder.getVerticalSchema(), MultitenancyContextHolder.getTenantName(), className,
					id, entityPayload, tab.name(), user);
			eventProducer.publish(event);
		}

	}

	@PostUpdate
	public void updateEntity(Object entity) {
		if (eventProducer != null) {
			final String className = entity.getClass().getSimpleName();
			final String user = getPrincipalName();
			log.trace("updated entity of class {} by user {}", className, user);

			final String message = "Updated entity of class " + className + " by user " + user;
			final Date today = new Date();
			final Table tab = entity.getClass().getAnnotation(Table.class);
			final String id = findIdFieldValue(entity.getClass(), entity);
			String entityPayload = null;
			try {
				entityPayload = mapper.writeValueAsString(entity);
			} catch (final Exception e) {
				//NO-OP
			}

			final OPPersistenceAuditEvent event = new OPPersistenceAuditEvent(message, UUID.randomUUID().toString(),
					EventType.SYSTEM, today.getTime(), null, SYS_ADMIN, null, OperationType.UPDATE.name(),
					Module.PERSISTENCE, null, null, ResultOperationType.SUCCESS,
					MultitenancyContextHolder.getVerticalSchema(), MultitenancyContextHolder.getTenantName(), className,
					id, entityPayload, tab.name(), user);
			eventProducer.publish(event);
		}
	}

	@PostPersist
	public void persistEntity(Object entity) {
		if (eventProducer != null) {
			final String className = entity.getClass().getSimpleName();
			final String user = getPrincipalName();
			log.trace("persisted entity of class {} by user {}", className, user);

			final String message = "Persisted entity of class " + className + " by user " + user;
			final Date today = new Date();
			final Table tab = entity.getClass().getAnnotation(Table.class);
			final String id = findIdFieldValue(entity.getClass(), entity);
			String entityPayload = null;
			try {
				entityPayload = mapper.writeValueAsString(entity);
			} catch (final Exception e) {
				//NO-OP
			}
			final OPPersistenceAuditEvent event = new OPPersistenceAuditEvent(message, UUID.randomUUID().toString(),
					EventType.SYSTEM, today.getTime(), null, SYS_ADMIN, null, OperationType.INSERT.name(),
					Module.PERSISTENCE, null, null, ResultOperationType.SUCCESS,
					MultitenancyContextHolder.getVerticalSchema(), MultitenancyContextHolder.getTenantName(), className,
					id, entityPayload, tab.name(), user);
			eventProducer.publish(event);
		}
	}

	@SuppressWarnings("rawtypes")
	public String findIdFieldValue(Class clazz, Object entity) {
		final int maxSuperclassIterations = 3;
		String id = null;
		int iterations = 0;
		boolean foundId = false;
		Class currentClass = clazz;
		while (!foundId && iterations < maxSuperclassIterations
				&& !currentClass.getSuperclass().getClass().equals(Object.class)) {
			final Field[] fields = currentClass.getDeclaredFields();
			for (final Field f : fields) {
				if (f.getAnnotation(Id.class) != null) {
					try {
						foundId = true;
						f.setAccessible(true);
						id = (String) f.get(entity);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("Error while extracting id for audit");
					}
				}
			}
			if (!foundId) {
				iterations++;
				currentClass = currentClass.getSuperclass();
			}

		}
		return id;
	}

	public String getPrincipalName() {
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null) {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		} else {
			return BATCH_USER;
		}
	}

}
