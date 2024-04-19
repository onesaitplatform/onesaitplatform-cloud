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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.ConfigDBTenantConfig;
import com.minsait.onesait.platform.config.converters.MasterUserConverter;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.MigrationData.DataType;
import com.minsait.onesait.platform.config.model.MigrationData.Status;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectExport;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserExport;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.MigrationDataRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepository;

import avro.shaded.com.google.common.collect.Lists;
import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.Verjson;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("migrationService")
public class MigrationServiceImpl implements MigrationService {

	@PersistenceContext(unitName = ConfigDBTenantConfig.PERSISTENCE_UNIT_NAME_TENANT)
	private EntityManager entityManager;

	@Autowired
	private MigrationDataRepository repository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MasterUserRepository masterUserRepository;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private MasterUserConverter userToMasterConverter;

	// this is necessary to create a proxy for transactions
	@Autowired
	@Qualifier("migrationService")
	MigrationService selfReference;

	private final Verjson<DataFromDB> verjson;

	private final ObjectMapper mapper;

	private static final String ID_STR = " id: ";
	private static final String CLASS_STR = "class";
	private static final String FIELDNAME_STR = "fieldName";
	private static final String FIELDTYPE_STR = "fieldType";
	private static final String USER_EXPORT = "com.minsait.onesait.platform.config.model.UserExport";
	private static final String PROJECT = "com.minsait.onesait.platform.config.model.ProjectExport";
	private static final String FLOWDOMAIN = "com.minsait.onesait.platform.config.model.Flow";

	@Getter
	private final MigrationErrors exportErrors = new MigrationErrors();

	@Getter
	private final List<String> errors = new ArrayList<>();

	public MigrationServiceImpl() {
		verjson = Verjson.create(DataFromDB.class, new ImportExportVersions());

		mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
		module.addSerializer(SchemaFromDB.class, new SchemaFromDBJsonSerializer());
		module.addDeserializer(SchemaFromDB.class, new SchemaFromDBJsonDeserializer());
		mapper.registerModule(module);
	}

	@Override
	public ExportResult exportData(MigrationConfiguration config, Boolean isProject) throws IllegalAccessException {
		final DataFromDB data = new DataFromDB();
		final HashMap processedInstances = new HashMap<String, String>();
		MigrationErrors errors = new MigrationErrors();
		if (isProject) {
			Set<Serializable> ids = config.get(ProjectExport.class);
			errors = data.addObjectsProject(config, entityManager, processedInstances, ids.iterator().next());
		} else {
			errors = data.addObjects(config, entityManager, processedInstances);
		}
		final ExportResult result = new ExportResult(data, errors);
		return result;
	}

	@Override
	public String getJsonFromData(DataFromDB data) throws JsonProcessingException {
		return verjson.write(data);
	}

	@Override
	public DataFromDB getDataFromJson(String json)
			throws VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException {
		return verjson.read(json);
	}

	@Override
	public LoadEntityResult loadData(MigrationConfiguration config, DataFromDB data, Boolean override)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
		final DataToDB dataToDB = new DataToDB();

