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
import javax.persistence.OneToMany;

import org.apache.commons.collections4.map.LinkedMap;

import com.minsait.onesait.platform.config.model.AppChildExport;
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppRoleChildExport;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.ProjectExport;
import com.minsait.onesait.platform.config.model.UserExport;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataToDB {

	private static final String PROJECT = "com.minsait.onesait.platform.config.model.ProjectExport";
	private static final String PROJECT_RESOURCE_ACCESS = "com.minsait.onesait.platform.config.model.ProjectResourceAccessExport";
	private static final String WARN_MSG = "The entity is already in the database, nothing was done";

	private class EntityCache {
		private final Map<Class<?>, Map<Serializable, Object>> cache = new LinkedMap<>();

		private EntityCache() {

		}

		private EntityCache(EntityCache other) {
			final Set<Class<?>> clazzes = other.cache.keySet();
			for (final Class<?> clazz : clazzes) {
				final Map<Serializable, Object> entitiesCache = other.cache.get(clazz);
				final Set<Serializable> ids = entitiesCache.keySet();
				for (final Serializable id : ids) {
					add(clazz, id, other.get(clazz, id));
				}
			}
		}

		private void add(Class<?> clazz, Serializable id, Object entity) {
			if (!cache.containsKey(clazz)) {
				cache.put(clazz, new HashMap<Serializable, Object>());
			}
			if (MigrationConfiguration.idInsertable(clazz, id) && !cache.get(clazz).containsKey(id)) {
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

	public LoadEntityResult getEntitiesFromData(MigrationConfiguration config, DataFromDB data, EntityManager em,
			Boolean override)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {

		final MigrationErrors allErrors = new MigrationErrors();
		for (int i = 0; i < config.size(); i++) {
			final Instance inst = config.getInstance(i);
			final Class<?> clazz = inst.getClazz();
			final Serializable id = inst.getId();

			log.debug("Loading class from file: " + clazz.getName() + " id: " + id);

			if (!entities.exist(clazz, id)) {
				if (OPResource.class.isAssignableFrom(clazz) || clazz.getName().equals(PROJECT)) {
					List<Object> result = new ArrayList<>();
					if (!clazz.getSimpleName().equals("Api") && !clazz.getName().equals(PROJECT_RESOURCE_ACCESS)) {
						result = em
								.createQuery("SELECT c FROM " + clazz.getSimpleName()
										+ " c WHERE c.identification = :identification")
								.setParameter("identification", inst.getIdentification()).getResultList();
					} else if (clazz.getSimpleName().equals("Api")) {
						result = em
								.createQuery("SELECT c FROM " + clazz.getSimpleName()
										+ " c WHERE c.identification = :identification AND c.numversion = :numversion")
								.setParameter("identification", inst.getIdentification())
								.setParameter("numversion", inst.getVersion()).getResultList();
					}

					if (result.isEmpty() || !result.isEmpty() && override) {
						final Object dbInstance = em.find(clazz, id);
						if (dbInstance != null && !override) {
							allErrors.addError(new MigrationError(
									new Instance(clazz, id, inst.getIdentification(), inst.getVersion()), null,
									MigrationError.ErrorType.WARN, WARN_MSG));
						} else {
							final MigrationErrors entityErrors = new MigrationErrors();
							final EntityCache visited = new EntityCache();
							final Object instance = getEntityFromData(clazz, id, data, em, entityErrors, visited,
									config);
							allErrors.addErrors(entityErrors);
							if (instance instanceof MigrationError) {
								entitiesWithErrors.add(clazz, id, instance);
							} else {
								entities.add(clazz, id, instance);
							}
						}
					} else {
						allErrors.addError(
								new MigrationError(new Instance(clazz, id, inst.getIdentification(), inst.getVersion()),
										null, MigrationError.ErrorType.WARN, WARN_MSG));
					}
				} else {
					final List<Object> result = em
							.createQuery("SELECT c FROM " + clazz.getSimpleName() + " c WHERE c.id = :id")
							.setParameter("id", inst.getId()).getResultList();

					if (result.isEmpty() || !result.isEmpty() && override) {
						final Object dbInstance = em.find(clazz, id);
						if (dbInstance != null && !override) {
							allErrors.addError(new MigrationError(
									new Instance(clazz, id, inst.getIdentification(), inst.getVersion()), null,
									MigrationError.ErrorType.WARN, WARN_MSG));
						} else {
							if (clazz.equals(UserExport.class)) {
								final UserExport dbUser = (UserExport) dbInstance;
								if (dbUser != null) {
									final Map<String, Object> instanceData = data.getInstanceData(clazz, id);
									final List<String> projects = new ArrayList<>(
											(Collection<String>) instanceData.get("projects"));
									for (final ProjectExport p : dbUser.getProjects()) {
										if (!projects.contains(p.getId())) {
											projects.add(p.getId());
										}
									}
									instanceData.put("projects", projects);
								}
							}

							final MigrationErrors entityErrors = new MigrationErrors();
							final EntityCache visited = new EntityCache();
							final Object instance = getEntityFromData(clazz, id, data, em, entityErrors, visited,
									config);

							allErrors.addErrors(entityErrors);
							if (instance instanceof MigrationError) {
								entitiesWithErrors.add(clazz, id, instance);
							} else {
								entities.add(clazz, id, instance);
							}
						}
					} else {
						allErrors.addError(
								new MigrationError(new Instance(clazz, id, inst.getIdentification(), inst.getVersion()),
										null, MigrationError.ErrorType.WARN, WARN_MSG));
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
		if (clazz.equals(AppExport.class) && !entities.exist(AppExport.class, id)
				&& entities.exist(AppChildExport.class, id)) {
			return entities.get(AppChildExport.class, id);
		} else if (clazz.equals(AppRoleExport.class) && !entities.exist(AppRoleExport.class, id)
				&& entities.exist(AppRoleChildExport.class, id)) {
			return entities.get(AppRoleChildExport.class, id);
		}
		if (entitiesWithErrors.exist(clazz, id)) {
			return entitiesWithErrors.get(clazz, id);
		} else if (visited.exist(clazz, id)) {
			return visited.get(clazz, id);
		} else {
			Map<String, Object> instanceData = data.getInstanceData(clazz, id);
			if (instanceData == null && clazz.equals(AppExport.class)) {
				instanceData = data.getInstanceData(AppChildExport.class, id);
			} else if (instanceData == null && clazz.equals(AppChildExport.class)) {
				instanceData = data.getInstanceData(AppExport.class, id);
			}
			if (instanceData == null && clazz.equals(AppRoleExport.class)) {
				instanceData = data.getInstanceData(AppRoleChildExport.class, id);
			} else if (instanceData == null && clazz.equals(AppRoleChildExport.class)) {
				instanceData = data.getInstanceData(AppRoleExport.class, id);
			}

			clazz = data.getOPResourceClass(clazz, id);
			final Object instance = clazz.newInstance();
			final EntityCache newVisited = new EntityCache(visited);
			newVisited.add(clazz, id, instance);
			final Map<String, Field> allFields = MigrationUtils.getAllFields(clazz);
			final Set<String> unOrderedFields = instanceData.keySet();
			final List<String> orderedFields = orderFields(unOrderedFields, allFields, em);
			final String identification = instanceData.get("identification") != null
					? instanceData.get("identification").toString()
					: null;
			final String version = instanceData.get("numversion") != null ? instanceData.get("numversion").toString()
					: null;
			final Instance rootInstance = new Instance(clazz, id, identification, version);
			log.debug("### getEntityFromData ### Instance: {}", rootInstance.toString());
			for (final String fieldName : orderedFields) {
				final Field field = allFields.get(fieldName);
				if (!field.isAnnotationPresent(OneToMany.class)) {
					if (!Modifier.isFinal(field.getModifiers())) {
						final boolean accessible = field.isAccessible();
						field.setAccessible(true);
						final Object value = getCorrectValue(instanceData.get(fieldName), field.getType(),
								field.getGenericType(), data, em, errors, newVisited, config, rootInstance);
						if (value instanceof MigrationError) {
							final MigrationError errorReturned = (MigrationError) value;
							final Instance instanceThatNeedsIt = new Instance(clazz, id, identification, version);
							final MigrationError migrationError = new MigrationError(instanceThatNeedsIt,
									errorReturned.getNeededInstance(), MigrationError.ErrorType.ERROR,
									"The instance depends on a non available entity");
							errors.addError(migrationError);
							return migrationError;
						} else {
							field.set(instance, value);
						}

						field.setAccessible(accessible);
					}
				} else {
					log.debug("@OneToMany annotation.");
				}

			}
			return instance;
		}
	}

	private List<String> orderFields(Set<String> fields, Map<String, Field> allFields, EntityManager em) {
		final LinkedList<String> orderedFields = new LinkedList<>();
		for (final String fieldName : fields) {
			final Field field = allFields.get(fieldName);
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
			final Class<? extends Object> valueClazz = value.getClass();
			if (Serializable.class.isAssignableFrom(valueClazz)) {
				final Serializable sValue = (Serializable) value;

				if (MigrationUtils.isManagedEntity(em, fieldType)) {

					@SuppressWarnings("unchecked")
					final Object entity = em.find(fieldType, sValue);
					if (entity == null) {
						if (data.isDataStored(fieldType, sValue)) {
							// TODO test the possible collisions of get here entity in addition to do it in
							// the calling method.
							// analyze the impact of return a LoadResult with errors and list of entities.
							// This would allow to add
							// dependent entities.
							final Object entityFromData = getEntityFromData(fieldType, sValue, data, em, errors,
									visited, config);

							// this for cases in which not all data is in config, but there are dependencies
							// that are needed.
							// TODO implement a different way to avoid to pass the iterator
							if (!config.contains(entityFromData.getClass(), sValue)
									&& !entities.exist(entityFromData.getClass(), sValue)
									&& !entitiesWithErrors.exist(entityFromData.getClass(), sValue)) {

								final Instance instance = new Instance(fieldType, sValue,
										MigrationUtils.getIdentificationField(entity), null);
								config.add(instance);

								final MigrationError error = new MigrationError(rootInstance, instance,
										MigrationError.ErrorType.WARN,
										"Entity not configured added because other entity needs it");
								errors.addError(error);
							}
							return entityFromData;
						} else {

							final Instance instance = new Instance(fieldType, sValue,
									MigrationUtils.getIdentificationField(entity), null);
							final MigrationError error = new MigrationError(null, instance,
									MigrationError.ErrorType.ERROR, "Mandatory entity");
							errors.addError(error);
							return error;
						}
					}
					return entity;
				}
			}

			// Some types require transformations
			if (Date.class.isAssignableFrom(fieldType)) {
				final long time = (long) value;
				return new Date(time);
			} else if (ZonedDateTime.class.isAssignableFrom(fieldType)) {
				return ZonedDateTime.parse((String) value);
			} else if (byte[].class.equals(fieldType)) {
				final String string64 = (String) value;
				return Base64.getDecoder().decode(string64.getBytes(StandardCharsets.ISO_8859_1));
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				final Collection<?> collection = (Collection<?>) value;
				@SuppressWarnings("unchecked")
				final Collection<Object> newCollection = MigrationUtils.createCollection(fieldType);
				for (final Object obj : collection) {
					@SuppressWarnings("rawtypes")
					Class objClass;
					if (typeParameter != null) {
						objClass = Class.forName(typeParameter.getTypeName());
					} else {
						objClass = obj.getClass();
					}
					final Object correctValue = getCorrectValue(obj, objClass, null, data, em, errors, visited, config,
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
				final Object enumValue = Enum.valueOf(fieldType, (String) value);
				return enumValue;
			} else if (fieldType.isArray()) {
				// Arrays and collections are serialized equally.
				// At this point, if the data come from a json, arrays are deserialized as
				// Arraylists.
				// If data come from java objects directly they are Arrays. (Data did not pass
				// the serialization/deserialization process)
				if (value instanceof ArrayList) {
					final ArrayList<?> valueAsArrayList = (ArrayList<?>) value;
					final Class<?> parameterArrayClass = fieldType.getComponentType();
					final Object array = Array.newInstance(parameterArrayClass, valueAsArrayList.size());
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