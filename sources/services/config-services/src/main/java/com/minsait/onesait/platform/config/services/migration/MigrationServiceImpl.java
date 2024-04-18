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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityManager;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MigrationDataRepository;

import avro.shaded.com.google.common.collect.Lists;
import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.Verjson;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("migrationService")
public class MigrationServiceImpl implements MigrationService {

	@PersistenceContext(unitName = "onesaitPlatform")
	private EntityManager entityManager;

	@Autowired
	private MigrationDataRepository repository;

	@Autowired
	private ApplicationContext ctx;

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

	public MigrationServiceImpl() {
		verjson = Verjson.create(DataFromDB.class, new ImportExportVersions());

		mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
		module.addSerializer(SchemaFromDB.class, new SchemaFromDBJsonSerializer());
		module.addDeserializer(SchemaFromDB.class, new SchemaFromDBJsonDeserializer());
		mapper.registerModule(module);
	}

	@Override
	public ExportResult exportData(MigrationConfiguration config) throws IllegalAccessException {
		final DataFromDB data = new DataFromDB();
		data.addObjects(config, entityManager);
		return exportData(config);
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
	public LoadEntityResult loadData(MigrationConfiguration config, DataFromDB data)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
		final DataToDB dataToDB = new DataToDB();
		return dataToDB.getEntitiesFromData(config, data, entityManager);
	}

	// Due to transactions, it has to be public because it has to be invoked using
	// the self reference proxy.
	@Override
	@Transactional(transactionManager = "transactionManager", propagation = Propagation.MANDATORY, noRollbackFor = {
			IllegalArgumentException.class })
	public MigrationError persistEntity(Object entity, Serializable id) {
		final Class<?> clazz = entity.getClass();
		Object storedObj = null;

		storedObj = entityManager.merge(entity);

		if (storedObj == null) {
			Instance instance;
			try {
				instance = new Instance(clazz, id);
			} catch (final IllegalArgumentException e) {
				instance = new Instance(clazz, null);
			}
			return new MigrationError(instance, null, MigrationError.ErrorType.ERROR, "Error persisting entity");
		} else {
			Instance instance;
			try {
				instance = new Instance(clazz, id);
			} catch (final IllegalArgumentException e) {
				instance = new Instance(storedObj.getClass(), null);
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
						final MigrationError msg = selfReference.persistEntity(entity, id);
						errors.addError(msg);
						processedEntities.add(entity);
					} catch (final javax.persistence.EntityNotFoundException e) {
						entitiesForTheNextStep.addLast(entity);
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
				final Object attObjectInDB = entityManager.find(attObject.getClass(), attObjectId);
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
			final JpaRepository<?, Serializable> repository1 = MigrationUtils.getRepository(javaType, ctx);
			if (repository1 != null) {
				final List<?> entities = repository1.findAll();
				for (final Object entity : entities) {
					final Serializable id = MigrationUtils.getId(entity);
					config.add(entity.getClass(), id);
				}
			}
		}
		return exportData(config);
	}

	@Override
	public MigrationConfiguration configImportAll(DataFromDB data) {
		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = data.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = data.getInstances(clazz);
			for (final Serializable id : instances) {
				config.add(clazz, id);
			}
		}
		return config;
	}

	@Override
	public MigrationErrors importData(MigrationConfiguration config, DataFromDB data)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {

		try {
			return doImportData(config, data);
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
	}

	private MigrationErrors doImportData(MigrationConfiguration config, DataFromDB data)
			throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		final LoadEntityResult loadResult = loadData(config, data);
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
		return importData(config, data);

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
	public void storeMigrationData(User user, String name, String description, String fileName, byte[] file) {
		final List<MigrationData> migrationData = repository.findByUser(user);
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
		fileForImport.setName(name);
		fileForImport.setDescription(description);
		fileForImport.setFileName(fileName);
		fileForImport.setFile(file);
		repository.save(fileForImport);
	}

	@Override
	public MigrationData findMigrationData(User user) {
		final List<MigrationData> migrationData = repository.findByUser(user);
		if (migrationData != null && !migrationData.isEmpty()) {
			if (migrationData.size() > 1) {
				throw new IllegalStateException("There should be only one migration data per user");
			}
			return migrationData.get(0);
		}
		return null;
	}
}