		return dataToDB.getEntitiesFromData(config, data, entityManager, override);

	}

	// Due to transactions, it has to be public because it has to be invoked using
	// the self reference proxy.
	@Override
	@Transactional(transactionManager = "transactionManager", propagation = Propagation.MANDATORY, noRollbackFor = {
			IllegalArgumentException.class })
	public MigrationError persistEntity(Object entity, Serializable id) {
		final Class<?> clazz = entity.getClass();
		Object storedObj = null;
		String identification = null;
		try {
			identification = MigrationUtils.getIdentificationField(entity);
		} catch (IllegalAccessException e1) {
		}
		try {
			storedObj = entityManager.merge(entity);
		} catch (Exception e) {
			Instance instance;
			try {
				instance = new Instance(clazz, id, identification, null);
			} catch (final IllegalArgumentException e2) {
				instance = new Instance(clazz, null, identification, null);
			}
			return new MigrationError(instance, null, MigrationError.ErrorType.ERROR, "Error persisting entity");
		}

		if (storedObj == null) {
			Instance instance;
			try {
				instance = new Instance(clazz, id, identification, null);
			} catch (final IllegalArgumentException e) {
				instance = new Instance(clazz, null, identification, null);
			}
			return new MigrationError(instance, null, MigrationError.ErrorType.ERROR, "Error persisting entity");
		} else {
			Instance instance;
			try {
				instance = new Instance(clazz, id, identification, null);
			} catch (final IllegalArgumentException e) {
				instance = new Instance(storedObj.getClass(), null, identification, null);
			}
			return new MigrationError(instance, null, MigrationError.ErrorType.INFO, "Entity Persisted");
		}

	}

	@Override
	@Transactional(transactionManager = "transactionManager", noRollbackFor = { IllegalArgumentException.class })
	public void persistData(List<Object> entities, MigrationErrors errors)
			throws NoSuchFieldException, IllegalAccessException {
		final Set<Object> processedEntities = new HashSet<>();
		final Set<Type<?>> managedTypes = new HashSet<>();
		managedTypes.addAll(entityManager.getMetamodel().getManagedTypes());
		LinkedList<Object> entitiesForTheNextStep = Lists.newLinkedList(entities);
		int iterationCount = 0;
		while (!entitiesForTheNextStep.isEmpty()) {
			iterationCount++;
			log.debug(
					"##### Entities to be persisted in round " + iterationCount + ": " + entitiesForTheNextStep.size());

			final List<Object> nextProcessingList = entitiesForTheNextStep;
			final Iterator<Object> it = nextProcessingList.iterator();
			entitiesForTheNextStep = new LinkedList<>();

			int count = 0;
			while (it.hasNext()) {
				count++;
				log.debug("####### Entity number: " + iterationCount + "-" + count);
				final Object entity = it.next();
				final Serializable id = MigrationUtils.getId(entity);
				final Class<?> entityClazz = entity.getClass();

				log.debug("Entity to process: " + entityClazz + ID_STR + id);
				if (!processedEntities.contains(entity) && !entitiesForTheNextStep.contains(entity)) {
					doPersistData(entity, entityClazz, id, managedTypes);
					log.debug("Entity to be persisted: " + id.toString());
					try {
						final EntityType<? extends Object> entityMetaModel;
						entityMetaModel = entityManager.getMetamodel().entity(entityClazz);
						final Set<?> attributes = entityMetaModel.getDeclaredSingularAttributes();

						for (final Object att : attributes) {
							final SingularAttribute<Object, Object> singularAtt = (SingularAttribute<Object, Object>) att;
							final String attName = singularAtt.getName();
							final Field declaredField = entityClazz.getDeclaredField(attName);
							if (declaredField.isAnnotationPresent(ManyToOne.class)) {
								// First we have to persist the parent and then the child
								final boolean accessible = declaredField.isAccessible();
								declaredField.setAccessible(true);
								final Object attObject = declaredField.get(entity);
								declaredField.setAccessible(accessible);

								if (entitiesForTheNextStep.contains(attObject)) {
									log.debug("Entity needs the parent: {}", attName);
									entitiesForTheNextStep.addLast(entity);
								}
							}
						}

						if (!entitiesForTheNextStep.contains(entity)) {
							final MigrationError msg = selfReference.persistEntity(entity, id);
							errors.addError(msg);
							processedEntities.add(entity);
						}
					} catch (final javax.persistence.EntityNotFoundException e) {
						entitiesForTheNextStep.addLast(entity);
						log.debug("Entity not found: {}", e.getMessage());
					}
				}

			}
		}

	}

	private void doPersistData(Object entity, Class<?> entityClazz, Serializable id, Set<Type<?>> managedTypes)
			throws NoSuchFieldException, IllegalAccessException {

		log.debug("Entity needs to be processed: " + entityClazz + ID_STR + id);
		final EntityType<? extends Object> entityMetaModel = entityManager.getMetamodel().entity(entityClazz);

		final Set<?> declaredSingularAttributes = entityMetaModel.getDeclaredSingularAttributes();
		for (final Object att : declaredSingularAttributes) {
			analyzeSingularAttribute(att, managedTypes, entity, entityClazz);
		}

		final Set<?> declaredPluralAttributes = entityMetaModel.getDeclaredPluralAttributes();
		for (final Object att : declaredPluralAttributes) {
			analyzePluralAttribute(att, managedTypes, entity, entityClazz);
		}

	}

	private void analyzeSingularAttribute(Object att, Set<Type<?>> managedTypes, Object entity, Class<?> entityClazz)
			throws NoSuchFieldException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		final SingularAttribute<Object, Object> singularAtt = (SingularAttribute<Object, Object>) att;
		final Type<Object> attType = singularAtt.getType();
		log.debug("\tSingular attribute to analyze: " + singularAtt.getName());
		if (managedTypes.contains(attType)) {
			final String attName = singularAtt.getName();
			final Field declaredField = entityClazz.getDeclaredField(attName);
			final boolean accessible = declaredField.isAccessible();
			declaredField.setAccessible(true);
			final Object attObject = declaredField.get(entity);
			declaredField.setAccessible(accessible);
			if (attObject != null) {
				final Serializable attObjectId = MigrationUtils.getId(attObject);
				log.debug("\t\tId of entity attribute: " + attObjectId.toString());
				Object attObjectInDB = entityManager.find(attObject.getClass(), attObjectId);

				if (attObjectInDB != null) {
					declaredField.setAccessible(true);
					declaredField.set(entity, attObjectInDB);
					declaredField.setAccessible(accessible);
				}
			}
		}
	}

	private void analyzePluralAttribute(Object att, Set<Type<?>> managedTypes, Object entity, Class<?> entityClazz)
			throws NoSuchFieldException, IllegalAccessException {
		final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) att;
		log.debug("\tPlural attribute to analyze: " + pluralAttribute.getName());
		if (managedTypes.contains(pluralAttribute.getElementType())) {
			final String attName = pluralAttribute.getName();
			final Field declaredField = entityClazz.getDeclaredField(attName);
			final boolean accessible = declaredField.isAccessible();
			declaredField.setAccessible(true);
			final Object attObject = declaredField.get(entity);
			declaredField.setAccessible(accessible);
			final Collection<?> collection = (Collection<?>) attObject;
			if (collection != null) {
				final Iterator<?> subIt = collection.iterator();
				while (subIt.hasNext()) {
					final Object subAtt = subIt.next();
					if (subAtt != null) {
						final Serializable subId = MigrationUtils.getId(subAtt);
						log.debug("\t\tId of entity pluralAttribute: " + subId.toString());
					}
				}
			}
		}
	}

	@Override
	public ExportResult exportAll() throws IllegalAccessException {
		final Set<ManagedType<?>> managedTypes = entityManager.getMetamodel().getManagedTypes();
		final MigrationConfiguration config = new MigrationConfiguration();
		for (final ManagedType<?> managedType : managedTypes) {
			final Class<?> javaType = managedType.getJavaType();
			// Exclude table APP
			final JpaRepository<?, Serializable> repository1 = MigrationUtils.getRepository(javaType, ctx);
			if (repository1 != null) {
				try {
					final List<?> entities = repository1.findAll();
					for (final Object entity : entities) {
						final Serializable id = MigrationUtils.getId(entity);
						config.add(entity.getClass(), id, null, null);
					}
				} catch (final Exception e) {
					errors.add(javaType.toString());
					log.warn("Table {} not exporting. {}", javaType, e);
				}
			}
		}
		return exportData(config, false);
	}

	@Override
	public ExportResult exportUser(User user) throws IllegalArgumentException, IllegalAccessException {
		final Set<ManagedType<?>> managedTypes = entityManager.getMetamodel().getManagedTypes();
		final MigrationConfiguration config = new MigrationConfiguration();
		final DataFromDB data = new DataFromDB();

		final HashMap visitedTypes = new HashMap<String, String>();
		visitedTypes.put(OPResource.class.getCanonicalName(), OPResource.class.getCanonicalName());

		for (final ManagedType<?> managedType : managedTypes) {
			final Class<?> javaType = managedType.getJavaType();

			if (null == visitedTypes.get(javaType.getCanonicalName())
					&& !config.getBlacklist().contains(javaType.getCanonicalName())) {
				visitedTypes.put(javaType.getCanonicalName(), javaType.getCanonicalName());

				if (dependsOnUser(javaType, new HashMap<String, String>())) {
					final JpaRepository<?, Serializable> repository = MigrationUtils.getRepository(javaType, ctx);
					if (repository != null) {
						try {
							final List<?> entities = repository.findAll();
							log.debug("*********** EXPORT:         " + javaType.getCanonicalName());
							for (final Object entity : entities) {

								if (isCandidateForUser(entity, user, new HashMap<String, String>(), config)) {

									trimEntity(entity, user, config, new HashMap<String, String>());

									if (isOwnedByUser(entity, user, new HashMap<String, String>(), config)) {
										log.debug("***********");
										final Serializable id = MigrationUtils.getId(entity);
										config.add(entity.getClass(), id, null, null);
									}
								}
							}
						} catch (final JpaSystemException e) {
							log.warn("Class {} not exported. Error: {}", javaType.getName(), e.getMessage());
						}
					}
				}
			}
		}

		// Add user
		final Serializable id = MigrationUtils.getId(user);
		config.addUser(user.getClass(), id);

		final ExportResult result = exportData(config, false);

		return result;
	}

	@Override
	public ExportResult exportUsers(List<String> users) throws IllegalArgumentException, IllegalAccessException {

		final MigrationConfiguration config = new MigrationConfiguration();

		for (final String userId : users) {
			final User user = userRepository.findByUserId(userId);
			config.addUser(user.getClass(), MigrationUtils.getId(user));
		}

		return exportData(config, false);
	}

	@Override
	public ExportResult exportProject(String projectName) throws IllegalAccessException {
		final Project project = projectRepository.findByIdentification(projectName).get(0);
		final MigrationConfiguration config = new MigrationConfiguration();

		final Class<?> javaType = project.getClass();
		try {
			final Serializable id = project.getId();
			config.addProject(project.getClass(), id, project.getIdentification());
		} catch (final Exception e) {
			errors.add(javaType.toString());
			log.warn("Project {} not exporting. {}", javaType, e);
		}
		return exportData(config, true);
	}

	private Object trimEntity(Object entity, User user, MigrationConfiguration config, HashMap visitedTypesForTrim) {
		try {
			if (null != entity) {
				visitedTypesForTrim.put(entity.getClass().getCanonicalName(), entity.getClass().getCanonicalName());
				for (final Field field : entity.getClass().getDeclaredFields()) {
					if (null == visitedTypesForTrim.get(field.getType().getCanonicalName())) {
						if (AuditableEntity.class.isAssignableFrom(field.getType())) {
							final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
							if (config.getTrimlist().contains(field.getType().getCanonicalName())) {
								pd.getWriteMethod().invoke(entity, new Object[] { null });
							} else {
								pd.getWriteMethod().invoke(entity, trimEntity(pd.getReadMethod().invoke(entity), user,
										config, visitedTypesForTrim));
							}
						} else if (Collection.class.isAssignableFrom(field.getType())) {
							final String className = ((Class) ((((java.lang.reflect.ParameterizedType) field
									.getGenericType()).getActualTypeArguments())[0])).getCanonicalName();

							final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
							if (config.getTrimlist().contains(className)) {
								pd.getWriteMethod().invoke(entity, new Object[] { null });
							} else {
								final Object fieldValue = pd.getReadMethod().invoke(entity);
								if (null != fieldValue) {
									final Set<Serializable> newValues = new HashSet<>();
									for (final Object fieldcollection : ((Set<Object>) fieldValue)) {
										if (isOwnedByUser(fieldcollection, user, visitedTypesForTrim, config)) {
											newValues.add((Serializable) trimEntity(fieldcollection, user, config,
													visitedTypesForTrim));
										}
									}
									pd.getWriteMethod().invoke(entity, new Object[] { newValues });
								}
							}
						}
					}
				}
			}
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return entity;
	}

	private boolean dependsOnUser(Class<?> javaType, HashMap<String, String> visitedTypes) {
		boolean depends = false;
		// The entity extends an user oriented class
		if (OPResource.class.isAssignableFrom(javaType)) {
			return true;
		}
		// For every attribute check
		for (final Field field : javaType.getDeclaredFields()) {
			// If the attribute extends an user oriented class or is the user atribute
			if (OPResource.class.isAssignableFrom(field.getType()) || field.getName().equals("user")) {
				return true;
				// If the attribute extends a model base class and not visited yet by
				// introspection
			} else if (AuditableEntity.class.isAssignableFrom(field.getType())
					&& (null == visitedTypes.get(field.getType().getCanonicalName()))) {
				visitedTypes.put(field.getType().getCanonicalName(), field.getType().getCanonicalName());
				depends = (depends || dependsOnUser(field.getType(), visitedTypes));
			}
		}
		return depends;
	}

	private boolean isCandidateForUser(Object entity, User user, HashMap<String, String> visitedTypes,
			MigrationConfiguration config) {
		boolean owned = true;
		// The entity extends an user oriented class has to be its owner
		try {
			if (OPResource.class.isAssignableFrom(entity.getClass())) {
				owned = ((null == entity.getClass().getMethod("getUser").invoke(entity))
						|| entity.getClass().getMethod("getUser").invoke(entity).equals(user));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			log.debug("No getUser method for: " + entity.getClass().getCanonicalName());
		}
		if (owned) {
			// For every attribute check if the related entities are owned by the same user
			for (final Field field : entity.getClass().getDeclaredFields()) {
				// If the attribute extends an user oriented class or is the user atribute
				// validate user
				try {
					if (field.getType().getCanonicalName().equals("com.minsait.onesait.platform.config.model.User")) {
						final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
						owned = owned && pd.getReadMethod().invoke(entity).equals(user);
					} else if (AuditableEntity.class.isAssignableFrom(field.getType())
							&& (null == visitedTypes.get(field.getType().getCanonicalName()))
							&& (!config.getWhitelist().contains(field.getType().getCanonicalName()))) {
						final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
						final Object fieldValue = pd.getReadMethod().invoke(entity);
						visitedTypes.put(field.getType().getCanonicalName(), field.getType().getCanonicalName());
						owned = (owned
								&& (fieldValue == null || isCandidateForUser(fieldValue, user, visitedTypes, config)));
					}
				} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | SecurityException | NullPointerException e) {
					owned = false;
				}
			}
		}
		return owned;
	}

	private boolean isOwnedByUser(Object entity, User user, HashMap<String, String> visitedTypes,
			MigrationConfiguration config) {
		boolean owned = true;
		// The entity extends an user oriented class has to be its owner
		try {
			if (OPResource.class.isAssignableFrom(entity.getClass())) {
				owned = entity.getClass().getMethod("getUser").invoke(entity).equals(user);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			log.debug("No getUser method for: " + entity.getClass().getCanonicalName());
		}
		if (owned) {
			// For every attribute check if the related entities are owned by the same user
			for (final Field field : entity.getClass().getDeclaredFields()) {
				// If the attribute extends an user oriented class or is the user atribute
				// validate user
				try {
					if (field.getType().getCanonicalName().equals("com.minsait.onesait.platform.config.model.User")) {
						final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
						owned = owned && pd.getReadMethod().invoke(entity).equals(user);
					} else if (AuditableEntity.class.isAssignableFrom(field.getType())
							&& (null == visitedTypes.get(field.getType().getCanonicalName()))
							&& (!config.getWhitelist().contains(field.getType().getCanonicalName()))) {
						final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
						final Object fieldValue = pd.getReadMethod().invoke(entity);
						visitedTypes.put(field.getType().getCanonicalName(), field.getType().getCanonicalName());
						owned = (owned
								&& (fieldValue == null || isOwnedByUser(fieldValue, user, visitedTypes, config)));
					} else if (Collection.class.isAssignableFrom(field.getType())) {
						final PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
						final Object fieldValue = pd.getReadMethod().invoke(entity);

						if ((null == visitedTypes.get(field.getType().getCanonicalName()))
								&& (!config.getWhitelist().contains(field.getType().getCanonicalName()))) {
							visitedTypes.put(field.getType().getCanonicalName(), field.getType().getCanonicalName());
							if (null != fieldValue) {
								for (final Object fieldcollection : ((Set<Object>) fieldValue)) {
									if (!config.getWhitelist()
											.contains(fieldcollection.getClass().getCanonicalName())) {
										owned = (owned && isOwnedByUser(fieldcollection, user, visitedTypes, config));
									}
								}
							}
						}
					}
				} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | SecurityException | NullPointerException e) {
					owned = false;
				}
			}
		}
		return owned;
	}

	@Override
	public MigrationConfiguration configImportAll(DataFromDB data) {
		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = data.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = data.getInstances(clazz);
			for (final Serializable id : instances) {
				config.add(clazz, id, null, null);
			}
		}
		return config;
	}

	@Override
	public MigrationErrors importData(MigrationConfiguration config, DataFromDB data, Boolean isProjectLoad,
			Boolean isUserLoad, Boolean override)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {

		try {

			if (!isProjectLoad && !isUserLoad) {

				final MigrationConfiguration configAux = new MigrationConfiguration();
				log.debug("User loading: First the user");
				List<String> users = new ArrayList<>();
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(USER_EXPORT)) {
						configAux.addUser(inst.getClazz(), inst.getId());
						users.add(inst.getId().toString());
					}
				}

				final MigrationErrors errors = doImportData(configAux, data, override);

				log.debug("User loading: Second the rest of the entities");
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(USER_EXPORT)) {
						config.removeClazz(clazz);
					}
				}
				errors.addAll(doImportData(config, data, override).getErrors());
				createMasterUsers(users);
				return errors;

			} else if (isProjectLoad) {

				// If is a project: first we have to persist the users, then the resource
				// entities and finally the
				// project itself to avoid persistence problems

				MigrationConfiguration configAux = new MigrationConfiguration();
				List<String> users = new ArrayList<>();
				log.debug("User loading: First the user");

				Map<Instance, List<String>> mapUsers = new HashMap<>();
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(USER_EXPORT)) {
						Map<String, Object> instanceData = data.getInstanceData(clazz, inst.getId());
						List<String> projects = new ArrayList<>((Collection<String>) instanceData.get("projects"));
						mapUsers.put(inst, projects);
						instanceData.put("projects", new ArrayList<>());
						configAux.addUser(inst.getClazz(), inst.getId());
						users.add(inst.getId().toString());
					}
				}

				final MigrationErrors errors = doImportData(configAux, data, true);

				configAux = new MigrationConfiguration();
				log.debug("Project loading: second the project");
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(PROJECT)) {
						configAux.addProject(inst.getClazz(), inst.getId(), inst.getIdentification());
					}
				}

				errors.addAll(doImportData(configAux, data, override).getErrors());

				for (Map.Entry<Instance, List<String>> entry : mapUsers.entrySet()) {
					Instance instance = entry.getKey();
					List<String> projects = entry.getValue();
					if (!projects.isEmpty()) {
						Map<String, Object> instanceData = data.getInstanceData(UserExport.class, instance.getId());
						instanceData.put("projects", projects);
						configAux = new MigrationConfiguration();
						configAux.addUser(instance.getClazz(), instance.getId());
					}
				}

				errors.addAll(doImportData(configAux, data, override).getErrors());

				configAux = new MigrationConfiguration();
				log.debug("Project loading: finally the resources");
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (!clazz.getName().equals(PROJECT) && !clazz.getName().equals(USER_EXPORT)) {
						configAux.addProject(inst.getClazz(), inst.getId(), inst.getIdentification());
					}
				}

				errors.addAll(doImportData(configAux, data, override).getErrors());

				createMasterUsers(users);

				return errors;
			} else if (isUserLoad) {
				// If is a user load: first we have to persist the user entity and then the
				// other entities to avoid persistence problems

				final MigrationConfiguration configAux = new MigrationConfiguration();
				log.debug("User loading: First the user");
				List<String> users = new ArrayList<>();
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(USER_EXPORT)) {
						configAux.addUser(inst.getClazz(), inst.getId());
						users.add(inst.getId().toString());
					}
				}

				final MigrationErrors errors = doImportData(configAux, data, override);

				log.debug("User loading: Second the rest of the entities");
				for (int i = 0; i < config.size(); i++) {
					final Instance inst = config.getInstance(i);
					final Class<?> clazz = inst.getClazz();
					if (clazz.getName().equals(USER_EXPORT)) {
						config.removeClazz(clazz);
					}
				}

				errors.addAll(doImportData(config, data, override).getErrors());
				createMasterUsers(users);
				return errors;
			}
		} catch (final Exception ex) {
			final MigrationErrors blockingErrors = new MigrationErrors();
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			ex.printStackTrace(printWriter);
			final String msg = stringWriter.toString();
			final MigrationError error = new MigrationError(Instance.NO_INSTANCE, null, MigrationError.ErrorType.ERROR,
					msg);
			blockingErrors.addError(error);
			return blockingErrors;
		}
		return null;
	}

	private void createMasterUsers(List<String> usersName) {
		List<User> users = new ArrayList<>();
		for (String u : usersName) {
			User user = userRepository.findByUserId(u);
			if (user != null)
				users.add(user);
		}

		users.stream().map(userToMasterConverter::convert).forEach(mu -> {
			try {
				mu.setTenant(tenantRepository.findByName(MultitenancyContextHolder.getTenantName()));
				masterUserRepository.save(mu);
			} catch (final Exception e) {
				log.warn("Update mode activated for multitenant, error while adding user {} {}", mu.getUserId(), e);
			}
		});
	}

	private MigrationErrors doImportData(MigrationConfiguration config, DataFromDB data, Boolean override)
			throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		final LoadEntityResult loadResult = loadData(config, data, override);
		final MigrationErrors errors = loadResult.getErrors();
		try {
			selfReference.persistData(Lists.newArrayList(loadResult.getAllObjects()), errors);
			return errors;
		} catch (final TransactionSystemException e) {
			/*
			 * This is a workaround to persist the data. Spring @Transactional mark the
			 * transaction as rollback only when any exception happens. Furthermore,
			 * RuntimeExceptions thrown by the EntityManager always have this behavior even
			 * caching them and mark them as noRollbackFor. Due to the order in which
			 * entities must be stored is unknown, if the first attempt to store the data
			 * fails, it is used to obtain the correct order, and the second one is used to
			 * persist the data following that order. The correct order can be obtained from
			 * the messages returned by the persistData. (variable erros).
			 */
			log.error("Error importing data", e);
			final Predicate<MigrationError> persistedEntitiesFilter = error -> error
					.getType() == MigrationError.ErrorType.INFO && "Entity Persisted".equals(error.getMsg());
			final List<MigrationError> instancesToBeProcessed = errors.getErrors(persistedEntitiesFilter);
			final List<Object> entitiesToBeProcessed = new ArrayList<>();
			final Map<Class<?>, Map<Serializable, Object>> entities = loadResult.getEntities();

			final Predicate<MigrationError> previousErrorsSelector = error -> (error
					.getType() != MigrationError.ErrorType.INFO);
			final List<MigrationError> previousErrors = errors.getErrors(previousErrorsSelector);

			for (final MigrationError error : instancesToBeProcessed) {

				final Instance instance = error.getProcessedInstance();

				final Object entity = entities.get(instance.getClazz()).get(instance.getId());
				entitiesToBeProcessed.add(entity);
			}

			final MigrationErrors newErrors = new MigrationErrors();
			newErrors.addAll(previousErrors);
			selfReference.persistData(entitiesToBeProcessed, newErrors);
			return newErrors;

		}
	}

	@Override
	public MigrationErrors importAll(DataFromDB data)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {

		final MigrationConfiguration config = configImportAll(data);
		// TODO
		return importData(config, data, false, false, false);

	}

	@Override
	public SchemaFromDB exportSchema() {
		final Set<ManagedType<?>> managedTypes = entityManager.getMetamodel().getManagedTypes();
		final SchemaFromDB schema = new SchemaFromDB();
		for (final ManagedType<?> managedType : managedTypes) {
			final Class<?> javaType = managedType.getJavaType();
			schema.addClass(javaType);
		}
		return schema;
	}

	@Override
	public String getJsonFromSchema(SchemaFromDB schema) throws JsonProcessingException {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
	}

	@Override
	public String compareSchemas(String currentSchemaJson, String otherSchemaJson) throws IOException {
		final SchemaFromDB currentSchema = mapper.readValue(currentSchemaJson, SchemaFromDB.class);
		final SchemaFromDB otherSchema = mapper.readValue(otherSchemaJson, SchemaFromDB.class);
		final ArrayNode diffs = mapper.createArrayNode();
		final Set<String> curentClasses = currentSchema.getClasses();
		for (final String className : curentClasses) {

			if (otherSchema.hasClazz(className)) {
				final Map<String, String> currentFields = currentSchema.getFields(className);
				final Map<String, String> otherFields = otherSchema.getFields(className);
				final ArrayNode changes = compareFields(currentFields, otherFields);
				if (changes.size() > 0) {
					// there are changes in the class
					final ObjectNode diff = mapper.createObjectNode();
					diff.put(CLASS_STR, className);
					diff.put("type", "class changed");
					diff.set("changes", changes);
					diffs.add(diff);
				}
			} else {
				// add class detected
				final ObjectNode diff = mapper.createObjectNode();
				final Map<String, String> currentFields = currentSchema.getFields(className);
				diff.put(CLASS_STR, className);
				diff.put("type", "class added");
				final ArrayNode fields = getFieldsAsArray(currentFields);
				diff.set("fields", fields);
				diffs.add(diff);
			}
		}

		final Set<String> otherClasses = otherSchema.getClasses();
		for (final String className : otherClasses) {
			if (!currentSchema.hasClazz(className)) {
				// remove class detected
				final ObjectNode diff = mapper.createObjectNode();
				final Map<String, String> otherFields = otherSchema.getFields(className);
				diff.put(CLASS_STR, className);
				diff.put("type", "class removed");
				final ArrayNode fields = getFieldsAsArray(otherFields);
				diff.set("fields", fields);
				diffs.add(diff);
			}
		}

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(diffs);
	}

	private ArrayNode getFieldsAsArray(Map<String, String> fields) {
		final Set<String> fieldNames = fields.keySet();
		final ArrayNode fieldsArray = mapper.createArrayNode();
		for (final String fieldName : fieldNames) {
			final String fieldClass = fields.get(fieldName);
			final ObjectNode field = mapper.createObjectNode();
			field.put(FIELDNAME_STR, fieldName);
			field.put(FIELDTYPE_STR, fieldClass);
			fieldsArray.add(field);
		}
		return fieldsArray;
	}

	private ArrayNode compareFields(Map<String, String> currentFields, Map<String, String> otherFields) {
		final Set<String> currentFieldNames = currentFields.keySet();
		final ArrayNode changes = mapper.createArrayNode();
		for (final String fieldName : currentFieldNames) {
			if (otherFields.containsKey(fieldName)) {
				final String currentFieldClass = currentFields.get(fieldName);
				final String otherFieldClass = otherFields.get(fieldName);
				if (!currentFieldClass.equals(otherFieldClass)) {
					// change field type detected
					final ObjectNode change = mapper.createObjectNode();
					change.put("type", "change");
					change.put(FIELDNAME_STR, fieldName);
					change.put(FIELDTYPE_STR, currentFieldClass);
					change.put("oldFieldType", otherFieldClass);
					changes.add(change);
				}
			} else {
				// add field detected
				final ObjectNode change = mapper.createObjectNode();
				change.put("type", "add");
				change.put(FIELDNAME_STR, fieldName);
				change.put(FIELDTYPE_STR, currentFields.get(fieldName));
				changes.add(change);
			}
		}

		final Set<String> otherFieldNames = otherFields.keySet();
		for (final String fieldName : otherFieldNames) {
			if (!currentFields.containsKey(fieldName)) {
				// remove field detected
				final ObjectNode change = mapper.createObjectNode();
				change.put("type", "remove");
				change.put(FIELDNAME_STR, fieldName);
				change.put(FIELDTYPE_STR, otherFields.get(fieldName));
				changes.add(change);
			}
		}
		return changes;
	}

	@Override
	public void storeMigrationData(User user, String name, String description, String fileName, byte[] file,
			DataType type, Status status) {
		final List<MigrationData> migrationData = repository.findByUserAndType(user, DataType.IMPORT);
		MigrationData fileForImport;

		if (migrationData != null && !migrationData.isEmpty()) {
			if (migrationData.size() > 1) {
				throw new IllegalStateException("There should be only one migration data per user");
			}
			fileForImport = migrationData.get(0);
		} else {
			fileForImport = new MigrationData();
		}

		fileForImport.setUser(user);
		fileForImport.setIdentification(name);
		fileForImport.setDescription(description);
		fileForImport.setFileName(fileName);
		fileForImport.setFile(file);
		fileForImport.setType(type);
		fileForImport.setStatus(status);
		repository.save(fileForImport);
	}

	@Override
	public void updateStoreMigrationData(User user, String json, DataType type) {
		final MigrationData migrationData = repository.findByUserAndTypeAndStatus(user, type, Status.IN_PROGRESS)
				.get(0);
		if (json != null) {
			migrationData.setStatus(Status.FINISHED);
			migrationData.setFile(json.getBytes());
		} else {
			migrationData.setStatus(Status.ERROR);
		}
		repository.save(migrationData);
	}

	@Override
	public MigrationData findMigrationData(User user, DataType type) {
		final List<MigrationData> migrationData = repository.findByUserAndType(user, type);
		if (migrationData != null && !migrationData.isEmpty()) {
			if (migrationData.size() > 1) {
				throw new IllegalStateException("There should be only one migration data per user");
			}
			return migrationData.get(0);
		}
		return null;
	}

	@Override
	public List<MigrationData> findByUserAndTypeAndStatus(User user, DataType type, Status status) {
		return repository.findByUserAndTypeAndStatus(user, type, status);
	}

	@Override
	public void deleteMigrationData(MigrationData data) {
		repository.delete(data);

	}
}
