/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.external.api.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.DefaultOperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam.ParamOperationType;
import com.minsait.onesait.platform.config.services.ontologyrest.OntologyRestServiceImpl;
import com.minsait.onesait.platform.config.services.ontologyrest.operation.OntologyRestOperationService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.api.rest.client.APIRestClient;
import com.minsait.onesait.platform.persistence.external.api.rest.client.APIRestResponse;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.SQLStatementRestParser;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult.QueryType;
import com.minsait.onesait.platform.persistence.external.exception.NotSupportedOperationException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.extern.slf4j.Slf4j;

@Component("ExternalApiRestOpsDBRepository")
@Lazy
@Slf4j
public class ExternalApiRestOpsDBRepository implements BasicOpsDBRepository {

	private static final String NOT_IMPLEMENTED = "Method not implemented";
	private static final String NOT_SUPPORTED = "Operation not supported for Api Rest Ontologies";
	private static final String SELECT_ALL_FROM = "SELECT * FROM ";

	@Autowired
	private APIRestClient apiRestClient;

	@Autowired
	private SQLStatementRestParser sqlRestParser;

	@Autowired
	private OntologyRestServiceImpl ontologRestService;

	@Autowired
	private OntologyRestOperationService ontologyRestOperationService;

	private ObjectMapper mapper;

