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
package com.minsait.onesait.platform.persistence.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyReferencesValidationImp implements OntologyReferencesValidation {

	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private OntologyRepository ontologyRepository;

	private static final String PROPERTIES_STR = "properties.";
	private static final String ITEMS_STR = "items.";

	@Override
	public void validate(OperationModel operationModel, Ontology ontology) throws IOException, GenericOPException {
		final JsonNode dataNode = mapper.readTree(operationModel.getBody());
		if (dataNode.isArray()) {
			((ArrayNode) dataNode).elements().forEachRemaining(i -> {
				try {
					validateReferences(ontology, i.toString(), true);
				} catch (GenericOPException e) {
					log.error("Cannot validate references ", e);
				}
			});
		} else
			validateReferences(ontology, dataNode.toString(), true);

	}

	void validateReferences(Ontology ontology, String instance, boolean validateInstance) throws GenericOPException {
		try {
			final JsonNode inst = mapper.readTree(instance);
			final JsonNode schema = mapper.readTree(ontology.getJsonSchema());
			if (!schema.path("_references").isMissingNode()) {
				final ArrayNode references = (ArrayNode) schema.path("_references");
				final Iterator<JsonNode> elements = references.elements();
				while (elements.hasNext()) {
					final JsonNode reference = elements.next();
					if (reference.path("validate").asBoolean()) {
						validateSingleReference(reference, ontology, schema, inst, validateInstance);
					}
				}
			}
		} catch (final IOException e) {
			log.error("Not valid schema");
		} catch (DBPersistenceException e) {
			log.error("DBPersistence Exception");
		} catch (OntologyDataUnauthorizedException e) {
			log.error("OntologyData Unauthorized");
		}

	}

	private void validateSingleReference(JsonNode reference, Ontology srcOntology, JsonNode srcSchema,
			JsonNode instance, boolean validateInstance)
			throws IOException, DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		final String srcPath = reference.path("self").asText().replaceAll("\\.", "/");
		final String dstPath = reference.path("target").asText().split("#")[1].replaceAll("\\.", "/");
		final String dstOnt = reference.path("target").asText().split("#")[0].replace("ontologies/schema/", "");
		final Ontology dstOntology = ontologyRepository.findByIdentification(dstOnt);
		if (dstOntology == null)
			throw new OntologyDataJsonProblemException("Target ontology of the relationship does not exist: " + dstOnt);
		final JsonNode dstSchema = mapper.readTree(dstOntology.getJsonSchema());
		if (sameType(srcSchema.at("/" + srcPath), dstSchema.at("/" + dstPath))) {
			if (!srcSchema.at("/" + srcPath).isArray()
					&& !srcSchema.at("/" + srcPath).path("type").asText().equals("array")) {
				final String srcField = srcPath.split("/")[srcPath.split("/").length - 1];
				final String query = getQueryForValidation(srcSchema.at("/" + srcPath).path("type").asText(),
						instance.findPath(srcField), dstPath, dstOnt, dstSchema, dstOntology.getRtdbDatasource());
				if (!query.equals("")) {
					final String value = queryToolService.querySQLAsJson(srcOntology.getUser().getUserId(),
							dstOntology.getIdentification(), query, 0);
					try {
						if (mapper.readTree(value).findPath("value").asInt() == 0)
							throw new OntologyDataJsonProblemException("There are no instances of referenced ontology "
									+ dstOntology.getIdentification() + " with value specified in field " + srcField);
					} catch (final IOException e) {
						log.error("Could not get count result for query {}", query);
					}
				}
			} else if (srcSchema.at("/" + srcPath).isArray()) {
				final List<String> queries = new ArrayList<>();
				final String srcField = srcPath.split("/")[srcPath.split("/").length - 2];
				((ArrayNode) instance.findPath(srcField)).elements().forEachRemaining(nv -> {

					final String query = getQueryForValidation(srcSchema.at("/" + srcPath).get(0).path("type").asText(),
							nv, dstPath, dstOnt, dstSchema, dstOntology.getRtdbDatasource());
					if (!"".equals(query)) {
						queries.add(query);
					}
				});
				if (!queries.isEmpty()) {
					queries.stream().forEach(q -> {
						try {
							final String value = queryToolService.querySQLAsJson(srcOntology.getUser().getUserId(),
									dstOntology.getIdentification(), q, 0);
							if (mapper.readTree(value).path(0).path("value").asInt() == 0)
								throw new OntologyDataJsonProblemException(
										"There are no instances of referenced ontology "
												+ dstOntology.getIdentification() + " with values specified in field "
												+ srcField);
						} catch (final IOException | DBPersistenceException | OntologyDataUnauthorizedException
								| GenericOPException e) {
							log.error("Could not get count result for query {}", q);
						}
					});
				}

			}

		} else
			throw new OntologyDataJsonProblemException(
					"Attributes of the relation are not of the same type att: " + srcPath + " att: " + dstPath);
	}

	private boolean sameType(JsonNode srcProperty, JsonNode dstProperty) {
		final String srcType = srcProperty.isArray() ? srcProperty.get(0).path("type").asText()
				: srcProperty.path("type").asText();
		final String dstType = dstProperty.isArray() ? dstProperty.get(0).path("type").asText()
				: dstProperty.path("type").asText();
		return srcType.equals(dstType);
	}

	private String getQueryForValidation(String type, JsonNode instanceValue, String dstPath, String dstOntology,
			JsonNode dstJsonSchema, Ontology.RtdbDatasource datasource) {
		String query = "";
		if (datasource.equals(Ontology.RtdbDatasource.ELASTIC_SEARCH) || datasource.equals(Ontology.RtdbDatasource.OPEN_SEARCH))
			query = "select count(*) from " + dstOntology.toLowerCase() + " where ";
		else
			query = "select count(*) from " + dstOntology + " dstOntology where dstOntology.";
		final String ref = refJsonSchema(dstJsonSchema);
		String dstPathReplaced = "";
		if (!ref.equals("")) {
			query += dstJsonSchema.at("/required/0").asText() + ".";
			dstPathReplaced = refJsonSchema(dstJsonSchema).replace("/", "") + ".";
		}

		switch (type) {
		case "string":
			query += dstPath.replaceAll(PROPERTIES_STR, "").replaceAll(dstPathReplaced, "").replaceAll(ITEMS_STR, "")
					+ "=\"" + instanceValue.asText() + "\"";
			break;
		case "number":
			query += dstPath.replaceAll(PROPERTIES_STR, "").replaceAll(dstPathReplaced, "").replaceAll(ITEMS_STR, "")
					+ "=" + instanceValue.asDouble();
			break;
		case "integer":
			query += dstPath.replaceAll(PROPERTIES_STR, "").replaceAll(dstPathReplaced, "").replaceAll(ITEMS_STR, "")
					+ "=" + instanceValue.asInt();
			break;
		case "boolean":
			query += dstPath.replaceAll(PROPERTIES_STR, "").replaceAll(dstPathReplaced, "").replaceAll(ITEMS_STR, "")
					+ "=" + instanceValue.asBoolean();
			break;
		default:
			query = "";
			break;
		}
		return query;
	}

	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path("properties").fields();
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
}
