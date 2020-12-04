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
package com.minsait.onesait.platform.business.services.ontology;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationServiceImpl;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBManageDBRepository;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyOpsDBRepository;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicService;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyBusinessServiceImpl implements OntologyBusinessService {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyLogicService ontologyLogicService;

	@Autowired
	private KafkaAuthorizationServiceImpl kafkaAuthorizationService;

	@Autowired
	private UserService userService;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";
	static final String ERROR_KAFKA_TOPIC = "Error creationg kafka topic";

	@Autowired
	private VirtualOntologyOpsDBRepository virtualRepo;

	@Value("${onesaitplatform.database.hadoop.enabled:false}")
	private boolean hadoopEnable;

	@Autowired
	private EntityDeletionService entityDeleteionService;

	@Autowired
	private CosmosDBManageDBRepository cosmosManageRepository;

	@Autowired
	private CosmosDBBasicOpsDBRepository cosmosBasicOpsRepository;

	@Override
	public void createOntology(Ontology ontology, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException {

		if (!ontologyService.isIdValid(ontology.getIdentification())) {
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT,
					"Ontology identification is not valid");
		}

		final User user = userService.getUser(userId);
		ontology.setUser(user);

		try {
			ontologyService.checkOntologySchema(ontology.getJsonSchema());
		} catch (final OntologyDataJsonProblemException e) {
			metricsManagerLogControlPanelOntologyCreation(userId, "KO");

			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.NO_VALID_SCHEMA,
					"The provided json schema is not valid", e);
		}

		if (ontology.getRtdbDatasource().equals(RtdbDatasource.KUDU) && !hadoopEnable) {
			metricsManagerLogControlPanelOntologyCreation(userId, "KO");

			throw new OntologyBusinessServiceException(
					OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR,
					"Hadoop installation not available");
		}
		prepareOntologyRtdbToHdb(ontology);
		invokeCreateOntology(ontology, config);
		invokeCreateOntologyLogic(ontology, config);
	}

	private void prepareOntologyRtdbToHdb(Ontology ontology) {
		if (ontology.isRtdbToHdb())
			ontology.setRtdbClean(true);
		else
			ontology.setRtdbToHdbStorage(null);

		if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
			ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_DAY);
		}
	}

	private void invokeCreateOntology(Ontology ontology, OntologyConfiguration config) {
		ontologyService.createOntology(ontology, config);
	}

	private void invokeCreateOntologyLogic(Ontology ontology, OntologyConfiguration config)
			throws OntologyBusinessServiceException {
		try {
			ontologyLogicService.createOntology(ontology, config == null ? null : configToConfigMap(config));
		} catch (final Exception e) {
			rollBackInvokeCreateOntologyLogic(e, ontology, true);
		}

	}

	private void rollBackInvokeCreateOntologyLogic(Exception e, Ontology ontology, boolean externalTableCreated)
			throws OntologyBusinessServiceException {
		boolean raiseException = false;
		final String errorRollingBack = "Error creating the persistence infrastructure for ontology";
		final String errorRollingBackExternalTable = "it was not possible to undo the ontology configuration and/or the external table creation";
		String errorMsg = errorRollingBack;

		try {
			if (externalTableCreated) {
				ontologyLogicService.removeOntology(ontology);
			}
		} catch (final Exception e3) {
			errorMsg = errorRollingBack + ", " + errorRollingBackExternalTable;
			raiseException = true;
		}

		if (raiseException) {
			throw new OntologyBusinessServiceException(
					OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR_UNCLEAN,
					errorMsg + ": " + e.getMessage());
		}

		throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR,
				errorRollingBack + ": " + e.getMessage(), e);
	}

	@Override
	public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = organizeRootNodeIfExist(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}

	@Override
	public JsonNode organizeRootNodeIfExist(String schema) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaSubTree = mapper.readTree(schema);
		boolean find = Boolean.FALSE;
		for (final Iterator<Entry<String, JsonNode>> elements = schemaSubTree.fields(); elements.hasNext();) {
			final Entry<String, JsonNode> e = elements.next();
			if (e.getKey().equals("properties")) {
				e.getValue().fields();
				for (final Iterator<Entry<String, JsonNode>> properties = e.getValue().fields(); properties
						.hasNext();) {
					final Entry<String, JsonNode> prop = properties.next();
					final String field = prop.getKey();
					if (!field.equalsIgnoreCase(field) && Character.isUpperCase(field.charAt(0))) {
						((ObjectNode) schemaSubTree).set("datos", prop.getValue());
						// Add required
						final ArrayNode required = ((ObjectNode) schemaSubTree).putArray("required");
						required.add(prop.getKey());
						final String newString = "{\"type\": \"string\",\"$ref\": \"#/datos\"}";
						final JsonNode newNode = mapper.readTree(newString);
						prop.setValue(newNode);
						find = Boolean.TRUE;
						break;
					}
					if (find) {
						break;
					}
				}
			}
		}
		return schemaSubTree;
	}

	@Override
	public List<String> getTablesFromDatasource(String datasource) {
		return virtualRepo.getTables(datasource);
	}

	@Override
	public List<String> getStringSupportedFieldDataTypes() {
		return virtualRepo.getStringSupportedFieldDataTypes();
	}

	@Override
	public List<String> getStringSupportedConstraintTypes() {
		return virtualRepo.getStringSupportedConstraintTypes();
	}

	@Override
	public String getInstance(String datasource, String collection) {
		final List<String> result = virtualRepo.getInstanceFromTable(datasource, "select * from " + collection);
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return "";
		}
	}

	@Override
	public String getRelationalSchema(String datasource, String collection) {
		return virtualRepo.getTableMetadata(datasource, collection);
	}

	@Override
	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema,
			final VirtualDatasourceType datasource) {
		// implements logic for EmptyBase schema and VirtualSchema
		return virtualRepo.getSqlTableDefinitionFromSchema(ontology, schema, datasource);
	}

	@Override
	public String getSQLCreateTable(CreateStatement statement, VirtualDatasourceType datasource) {
		return virtualRepo.getSQLCreateStatment(statement, datasource);
	}

	/***
	 * Generate config map with all specific params for internal database
	 */
	private HashMap<String, String> configToConfigMap(OntologyConfiguration config) {
		final HashMap<String, String> cmap = new HashMap<>();
		// relational
		cmap.put("allowsCreateTable", String.valueOf(config.isAllowsCreateTable()));
		if (config.getSqlStatement() != null) {
			cmap.put("sqlStatement", config.getSqlStatement());
		}

		// kudu
		if (config.getEnablePartitionIndexes() != null || "false".equals(config.getEnablePartitionIndexes())) {
			cmap.put("partitions", config.getPartitions());
			cmap.put("primarykey", config.getPrimarykey());
			cmap.put("npartitions", config.getNpartitions());

		}
		if (!StringUtils.isEmpty(config.getPartitionKey())) {
			cmap.put("partitionKey", config.getPartitionKey());
		}
		if (!StringUtils.isEmpty(config.getUniqueKeys())) {
			cmap.put("uniqueKeys", config.getUniqueKeys());
		}
		return cmap;
	}

	@Override
	public HashMap<String, String> getAditionalDBConfig(Ontology ontology) {
		final HashMap<String, String> dbProperties = new HashMap<>();
		try {
			dbProperties.putAll(ontologyLogicService.getAdditionalDBConfig(ontology));
		} catch (final OntologyLogicServiceException e) {
			log.error("There was a problem getting DB config of ontology ", e);
		}
		return dbProperties;
	}

	@Override
	public void updateOntology(Ontology ontology, OntologyConfiguration config, boolean hasDocuments)
			throws OntologyBusinessServiceException {
		if (ontology.getRtdbDatasource().equals(RtdbDatasource.KUDU)) {
			if (!hasDocuments) {
				try {
					ontologyLogicService.updateOntology(ontology, configToConfigMap(config));
				} catch (OntologyLogicServiceException | OntologyDataJsonProblemException e) {
					throw new OntologyBusinessServiceException(
							OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR,
							"Error updating the ontology configuration: " + e.getMessage(), e);
				}
			} else {
				try {
					ontologyLogicService.checkSameInternalDBConfig(ontology, configToConfigMap(config));
				} catch (OntologyLogicServiceException | OntologyDataJsonProblemException e) {
					throw new OntologyBusinessServiceException(
							OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR,
							"Error cannot update internal config with data in ontology, please remove data and update it again. The error is the following: "
									+ e.getMessage(),
							e);
				}
			}
		}
		kafkaAuthorizationService.checkOntologyAclAfterUpdate(ontology);
	}

	@Override
	public boolean existsOntology(String identificacion) {
		return ontologyService.existsOntology(identificacion);
	}

	private void metricsManagerLogControlPanelOntologyCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelOntologyCreation(userId, result);
		}
	}

	@Override
	public void deleteOntology(String id, String userId) {
		final Ontology o = ontologyService.getOntologyById(id, userId);
		// IF IT'S A COSMOSDB COLLECTION AND NO RECORDS REMOVE IT
		if (RtdbDatasource.COSMOS_DB.equals(o.getRtdbDatasource())
				&& cosmosBasicOpsRepository.count(o.getIdentification()) == 0)
			cosmosManageRepository.removeTable4Ontology(o.getIdentification());
		entityDeleteionService.deleteOntology(id, userId);

	}

}
