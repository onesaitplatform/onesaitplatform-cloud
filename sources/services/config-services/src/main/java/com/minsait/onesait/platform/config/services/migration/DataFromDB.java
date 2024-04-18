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
package com.minsait.onesait.platform.config.services.migration;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.minsait.onesait.platform.config.model.Ontology;

public class DataFromDB {

	Map<Class<?>, Map<Serializable, Map<String, Object>>> data = new HashMap<>();

	private static final String AUDIT_PREFIX = "Audit_";

	public DataFromDB() {
		super();
	}

	void addClass(Class<?> clazz) {
		if (!data.containsKey(clazz)) {
			data.put(clazz, new HashMap<>());
		}
	}

	public Set<Class<?>> getClasses() {
		return data.keySet();
	}

	public Set<Serializable> getInstances(Class<?> clazz) {
		return data.get(clazz).keySet();
	}

	public Map<String, Object> getInstanceData(Class<?> clazz, Serializable id) {
		return data.get(clazz).get(id);
	}

	void addInstance(Class<?> clazz, Serializable id, Map<String, Object> instanceData) {
		addClass(clazz);

		final Map<Serializable, Map<String, Object>> instances = data.get(clazz);
		if (!instances.containsKey(id)) {
			instances.put(id, instanceData);
		}
	}

	public MigrationErrors addObjects(MigrationConfiguration config, EntityManager em) {
		final MigrationErrors errors = new MigrationErrors();
		final Set<Class<?>> types = config.getTypes();

		types.forEach(type -> {
			final Set<Serializable> ids = config.get(type);
			if (ids != null) {
				addObjects(em, ids, errors, type);

			}
		});

		return errors;
	}

	private void addObjects(EntityManager em, Set<Serializable> ids, MigrationErrors errors, Class<?> type) {
		ids.forEach(id -> {
			final Object entity = em.find(type, id);
			final Instance instanceToExport = new Instance(type, id);
			try {

				if (entity == null || MigrationUtils.getId(entity) == null) {
					errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
							"The entity does not exist"));
				} else {

					final boolean insert = specificChecks(type, entity, errors, instanceToExport);
					if (insert) {
						final Set<Instance> notFoundInstances = addEntity(entity, id);
						final MigrationConfiguration newConfig = new MigrationConfiguration();

						notFoundInstances.forEach(instanceNotFound -> {
							if (!newConfig.add(instanceNotFound.getClazz(), instanceNotFound.getId())) {
								errors.addError(new MigrationError(instanceToExport, instanceNotFound,
										MigrationError.ErrorType.WARN, "Entity needed but not exporterd"));
							}
						});

						// if previous errors were recorded based on dependencies with the entity
						// that it is stored, then they are removed.
						errors.removeRequired(instanceToExport);
						final MigrationErrors additionalErrors = addObjects(newConfig, em);
						errors.addErrors(additionalErrors);
					}

				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
						"There was an error exporting an entity"));
			}

		});

	}

	private boolean specificChecks(Class<?> type, Object entity, MigrationErrors errors, Instance instanceToExport) {
		if (Ontology.class.isAssignableFrom(type)) {
			final Ontology ontology = (Ontology) entity;
			if (ontology.getIdentification().startsWith(AUDIT_PREFIX)) {
				errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.WARN,
						"Ontology for Auditory was ignored"));
				return false;
			}
		}
		return true;
	}

	/**
	 * If the object already exist the values previously stored will be overridden.
	 *
	 * @param o
	 *            object origin of the data
	 * @param id
	 *            real id of the object.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private Set<Instance> addEntity(Object o, Serializable id) throws IllegalAccessException {
		final Set<Instance> notFoundData = new HashSet<>();

		final Class<?> clazz = o.getClass();
		final Map<String, Field> fields = MigrationUtils.getAllFields(clazz);

		if (!fields.isEmpty()) {

			// If the type of object does not exist is created
			if (!data.containsKey(clazz)) {
				data.put(clazz, new HashMap<Serializable, Map<String, Object>>());
			}

			final Map<Serializable, Map<String, Object>> obj = data.get(clazz);

			if (!obj.containsKey(id)) {
				obj.put(id, new HashMap<>());
			}

			final Map<String, Object> attrs = obj.get(id);

			for (final Field field : fields.values()) {
				final Object value = processField(field, o, notFoundData);

				attrs.put(field.getName(), value);
			}
		}
		return notFoundData;
	}

	private Object processField(Field field, Object o, Set<Instance> notFoundData) throws IllegalAccessException {
		final boolean accessible = field.isAccessible();
		field.setAccessible(true);
		final Object obj = field.get(o);

		// if the field does not have value the method returns null.
		if (obj == null) {
			return obj;
		}

		final Serializable idField = MigrationUtils.getId(obj);
		final Object dataToReturn;
		if (idField != null) {
			dataToReturn = idField;
			if (!isDataStored(field.getType(), idField)) {
				notFoundData.add(new Instance(field.getType(), idField));
			}
		} else {
			dataToReturn = getDataToReturn(field, notFoundData, obj);
		}
		field.setAccessible(accessible);
		return dataToReturn;
	}

	private Object getDataToReturn(Field field, Set<Instance> notFoundData, Object obj) throws IllegalAccessException {
		Object dataToReturn;

		if (Collection.class.isAssignableFrom(field.getType())) {
			final Collection<?> collection = (Collection<?>) obj;
			final List<Object> dataCollection = new ArrayList<>();
			for (final Object member : collection) {
				final Object memberId = MigrationUtils.getId(member);
				if (memberId != null) {
					dataCollection.add(memberId);
					notFoundData.add(new Instance(member.getClass(), (String) memberId));
				} else {
					dataCollection.add(member);
				}
			}
			dataToReturn = dataCollection;
		} else if (Date.class.isAssignableFrom(field.getType())) {
			final Date date = (Date) obj;
			dataToReturn = date.getTime();
		} else if (byte[].class.isAssignableFrom(field.getType())) {
			final byte[] bytes = (byte[]) obj;
			final byte[] encodedBytes = Base64.getEncoder().encode(bytes);
			final String string64 = new String(encodedBytes, StandardCharsets.ISO_8859_1);
			dataToReturn = string64;
		} else {
			dataToReturn = obj;
		}

		return dataToReturn;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public boolean isDataStored(Class<?> clazz, Serializable id) {
		if (data.containsKey(clazz)) {
			final Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
			return dataForClass.containsKey(id);
		}

		return false;
	}
}
