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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataToDB {

	private class EntityCache {
		private Map<Class<?>, Map<Serializable, Object>> cache = new HashMap<>();

		private EntityCache() {

		}

		private EntityCache(EntityCache other) {
			Set<Class<?>> clazzes = other.cache.keySet();
			for (Class<?> clazz : clazzes) {
				Map<Serializable, Object> entitiesCache = other.cache.get(clazz);
				Set<Serializable> ids = entitiesCache.keySet();
				for (Serializable id : ids) {
					this.add(clazz, id, other.get(clazz, id));
				}
			}
		}

		private void add(Class<?> clazz, Serializable id, Object entity) {
			if (!cache.containsKey(clazz)) {
				cache.put(clazz, new HashMap<Serializable, Object>());
			}
			if (MigrationConfiguration.idInsertable(clazz, id)) {
				cache.get(clazz).put(id, entity);
			}
		}

		Object get(Class<?> clazz, Serializable id) {
			return cache.get(clazz).get(id);
		}

		boolean exist(Class<?> clazz, Serializable id) {
			return cache.containsKey(clazz) && cache.get(clazz).containsKey(id);
		}

		private Map<Class<?>, Map<Serializable, Object>> getAll() {
			return cache;
		}

	}

	EntityCache entities = new EntityCache();
	EntityCache entitiesWithErrors = new EntityCache();

	public LoadEntityResult getEntitiesFromData(MigrationConfiguration config, DataFromDB data, EntityManager em)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {

		MigrationErrors allErrors = new MigrationErrors();

		for (int i = 0; i < config.size(); i++) {
			Instance inst = config.getInstance(i);
			Class<?> clazz = inst.getClazz();
			Serializable id = inst.getId();

			log.debug("Loading class from file: " + clazz.getName() + " id: " + id);

			if (!entities.exist(clazz, id)) {
				if (OPResource.class.isAssignableFrom(clazz)
						|| clazz.getName().equals("com.minsait.onesait.platform.config.model.Project")) {
					List<Object> result = new ArrayList<>();
					if (!clazz.getSimpleName().equals("Api") && !clazz.getName()
							.equals("com.minsait.onesait.platform.config.model.ProjectResourceAccess")) {
						result = em
								.createQuery("SELECT c FROM " + clazz.getSimpleName()
										+ " c WHERE c.identification = :identification")
								.setParameter("identification", inst.getIdentification()).getResultList();
					} else {
						result = em
								.createQuery("SELECT c FROM " + clazz.getSimpleName()
										+ " c WHERE c.identification = :identification AND c.numversion = :numversion")
								.setParameter("identification", inst.getIdentification())
								.setParameter("numversion", inst.getVersion()).getResultList();
					}

					if (result.isEmpty()) {
						Object dbInstance = em.find(clazz, id);
						if (dbInstance != null) {
							allErrors.addError(new MigrationError(new Instance(clazz, id, null, null), null,
									MigrationError.ErrorType.WARN,
									"The entity is already in the database, nothing was done"));
						} else {
							MigrationErrors entityErrors = new MigrationErrors();
							EntityCache visited = new EntityCache();
							Object instance = getEntityFromData(clazz, id, data, em, entityErrors, visited, config);
							allErrors.addErrors(entityErrors);
							if (instance instanceof MigrationError) {
								entitiesWithErrors.add(clazz, id, instance);
							} else {
								entities.add(clazz, id, instance);
							}
						}
					} else {
						allErrors.addError(new MigrationError(new Instance(clazz, id, null, null), null,
								MigrationError.ErrorType.WARN,
								"The entity is already in the database, nothing was done"));
					}
				} else {
					List<Object> result = new ArrayList<>();
					result = em.createQuery("SELECT c FROM " + clazz.getSimpleName() + " c WHERE c.id = :id")
							.setParameter("id", inst.getId()).getResultList();

					if (result.isEmpty()) {
						Object dbInstance = em.find(clazz, id);
						if (dbInstance != null) {
							allErrors.addError(new MigrationError(new Instance(clazz, id, null, null), null,
									MigrationError.ErrorType.WARN,
									"The entity is already in the database, nothing was done"));
						} else {
							MigrationErrors entityErrors = new MigrationErrors();
							EntityCache visited = new EntityCache();
							Object instance = getEntityFromData(clazz, id, data, em, entityErrors, visited, config);
							allErrors.addErrors(entityErrors);
							if (instance instanceof MigrationError) {
								entitiesWithErrors.add(clazz, id, instance);
							} else {
								entities.add(clazz, id, instance);
							}
						}
					} else {
						allErrors.addError(new MigrationError(new Instance(clazz, id, null, null), null,
								MigrationError.ErrorType.WARN,
								"The entity is already in the database, nothing was done"));
					}
				}

			}
		}

		return new LoadEntityResult(entities.getAll(), allErrors);
	}

	private Object getEntityFromData(Class<?> clazz, Serializable id, DataFromDB data, EntityManager em,
			MigrationErrors errors, EntityCache visited, MigrationConfiguration config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// this stops the recursive calls in the case of cyclic references
		if (entities.exist(clazz, id)) {
			return entities.get(clazz, id);
		}
		if (entitiesWithErrors.exist(clazz, id)) {
			return entitiesWithErrors.get(clazz, id);
		} else if (visited.exist(clazz, id)) {
			return visited.get(clazz, id);
		} else {
			Map<String, Object> instanceData = data.getInstanceData(clazz, id);
			Object instance = clazz.newInstance();
			EntityCache newVisited = new EntityCache(visited);
			newVisited.add(clazz, id, instance);
			Map<String, Field> allFields = MigrationUtils.getAllFields(clazz);
			Set<String> unOrderedFields = instanceData.keySet();
			List<String> orderedFields = orderFields(unOrderedFields, allFields, em);
			Instance rootInstance = new Instance(clazz, id, null, null);
			for (String fieldName : orderedFields) {
				Field field = allFields.get(fieldName);
				if (!Modifier.isFinal(field.getModifiers())) {
					boolean accessible = field.isAccessible();
					field.setAccessible(true);
					Object value = getCorrectValue(instanceData.get(fieldName), field.getType(), field.getGenericType(),
							data, em, errors, newVisited, config, rootInstance);
					if (value instanceof MigrationError) {
						MigrationError errorReturned = (MigrationError) value;
						Instance instanceThatNeedsIt = new Instance(clazz, id, null, null);
						MigrationError migrationError = new MigrationError(instanceThatNeedsIt,
								errorReturned.getNeededInstance(), MigrationError.ErrorType.ERROR,
								"The instance depends on a non available entity");
						errors.addError(migrationError);
						return migrationError;
					} else {
						field.set(instance, value);
					}

					field.setAccessible(accessible);
				}
			}
			return instance;
		}
	}

	private List<String> orderFields(Set<String> fields, Map<String, Field> allFields, EntityManager em) {
		LinkedList<String> orderedFields = new LinkedList<>();
		for (String fieldName : fields) {
			Field field = allFields.get(fieldName);
			if (field != null) {
				if (Collection.class.isAssignableFrom(field.getType())) {
					orderedFields.addLast(fieldName);
				} else if (MigrationUtils.isManagedEntity(em, field.getType())) {
					orderedFields.addLast(fieldName);
				} else {
					orderedFields.addFirst(fieldName);
				}
			}
		}

		return orderedFields;
	}

	private Object getCorrectValue(Object value, @SuppressWarnings("rawtypes") Class fieldType, Type type,
			DataFromDB data, EntityManager em, MigrationErrors errors, EntityCache visited,
			MigrationConfiguration config, Instance rootInstance)
			throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		Type typeParameter = null;

		if (type != null && Collection.class.isAssignableFrom(fieldType)) {
			typeParameter = ((ParameterizedType) type).getActualTypeArguments()[0];
		}

		if (value != null) {
			Class<? extends Object> valueClazz = value.getClass();
			if (Serializable.class.isAssignableFrom(valueClazz)) {
				Serializable sValue = (Serializable) value;

				if (MigrationUtils.isManagedEntity(em, fieldType)) {

					@SuppressWarnings("unchecked")
					Object entity = em.find(fieldType, sValue);
					if (entity == null) {
						if (data.isDataStored(fieldType, sValue)) {
							// TODO test the possible collisions of get here entity in addition to do it in
							// the calling method.
							// analyze the impact of return a LoadResult with errors and list of entities.
							// This would allow to add
							// dependent entities.
							Object entityFromData = getEntityFromData(fieldType, sValue, data, em, errors, visited,
									config);

							// this for cases in which not all data is in config, but there are dependencies
							// that are needed.
							// TODO implement a different way to avoid to pass the iterator
							if (!config.contains(fieldType, sValue) && !entities.exist(fieldType, sValue)
									&& !entitiesWithErrors.exist(fieldType, sValue)) {

								Instance instance = new Instance(fieldType, sValue, null, null);
								config.add(instance);

								MigrationError error = new MigrationError(rootInstance, instance,
										MigrationError.ErrorType.WARN,
										"Entity not configured added because other entity needs it");
								errors.addError(error);
							}
							return entityFromData;
						} else {
							Instance instance = new Instance(fieldType, sValue, null, null);
							MigrationError error = new MigrationError(null, instance, MigrationError.ErrorType.ERROR,
									"Mandatory entity");
							errors.addError(error);
							return error;
						}
					}
					return entity;
				}
			}

			// Some types require transformations
			if (Date.class.isAssignableFrom(fieldType)) {
				long time = (long) value;
				return new Date(time);
			} else if (ZonedDateTime.class.isAssignableFrom(fieldType)) {
				return ZonedDateTime.parse((String) value);
			} else if (byte[].class.equals(fieldType)) {
				String string64 = (String) value;
				return Base64.getDecoder().decode(string64.getBytes(StandardCharsets.ISO_8859_1));
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				Collection<?> collection = (Collection<?>) value;
				@SuppressWarnings("unchecked")
				Collection<Object> newCollection = MigrationUtils.createCollection(fieldType);
				for (Object obj : collection) {
					@SuppressWarnings("rawtypes")
					Class objClass;
					if (typeParameter != null) {
						objClass = Class.forName(typeParameter.getTypeName());
					} else {
						objClass = obj.getClass();
					}
					Object correctValue = getCorrectValue(obj, objClass, null, data, em, errors, visited, config,
							rootInstance);
					if (correctValue instanceof MigrationError) {
						return correctValue;
					} else {
						newCollection.add(correctValue);
					}
				}

				return newCollection;

			} else if (fieldType.isEnum()) {
				@SuppressWarnings("unchecked")
				Object enumValue = Enum.valueOf(fieldType, (String) value);
				return enumValue;
			} else if (fieldType.isArray()) {
				// Arrays and collections are serialized equally.
				// At this point, if the data come from a json, arrays are deserialized as
				// Arraylists.
				// If data come from java objects directly they are Arrays. (Data did not pass
				// the serialization/deserialization process)
				if (value instanceof ArrayList) {
					ArrayList<?> valueAsArrayList = (ArrayList<?>) value;
					Class<?> parameterArrayClass = fieldType.getComponentType();
					Object array = Array.newInstance(parameterArrayClass, valueAsArrayList.size());
					for (int i = 0; i < valueAsArrayList.size(); i++) {
						Array.set(array, i, valueAsArrayList.get(i));
					}
					return array;
				} else {
					return fieldType.cast(value);
				}
			}
		}
		// If the value is not an entity, it is returned.
		return value;
	}

}
