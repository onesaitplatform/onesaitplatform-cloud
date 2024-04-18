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
package com.minsait.onesait.platform.config.services.ontologydata;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.ContextData;
import com.minsait.onesait.platform.commons.security.BasicEncryption;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
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

	private final ObjectMapper objectMapper = new ObjectMapper();

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

			final JsonNode jsonSchema = mapper.readTree(ontologyRepository.getSchemaAsJsonNode(ontology));

			checkJsonCompliantWithSchema(data, jsonSchema);
		} catch (final IOException e) {
			throw new DataSchemaValidationException("Error reading data for checking schema compliance", e);
		} catch (final ProcessingException e) {
			throw new DataSchemaValidationException("Error checking data schema compliance", e);
		}
	}

	void checkJsonCompliantWithSchema(final JsonNode data, final JsonNode schemaJson) throws ProcessingException {
		final JsonSchemaFactory factoryJson = JsonSchemaFactory.byDefault();
		final JsonSchema schema = factoryJson.getJsonSchema(schemaJson);
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
		JsonNode schemaJson;

		try {
			dataJson = JsonLoader.fromString(dataString);
			schemaJson = JsonLoader.fromString(schemaString);
			checkJsonCompliantWithSchema(dataJson, schemaJson);

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
	public List<String> preProcessInsertData(OperationModel operationModel, final boolean addContext)
			throws IOException {
		final String ontologyName = operationModel.getOntologyName();
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);

		final JsonNode dataNode = objectMapper.readTree(operationModel.getBody());

		final List<String> encryptedData = new ArrayList<>();
		if (dataNode.isArray()) {
			for (final JsonNode instance : (ArrayNode) dataNode) {
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
			checkOntologySchemaCompliance(dataNode, ontology);
			try {

				final String bodyWithDataContext = addContext ? addContextData(operationModel, null)
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
		if (!StringUtils.isEmpty(ref))
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
}
