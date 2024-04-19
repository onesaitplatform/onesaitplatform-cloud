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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.ManagedType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance.StatusType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.Verjson;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Category(IntegrationTest.class)
public class MigrationServiceTest {

	@Autowired
	private MigrationService migrationService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private ClientPlatformInstanceRepository deviceRepository;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private EntityManager em;

	private File exportFile;
	private File importFile;
	private File importFileOldOntology;
	private File oldSchemaFile;
	private File currentSchemaFile;
	private File importDeviceFile;
	private File importClintPlatformFile;
	private File importClintPlatform2File;
	private File fullImportFile;

	@Before
	public void configFiles() {
		final ClassLoader classLoader = getClass().getClassLoader();
		exportFile = new File(classLoader.getResource("migration-examples/export.json").getFile());
		importFile = new File(classLoader.getResource("migration-examples/import.json").getFile());
		importDeviceFile = new File(classLoader.getResource("migration-examples/import-device.json").getFile());
		importClintPlatformFile = new File(
				classLoader.getResource("migration-examples/import-clientplatform.json").getFile());
		importClintPlatform2File = new File(
				classLoader.getResource("migration-examples/import-clientplatform2.json").getFile());
		fullImportFile = new File(classLoader.getResource("migration-examples/full-import.json").getFile());
		importFileOldOntology = new File(
				classLoader.getResource("migration-examples/import-oldOntology.json").getFile());
		oldSchemaFile = new File(classLoader.getResource("migration-examples/schema-old.json").getFile());
		currentSchemaFile = new File(classLoader.getResource("migration-examples/schema-current.json").getFile());
	}

