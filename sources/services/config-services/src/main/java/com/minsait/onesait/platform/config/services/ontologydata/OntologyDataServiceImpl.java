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
package com.minsait.onesait.platform.config.services.ontologydata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.ContextData;
import com.minsait.onesait.platform.commons.security.BasicEncryption;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyDataServiceImpl implements OntologyDataService {

	public enum EncryptionOperations {
		ENCRYPT, DECRYPT
	}

	@Autowired
	private OntologyRepository ontologyRepository;
	
    @Autowired
	private ConfigurationRepository configurationRepository;

    @Autowired
    ConfigurationService configurationService;    
    
    @Autowired
    EventProducer auditableAscpect;

    private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
    
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final ConcurrentHashMap<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

	final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

	private static final String ENCRYPT_PROPERTY = "encrypted";
	private static final String ENCRYPT_WORD = "$ENCRYPT(";
	private static final String DELIMITER = ")";
	private static final String ESCAPE_CHAR = "\\";
	private static final String REGEX = "(?<!" + Pattern.quote(ESCAPE_CHAR) + ")" + Pattern.quote(DELIMITER);
	private static final String OBJECT_TYPE = "object";
	private static final String ARRAY_TYPE = "array";
	private static final String TYPE_WORD = "type";
	private static final String SLASH = "/";

	// This is a basic functionality, it has to be improved. For instance,
	// initVector should be random. Review AES best practices to improve this class.
	private static final String KEY = "Bar12345Bar12345"; // 128 bit key
	private static final String INIT_VECTOR = "RandomInitVector"; // 16 bytes IV
	private static final String PROP_STR = "properties";
	private static final String JSON_ERROR = "Error working with JSON data";
	private static final String REQ_STR = "required";
	private static final String REQ_PROP_STR = "Required properties of new schema do not match old schema";
	private static final String REQ_SAME_SCH = "Schema can't be modified in this type of ontology, please delete all data and then change it";

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneId.of("UTC"));

	final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void checkOntologySchemaCompliance(final JsonNode data, final Ontology ontology) {

		try {
			String ontologySchema = ontologyRepository.getSchemaAsJsonNode(ontology);

			MessageDigest md = MessageDigest.getInstance("SHA-1");
			String sha1 = new String(md.digest(ontologySchema.getBytes(StandardCharsets.UTF_8)));

			JsonSchema jsonSchema = schemaCache.get(sha1);
			if (jsonSchema == null) {
				jsonSchema = createJsonSchema(ontologySchema);
				schemaCache.put(sha1, jsonSchema);
			}
			checkJsonCompliantWithSchema(data, jsonSchema);
		} catch (final IOException e) {
			throw new DataSchemaValidationException("Error reading data for checking schema compliance", e);
		} catch (final ProcessingException e) {
			throw new DataSchemaValidationException("Error checking data schema compliance", e);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("No SHA-1 algoritm was found");
		}
	}

	private JsonSchema createJsonSchema(String schema)
			throws ProcessingException, JsonProcessingException, IOException {
		final JsonNode jsonSchemaNode = mapper.readTree(schema);
		final JsonSchemaFactory factoryJson = JsonSchemaFactory.byDefault();
		return factoryJson.getJsonSchema(jsonSchemaNode);
	}

	void checkJsonCompliantWithSchema(final JsonNode data, final JsonSchema schema) throws ProcessingException {

		try {
			final ProcessingReport report = schema.validate(data);
			if (report != null && !report.isSuccess()) {
				final Iterator<ProcessingMessage> it = report.iterator();
				final StringBuilder msgerror = new StringBuilder();
				while (it.hasNext()) {
					final ProcessingMessage msg = it.next();
					if (msg.getLogLevel().equals(LogLevel.ERROR)) {
						msgerror.append(msg.asJson());
					}
				}

				throw new DataSchemaValidationException(
						"Error processing data:" + data.toString() + "by:" + msgerror.toString());
			}

		} catch (final DataSchemaValidationException e) {
			throw e;
		} catch (final Exception e) {
			log.error("", e);
			throw new DataSchemaValidationException(
					"Error processing data:" + data.toString() + "by:" + e.getMessage());
		}
	}

	void checkJsonCompliantWithSchema(final String dataString, final String schemaString) {
		JsonNode dataJson;

		try {
			dataJson = JsonLoader.fromString(dataString);
			JsonSchema schema = createJsonSchema(schemaString);
			checkJsonCompliantWithSchema(dataJson, schema);

		} catch (final IOException e) {
			throw new DataSchemaValidationException("Error reading data for checking schema compliance", e);
		} catch (final ProcessingException e) {
			throw new DataSchemaValidationException("Error checking data schema compliance", e);
		}
	}

	String addContextData(final OperationModel operationModel, JsonNode data) throws IOException {

		final String body = operationModel.getBody();
		final String user = operationModel.getUser();
		final String clientConnection = operationModel.getClientConnection();
		final String deviceTemplate = operationModel.getDeviceTemplate();
		final String device = operationModel.getDevice();
		final String clientSession = operationModel.getClientSession();
		final String source = operationModel.getSource().name();

		final String timezoneId = ZoneId.of("UTC").toString();
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		final String timestamp = now.format(formatter);

		final long timestampMillis = System.currentTimeMillis();
		final ContextData contextData = ContextData.builder(user, timezoneId, timestamp, timestampMillis, source)
				.clientConnection(clientConnection).deviceTemplate(deviceTemplate).device(device)
				.clientSession(clientSession).build();

		final JsonNode jsonBody;
		if (data == null)
			jsonBody = objectMapper.readTree(body);
		else
			jsonBody = data;
		if (jsonBody.isObject()) {
			final ObjectNode nodeBody = (ObjectNode) jsonBody;
			nodeBody.set("contextData", objectMapper.valueToTree(contextData));
			return objectMapper.writeValueAsString(nodeBody);
		} else {
			throw new IllegalStateException("Body should have a valid json object");
		}

	}

	String encryptionOperationAllowingArrays(String data, Ontology ontology, EncryptionOperations operation)
			throws IOException {
		if (ontology.isAllowsCypherFields()) {
			final JsonNode jsonData = objectMapper.readTree(data);
			if (jsonData.isArray()) {
				ArrayNode newArray = mapper.createArrayNode();
				for (JsonNode arrayElement : jsonData) {
					newArray.add(encryptionOperation(arrayElement, ontology, operation));
				}
				return newArray.toString();
			} else {
				JsonNode newJsonData = encryptionOperation(jsonData, ontology, operation);
				return newJsonData.toString();
			}
		} else {
			return data;
		}
	}

	JsonNode encryptionOperation(JsonNode jsonData, Ontology ontology, EncryptionOperations operation)
			throws IOException {

		final JsonNode jsonSchema = objectMapper.readTree(ontology.getJsonSchema());

		final String path = "#";
		final String schemaPointer = "";

		processProperties(jsonData, jsonSchema, jsonSchema, path, schemaPointer, operation);

		return jsonData;

	}

	private void processProperties(JsonNode allData, JsonNode schema, JsonNode rootSchema, String path,
			String schemaPointer, EncryptionOperations operation) {

		final JsonNode properties = schema.path(PROP_STR);
		final Iterator<Entry<String, JsonNode>> elements = properties.fields();

		while (elements.hasNext()) {
			final Entry<String, JsonNode> element = elements.next();
			if (element != null) {
				processProperty(allData, element.getKey(), element.getValue(), rootSchema,
						path + "/" + element.getKey(), schemaPointer + "/" + "properties/" + element.getKey(),
						operation);
			}
		}
	}

	private void processProperty(JsonNode allData, String elementKey, JsonNode elementValue, JsonNode rootSchema,
			String path, String schemaPointer, EncryptionOperations operation) {

		final JsonNode ref = elementValue.path("$ref");
		if (!ref.isMissingNode()) {
			final String refString = ref.asText();
			final JsonNode referencedElement = getReferencedJsonNode(refString, rootSchema);
			final String newSchemaPointer = refString.substring(refString.lastIndexOf("#/")).substring(1);
			processProperties(allData, referencedElement, rootSchema, path, newSchemaPointer, operation);
		} else {
			processNonRefProperty(allData, elementKey, elementValue, rootSchema, path, schemaPointer, operation);
		}
	}

	private void processNonRefProperty(JsonNode allData, String elementKey, JsonNode elementValue, JsonNode rootSchema,
			String path, String schemaPointer, EncryptionOperations operation) {

		if (!elementValue.path("oneOf").isMissingNode()) {
			processOneOfProperty(allData, elementKey, elementValue, rootSchema, path, schemaPointer, operation);
		} else {

			if (getPropertyMiniSchemas(elementValue) != null) {
				processPropertyWithMiniSchemas(allData, elementKey, elementValue, rootSchema, path, schemaPointer,
						operation);
			} else {
				final JsonNode encrypt = elementValue.path(ENCRYPT_PROPERTY);
				final JsonNode type = elementValue.path(TYPE_WORD);
				if (encrypt.asBoolean()) {
					processEncryptDecryptProperty(allData, elementKey, path, operation, type.asText());
				} else {
					processProperties(allData, elementValue, rootSchema, path, schemaPointer, operation);
				}
			}
		}
	}

	private void processOneOfProperty(JsonNode allData, String elementKey, JsonNode elementValue, JsonNode rootSchema,
			String path, String schemaPointer, EncryptionOperations operation) {
		// only one of the schemas is valid for the property
		final JsonNode oneOf = elementValue.path("oneOf");
		if (oneOf.isArray()) {
			final Iterator<JsonNode> miniSchemas = oneOf.elements();
			final JsonNode miniData = getReferencedJsonNode(path, allData);
			boolean notFound = true;
			while (notFound && miniSchemas.hasNext()) {
				try {
					final JsonNode miniSchema = miniSchemas.next();
					final JsonSchema schema = factory.getJsonSchema(rootSchema, schemaPointer);
					final ProcessingReport report = schema.validate(miniData);
					if (report.isSuccess()) {
						notFound = false;

						processProperty(allData, elementKey, miniSchema, rootSchema, path, schemaPointer, operation);
					}
				} catch (final ProcessingException e) {
					// if it is not the valid schema it must be ignored
					log.trace("Mini Schema skipped", e);
				}
			}
		}
	}

	private Iterator<JsonNode> getPropertyMiniSchemas(JsonNode elementValue) {
		final JsonNode allOf = elementValue.path("allOf");
		final JsonNode anyOf = elementValue.path("anyOf");
		Iterator<JsonNode> miniSchemas = null;
		if (!anyOf.isMissingNode() && anyOf.isArray()) {
			miniSchemas = anyOf.elements();
		} else if (!allOf.isMissingNode() && allOf.isArray()) {
			miniSchemas = allOf.elements();
		}
		return miniSchemas;
	}

	private void processEncryptDecryptProperty(JsonNode allData, String elementKey, String path,
			EncryptionOperations operation, String type) {
		final JsonNode data = getReferencedJsonNode(path, allData);
		if (!data.isMissingNode()) {
			final String dataToProcess;
			final String propertyPath = path.substring(0, path.lastIndexOf('/'));
			final JsonNode originalData = getReferencedJsonNode(propertyPath, allData);
			JsonNode obj = null;
			if (data.isValueNode())
				dataToProcess = data.asText();
			else
				dataToProcess = data.toString();
			String dataProcessed = null;
			try {
				switch (operation) {
				case ENCRYPT:
					dataProcessed = BasicEncryption.encrypt(KEY, INIT_VECTOR, dataToProcess);
					((ObjectNode) originalData).put(elementKey, dataProcessed);
					break;
				case DECRYPT:
					dataProcessed = BasicEncryption.decrypt(KEY, INIT_VECTOR, dataToProcess);
					if ((type.equalsIgnoreCase(OBJECT_TYPE) || type.equalsIgnoreCase(ARRAY_TYPE))
							&& !dataProcessed.equalsIgnoreCase("")) {
						obj = objectMapper.readTree(dataProcessed);
						((ObjectNode) originalData).set(elementKey, obj);
					} else
						((ObjectNode) originalData).put(elementKey, dataProcessed);
					break;

				default:
					throw new IllegalArgumentException("Operation not soported");
				}

			} catch (final Exception e) {
				log.error("Error in encrypting data: " + e.getMessage());
				throw new GenericRuntimeOPException(e);
			}
		}
	}

	private void processPropertyWithMiniSchemas(JsonNode allData, String elementKey, JsonNode elementValue,
			JsonNode rootSchema, String path, String schemaPointer, EncryptionOperations operation) {
		final JsonNode miniData = getReferencedJsonNode(path, allData);
		final Iterator<JsonNode> miniSchemas = getPropertyMiniSchemas(elementValue);
		if (miniSchemas != null) {
			while (miniSchemas.hasNext()) {
				try {
					final JsonNode miniSchema = miniSchemas.next();
					final JsonSchema schema = factory.getJsonSchema(rootSchema, schemaPointer);
					final ProcessingReport report = schema.validate(miniData);
					if (report.isSuccess()) {
						processProperty(allData, elementKey, miniSchema, rootSchema, path, schemaPointer, operation);
					}
				} catch (final ProcessingException e) {
					// if it is not the valid schema it must be ignored
					log.trace("Mini Schema skipped", e);
				}
			}
		}
	}

	private JsonNode getReferencedJsonNode(String ref, JsonNode root) {
		final String[] path = ref.split("/");
		assert path[0].equals("#");
		JsonNode referecedElement = root;

		for (int i = 1; i < path.length; i++) {
			referecedElement = referecedElement.path(path[i]);
		}

		return referecedElement;
	}

	@Override
	public List<String> preProcessInsertData(OperationModel operationModel, final boolean addContext,
			final Ontology ontology) throws IOException {

		final JsonNode dataNode = objectMapper.readTree(operationModel.getBody());

		final List<String> encryptedData = new ArrayList<>();
		if (dataNode.isArray()) {
			for (final JsonNode instance : (ArrayNode) dataNode) {
				insertDefaultValues(ontology.getJsonSchema(), instance, ontology.isEnableDataClass(), operationModel);
				checkOntologySchemaCompliance(instance, ontology);
				try {

					final String bodyWithDataContext = addContext ? addContextData(operationModel, instance)
							: instance.toString();

					final String encryptedDataInBODY = encryptionOperationAllowingArrays(bodyWithDataContext, ontology,
							EncryptionOperations.ENCRYPT);
					encryptedData.add(encryptedDataInBODY);

				} catch (final IOException e) {
					throw new GenericRuntimeOPException(JSON_ERROR, e);
				}
			}
		} else {
			insertDefaultValues(ontology.getJsonSchema(), dataNode, ontology.isEnableDataClass(), operationModel);

			checkOntologySchemaCompliance(dataNode, ontology);
			try {

				final String bodyWithDataContext = addContext ? addContextData(operationModel, dataNode)
						: dataNode.toString();

				final String encryptedDataInBODY = encryptionOperationAllowingArrays(bodyWithDataContext, ontology,
						EncryptionOperations.ENCRYPT);
				encryptedData.add(encryptedDataInBODY);

			} catch (final IOException e) {
				throw new GenericRuntimeOPException(JSON_ERROR, e);
			}

		}
		return encryptedData;

	}

	@Override
	public String preProcessUpdateData(final OperationModel operationModel) throws IOException {
		final String ontologyName = operationModel.getOntologyName();
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		final String encryptedDataInBODY = encryptionOperationAllowingArrays(operationModel.getBody(), ontology,
				EncryptionOperations.ENCRYPT);
		return encryptedDataInBODY;
	}

	@Override
	public String decrypt(String data, String ontologyName, String user) throws OntologyDataUnauthorizedException {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		if (ontology.getUser().getUserId().equals(user)) {
			try {
				return encryptionOperationAllowingArrays(data, ontology, EncryptionOperations.DECRYPT);
			} catch (final IOException e) {
				throw new OntologyDataJsonProblemException(JSON_ERROR, e);
			}
		} else {
			throw new OntologyDataUnauthorizedException("Only the owner can decrypt data");
		}
	}

	@Override
	public void checkTitleCaseSchema(String jsonSchema) {
		try {
			final JsonNode rootSchema = objectMapper.readTree(jsonSchema);
			final Iterator<Entry<String, JsonNode>> elements = rootSchema.path(PROP_STR).fields();
			elements.forEachRemaining(
					e -> processPropertiesForTitleCase(e.getKey(), e.getValue(), rootSchema, "/" + PROP_STR));

		} catch (final IOException e) {
			log.error("checkTitleCaseSchema", e);
			throw new OntologyDataJsonProblemException("Schema is not json");
		}
	}

	public void processPropertiesForTitleCase(String field, JsonNode value, JsonNode root, String pointer) {
		if (!value.path("$ref").isMissingNode()) {
			final String ref = value.path("$ref").asText();
			final String newPointer = ref.substring(ref.lastIndexOf("#/")).substring(1);
			root.at(newPointer + "/" + PROP_STR).fields()
					.forEachRemaining(e -> processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer));
		} else {
			// if all field is UPPER is not a exception
			if (!field.equalsIgnoreCase(field) && Character.isUpperCase(field.charAt(0)))
				throw new OntologyDataJsonProblemException("Properties can not start with Upper case : " + field);
			if (!value.path(TYPE_WORD).isMissingNode()) {
				processSubPropertiesForTilteCase(field, value, root, pointer);
			}
		}
	}

	private void processSubPropertiesForTilteCase(String field, JsonNode value, JsonNode root, String pointer) {
		final String type = value.path(TYPE_WORD).asText();
		if (type.equalsIgnoreCase(OBJECT_TYPE)) {
			final String newPointer = pointer + "/" + field + "/" + PROP_STR;
			root.at(newPointer).fields()
					.forEachRemaining(e -> processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer));

		} else if (type.equalsIgnoreCase(ARRAY_TYPE)) {
			final String newPointer = pointer + "/" + field + "/items";
			root.at(newPointer).elements().forEachRemaining(n -> {
				if (!n.path(PROP_STR).isMissingNode()) {
					n.path(PROP_STR).fields().forEachRemaining(
							e -> processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer));
				}
			});
		}
	}

	@Override
	public void checkRequiredFields(String dbJsonSchema, String newJsonSchema) {
		try {
			final JsonNode rootNew = objectMapper.readTree(newJsonSchema);
			final JsonNode rootDb = objectMapper.readTree(dbJsonSchema);
			iteratePropertiesRequired(rootDb, rootNew);
		} catch (final IOException e) {
			log.error("checkRequiredFields", e);
			throw new OntologyDataJsonProblemException("Not valid json schema");
		}

	}

	public void proccessRequiredProperties(JsonNode oldSchema, JsonNode newSchema) {
		if (!oldSchema.path(REQ_STR).isMissingNode()) {
			JsonNode required = oldSchema.path(REQ_STR);
			final ArrayList<String> properties = new ArrayList<>();
			required.elements().forEachRemaining(n -> properties.add(n.asText()));
			required = newSchema.path(REQ_STR);
			final ArrayList<String> propertiesNew = new ArrayList<>();
			required.elements().forEachRemaining(n -> propertiesNew.add(n.asText()));
			if (!properties.containsAll(propertiesNew)) {
				throw new OntologyDataJsonProblemException(REQ_PROP_STR);
			}
		} else if (!newSchema.path(REQ_STR).isMissingNode()) {
			throw new OntologyDataJsonProblemException(REQ_PROP_STR);
		}
	}

	public void iteratePropertiesRequired(JsonNode oldSchema, JsonNode newSchema) {
		if (StringUtils.isEmpty(oldSchema) || oldSchema.asText().equals("{}"))
			return;
		final String ref = refJsonSchema(oldSchema);
		String pointer = "/" + PROP_STR;
		if (StringUtils.hasText(ref))
			pointer = ref + pointer;
		if (!oldSchema.at(ref + "/required").isMissingNode()) {
			proccessRequiredProperties(oldSchema.at(ref), newSchema.at(ref));
		} else if (!newSchema.at(ref + "/required").isMissingNode()) {
			throw new OntologyDataJsonProblemException(REQ_PROP_STR);
		}

		if (!oldSchema.at(pointer).isMissingNode()) {
			final JsonNode properties = oldSchema.at(pointer);
			final JsonNode propertiesNew = newSchema.at(pointer);
			final Iterator<Entry<String, JsonNode>> elements = properties.fields();
			while (elements.hasNext()) {
				final Entry<String, JsonNode> e = elements.next();
				if (!e.getValue().path(REQ_STR).isMissingNode()) {
					final String path = e.getKey();
					processSingleProperty4Required(e.getValue(), propertiesNew.path(path));

				}
			}
		}
	}

	public void processSingleProperty4Required(JsonNode oldSchema, JsonNode newSchema) {
		proccessRequiredProperties(oldSchema, newSchema);
		if (!oldSchema.path(TYPE_WORD).isMissingNode() && oldSchema.path(TYPE_WORD).asText().equals(OBJECT_TYPE)) {
			final JsonNode properties = oldSchema.path(PROP_STR);
			final JsonNode propertiesNew = newSchema.path(PROP_STR);
			final Iterator<Entry<String, JsonNode>> elements = properties.fields();
			while (elements.hasNext()) {
				final Entry<String, JsonNode> e = elements.next();
				if (!e.getValue().path(REQ_STR).isMissingNode()) {
					final String path = e.getKey();
					processSingleProperty4Required(e.getValue(), propertiesNew.path(path));

				}
			}
		}
	}

	@Override
	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path(PROP_STR).fields();
		String reference = "";
		while (elements.hasNext()) {
			final Entry<String, JsonNode> entry = elements.next();
			if (!entry.getValue().path("$ref").isMissingNode()) {
				final String ref = entry.getValue().path("$ref").asText();
				reference = ref.substring(ref.lastIndexOf("#/")).substring(1);
			}
		}
		return reference;
	}

	@Override
	public ProcessingReport reportJsonSchemaValid(String jsonSchema) throws IOException {
		final JsonSchemaFactory factoryJson = JsonSchemaFactory.byDefault();
		final SyntaxValidator validator = factoryJson.getSyntaxValidator();
		return validator.validateSchema(objectMapper.readTree(jsonSchema));
	}

	@Override
	public Set<OntologyRelation> getOntologyReferences(String ontologyIdentification) throws IOException {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		final Set<OntologyRelation> relations = new TreeSet<>();
		final ObjectMapper mapperI = new ObjectMapper();
		final JsonNode schemaOrigin = mapperI.readTree(ontology.getJsonSchema());
		if (!schemaOrigin.path("_references").isMissingNode()) {
			schemaOrigin.path("_references").forEach(r -> {
				String srcAtt = r.path("self").asText();
				String targetAtt = r.path("target").asText().split("#")[1];
				final String targetOntology = r.path("target").asText().split("#")[0].replace("ontologies/schema/", "");
				final Ontology target = ontologyRepository.findByIdentification(targetOntology);
				final String refOrigin = refJsonSchema(schemaOrigin);
				if (!"".equals(refOrigin))
					srcAtt = srcAtt.replaceAll(refOrigin.replace("/", ""), schemaOrigin.at("/required/0").asText());
				if (target == null)
					log.error("Target ontology of " + ontology.getIdentification() + " not found on platform");

				try {
					final JsonNode schemaTarget = mapperI.readTree(target.getJsonSchema());
					final String refTarget = refJsonSchema(schemaTarget);
					if (!"".equals(refTarget))
						targetAtt = targetAtt.replaceAll(refTarget.replace("/", ""),
								schemaTarget.at("/required/0").asText());
				} catch (final IOException e) {
					log.debug("No $ref");
				}
				targetAtt = targetAtt.replaceAll(PROP_STR + ".", "").replaceAll("items.", "").replaceAll(".items", "");
				srcAtt = srcAtt.replaceAll(PROP_STR + ".", "").replaceAll("items.", "").replaceAll(".items", "");
				relations.add(new OntologyRelation(ontology.getIdentification(), target.getIdentification(), srcAtt,
						targetAtt));

			});

		}
		return relations;
	}

	@Override
	public Map<String, String> getOntologyPropertiesWithPath4Type(String ontologyIdentification, String type) {
		final Map<String, String> map = new HashMap<>();
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		if (ontology != null) {
			final ObjectMapper mapperI = new ObjectMapper();
			try {
				final JsonNode schema = mapperI.readTree(ontology.getJsonSchema());
				final String reference = refJsonSchema(schema);
				final String parentNode = reference.equals("") ? PROP_STR + "."
						: reference.replace(SLASH, "") + ".properties.";
				final String path = reference.equals("") ? SLASH + PROP_STR : reference + SLASH + PROP_STR;
				final JsonNode properties = schema.at(path);

				properties.fields().forEachRemaining(e -> {
					if (e.getValue().path(TYPE_WORD).asText().equals(type))
						map.put(e.getKey(), parentNode + e.getKey());
				});
			} catch (final IOException e) {
				log.error("Could not read json schema for properties");
			}
		}
		return map;
	}

	@Override
	public void checkSameSchema(String dbJsonSchema, String newJsonSchema) {
		if (!dbJsonSchema.equals(newJsonSchema)) {
			throw new OntologyDataJsonProblemException(REQ_SAME_SCH);
		}
	}

	@Override
	public String decryptAllUsers(String data, String ontologyName) throws OntologyDataUnauthorizedException {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		if (ontology != null) {
			try {
				return encryptionOperationAllowingArrays(data, ontology, EncryptionOperations.DECRYPT);
			} catch (final IOException e) {
				throw new OntologyDataJsonProblemException(JSON_ERROR, e);
			}
		} else {
			log.error("Target ontology of " + ontologyName + " not found on platform");
			return "Target ontology of " + ontologyName + " not found on platform";
		}
	}

	@Override
	public String encryptQuery(String query, boolean mongo) {
		try {
			int firstInd;
			String firstPart;
			String dataToEncryptEscaped;
			String dataToEncrypt;
			String datosEnc;
			while (query.contains(ENCRYPT_WORD)) {
				firstInd = query.indexOf(ENCRYPT_WORD);
				firstPart = query.substring(firstInd + ENCRYPT_WORD.length());
				dataToEncryptEscaped = firstPart.split(REGEX)[0];
				dataToEncrypt = dataToEncryptEscaped.replace(ESCAPE_CHAR + DELIMITER, DELIMITER);
				datosEnc = BasicEncryption.encrypt(KEY, INIT_VECTOR, dataToEncrypt);
				query = query.replace(ENCRYPT_WORD + dataToEncryptEscaped + ')', datosEnc);
			}
			return query;
		} catch (GenericOPException e) {
			log.error("Cannot encrypt query", e);
			return query;
		}
	}

	private JsonNode insertDefaultValues(String schema, JsonNode inst, boolean dclasspreprocessing, OperationModel operationModel) {
		try {
			final JsonNode jsonSchmema = mapper.readTree(schema);
			HashMap<String, JsonNode> hash = new HashMap<String, JsonNode>();
			findDefault(jsonSchmema, "", hash, dclasspreprocessing, inst, operationModel);
			for (String i : hash.keySet()) {
				updateInstance(inst, i, hash.get(i));
			}
		} catch (JsonProcessingException e) {
			log.error("Cannot insert default values for schema", e);
		}
		return inst;
	}

	private void findDefault(JsonNode schema, String path, HashMap<String, JsonNode> hash, boolean dclasspreprocessing, JsonNode inst, OperationModel operationModel) {
	    // find properties
		if (schema.has("properties")) {
			JsonNode properties = schema.path("properties");
			for (Iterator<Map.Entry<String, JsonNode>> jsonFields = properties.fields(); jsonFields.hasNext();) {
				Map.Entry<String, JsonNode> jsonField = jsonFields.next();
				String name = jsonField.getKey();
				JsonNode jsonValue = jsonField.getValue();

				// check if has $ref else check default
				if (jsonValue.has("$ref")) {
					String definition = jsonValue.path("$ref").asText();
					definition = definition.substring(2);
					jsonValue = schema.path(definition);
					if (path.length() == 0) {
						path = name;
					} else {
						path = path + "." + name;
					}
					findDefault(jsonValue, path, hash, dclasspreprocessing, inst, operationModel);

				} else if (jsonValue.has("default")) {
					// if has default save node and path
					if (path.length() == 0) {
						hash.put(name, jsonValue);
					} else {
						hash.put(path + "." + name, jsonValue);
					}

				} else if (jsonValue.has("properties")) {
					if (path.length() == 0) {
						findDefault(jsonValue, name, hash, dclasspreprocessing, inst, operationModel);
					} else {
						findDefault(jsonValue, path + "." + name, hash, dclasspreprocessing, inst, operationModel);
					}

				} else if (jsonValue.has("propdclass") && dclasspreprocessing) {
				    JsonNode dclassNames = jsonValue.get("propdclass");
				    for(JsonNode dclassName : dclassNames) {
	                    String[] dclassParts = dclassName.textValue().split("\\.");
	                    Configuration config = configurationRepository.findByTypeAndIdentificationIgnoreCase(Type.DATACLASS, 
	                        dclassParts[0]);
	                    
	                    String propToChange = name;
	                    if(config != null && properties.has(propToChange)) {
                            final Map<String, Object> dclassyml = (Map<String, Object>) configurationService
                                .fromYaml(config.getYmlConfig()).get("dataclass");

                            ArrayList<Map<String, Object>> rules = (ArrayList<Map<String, Object>>)dclassyml.get("dataclassrules");
                            for(Map<String, Object> rule : rules) {
                                if(rule.get("rulename").toString().equalsIgnoreCase(dclassParts[1]) && rule.get("ruletype").toString().equalsIgnoreCase("property")) {
                                    ArrayList<Map<String, Object>> changes = (ArrayList<Map<String, Object>>)rule.get("changes");
                                    if(changes != null) {
                                        changes.sort(Comparator.comparing(m -> (Integer)m.get("order")));
                                        executeScripts(changes, propToChange, inst, path, operationModel);
                                    }
                                    
                                    ArrayList<Map<String, Object>> validations = (ArrayList<Map<String, Object>>)rule.get("validations");
                                    if(validations != null) {
                                        validations.sort(Comparator.comparing(m -> (Integer)m.get("order")));
                                        executeScripts(validations, propToChange, inst, path, operationModel); 
                                    } 
                                }
                                //ELSE NO SE ENCUENTRA LA REGLA ESPECÍFICA DEL DATACLASS
                            }                  
	                    }
				    }
				}
			}
		}
		
        if(schema.has("entitydclass") && dclasspreprocessing) {
            JsonNode entitydclassname = schema.get("entitydclass");
            for(JsonNode edclassName : entitydclassname) {
                String[] dclassParts = edclassName.textValue().split("\\.");
                Configuration config = configurationRepository.findByTypeAndIdentificationIgnoreCase(Type.DATACLASS, 
                    dclassParts[0]);
                final Map<String, Object> dclassyml = (Map<String, Object>) configurationService
                    .fromYaml(config.getYmlConfig()).get("dataclass");
                
                ArrayList<Map<String, Object>> rules = (ArrayList<Map<String, Object>>)dclassyml.get("dataclassrules");
                for(Map<String, Object> rule : rules) {
                    if(rule.get("rulename").toString().equalsIgnoreCase(dclassParts[1]) && rule.get("ruletype").toString().equalsIgnoreCase("entity")) {
                        ArrayList<Map<String, Object>> changes = (ArrayList<Map<String, Object>>)rule.get("changes");
                        if(changes != null) {
                            changes.sort(Comparator.comparing(m -> (Integer)m.get("order")));
                            entityDClassCondition(changes,inst, schema.get("title"), operationModel);
                        }
                        
                        ArrayList<Map<String, Object>> validations = (ArrayList<Map<String, Object>>)rule.get("validations");
                        if(validations != null) {
                            validations.sort(Comparator.comparing(m -> (Integer)m.get("order")));
                            entityDClassCondition(validations, inst, schema.get("title"), operationModel);
                        }
                    }
                }
            }
        }
	}
	
	private void entityDClassCondition(ArrayList<Map<String, Object>> changes, JsonNode inst, JsonNode ontology, OperationModel operationModel) {
	    for(Map<String, Object> change: changes) {
	        Object result = null;
	        JsonNode rawdata = inst.get(ontology.asText());
	        JSONObject jsonObject = new JSONObject(rawdata.toString());
	        Object script = change.get("script");
	        if(script != null) {
                String scriptRegular = isRegularFunction(script.toString(), rawdata.toString());
                if(scriptRegular.equals("0")) {
                    try {
                        final String scriptPostprocessFunction = "function preprocess(rawdata){ " + change.get("script") + " }";
                        final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
                                scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
                        scriptEngine.eval(new InputStreamReader(scriptInputStream));
                        final Invocable inv = (Invocable) scriptEngine;
                        result = inv.invokeFunction("preprocess", jsonObject);
                    } catch (NoSuchMethodException e) {
                        log.error("Cannot eval preprocessing", e);
                        auditWarning(e.getMessage(), operationModel);
                    } catch (ScriptException ex) {
                        log.error("Cannot eval preprocessing", ex);
                        auditWarning(ex.getMessage(), operationModel);
                    }
                } else {
                    result = scriptRegular;
                }
                
	        } else {
	            //condition- effect- else
	            String condition = change.get("condition").toString().replace("rawdata", "json");
	            String effect = change.get("effect").toString().replace("rawdata", "json");
	            String scriptPostprocessFunction = "function preprocess(rawdata){ var json = JSON.parse(rawdata); if(" +condition + "){" + effect + ";}";
	            if(change.get("else") != null) {
	                String elseC = change.get("else").toString().replace("rawdata", "json");
	                scriptPostprocessFunction = scriptPostprocessFunction + "else {" + elseC + ";}";
	            }
	            scriptPostprocessFunction = scriptPostprocessFunction + " return json;}";
	            try {
                    final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
                            scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
                    scriptEngine.eval(new InputStreamReader(scriptInputStream));
                    final Invocable inv = (Invocable) scriptEngine;
                    result = inv.invokeFunction("preprocess", jsonObject);
                } catch (NoSuchMethodException e) {
                    log.error("Cannot eval preprocessing", e);
                    auditWarning(e.getMessage(), operationModel);
                } catch (ScriptException ex) {
                    log.error("Cannot eval preprocessing", ex);
                    auditWarning(ex.getMessage(), operationModel);
                }
	        }
	        
	        if(result instanceof Boolean) {
                if(!(Boolean) result) {
                    dataClassError(change, jsonObject.toString(), operationModel);
                }

            } else {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonresult;
                    if(result instanceof Object) {
                        jsonresult =  mapper.readTree(mapper.writeValueAsString(result));
                    } else {
                        jsonresult = mapper.readTree(result.toString());
                    }
                    ((ObjectNode)inst).replace(ontology.asText(), jsonresult);
                } catch (JsonMappingException ex) {
                    log.error("Cannot process result", ex);
                    auditWarning(ex.getMessage(), operationModel);
                } catch (JsonProcessingException ex) {
                    log.error("Cannot process result", ex);
                    auditWarning(ex.getMessage(), operationModel);
                }
            }
	    }
	}
	
	private String isRegularFunction(String script, String value) {
	    if(script.contains("return value.toUpperCase()")) {
	        return value.toUpperCase();
	    } else if(script.contains("return value.toLowerCase()")) {
	        return value.toLowerCase();
	        
	    } else if(script.contains("return value.toDate(")){
	        int start = script.indexOf("(");
	        int end = script.indexOf(")");
	        int start2 = script.lastIndexOf("(");
	        int end2 = script.lastIndexOf(")");
	        String toDate = script.substring(start + 1, end);
	        String toString = script.substring(start2 + 1, end2);
	        toDate = toDate.replaceAll("'", "");
	        toDate = toDate.replaceAll("\"", "");
	        toString = toString.replaceAll("'", "");
            toString = toString.replaceAll("\"", "");
	        SimpleDateFormat fromUser = new SimpleDateFormat(toDate);
            SimpleDateFormat myFormat = new SimpleDateFormat(toString);
            try {
                return myFormat.format(fromUser.parse(value));
            } catch (ParseException ex) {
                return "0";
            }
            
	    } else if(script.contains("return value.replace(")) {
	        int start = script.indexOf(",");
            int end = script.indexOf(")");
	        String newValue = script.substring(start + 1, end);
	        newValue = newValue.replaceAll("'", "");
            newValue = newValue.replaceAll("\"", "");
            newValue = newValue.trim();
            int oldstart = script.indexOf("(");
            String oldValue = script.substring(oldstart + 1, start);
            oldValue = oldValue.replaceAll("'", "");
            oldValue = oldValue.replaceAll("\"", "");
            oldValue = oldValue.trim();
	        return value.replace(oldValue, newValue);
	    
	    } else {
	        return "0";
	    }
	}
	
	private void executeScripts(ArrayList<Map<String, Object>> changes, String propToChange, JsonNode inst, String path, OperationModel operationModel) {
	    for(Map<String, Object> change: changes) {
            Object toReplace = inst.get(propToChange);
            boolean rootElement = false;
            if(toReplace == null) {
                toReplace = inst.get(path).get(propToChange);
                rootElement = true;
            }
            if(!toReplace.toString().equals("null")) {
                String toReplaceStr = toReplace.toString().substring(1, toReplace.toString().length() - 1);
                Object result = null;
                Object script = change.get("script");
                if(script != null) {
                    String scriptRegular = isRegularFunction(script.toString(), toReplaceStr);
                    if(scriptRegular.equals("0")) {
                        try {
                            final String scriptPostprocessFunction = "function preprocess(value){ " + script + " }";
                            final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
                                    scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
                            scriptEngine.eval(new InputStreamReader(scriptInputStream));
                            final Invocable inv = (Invocable) scriptEngine;
                            result = inv.invokeFunction("preprocess", toReplaceStr);
                            
                        } catch (NoSuchMethodException e) {
                            log.error("Cannot eval preprocessing", e);
                            auditWarning(e.getMessage(), operationModel);
                        } catch (ScriptException ex) {
                            log.error("Cannot eval preprocessing", ex);
                            auditWarning(ex.getMessage(), operationModel);
                        }
                    } else {
                        result = scriptRegular;
                    }
                    
                    if(result instanceof Boolean) {
                        if(!(Boolean) result) {
                            dataClassError(change, toReplaceStr, operationModel);
                        }
        
                    } else if(result != null) {
                        if(rootElement) {
                            ((ObjectNode)inst.get(path)).put(propToChange, result.toString());
                        } else {
                            ((ObjectNode)inst).put(propToChange, result.toString());
                        }
                    }              
    
                }
            }
        }
	}
	
	private void dataClassError(Map<String, Object> change, String value, OperationModel operationModel) {
        String errorType = change.get("error").toString();
        String errorMsg = change.get("errormsg").toString();
        
        if(errorMsg.contains("${value}")) {
            errorMsg = errorMsg.replace("${value}", value);
        } else if(errorMsg.contains("${rawdata")) {
            int start = errorMsg.indexOf("{");
            int end = errorMsg.lastIndexOf("}");
            String toReplace = errorMsg.substring(start + 1, end);
            String data = toReplace.replace("rawdata.", "");
            JSONObject json = new JSONObject(value);
            errorMsg = errorMsg.replace("${" + toReplace + "}", json.getString(data));
        }
        
	    if(errorType.equalsIgnoreCase("error")) {
	        throw new DataClassValidationException(errorMsg);
        } else if(errorType.equalsIgnoreCase("warning")) {
            log.error(errorMsg);
           auditWarning(errorMsg, operationModel);
        }
	}
	
	private void auditWarning(String errorMsg, OperationModel operationModel) {
	    OPAuditError auditEvent = null;
        auditEvent = OPEventFactory.builder().build().createAuditEventWarning(errorMsg);
	    auditEvent.setOperationType(operationModel.getOperationType().name());
	    auditEvent.setOntology(operationModel.getOntologyName());
	    auditEvent.setUser(operationModel.getUser());
	    auditEvent.setMethodName("dataClassError");
	    
        auditableAscpect.publish(auditEvent);
	}

	private void updateInstance(JsonNode inst, String path, JsonNode defau) {
		String[] parts = path.split("\\.");
		List<String> partsList = new ArrayList<String>();
		partsList.addAll(Arrays.asList(parts));
		completInstance(inst, partsList, defau);
	}

	private void completInstance(JsonNode inst, List<String> parts, JsonNode defau) {
		ObjectMapper mapper = new ObjectMapper();
		String index = parts.remove(0);
		if (parts.size() == 0) {
			if (inst.path(index).isMissingNode()) {
				// check if there are array of types
				String type = "string";
				if (defau.path("type").isArray()) {
					for (JsonNode arrayItem : defau.path("type")) {
						if (!arrayItem.asText().equals("null")) {
							type = arrayItem.asText().toLowerCase();
						}
					}
				} else {
					type = defau.path("type").asText().toLowerCase();
				}
				if (type.equals("string")) {
					((ObjectNode) inst).put(index, defau.path("default").textValue());
				} else if (type.equals("number")) {
					((ObjectNode) inst).put(index, defau.path("default").asDouble());
				} else if (type.equals("integer")) {
					((ObjectNode) inst).put(index, defau.path("default").asInt());
				} else if (type.equals("boolean")) {
					((ObjectNode) inst).put(index, defau.path("default").asBoolean());
				}
			}
		} else {
			if (inst.path(index).isMissingNode()) {
				((ObjectNode) inst).put(index, mapper.createObjectNode());
				completInstance(inst.path(index), parts, defau);
			} else {
				completInstance(inst.path(index), parts, defau);
			}
		}

	}

}
