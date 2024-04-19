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
package com.minsait.onesait.platform.router.service.app.service.crud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.DBResult;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontologydata.DataClassValidationException;
import com.minsait.onesait.platform.config.services.ontologydata.DataSchemaValidationException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualRelationalOntologyOpsDBRepository;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.OntologyReferencesValidation;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.router.audit.aop.Auditable;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.ProcessExecutionService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouterCrudServiceImpl implements RouterCrudService {

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsService;

	@Autowired
	private RouterCrudCachedOperationsService routerCrudCachedOperationsService;

	@Autowired
	private OntologyDataService ontologyDataService;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private VirtualRelationalOntologyOpsDBRepository virtualRepo;

	@Autowired
	private OntologyReferencesValidation referencesValidation;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Autowired
	private ProcessExecutionService processExecutionService;
	private final ExecutorService proccessExecutor = Executors.newSingleThreadExecutor();

	private static final String ERROR_STR = "ERROR";
	private static final String INSERT_STR = "INSERT";
	private static final String INSERT_ERROR = "Error inserting data";

	@Override
	@Auditable
	public OperationResultModel insert(OperationModel operationModel) throws RouterCrudServiceException {
		if (log.isDebugEnabled()) {
			log.debug("Insert: {}", operationModel.toString());
		}
		final OperationResultModel result = new OperationResultModel();
		final String METHOD = operationModel.getOperationType().name();
		final String ontologyName = operationModel.getOntologyName();

		Integer count = 0;
		String output = "";
		result.setMessage("OK");
		result.setStatus(true);
		RtdbDatasource rtdbDatasource = null;

		try {
			final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);

			rtdbDatasource = ontology.getRtdbDatasource();

			try {
				referencesValidation.validate(operationModel, ontology);
			} catch (final Exception e) {
				//ADD EXCEPTION FOR NEBULA QUERIES
				if (!RtdbDatasource.NEBULA_GRAPH.equals(rtdbDatasource)) {
					log.error("Could not validate references {}", e.getMessage());
					if (e instanceof OntologyDataJsonProblemException) {
						throw e;
					}
				}
			}

			if (METHOD.equalsIgnoreCase("POST")
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.INSERT.name())) {

				if (rtdbDatasource.equals(RtdbDatasource.VIRTUAL) || rtdbDatasource.equals(RtdbDatasource.PRESTO)) {
					final List<String> processedData = ontologyDataService.preProcessInsertData(operationModel, false,
							ontology);
					final ComplexWriteResult data = basicOpsService.insertBulk(ontologyName, processedData, true, true);
					final List<? extends DBResult> results = data.getData();
					final InsertResult insertResult = new InsertResult();
					final MultiDocumentOperationResult insertResultData = new MultiDocumentOperationResult();
					final ArrayList<String> idList = new ArrayList<>();

					if (!results.isEmpty()) {
						results.stream().map(DBResult::getId).map(Optional::ofNullable).filter(Optional::isPresent)
						.map(Optional::get).forEach(idList::add);
					}
					count = results.size();
					insertResultData.setCount(results.size());
					insertResultData.setIds(idList);
					insertResult.setType(ComplexWriteResultType.BULK);
					insertResult.setData(insertResultData);
					output = insertResult.toString();
				} else if (rtdbDatasource.equals(RtdbDatasource.AI_MINDS_DB)) {
					final String query = "SELECT * FROM " + ontology.getIdentification() + " WHERE when_data='"
							+ operationModel.getBody() + "'";
					output = queryToolService.querySQLAsJson(operationModel.getUser(), ontology.getIdentification(),
							query, 0);
				} else if (rtdbDatasource.equals(RtdbDatasource.NEBULA_GRAPH)) {
					output = queryToolService.querySQLAsJson(operationModel.getUser(), ontology.getIdentification(),
							operationModel.getBody(), 0);
				} else {

					final List<String> processedData = ontologyDataService.preProcessInsertData(operationModel,
							ontology.isContextDataEnabled(), ontology);

					final ComplexWriteResult data = basicOpsService.insertBulk(ontologyName, processedData, true, true);

					final InsertResult insertResult = new InsertResult();

					final List<? extends DBResult> results = data.getData();

					if (data.getType() == ComplexWriteResultType.BULK) {

						insertResult.setType(ComplexWriteResultType.BULK);

						final MultiDocumentOperationResult insertResultData = new MultiDocumentOperationResult();

						if (results.size() > 1) {
							final List<String> lIds = new ArrayList<>();
							for (final DBResult inserted : results) {
								if (inserted.isOk()) {
									lIds.add(inserted.getId());
								} else {
									throw new GenericOPException(inserted.getErrorMessage());
								}
							}

							insertResultData.setCount(lIds.size());
							insertResultData.setIds(lIds);

							insertResult.setData(insertResultData);

							output = insertResult.toString();
						} else if (!results.isEmpty()) {// Single message Insert
							final List<String> lIds = new ArrayList<>();
							if (results.get(0).isOk()) {
								lIds.add(((BulkWriteResult) results.get(0)).getId());
							} else {
								throw new GenericOPException(results.get(0).getErrorMessage());
							}

							insertResultData.setCount(1);
							insertResultData.setIds(lIds);

							insertResult.setData(insertResultData);

							output = insertResult.toString();
						}

					} else if (data.getType() == ComplexWriteResultType.TIME_SERIES) {
						insertResult.setType(ComplexWriteResultType.TIME_SERIES);
						insertResult.setData(results);
						output = insertResult.toString();
					}
					count = results.size();
				}
			}
		} catch (final DataSchemaValidationException e) {
			log.error("Error validating Schema of the Ontology", e);
			result.setResult(ERROR_STR);
			result.setStatus(false);
			result.setMessage("Error validating schema of the ontology:" + e.getMessage());
			result.setErrorCode("ErrorValidationSchema");
			result.setOperation(INSERT_STR);
			final int c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
			throw new RouterCrudServiceException(INSERT_ERROR, e, result);
		} catch (final DataClassValidationException e) {
            log.error("Error preprocessing insert data", e);
            result.setResult(ERROR_STR);
            result.setStatus(false);
            result.setMessage("Error preprocessing insert data: " + e.getMessage());
            result.setErrorCode("ErrorDataClass");
            result.setOperation(INSERT_STR);
            final int c = count;
            proccessExecutor.execute(() -> {
                processExecutionService.checkOperation(operationModel, result, c);
            });
            throw new RouterCrudServiceException(INSERT_ERROR + ": " + e.getMessage(), e, result);
		} catch (final OntologyDataJsonProblemException e) {
			log.error("Error validating ontology references", e);
			result.setResult(ERROR_STR);
			result.setStatus(false);
			result.setMessage("Error validating ontology references:" + e.getMessage());
			result.setErrorCode("ErrorValidationReferences");
			result.setOperation(INSERT_STR);
			final int c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
			throw new RouterCrudServiceException(INSERT_ERROR, e, result);
		} catch (final DBPersistenceException e) {
			log.error("insert", e);
			result.setResult(ERROR_STR);
			result.setStatus(false);
			result.setMessage(e.getMessage());
			result.setErrorCode("");
			result.setOperation(INSERT_STR);
			final int c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
			throw new RouterCrudServiceException(INSERT_ERROR, e, result);
		} catch (final Exception e) {
		    log.error("insert", e);
			result.setResult(ERROR_STR);
			result.setStatus(false);
			result.setMessage(e.getMessage());
			result.setErrorCode("");
			result.setOperation(INSERT_STR);
			final int c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
			throw new RouterCrudServiceException(e, result);
		}
		result.setResult(output);
		result.setOperation(METHOD);
		final int c = count;
		proccessExecutor.execute(() -> {
			processExecutionService.checkOperation(operationModel, result, c);
		});
		return result;
	}

	@Override
	@Auditable
	public OperationResultModel update(OperationModel operationModel) {
		if (log.isDebugEnabled()) {
			log.debug("Update: {}", operationModel.toString());
		}
		final OperationResultModel result = new OperationResultModel();

		final String method = operationModel.getOperationType().name();
		final String body = operationModel.getBody();
		final String ontologyName = operationModel.getOntologyName();
		final String objectId = operationModel.getObjectId();
		final String user = operationModel.getUser();
		final boolean includeIds = operationModel.isIncludeIds();
		final String clientPlatform = operationModel.getDeviceTemplate();

		Integer count = 0;
		String output = "";
		result.setMessage("OK");
		result.setStatus(true);
		try {
			final RtdbDatasource rtdbDatasource = ontologyRepository.findByIdentification(ontologyName)
					.getRtdbDatasource();
			if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase(OperationModel.OperationType.UPDATE.name())) {
				if (rtdbDatasource.equals(RtdbDatasource.VIRTUAL)) {
					final MultiDocumentOperationResult operationResult;
					if (objectId != null && !objectId.isEmpty()) {
						operationResult = virtualRepo.updateNativeByObjectIdAndBodyData(ontologyName, objectId, body);
					} else {
						operationResult = virtualRepo.updateNative(ontologyName, body, includeIds);
					}
					if (operationResult.getCount() == 0) {
						output = "[]"; // It needs to return an empty array as the methods from non relational ontology
					} else {
						output = operationResult.toString();
					}
					count = new Long(operationResult.getCount()).intValue();
				} else {
					if (objectId != null && !objectId.isEmpty()) {
						final String processedBody = ontologyDataService.preProcessUpdateData(operationModel);
						basicOpsService.updateNativeByObjectIdAndBodyData(ontologyName, objectId, processedBody);
						final String query;
						if (rtdbDatasource.equals(RtdbDatasource.MONGO)) {// OID Search explicit return of OID
							query = getQueryForOid(ontologyName, objectId);
							output = executeSQLQuery(query, ontologyName, user, clientPlatform);
						} else if (rtdbDatasource.equals(RtdbDatasource.ELASTIC_SEARCH)) {// _id Search explicit return
							// _id
							query = getQueryForId(ontologyName, objectId);
							output = executeSQLQuery(query, ontologyName, user, clientPlatform);
						} else {// generic _id search no explicit return of oid (inside data)
							output = basicOpsService.findById(ontologyName, objectId);
						}
						count = 1;
					} else {
						switch (operationModel.getQueryType()) {
						case SQL:
							output = executeSQLQuery(body, ontologyName, user, clientPlatform);
							count = 1;
							break;
						case NATIVE:
						default:
							if (body.split("db.".concat(ontologyName)).length > 2) {
								final ComplexWriteResult data = basicOpsService.updateBulk(ontologyName, body,
										includeIds);
								final InsertResult insertResult = new InsertResult();

								final List<? extends DBResult> results = data.getData();

								if (data.getType() == ComplexWriteResultType.BULK) {

									insertResult.setType(ComplexWriteResultType.BULK);

									final MultiDocumentOperationResult insertResultData = new MultiDocumentOperationResult();

									if (results.size() > 1) {
										final List<String> lIds = new ArrayList<>();
										for (final DBResult inserted : results) {
											if (inserted.isOk()) {
												if (inserted.getId() != null) {
													lIds.add(inserted.getId());
												}
											} else {
												throw new GenericOPException(inserted.getErrorMessage());
											}
										}

										insertResultData.setCount(lIds.size());
										insertResultData.setIds(lIds);

										insertResult.setData(insertResultData);

										output = insertResult.toString();
									} else if (!results.isEmpty()) {// Single message Insert
										final List<String> lIds = new ArrayList<>();
										if (results.get(0).isOk()) {
											lIds.add(((BulkWriteResult) results.get(0)).getId());
										} else {
											throw new GenericOPException(results.get(0).getErrorMessage());
										}

										insertResultData.setCount(1);
										insertResultData.setIds(lIds);

										insertResult.setData(insertResultData);

										output = insertResult.toString();
									}
									count = results.size();
								}
							} else {
								output = basicOpsService.updateNative(ontologyName, body, includeIds).toString();
								count = 1;
							}
							break;

						}
					}
				}
			}
		} catch (final Exception e) {
			log.error("update", e);
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
			final int c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
		}

		result.setResult(output);
		result.setOperation(method);
		final Integer c = count;
		proccessExecutor.execute(() -> {
			processExecutionService.checkOperation(operationModel, result, c);
		});

		return result;
	}

	private String executeSQLQuery(final String body, final String ontologyName, final String user,
			final String clientPlatform) throws OntologyDataUnauthorizedException, GenericOPException {
		final String result;
		if (clientPlatform != null && !clientPlatform.isEmpty()) {
			result = queryToolService.querySQLAsJsonForPlatformClient(clientPlatform, ontologyName, body, 0);
		} else {
			result = queryToolService.querySQLAsJson(user, ontologyName, body, 0);
		}

		return extractUpdatedResource(result);
	}

	private String extractUpdatedResource(final String result) {
		try {
			if (result != null && !result.isEmpty()) {
				final ArrayNode updatedResource = (ArrayNode) mapper.readTree(result);
				if (updatedResource.size() > 0) {
					return mapper.writeValueAsString(updatedResource.get(0));
				}
			}
		} catch (final Exception e) {
			log.error("Could not extract updated resource from array");
		}
		return result;
	}

	@Override
	@Auditable
	public OperationResultModel delete(OperationModel operationModel) {
		log.debug("Delete: {}", operationModel.toString());
		final OperationResultModel result = new OperationResultModel();
		final String METHOD = operationModel.getOperationType().name();
		final String BODY = operationModel.getBody();
		final String ontologyName = operationModel.getOntologyName();
		final String OBJECT_ID = operationModel.getObjectId();
		final boolean INCLUDEIDs = operationModel.isIncludeIds();
		final String USER = operationModel.getUser();
		final String CLIENTPLATFORM = operationModel.getDeviceTemplate();

		Integer count = 0;
		String output = "";
		result.setMessage("OK");
		result.setStatus(true);
		try {
			final RtdbDatasource rtdbDatasource = ontologyRepository.findByIdentification(ontologyName)
					.getRtdbDatasource();
			if (METHOD.equalsIgnoreCase("DELETE")
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.DELETE.name())) {

				if (rtdbDatasource.equals(RtdbDatasource.VIRTUAL)) {
					if (OBJECT_ID != null && OBJECT_ID.length() > 0) {
						output = String.valueOf(virtualRepo.deleteNativeById(ontologyName, OBJECT_ID));
					} else {
						output = String.valueOf(virtualRepo.deleteNative(ontologyName, BODY, INCLUDEIDs));
					}
					count = 1;
				} else {

					if (OBJECT_ID != null && OBJECT_ID.length() > 0) {
						output = basicOpsService.deleteNativeById(ontologyName, OBJECT_ID).toString();
						count = OBJECT_ID.length();
					} else {
						if (operationModel.getQueryType().equals(QueryType.SQL)) {
							output = !nullString(CLIENTPLATFORM)
									? queryToolService.querySQLAsJsonForPlatformClient(CLIENTPLATFORM, ontologyName,
											BODY, 0)
											: queryToolService.querySQLAsJson(USER, ontologyName, BODY, 0);
						} else {
							output = basicOpsService.deleteNative(ontologyName, BODY, INCLUDEIDs).toString();
						}
						count = 1;
					}

				}
			}
		} catch (final Exception e) {
			log.error("delete", e);
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
			final Integer c = count;
			proccessExecutor.execute(() -> {
				processExecutionService.checkOperation(operationModel, result, c);
			});
		}
		result.setResult(output);
		result.setOperation(METHOD);

		final Integer c = count;
		proccessExecutor.execute(() -> {
			processExecutionService.checkOperation(operationModel, result, c);
		});

		return result;
	}

	@Override
	@Auditable
	public OperationResultModel query(OperationModel operationModel) {
		log.debug("Query: {}", operationModel.toString());
		OperationResultModel result = null;
		final boolean cacheable = operationModel.isCacheable();
		if (cacheable) {
			log.debug("QueryCache " + operationModel.toString());
			result = routerCrudCachedOperationsService.queryCache(operationModel);

		} else {
			log.debug("QueryNoCache" + operationModel.toString());
			result = queryNoCache(operationModel);
		}
		return result;

	}

	public OperationResultModel queryNoCache(OperationModel operationModel) {
		log.debug("queryNoCache: {}", operationModel.toString());
		final OperationResultModel result = new OperationResultModel();
		final String METHOD = operationModel.getOperationType().name();
		final String BODY = operationModel.getBody();
		final String QUERY_TYPE = operationModel.getQueryType().name();
		final String ontologyName = operationModel.getOntologyName();
		final String OBJECT_ID = operationModel.getObjectId();
		final String USER = operationModel.getUser();
		final String CLIENTPLATFORM = operationModel.getDeviceTemplate();

		String OUTPUT = "";
		result.setMessage("OK");
		result.setStatus(true);
		try {
			final RtdbDatasource rtdbDatasource = ontologyRepository.findByIdentification(ontologyName)
					.getRtdbDatasource();
			if (METHOD.equalsIgnoreCase("GET") || METHOD.equalsIgnoreCase(OperationModel.OperationType.QUERY.name())) {

				if (QUERY_TYPE != null) {
					if (QUERY_TYPE.equalsIgnoreCase(QueryType.SQL.name())) {
						// OUTPUT = queryToolService.querySQLAsJson(ontologyName, QUERY, 0);
						OUTPUT = !nullString(CLIENTPLATFORM)
								? queryToolService.querySQLAsJsonForPlatformClient(CLIENTPLATFORM, ontologyName, BODY,
										0)
										: queryToolService.querySQLAsJson(USER, ontologyName, BODY, 0);
					} else if (QUERY_TYPE.equalsIgnoreCase(QueryType.NATIVE.name())) {
						if (rtdbDatasource.equals(RtdbDatasource.VIRTUAL)) {
							OUTPUT = virtualRepo.queryNativeAsJson(ontologyName, BODY);
						} else {
							// OUTPUT = queryToolService.queryNativeAsJson(ontologyName, QUERY, 0,0);
							OUTPUT = !nullString(CLIENTPLATFORM)
									? queryToolService.queryNativeAsJsonForPlatformClient(CLIENTPLATFORM, ontologyName,
											BODY, 0, getMaxRegisters())
											: queryToolService.queryNativeAsJson(USER, ontologyName, BODY, 0,
													getMaxRegisters());
						}
					} else {
						OUTPUT = basicOpsService.findById(ontologyName, OBJECT_ID);
					}
				} else {
					OUTPUT = basicOpsService.findById(ontologyName, OBJECT_ID);
				}
			}
		} catch (final Exception e) {
			log.error("queryNoCache", e);
			result.setResult(OUTPUT);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(OUTPUT);
		result.setOperation(METHOD);
		return result;
	}

	@Override
	// @Auditable
	public OperationResultModel execute(OperationModel operationModel) {
		log.debug("Execute: {}", operationModel.toString());
		final String METHOD = operationModel.getOperationType().name();
		OperationResultModel result = new OperationResultModel();
		try {
			if (METHOD.equalsIgnoreCase("GET") || METHOD.equalsIgnoreCase(OperationModel.OperationType.QUERY.name())) {
				result = query(operationModel);
			}

			if (METHOD.equalsIgnoreCase("POST")
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.INSERT.name())) {
				result = insert(operationModel);
			}
			if (METHOD.equalsIgnoreCase("PUT") || METHOD.equalsIgnoreCase(OperationModel.OperationType.UPDATE.name())) {
				result = update(operationModel);
			}
			if (METHOD.equalsIgnoreCase("DELETE")
					|| METHOD.equalsIgnoreCase(OperationModel.OperationType.DELETE.name())) {
				result = delete(operationModel);
			}
		} catch (final Exception e) {
			log.error("execute", e);
		}
		return result;
	}

	public QueryToolService getQueryToolService() {
		return queryToolService;
	}

	public void setQueryToolService(QueryToolService queryToolService) {
		this.queryToolService = queryToolService;
	}

	public static boolean nullString(String l) {
		if (l == null) {
			return true;
		} else {
			return l.equalsIgnoreCase("");
		}
	}

	@Override
	public OperationResultModel insertWithNoAudit(OperationModel model) throws RouterCrudServiceException {
		return insert(model);
	}

	private String getQueryForId(String ontology, String oid) {
		return "select c,_id from ".concat(ontology).concat(" as c where _id=\"").concat(oid).concat("\"");
	}

	private String getQueryForOid(String ontology, String oid) {

		if (useQuasar()) {
			return "select c,_id from ".concat(ontology).concat(" as c where _id=OID(\"").concat(oid).concat("\")");
		} else {
			return "select * from ".concat(ontology).concat(" as c where _id=OID('").concat(oid).concat("')");
		}
	}

	private int getMaxRegisters() {
		return ((Integer) resourcesServices.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
				.intValue();
	}

	private boolean useQuasar() {
		try {
			return ((Boolean) resourcesServices.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-quasar")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}
}