	private ExportResult exportOneUserTwoOntologies() throws IllegalArgumentException, IllegalAccessException {

		User u = new User();
		u.setUserId("developer-test-migration");
		u.setPassword("changeIt!");
		u.setFullName("Developer of the Platform");
		u.setEmail("developer@onesaitplatform.com");
		u.setActive(true);
		u.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null));
		u = userRepository.save(u);

		Ontology ontology1 = new Ontology();
		ontology1.setId("ontology-1");
		ontology1.setJsonSchema("{}");
		ontology1.setIdentification("OntologyMaster-1");
		ontology1.setDescription("Ontology created as Master Data");
		ontology1.setActive(true);
		ontology1.setRtdbClean(true);
		ontology1.setRtdbToHdb(true);
		ontology1.setPublic(true);
		ontology1.setUser(u);
		ontology1.setAllowsCypherFields(false);
		ontology1 = ontologyRepository.save(ontology1);

		Ontology ontology2 = new Ontology();
		ontology2.setId("ontology-2");
		ontology2.setJsonSchema("{}");
		ontology2.setIdentification("OntologyMaster-2");
		ontology2.setDescription("Ontology created as Master Data");
		ontology2.setActive(true);
		ontology2.setRtdbClean(true);
		ontology2.setRtdbToHdb(true);
		ontology2.setPublic(true);
		ontology2.setUser(u);
		ontology2.setAllowsCypherFields(false);
		ontology2 = ontologyRepository.save(ontology2);

		final MigrationConfiguration config = new MigrationConfiguration();
		config.add(User.class, "developer-test-migration", null, null);
		config.add(Ontology.class, "ontology-1", "OntologyMaster-1", null);
		config.add(Ontology.class, "ontology-2", "OntologyMaster-2", null);

		final ExportResult result = migrationService.exportData(config, false);
		return result;
	}

	private ExportResult exportOneDevice() throws IllegalArgumentException, IllegalAccessException {

		final ClientPlatformInstance device = new ClientPlatformInstance();
		device.setAccesEnum(StatusType.OK);
		device.setConnected(true);
		device.setCreatedAt(new Date());
		device.setDisabled(false);
		device.setId("TEST-DEVICE");
		device.setIdentification("TEST-DEVICE");
		device.setJsonActions("nothing");
		device.setLocation(new double[] { 1.1, 2.1 });
		device.setProtocol("http");
		device.setStatus("ok");
		device.setTags("test");
		device.setUpdatedAt(new Date());
		deviceRepository.save(device);

		final MigrationConfiguration config = new MigrationConfiguration();
		config.add(ClientPlatformInstance.class, "TEST-DEVICE", "TEST-DEVICE", null);

		final ExportResult result = migrationService.exportData(config, false);
		return result;
	}

	@Test
	@Transactional
	@Ignore // this is a time consuming test, it is used just for development porpouse.
			// Therefore it is not normally executed.
	public void given_All_When_IAsk_Then_IHaveAll()
			throws IOException, VersionNotSupportedException, NamespaceMismatchException, ProcessStepException,
			IOReadException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		final byte[] encoded = Files.readAllBytes(Paths.get(fullImportFile.getAbsolutePath()));
		final String json = new String(encoded, StandardCharsets.UTF_8);
		final DataFromDB readData = migrationService.getDataFromJson(json);

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = readData.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = readData.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult result = migrationService.loadData(config, readData, false);

		final MigrationErrors errors = new MigrationErrors();
		migrationService.persistData(Lists.newArrayList(result.getAllObjects()), errors);

		// Includ here all the assert that you want
	}

	@Test
	@Transactional
	public void given_OneClientPlatformWithManyCircularDependencies_When_ItIsImported_ItIsAddedToTheDatabase()
			throws IOException, VersionNotSupportedException, NamespaceMismatchException, ProcessStepException,
			IOReadException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		final byte[] encoded = Files.readAllBytes(Paths.get(importClintPlatform2File.getAbsolutePath()));
		final String json = new String(encoded, StandardCharsets.UTF_8);
		final DataFromDB readData = migrationService.getDataFromJson(json);

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = readData.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = readData.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult result = migrationService.loadData(config, readData, false);

		final MigrationErrors errors = new MigrationErrors();
		migrationService.persistData(Lists.newArrayList(result.getAllObjects()), errors);

		final Predicate<MigrationError> infoErrorSelector = error -> error.getType() == MigrationError.ErrorType.INFO;
		final List<MigrationError> infoErrors = errors.getErrors(infoErrorSelector);

		assertTrue("There should be 18 info msgs", infoErrors.size() == 18);
	}

	// Clientplatform has circular relationship with Device using
	// ClientPlatformOntologies class in the middle.
	// Probably, this relationship could be done with a manytomany, but this is the
	// current model.
	@Test
	@Transactional
	public void given_OneClientPlatform_When_ItIsImported_ItIsAddedToTheDatabase()
			throws IOException, VersionNotSupportedException, NamespaceMismatchException, ProcessStepException,
			IOReadException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		final byte[] encoded = Files.readAllBytes(Paths.get(importClintPlatformFile.getAbsolutePath()));
		final String json = new String(encoded, StandardCharsets.UTF_8);
		final DataFromDB readData = migrationService.getDataFromJson(json);

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = readData.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = readData.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult result = migrationService.loadData(config, readData, false);

		final MigrationErrors errors = new MigrationErrors();
		migrationService.persistData(Lists.newArrayList(result.getAllObjects()), errors);

		final Predicate<MigrationError> infoErrorSelector = error -> error.getType() == MigrationError.ErrorType.INFO;
		final List<MigrationError> infoErrors = errors.getErrors(infoErrorSelector);

		assertTrue("There should be 9 info msgs", infoErrors.size() == 9);
	}

	@Test
	@Transactional
	public void given_AnExportedDevice_When_ItIsImported_ItIsAddedToTheDatabase()
			throws IOException, VersionNotSupportedException, NamespaceMismatchException, ProcessStepException,
			IOReadException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		final byte[] encoded = Files.readAllBytes(Paths.get(importDeviceFile.getPath()));
		final String json = new String(encoded, StandardCharsets.UTF_8);

		final DataFromDB readData = migrationService.getDataFromJson(json);

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = readData.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = readData.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult result = migrationService.loadData(config, readData, false);

		assertTrue("The device should be imported", result.getAllObjects().size() == 1);
	}

	@Test
	@Transactional
	public void given_OneDevice_When_ItIsSerializedAndDeserialized_Then_FieldValuesAreNotLost()
			throws IllegalArgumentException, IllegalAccessException, IOException, ClassNotFoundException,
			NoSuchFieldException, SecurityException, InstantiationException {
		final ExportResult result = exportOneDevice();

		deviceRepository.deleteById("TEST-DEVICE");

		final DataFromDB data = result.getData();

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = data.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = data.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult importResult = migrationService.loadData(config, data, false);

		final Map<Serializable, Object> devicesData = importResult.getEntities().get(ClientPlatformInstance.class);
		final Object deviceData = devicesData.get("TEST-DEVICE");

		final ClientPlatformInstance device = (ClientPlatformInstance) deviceData;

		assertTrue("The id of the device should be \"TEST-DEVICE\"", "TEST-DEVICE".equals(device.getId()));
	}

	@Test
	@Transactional
	public void given_OneUserAndTwoOntologiesOfSuchUser_When_IsRequestedToObtainData_Then_TheDataAreObtainedWithWarningsInDataSchema()
			throws JsonGenerationException, JsonMappingException, IOException, IllegalArgumentException,
			IllegalAccessException {
		final ExportResult result = exportOneUserTwoOntologies();
		assertTrue("There should be data exported", result.getData() != null);
		final Predicate<MigrationError> typeWarnings = error -> error.getType() == MigrationError.ErrorType.WARN;
		final int size = result.getErrors().getErrors(typeWarnings).size();
		assertTrue("There should be 1 warnings", size == 1);
	}

	// for testing serializer and deserializer for DataFromDB.
	@Test
	@Transactional
	public void given_OneExportResultWithOneUserAndTwoOntologies_When_ItIsSerializedAndDeserialized_Then_FieldValuesAreNotLost()
			throws IOException, IllegalArgumentException, IllegalAccessException {
		final ExportResult result = exportOneUserTwoOntologies();

		final ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
		module.addSerializer(DataFromDB.class, new DataFromDBJsonSerializer());
		module.addDeserializer(DataFromDB.class, new DataFromDBJsonDeserializer());
		mapper.registerModule(module);

		final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getData());

		final DataFromDB data = mapper.readValue(json, DataFromDB.class);

		final String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);

		assertTrue("Jsons should be equals after a serialization/desirialization/serialization process",
				json.equals(json2));
	}

	// to test services that use the serialization and deserialization
	@Test
	@Transactional
	public void given_OneExportResultWithOneUserAndTwoOntologies_When_ItIsTransformedInJsonAndRestauredAsData_Then_FieldValuesAreNotLost()
			throws IllegalArgumentException, IllegalAccessException, JsonProcessingException,
			VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException {
		final ExportResult result = exportOneUserTwoOntologies();
		final String json = migrationService.getJsonFromData(result.getData());

		final DataFromDB data = migrationService.getDataFromJson(json);

		final String json2 = migrationService.getJsonFromData(data);

		assertTrue("Json and Json2 should be equals", json.equals(json2));
	}

	//
	@Test
	@Transactional
	public void given_OneClassInTheBlackList_When_ItIsExported_Then_ItisIgnored()
			throws IllegalArgumentException, IllegalAccessException {

		final MigrationConfiguration config = new MigrationConfiguration();
		config.add(Role.class, "ROLE_DEVELOPER", null, null);
		assertTrue("Role can not be exported", config.getTypes().size() == 0);
	}

	@Test
	@Transactional
	public void given_OneOntologyInDB_When_ItIsPersistedOnDiskAndRestored_Then_TheEntityHasTheSameValuesThanInTheBegining()
			throws IllegalArgumentException, IllegalAccessException, IOException {
		final String user = "administrator";

		final MigrationConfiguration config = new MigrationConfiguration();
		config.add(Ontology.class, ontologyService.getOntologyByIdentification("GeoAirQuality", user).getId(),
				"GeoAirQuality", null);
		config.add(Ontology.class, ontologyService.getOntologyByIdentification("AirQuality", user).getId(),
				"AirQuality", null);
		final ExportResult result = migrationService.exportData(config, false);
		final String jsonOri = migrationService.getJsonFromData(result.getData());

		try (BufferedWriter out = Files.newBufferedWriter(Paths.get(exportFile.getPath()), StandardCharsets.UTF_8)) {
			out.write(jsonOri);
			out.flush();
		}

		final byte[] encoded = Files.readAllBytes(Paths.get(exportFile.getPath()));
		final String jsonRecovered = new String(encoded, StandardCharsets.UTF_8);

		assertTrue("Data should be equal after persit it on disk", jsonOri.equals(jsonRecovered));
	}

	@Test
	@Transactional
	public void given_OneNewOntologyAndOneUserInExportedFile_When_ItIsImportedIntoDBAndTheUserAlreadyExists_Then_TheOntologyIsStoredInDBAndTheUserIgnored()
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException {

		final byte[] encoded = Files.readAllBytes(Paths.get(importFile.getPath()));
		final String json = new String(encoded, StandardCharsets.UTF_8);

		final DataFromDB readData = migrationService.getDataFromJson(json);

		final MigrationConfiguration config = new MigrationConfiguration();
		final Set<Class<?>> classes = readData.getClasses();
		for (final Class<?> clazz : classes) {
			final Set<Serializable> instances = readData.getInstances(clazz);
			for (final Serializable instance : instances) {
				config.add(clazz, instance, null, null);
			}
		}

		final LoadEntityResult importDataIntoDB = migrationService.loadData(config, readData, false);
		final List<Object> allObjects = importDataIntoDB.getAllObjects();
		final Predicate<MigrationError> typeWarnings = error -> error.getType() == MigrationError.ErrorType.WARN;

		assertTrue("There should be 0 warnings", importDataIntoDB.getErrors().getErrors(typeWarnings).size() == 0);
		assertTrue("There should be 4 entities to persist", allObjects.size() == 4);

		final MigrationErrors errors = new MigrationErrors();
		migrationService.persistData(allObjects, errors);

		final Optional<User> user = userRepository.findById("TestingImportUser");
		assertTrue("TestingImportUser should be persisted", user.isPresent());
		assertTrue("TestingImportUser should be persisted", user.get().getUserId().equals("TestingImportUser"));

		final Optional<Ontology> ontology = ontologyRepository.findById("TestingImportOntology");
		assertTrue("TestingImportOntology should be persisted", ontology.isPresent());
		assertTrue("TestingImportOntology should be persisted", ontology.get().getId().equals("TestingImportOntology"));

		final Optional<Ontology> ontology2 = ontologyRepository.findById("TestingImportOntology2");
		assertTrue("TestingImportOntology2 should be persisted", ontology2.isPresent());
		assertTrue("TestingImportOntology2 should have one UserAccess",
				ontology2.get().getOntologyUserAccesses().size() == 1);
	}

	@Test
	@Transactional
	public void given_OneExportResult_When_TheExportedDataIsTransformedToJsonAndToJavaObjectSeveralTimes_Then_TheDataExportedIsNotChanged()
			throws IllegalArgumentException, IllegalAccessException, JsonProcessingException,
			VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException {
		final ExportResult result = exportOneUserTwoOntologies();
		final DataFromDB data = result.getData();

		final Verjson<DataFromDB> verjson = Verjson.create(DataFromDB.class, new ImportExportVersions());
		final String vjson = verjson.write(data);
		final DataFromDB vdata = verjson.read(vjson);

		final String vjson2 = verjson.write(vdata);
		assertTrue("vjson and vjson2 should be equals", vjson.equals(vjson2));
	}

	// the ontology to import does not have the field allowsCypherFields
	// the current model already have this field.
	// A transformation is required to assign a default value to all the imported
	// data
	@Test
	@Transactional
	public void given_AnExportedOntologyWithoutOneField_When_ItIsImported_Then_TheCorrectTransformationIsPerformed()
			throws IOException, VersionNotSupportedException, NamespaceMismatchException, ProcessStepException,
			IOReadException {

		final byte[] encoded = Files.readAllBytes(Paths.get(importFileOldOntology.getPath()));
		final String jsonRecovered = new String(encoded, StandardCharsets.UTF_8);

		final Verjson<DataFromDB> verjson = Verjson.create(DataFromDB.class, new TestVersions());
		final DataFromDB vdata = verjson.read(jsonRecovered);

		final Map<String, Object> instanceData = vdata.getInstanceData(Ontology.class, "TestingImportOntology");
		assertTrue("allowsCypherFields should exist after importing the data",
				(Boolean) instanceData.get("allowsCypherFields") == false);
	}

	@Test
	@Transactional
	public void given_OneConfigDBWithDefaultData_When_AllDataIsExported_Then_NoDataIsExported()
			throws IllegalArgumentException, IllegalAccessException {
		final Set<ManagedType<?>> managedTypes = em.getMetamodel().getManagedTypes();
		final MigrationConfiguration config = new MigrationConfiguration();
		for (final ManagedType<?> managedType : managedTypes) {
			final Class<?> javaType = managedType.getJavaType();
			final JpaRepository<?, Serializable> repository = MigrationUtils.getRepository(javaType, ctx);
			if (repository != null) {
				final List<?> entities = repository.findAll();
				for (final Object entity : entities) {
					final Serializable id = MigrationUtils.getId(entity);
					config.add(entity.getClass(), id, null, null);
				}
			}
		}

		final ExportResult result = migrationService.exportData(config, false);
		final DataFromDB data = result.getData();
		System.out.println("Classes to be exported in test");
		for (final Class<?> clazz : data.getClasses()) {
			System.out.println(clazz.getName());
			final Set<Serializable> instances = data.getInstances(clazz);
			for (final Serializable id : instances) {
				System.out.println("\t" + id);
			}
		}

		final int size = data.getClasses().size();
		assertTrue("A clean database should not produce anything to export", size == 0);
	}

	@Test
	@Transactional
	public void given_OnePlatformDeployed_When_TheSchemaIsExported_Then_TheSchemaIsExportedBasedOnTheJPAClasses() {
		final SchemaFromDB schema = migrationService.exportSchema();
		// We test that almost one class is exported.
		assertTrue("Ontology class schema should be exported", schema.getClasses().contains(Ontology.class.getName()));
		// We test that almost one field for the class is exported
		assertTrue("", schema.getFields(Ontology.class.getName()).get("id").equals("java.lang.String"));
	}

	@Test
	@Transactional
	public void given_OneSchema_When_ItIsSerializedAsJson_Then_AValidJsonSchemaIsGenerated() throws IOException {
		final SchemaFromDB schema = migrationService.exportSchema();
		final String json = migrationService.getJsonFromSchema(schema);
		final ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
		module.addSerializer(SchemaFromDB.class, new SchemaFromDBJsonSerializer());
		module.addDeserializer(SchemaFromDB.class, new SchemaFromDBJsonDeserializer());
		mapper.registerModule(module);

		final SchemaFromDB recoveredSchema = mapper.readValue(json, SchemaFromDB.class);
		final String recoveredJson = migrationService.getJsonFromSchema(recoveredSchema);
		assertTrue("The json serialization should not have size effects", json.equals(recoveredJson));
	}

	@Test
	@Transactional
	public void given_TwoDataSchemaVersions_When_TheyAreCompared_ThenTheDifferencesAreIdentified() throws IOException {
		final byte[] encodedOldSchema = Files.readAllBytes(Paths.get(oldSchemaFile.getPath()));
		final String oldSchemaJson = new String(encodedOldSchema, StandardCharsets.UTF_8);
		final byte[] encodedCurrentSchema = Files.readAllBytes(Paths.get(currentSchemaFile.getPath()));
		final String currentSchemaJson = new String(encodedCurrentSchema, StandardCharsets.UTF_8);

		final String diffs = migrationService.compareSchemas(currentSchemaJson, oldSchemaJson);

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode diffsNode = mapper.readTree(diffs);

		assertTrue("It should return an array of diffs", diffsNode.isArray());

		final ArrayNode diffsArray = (ArrayNode) diffsNode;
		final Iterator<JsonNode> elements = diffsArray.elements();

		while (elements.hasNext()) {
			final JsonNode element = elements.next();
			final JsonNode classNode = element.get("class");
			final String className = classNode.asText();
			final JsonNode typeNode = element.get("type");
			final String typeOfChange = typeNode.asText();
			switch (className) {
			case "com.minsait.onesait.platform.config.model.Ontology":
				assertTrue("The type of change must be 'class changed'", typeOfChange.equals("class changed"));
				final ArrayNode changes = (ArrayNode) element.get("changes");
				final Iterator<JsonNode> changesIt = changes.elements();
				while (changesIt.hasNext()) {
					final JsonNode change = changesIt.next();
					final String changeType = change.get("type").asText();
					final String fieldName = change.get("fieldName").asText();
					final String fieldType = change.get("fieldType").asText();
					switch (changeType) {
					case "add":
						assertTrue("Unexpected field name", fieldName.equals("new"));
						assertTrue("Unexpected field type", fieldType.equals("java.lang.String"));
						break;
					case "change":
						final String oldFieldType = change.get("oldFieldType").asText();
						assertTrue("Unexpected field name", fieldName.equals("allowsCreateTopic"));
						assertTrue("Unexpected field type", fieldType.equals("java.lang.String"));
						assertTrue("Unexpected old field type", oldFieldType.equals("boolean"));
						break;
					case "remove":
						assertTrue("Unexpected field name", fieldName.equals("topic"));
						assertTrue("Unexpected field type", fieldType.equals("java.lang.String"));
						break;
					default:
						fail("Invalid change type");
						break;
					}
				}
				break;
			case "com.minsait.onesait.platform.config.model.Api_v2":
				assertTrue("The type of change must be 'class added'", typeOfChange.equals("class added"));
				break;
			case "com.minsait.onesait.platform.config.model.Api":
				assertTrue("The type of change must be 'class removed'", typeOfChange.equals("class removed"));
				break;
			default:
				fail("The class: '" + className + "' is not expected");
				break;
			}
		}

	}

}