	@Override
	public String insert(String ontology, String instance) {
		final OntologyRest ontologyRest = ontologRestService.getOntologyRestByIdentification(ontology);

		// try to find default UpdateById operation
		final List<OntologyRestOperation> operations = ontologyRestOperationService
				.getAllOperationsFromOntologyRest(ontologyRest);
		final Optional<OntologyRestOperation> insertOpertaion = operations.stream()
				.filter(operation -> operation.getDefaultOperationType() == DefaultOperationType.INSERT).findFirst();
		if (insertOpertaion.isPresent()) {
			final String completeUrl = ontologyRest.getBaseUrl() + insertOpertaion.get().getPath();
			final APIRestResponse response = apiRestClient.invokePost(completeUrl, instance,
					Optional.of(getHeadersAndSecurity(ontologyRest)), Optional.of(new HashMap<String, String>()),
					Optional.of(new HashMap<String, String>()));
			if (response.getResponse() == 200) {
				return response.getBody();
			} else {
				log.error("Error inserting data");
				throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.API, response.getBody()),
						"Unable to insert data in REST API Datasource. Error " + response.getResponse() + "."
								+ response.getBody());
			}
		} else {
			log.error("No Update by id operation was found for this Ontology. Ontology = {}.", ontology);
			throw new DBPersistenceException(
					new NotSupportedOperationException("No Update by id operation was found for this Ontology."));
		}

	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		for (final String instance : instances) {
			insert(ontology, instance);
		}
		return new ComplexWriteResult();
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		return update(ontology, updateStmt, Optional.ofNullable(null));
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		return update(collection, query, Optional.ofNullable(data));
	}

	private MultiDocumentOperationResult update(String ontology, String updateStmt, Optional<String> data) {

		final OntologyRest ontologyRest = ontologRestService.getOntologyRestByIdentification(ontology);
		QueryAnalysisResult parserResult = sqlRestParser.parseRestSqlStatement(updateStmt, new ArrayList<>(),
				new ArrayList<>());
		if (!parserResult.getState()) {
			throw new DBPersistenceException(parserResult.getErrorMessage());
		}
		OntologyRestOperation updateOperation = null;
		if (parserResult.getOperation() != null) {
			updateOperation = ontologyRestOperationService.getOntologyRestOperationByName(ontologyRest,
					parserResult.getOperation());
		} else {
			// try to find default UpdateById operation
			final List<OntologyRestOperation> operations = ontologyRestOperationService
					.getAllOperationsFromOntologyRest(ontologyRest);
			updateOperation = operations.stream()
					.filter(operation -> operation.getDefaultOperationType() == DefaultOperationType.UPDATE_BY_ID)
					.findFirst().orElse(null);
		}
		// if update operation found
		if (updateOperation != null) {
			final List<OntologyRestOperationParam> params = ontologyRestOperationService
					.getOntologyRestOperationParams(updateOperation);
			final List<String> pathParamNames = new ArrayList<>();
			final List<String> queryParamsNames = new ArrayList<>();
			params.forEach(param -> {
				if (param.getType() == ParamOperationType.PATH) {
					pathParamNames.add(param.getName());
				}
				if (param.getType() == ParamOperationType.QUERY) {
					queryParamsNames.add(param.getName());
				}
			});
			parserResult = sqlRestParser.parseRestSqlStatement(updateStmt, pathParamNames, queryParamsNames);
			final String completeUrl = ontologyRest.getBaseUrl() + updateOperation.getPath();
			String dataToUpdate = parserResult.getJsonObject();
			if (data.isPresent()) {
				dataToUpdate = data.get();
			}
			final APIRestResponse response = apiRestClient.invokePut(completeUrl, dataToUpdate,
					Optional.of(getHeadersAndSecurity(ontologyRest)), Optional.of(parserResult.getPathParams()),
					Optional.of(parserResult.getQueryParams()));
			if (response.getResponse() == 200) {
				final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
				result.setCount(1);
				return result;
			} else {
				throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.API, response.getBody()),
						"Unable to update data in REST API Datasource. Error " + response.getResponse() + "."
								+ response.getBody());
			}

		} else {
			log.error("No Update by id operation was found for this Ontology. Ontology = {}.", ontology);
			throw new DBPersistenceException(
					new NotSupportedOperationException("No Update by id operation was found for this Ontology."));
		}

	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String findById(String ontology, String objectId) {
		// Change implementation if default ID is added to this type of ontology
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {

		final OntologyRest ontologyRest = ontologRestService.getOntologyRestByIdentification(ontology);
		final List<String> pathParamNames = new ArrayList<>();
		final List<String> queryParamsNames = new ArrayList<>();

		// get headers and security
		final Map<String, String> headers = getHeadersAndSecurity(ontologyRest);

		String completeUrl = ontologyRest.getBaseUrl();

		// Parse QUERY
		QueryAnalysisResult analyzedQuery = sqlRestParser.parseRestSqlStatement(query, pathParamNames,
				queryParamsNames);
		if (!analyzedQuery.getState()) {
			throw new DBPersistenceException(analyzedQuery.getErrorMessage());
		}
		if (analyzedQuery.getQueryType() != QueryType.SELECT) {
			throw new DBPersistenceException(
					new NotSupportedOperationException("Only SELECT operations are allowed but "
							+ analyzedQuery.getQueryType().toString() + " was received."));
		}

		final String operationName = analyzedQuery.getOperation();

		// Get complete information of operation
		OntologyRestOperation operation = null;
		if (operationName == null) {
			// If no operation was specified in query, check if there is a
			// getAll defined
			final List<OntologyRestOperation> operations = ontologyRestOperationService
					.getAllOperationsFromOntologyRest(ontologyRest);
			final Optional<OntologyRestOperation> optionalOperation = operations.stream()
					.filter(oper -> oper.getDefaultOperationType() == DefaultOperationType.GET_ALL).findAny();
			if (!optionalOperation.isPresent()) {
				throw new DBPersistenceException(new NotSupportedOperationException(
						"No operation was selected and no GET_ALL operation by default was found."));
			}
			operation = optionalOperation.get();
		} else {
			operation = ontologyRestOperationService.getOntologyRestOperationByName(ontologyRest, operationName);
			if (operation == null) {
				throw new DBPersistenceException(new NotSupportedOperationException(
						"The specified operation '" + operation + "' was not found."));
			}
		}
		completeUrl += operation.getPath();

		final List<OntologyRestOperationParam> params = ontologyRestOperationService
				.getOntologyRestOperationParams(operation);
		for (final OntologyRestOperationParam param : params) {
			if (param.getType() == ParamOperationType.PATH) {
				pathParamNames.add(param.getName());
			} else {
				queryParamsNames.add(param.getName());
			}
		}
		// Parse again with parameters set to the operation selected
		analyzedQuery = sqlRestParser.parseRestSqlStatement(query, pathParamNames, queryParamsNames);

		final Optional<Map<String, String>> pathParams = Optional.of(analyzedQuery.getPathParams());
		final Optional<Map<String, String>> queryParams = Optional.of(analyzedQuery.getQueryParams());

		final APIRestResponse response = apiRestClient.invokeGet(completeUrl, Optional.of(headers), pathParams,
				queryParams);

		if (response.getResponse() == 200) {
			String jsonPath = "";
			// Apply filters
			final String filters = analyzedQuery.getJsonPathfilter();
			if (filters.isEmpty()) {
				jsonPath = "$.[*]";
			} else {
				jsonPath = "$.[?(" + filters + ")]";
			}
			// Apply Select clause if != *
			final List<String> selectedFfields = analyzedQuery.getProjectionFields().stream()
					.filter(field -> !("_id".equalsIgnoreCase(field) || "*".equalsIgnoreCase(field)))
					.collect(Collectors.toList());
			if (!analyzedQuery.isSelectAll() && !selectedFfields.isEmpty()) {
				final StringBuffer selectFilterBuffer = new StringBuffer("[");

				for (final String field : selectedFfields) {
					selectFilterBuffer.append("'" + field + "',");
				}
				selectFilterBuffer.deleteCharAt(selectFilterBuffer.length() - 1);
				selectFilterBuffer.append(']');
				jsonPath += selectFilterBuffer.toString();
			}
			String responseJson = JsonPath.read(response.getBody(), jsonPath).toString();
			// filter SKIP and LIMIT
			String skip = "0";
			if (analyzedQuery.getHasSkip()) {
				skip = String.valueOf(analyzedQuery.getSkip());
			}
			String limit = "";
			if (analyzedQuery.getHasLimit()) {
				limit = String.valueOf(analyzedQuery.getLimit());
			}
			jsonPath = "$.[" + skip + ":" + limit + "]";
			responseJson = JsonPath.read(responseJson, jsonPath).toString();
			return responseJson;
		} else {
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.API, response.getBody()),
					"Unable to run query in REST API Datasource. Error " + response.getResponse() + "."
							+ response.getBody());
		}
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		// Set Skip clause
		query = query.replaceAll("\n", " ").trim();
		if (offset > 0) {
			String offsetClause = " SKIP " + offset;
			if (query.endsWith(";")) {
				offsetClause += ";";
			}
			query += offsetClause;
		}
		return querySQLAsJson(ontology, query);

	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public String findAllAsJson(String ontology) {
		return querySQLAsJson(ontology, SELECT_ALL_FROM + ontology);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		return querySQLAsJson(ontology, SELECT_ALL_FROM + ontology + " LIMIT " + limit);
	}

	@Override
	public List<String> findAll(String ontology) {
		return findAll(ontology, 0);
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		String json = querySQLAsJson(ontology, SELECT_ALL_FROM + ontology);
		if (limit > 0) {
			json += " LIMIT " + limit;
		}

		final List<String> result = new ArrayList<>();
		ArrayNode arrayResult;
		try {
			arrayResult = (ArrayNode) mapper.readTree(json);
			for (final JsonNode node : arrayResult) {
				result.add(node.toString());
			}
		} catch (final IOException e) {
			log.error("Error parsing query result to array. Cause ={}, message = {}", e.getCause(), e.getMessage());
			throw new DBPersistenceException("Error parsing query result to array", e);
		}

		return result;
	}

	@Override
	public long count(String ontology) {
		return findAll(ontology).size();
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		// Check if DELETE_ALL exists
		final OntologyRest ontologyRest = ontologRestService.getOntologyRestByIdentification(ontology);

		final List<OntologyRestOperation> operations = ontologyRestOperationService
				.getAllOperationsFromOntologyRest(ontologyRest);
		final OntologyRestOperation deleteAllOperation = operations.stream()
				.filter(operation -> operation.getDefaultOperationType() == DefaultOperationType.DELETE_ALL).findFirst()
				.orElse(null);

		if (deleteAllOperation != null) {

			// get headers and security
			final Map<String, String> headers = getHeadersAndSecurity(ontologyRest);
			final long numRegs = count(ontology);
			final String completeUrl = ontologyRest.getBaseUrl() + deleteAllOperation.getPath();
			final APIRestResponse response = apiRestClient.invokeDelete(completeUrl, Optional.of(headers),
					Optional.of(new HashMap<String, String>()), Optional.of(new HashMap<String, String>()));
			if (response.getResponse() == 200) {
				final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
				result.setCount(numRegs);
				return result;
			} else {
				throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.API, response.getBody()),
						"Unable to delete data in REST API Datasource. Error " + response.getResponse() + "."
								+ response.getBody());
			}
		} else {
			log.error("No Delete all operation was found for this Ontology. Ontology = {}.", ontology);
			throw new DBPersistenceException(
					new NotSupportedOperationException("No Delete by id operation was found for this Ontology."));
		}
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {

		final OntologyRest ontologyRest = ontologRestService.getOntologyRestByIdentification(collection);
		final List<String> pathParamNames = new ArrayList<>();
		final List<String> queryParamsNames = new ArrayList<>();

		// get headers and security
		final Map<String, String> headers = getHeadersAndSecurity(ontologyRest);

		String completeUrl = ontologyRest.getBaseUrl();

		// Parse QUERY
		QueryAnalysisResult analyzedQuery = sqlRestParser.parseRestSqlStatement(query, pathParamNames,
				queryParamsNames);
		if (!analyzedQuery.getState()) {
			throw new DBPersistenceException(analyzedQuery.getErrorMessage());
		}
		OntologyRestOperation deleteOperation = null;
		if (analyzedQuery.getOperation() != null) {
			deleteOperation = ontologyRestOperationService.getOntologyRestOperationByName(ontologyRest,
					analyzedQuery.getOperation());
		} else {
			final List<OntologyRestOperation> operations = ontologyRestOperationService
					.getAllOperationsFromOntologyRest(ontologyRest);
			deleteOperation = operations.stream()
					.filter(operation -> operation.getDefaultOperationType() == DefaultOperationType.DELETE_BY_ID)
					.findFirst().orElse(null);
		}

		if (deleteOperation != null) {
			final List<OntologyRestOperationParam> params = ontologyRestOperationService
					.getOntologyRestOperationParams(deleteOperation);
			params.forEach(param -> {
				if (param.getType() == ParamOperationType.PATH) {
					pathParamNames.add(param.getName());
				}
				if (param.getType() == ParamOperationType.QUERY) {
					queryParamsNames.add(param.getName());
				}
			});
			analyzedQuery = sqlRestParser.parseRestSqlStatement(query, pathParamNames, queryParamsNames);
			completeUrl = ontologyRest.getBaseUrl() + deleteOperation.getPath();
			final APIRestResponse response = apiRestClient.invokeDelete(completeUrl, Optional.of(headers),
					Optional.of(analyzedQuery.getPathParams()), Optional.of(analyzedQuery.getQueryParams()));
			if (response.getResponse() == 200) {
				return new MultiDocumentOperationResult();
			} else {
				throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.API, response.getBody()),
						"Unable to delete data in REST API Datasource. Error " + response.getResponse() + "."
								+ response.getBody());
			}
		} else {
			log.error("No Delete by id operation was found for this Ontology. Ontology = {}.", collection);
			throw new DBPersistenceException(
					new NotSupportedOperationException("No Delete by id operation was found for this Ontology."));
		}
	}

	@Override
	public long countNative(String collectionName, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		// Change implementation if default ID is added to this type of ontology
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		// Change implementation if default ID is added to this type of ontology
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_SUPPORTED));
	}

	private Map<String, String> getHeadersAndSecurity(OntologyRest ontologyRest) {
		// Implement security into headers
		final Map<String, String> headers = new HashMap<>();
		final ObjectMapper mapperObj = new ObjectMapper();
		final TypeReference<Map<String, String>> mapType = new TypeReference<Map<String, String>>() {
		};
		Map<String, String> securityConf;
		try {
			securityConf = mapperObj.readValue(ontologyRest.getSecurityId().getConfig(), mapType);
		} catch (final IOException e1) {
			throw new DBPersistenceException(
					new NotSupportedOperationException("Defined Security Headers could no be retrieved"));
		}

		final String user = securityConf.get("user");
		final String password = securityConf.get("password");
		String auth;
		switch (ontologyRest.getSecurityType()) {
		case API_KEY:
			headers.put(securityConf.get("header"), securityConf.get("token"));
			break;
		case BASIC:
			auth = "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
			headers.put("Authorization", auth);
			break;
		case OAUTH:
			auth = "Bearer " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
			headers.put("Authorization", auth);
			break;
		default:
			break;
		}
		// transform headersConfig into map
		final String headersConfig = ontologyRest.getHeaderId().getConfig();
		final TypeReference<Map<String,String>[]> typeRef = new TypeReference<Map<String,String>[]>() {
		};

		try {
			//final List<Map<String, String>> headersList = mapperObj.readValue(headersConfig, typeRef);
			final Map<String, String>[] headersList = mapperObj.readValue(headersConfig, typeRef);
			for (final Map<String, String> headerMap : headersList) {
				headers.put(headerMap.get("key"), headerMap.get("value"));
			}
		} catch (final IOException e) {
			throw new DBPersistenceException("Defined Headers could no be retrieved", e);
		}
		return headers;
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {

		return this.querySQLAsJson(ontology, query, offset);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

}
