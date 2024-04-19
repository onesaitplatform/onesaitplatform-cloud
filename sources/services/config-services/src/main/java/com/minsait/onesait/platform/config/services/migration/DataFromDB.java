/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.AppChildExport;
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppRoleChildExport;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.AppUserChildExport;
import com.minsait.onesait.platform.config.model.AppUserExport;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserExport;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataFromDB {

	Map<Class<?>, Map<Serializable, Map<String, Object>>> data = new HashMap<>();

	private final static String AUDIT_PREFIX = "Audit_";
	private static final String OP_RESOURCES = "com.minsait.onesait.platform.config.model.base.OPResource";
	private static final String PROJECT_EXPORT = "com.minsait.onesait.platform.config.model.ProjectExport";
	private static final String USER_EXPORT = "com.minsait.onesait.platform.config.model.UserExport";

	public DataFromDB() {

	}

	void addClass(Class<?> clazz) {
		if (!data.containsKey(clazz)) {
			data.put(clazz, new HashMap<>());
		}
	}

	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = data.keySet();
		return classes;
	}

	public Set<Serializable> getInstances(Class<?> clazz) {
		if (data.containsKey(clazz)) {
			Set<Serializable> instances = data.get(clazz).keySet();
			return instances;
		} else {
			return new HashSet<Serializable>();
		}
	}

	public Map<String, Object> getInstanceData(Class<?> clazz, Serializable id) {
		if (clazz.getName().equals(OP_RESOURCES)) {
			for (Map.Entry<Class<?>, Map<Serializable, Map<String, Object>>> entry : data.entrySet()) {
				if (OPResource.class.isAssignableFrom(entry.getKey())) {
					Map<Serializable, Map<String, Object>> dataForClass = entry.getValue();
					if (dataForClass.containsKey(id)) {
						return dataForClass.get(id);
					}
				}
			}
		}
		return data.get(clazz).get(id);
	}

	public Class<?> getOPResourceClass(Class<?> clazz, Serializable id) {
		if (clazz.getName().equals(OP_RESOURCES)) {
			for (Map.Entry<Class<?>, Map<Serializable, Map<String, Object>>> entry : data.entrySet()) {
				if (OPResource.class.isAssignableFrom(entry.getKey())) {
					Map<Serializable, Map<String, Object>> dataForClass = entry.getValue();
					if (dataForClass.containsKey(id)) {
						return entry.getKey();
					}
				}
			}
		}
		return clazz;
	}

	void addInstance(Class<?> clazz, Serializable id, Map<String, Object> instanceData) {
		addClass(clazz);

		Map<Serializable, Map<String, Object>> instances = data.get(clazz);
		if (!instances.containsKey(id)) {
			instances.put(id, instanceData);
		}
	}

	public MigrationErrors addObjects(MigrationConfiguration config, EntityManager em,
			HashMap<Serializable, Serializable> processedInstances)
			throws IllegalArgumentException, IllegalAccessException {
		MigrationErrors errors = new MigrationErrors();

		Set<Class<?>> types = config.getTypes();
		for (Class<?> type : types) {
			log.debug("*********** DBEXPORT:         " + type.getCanonicalName());
			Set<Serializable> ids = config.get(type);
			if (ids != null) {

				for (Serializable id : ids) {
					log.debug("*********** DBEXPORT:ID       " + id);
					Object entity = em.find(type, id);
					Instance instanceToExport = new Instance(type, id, null, null);
					if (entity == null || MigrationUtils.getId(entity) == null) {
						errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
								"The entity does not exist"));
					} else {
						instanceToExport = new Instance(type, id, MigrationUtils.getIdentificationField(entity), null);
						try {
							boolean insert = specificChecks(type, entity, errors, instanceToExport);
							if (insert) {
								if (processedInstances.get(id) == null) {
									processedInstances.put(id, id);
									Set<Instance> notFoundInstances = addEntity(entity, id, em);
									MigrationConfiguration newConfig = new MigrationConfiguration();

									for (Instance instanceNotFound : notFoundInstances) {
										if (!newConfig.add(instanceNotFound.getClazz(), instanceNotFound.getId(),
												instanceNotFound.getIdentification(), instanceNotFound.getVersion())) {
											errors.addError(new MigrationError(instanceToExport, instanceNotFound,
													MigrationError.ErrorType.WARN, "Entity needed but not exporterd"));
										}
									}
									// if previous errors were recorded based on dependencies with the entity
									// that it is stored, then they are removed.
									errors.removeRequired(instanceToExport);
									MigrationErrors additionalErrors = addObjects(newConfig, em, processedInstances);
									errors.addErrors(additionalErrors);
								}
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
									"There was an error exporting an entity"));
						}
					}
				}
			}
		}
		return errors;
	}

	public MigrationErrors addObjectsProject(MigrationConfiguration config, EntityManager em,
			HashMap<Serializable, Serializable> processedInstances, Serializable projectId)
			throws IllegalArgumentException, IllegalAccessException {
		MigrationErrors errors = new MigrationErrors();

		Set<Class<?>> types = config.getTypes();
		for (Class<?> type : types) {
			log.debug("*********** DBEXPORT:         " + type.getCanonicalName());
			Set<Serializable> ids = config.get(type);
			if (ids != null) {

				for (Serializable id : ids) {
					log.debug("*********** DBEXPORT:ID       " + id);

					Object entity = em.find(type, id);

					Instance instanceToExport = new Instance(type, id, null, null);
					if (entity == null || MigrationUtils.getId(entity) == null) {
						errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
								"The entity does not exist"));
					} else {
						instanceToExport = new Instance(type, id, MigrationUtils.getIdentificationField(entity), null);
						try {
							boolean insert = specificChecks(type, entity, errors, instanceToExport);
							if (insert) {
								if (processedInstances.get(id) == null) {
									processedInstances.put(id, id);
									Set<Instance> notFoundInstances = addEntityProject(entity, id, em, projectId);
									MigrationConfiguration newConfig = new MigrationConfiguration();

									for (Instance instanceNotFound : notFoundInstances) {
										if (!newConfig.addProject(instanceNotFound.getClazz(), instanceNotFound.getId(),
												instanceNotFound.getIdentification())) {
											errors.addError(new MigrationError(instanceToExport, instanceNotFound,
													MigrationError.ErrorType.WARN, "Entity needed but not exporterd"));
										}
									}
									// if previous errors were recorded based on dependencies with the entity
									// that it is stored, then they are removed.
									errors.removeRequired(instanceToExport);
									MigrationErrors additionalErrors = addObjectsProject(newConfig, em,
											processedInstances, projectId);
									errors.addErrors(additionalErrors);
								}
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.ERROR,
									"There was an error exporting an entity"));
						}
					}

				}
			}
		}
		return errors;
	}

	private boolean specificChecks(Class<?> type, Object entity, MigrationErrors errors, Instance instanceToExport) {
		if (Ontology.class.isAssignableFrom(type)) {
			Ontology ontology = (Ontology) entity;
			if (ontology.getIdentification().startsWith(AUDIT_PREFIX)) {
				errors.addError(new MigrationError(instanceToExport, null, MigrationError.ErrorType.WARN,
						"Ontology for Auditory was ignored"));
				return false;
			}
		}
		return true;
	}

	// TODO unify process for objects with the same behavior
	/**
	 * If the object already exist the values previously stored will be overridden.
	 * 
	 * @param o  object origin of the data
	 * @param id real id of the object.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws JsonProcessingException
	 */
	public Set<Instance> addEntity(Object o, Serializable id, EntityManager em)
			throws IllegalArgumentException, IllegalAccessException {
		Set<Instance> notFoundData = new HashSet<>();

		Class<?> clazz = o.getClass();
		Map<String, Field> fields = MigrationUtils.getAllFields(clazz);

		if (!fields.isEmpty()) {

			// If the type of object does not exist is created
			if (!data.containsKey(clazz)) {
				data.put(clazz, new HashMap<Serializable, Map<String, Object>>());
			}

			Map<Serializable, Map<String, Object>> obj = data.get(clazz);

			if (!obj.containsKey(id)) {
				obj.put(id, new HashMap<>());
			}
			Map<String, Object> attrs = obj.get(id);

			for (Field field : fields.values()) {
				Object value = processField(field, o, notFoundData);
				if (clazz.getCanonicalName().equals(USER_EXPORT) && field.getName().equals("projects")
						&& !value.toString().equals("[]")) {
					attrs.put(field.getName(), new ArrayList<>());
				} else {
					attrs.put(field.getName(), value);
				}
				if (clazz.equals(AppExport.class) && field.getName().equals("childApps")
						&& !value.toString().equals("[]")) {
					String childs = value.toString().substring(1, value.toString().length() - 1);
					for (int i = 0; i < childs.split(",").length; i++) {
						String childId = childs.split(",")[i];
						if (obj.containsKey(childId)) {
							obj.remove(childId);
							Object entity = em.find(AppChildExport.class, childId);
							addEntity(entity, childId, em);
						}
					}

				}
				if (clazz.equals(AppRoleExport.class) && field.getName().equals("childRoles")
						&& !value.toString().equals("[]")) {
					String childs = value.toString().substring(1, value.toString().length() - 1);
					for (int i = 0; i < childs.split(",").length; i++) {
						String childId = childs.split(",")[i];
						if (obj.containsKey(childId)) {
							obj.remove(childId);
							Object entity = em.find(AppRoleChildExport.class, childId);
							addEntity(entity, childId, em);
						}
					}
				}
				if (clazz.equals(AppUserExport.class) && field.getName().equals("appUsers")
						&& !value.toString().equals("[]")) {
					String childs = value.toString().substring(1, value.toString().length() - 1);
					for (int i = 0; i < childs.split(",").length; i++) {
						String childId = childs.split(",")[i];
						if (obj.containsKey(childId)) {
							obj.remove(childId);
							Object entity = em.find(AppUserChildExport.class, childId);
							addEntity(entity, childId, em);
						}
					}
				}
			}

			if (clazz.equals(Gadget.class)) {
				// Export table gadget_measure
				List<Object> result = em.createQuery("SELECT c FROM GadgetMeasure as c WHERE c.gadget.id = :id")
						.setParameter("id", id).getResultList();

				if (!result.isEmpty()) {
					for (Object r : result) {
						final Serializable idAux = MigrationUtils.getId(r);
						addEntityProject(r, idAux, em, null);
					}
				}
			}

			if (clazz.equals(UserExport.class) || clazz.equals(User.class)) {
				// Export table user_token
				List<Object> result = em.createQuery("SELECT c FROM UserToken as c WHERE c.user.userId = :userId")
						.setParameter("userId", id).getResultList();

				if (!result.isEmpty()) {
					for (Object r : result) {
						final Serializable idAux = MigrationUtils.getId(r);
						addEntityProject(r, idAux, em, null);
					}
				}
			}
		}
		return notFoundData;
	}

	public Set<Instance> addEntityProject(Object o, Serializable id, EntityManager em, Serializable projectId)
			throws IllegalArgumentException, IllegalAccessException {
		Set<Instance> notFoundData = new HashSet<>();

		Class<?> clazz = o.getClass();
		Map<String, Field> fields = MigrationUtils.getAllFields(clazz);

		if (!fields.isEmpty()) {

			// If the type of object does not exist is created
			if (!data.containsKey(clazz)) {
				data.put(clazz, new HashMap<Serializable, Map<String, Object>>());
			} else if (clazz.getCanonicalName().equals(PROJECT_EXPORT) && data.containsKey(clazz)) {
				return notFoundData;
			}

			Map<Serializable, Map<String, Object>> obj = data.get(clazz);

			if (!obj.containsKey(id)) {
				obj.put(id, new HashMap<>());
			}
			Map<String, Object> attrs = obj.get(id);

			for (Field field : fields.values()) {
				Object value = processField(field, o, notFoundData);
				if (clazz.getCanonicalName().equals(USER_EXPORT) && field.getName().equals("projects")
						&& !value.toString().equals("[" + projectId.toString() + "]")) {
					if (value.toString().contains(projectId.toString())) {
						List<String> ps = new ArrayList<>();
						ps.add(projectId.toString());
						attrs.put(field.getName(), ps);
					} else {
						attrs.put(field.getName(), new ArrayList<>());
					}

				} else {
					attrs.put(field.getName(), value);
				}

			}

			if (clazz.equals(Gadget.class)) {
				// Export table gadget_measure
				List<Object> result = em.createQuery("SELECT c FROM GadgetMeasure as c WHERE c.gadget.id = :id")
						.setParameter("id", id).getResultList();

				if (!result.isEmpty()) {
					for (Object r : result) {
						final Serializable idAux = MigrationUtils.getId(r);
						addEntityProject(r, idAux, em, projectId);
					}
				}
			}

			if (clazz.equals(Ontology.class)) {
				// Export ontology_virtual table
				Ontology ont = (Ontology) o;
				if (ont.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					List<Object> result = em
							.createQuery("SELECT c FROM OntologyVirtual as c WHERE c.ontologyId.id = :id")
							.setParameter("id", id).getResultList();
					if (!result.isEmpty()) {
						for (Object r : result) {
							final Serializable idAux = MigrationUtils.getId(r);
							addEntityProject(r, idAux, em, projectId);
						}
					}
				}

			}

			if (clazz.equals(UserExport.class) || clazz.equals(User.class)) {
				// Export table user_token
				List<Object> result = em.createQuery("SELECT c FROM UserToken as c WHERE c.user.userId = :userId")
						.setParameter("userId", id).getResultList();

				if (!result.isEmpty()) {
					for (Object r : result) {
						final Serializable idAux = MigrationUtils.getId(r);
						addEntityProject(r, idAux, em, null);
					}
				}
			}
		}
		return notFoundData;
	}

	public Set<Instance> addEntityProjectAccess(ProjectResourceAccess o, Serializable id, EntityManager em)
			throws IllegalArgumentException, IllegalAccessException {
		Set<Instance> notFoundData = new HashSet<>();

		Class<?> clazz = o.getClass();
		Map<String, Field> fields = MigrationUtils.getAllFields(clazz);

		if (!fields.isEmpty()) {

			// If the type of object does not exist is created
			if (!data.containsKey(clazz)) {
				data.put(clazz, new HashMap<Serializable, Map<String, Object>>());
			}

			Map<Serializable, Map<String, Object>> obj = data.get(clazz);

			if (!obj.containsKey(id)) {
				obj.put(id, new HashMap<>());
			}
			Map<String, Object> attrs = obj.get(id);

			for (Field field : fields.values()) {
				Object value = processField(field, o, notFoundData);
				// TODO if the field is final, it has to be exported and, in the
				// import phase, has to be checked if it is the same value. If different,
				// the import phase has to indicate a warning.
				// if(!Modifier.isFinal(field.getModifiers()))
				attrs.put(field.getName(), value);
			}
		}
		return notFoundData;
	}

	private Object processField(Field field, Object o, Set<Instance> notFoundData)
			throws IllegalArgumentException, IllegalAccessException {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			Object obj = field.get(o);

			// if the field does not have value the method returns null.
			if (obj == null) {
				return obj;
			}

			Serializable idField = MigrationUtils.getId(obj);
			Object dataToReturn;
			if (idField != null) {
				dataToReturn = idField;
				if (!isDataStored(field.getType(), idField)) {
					notFoundData.add(
							new Instance(field.getType(), idField, MigrationUtils.getIdentificationField(obj), null));
				}
			} else {
				if (Collection.class.isAssignableFrom(field.getType())) {
					Collection<?> collection = (Collection<?>) obj;
					List<Object> dataCollection = new ArrayList<Object>();
					for (Object member : collection) {
						Serializable memberId = MigrationUtils.getId(member);
						if (memberId != null) {
							dataCollection.add(memberId);
							notFoundData.add(new Instance(member.getClass(), memberId,
									MigrationUtils.getIdentificationField(member), null));
						} else {
							dataCollection.add(member);
						}
					}
					dataToReturn = dataCollection;
				} else if (Date.class.isAssignableFrom(field.getType())) {
					Date date = (Date) obj;
					dataToReturn = date.getTime();
				} else if (byte[].class.isAssignableFrom(field.getType())) {
					byte[] bytes = (byte[]) obj;
					byte[] encodedBytes = Base64.getEncoder().encode(bytes);
					String string64 = new String(encodedBytes, StandardCharsets.ISO_8859_1);
					dataToReturn = string64;
				} else {
					dataToReturn = obj;
				}
			}
			field.setAccessible(accessible);
			return dataToReturn;
		} catch (Exception e) {
			log.warn("Error exporting data {}", o);
			return null;
		}
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public boolean isDataStored(Class<?> clazz, Serializable id) {
		if (!clazz.getName().equals(OP_RESOURCES)) {
			if (clazz.equals(AppExport.class) && data.containsKey(clazz)) {
				Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
				if (!dataForClass.containsKey(id)) {
					dataForClass = data.get(AppChildExport.class);
					return dataForClass == null ? false : dataForClass.containsKey(id);
				} else {
					return dataForClass == null ? false : dataForClass.containsKey(id);
				}
			} else if (clazz.equals(AppChildExport.class) && data.containsKey(clazz)) {
				Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
				if (!dataForClass.containsKey(id)) {
					dataForClass = data.get(AppExport.class);
					return dataForClass == null ? false : dataForClass.containsKey(id);
				} else {
					return dataForClass == null ? false : dataForClass.containsKey(id);
				}
			} else if (clazz.equals(AppRoleExport.class) && data.containsKey(clazz)) {
				Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
				if (!dataForClass.containsKey(id)) {
					dataForClass = data.get(AppRoleChildExport.class);
					return dataForClass == null ? false : dataForClass.containsKey(id);
				} else {
					return dataForClass == null ? false : dataForClass.containsKey(id);
				}
			} else if (clazz.equals(AppRoleChildExport.class) && data.containsKey(clazz)) {
				Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
				if (!dataForClass.containsKey(id)) {
					dataForClass = data.get(AppRoleExport.class);
					return dataForClass == null ? false : dataForClass.containsKey(id);
				} else {
					return dataForClass == null ? false : dataForClass.containsKey(id);
				}
			} else if (data.containsKey(clazz)) {
				Map<Serializable, Map<String, Object>> dataForClass = data.get(clazz);
				return dataForClass == null ? false : dataForClass.containsKey(id);
			}
		} else if (clazz.getName().equals(OP_RESOURCES)) {
			for (Map.Entry<Class<?>, Map<Serializable, Map<String, Object>>> entry : data.entrySet()) {
				if (OPResource.class.isAssignableFrom(entry.getKey())) {
					Map<Serializable, Map<String, Object>> dataForClass = entry.getValue();
					if (dataForClass.containsKey(id)) {
						return true;
					}
				}
			}
		}

		return false;

	}
}