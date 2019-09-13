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
package com.minsait.onesait.platform.persistence.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.DBResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESCountService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESDataService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESDeleteService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESNativeService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESUpdateService;
import com.minsait.onesait.platform.persistence.elasticsearch.sql.connector.ElasticSearchSQLDbHttpImpl;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component("ElasticSearchBasicOpsDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class ElasticSearchBasicOpsDBRepository implements BasicOpsDBRepository {

	private static final String NOT_IMPLEMENTED = "Not implemented";

	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Autowired
	private ESCountService eSCountService;
	@Autowired
	private ESDataService eSDataService;
	@Autowired
	private ESDeleteService eSDeleteService;
	@Autowired
	private ESInsertService eSInsertService;
	@Autowired
	private ESUpdateService eSUpdateService;
	@Autowired
	private ElasticSearchSQLDbHttpImpl elasticSearchSQLDbHttpConnector;

	@Autowired
	private ESNativeService eSNativeService;

	private static final String ERROR_IN_QUERY_AS_TABLE = "Error in query SQL as table";
	private static final String ERROR_ONTOLOGY_CANT_BE_NULL = "Ontology can't be null or empty";
	private static final String ERROR_QUERY_CANT_BE_NULL = "Query can't be null or empty";
	private static final String ERROR_ID_CANT_BE_NULL = "ID can't be null or empty";
	private static final String ERROR_OFFSET = "Offset must be greater or equals to 0";
	private static final String ERROR_SCHEMA_CANT_BE_NULL = "Schema can't be null or empty";
	private static final String ERROR_INSTANCE_CANT_BE_NULL = "Instance can't be null or empty";
	private static final String ERROR_STATEMENT_CANT_BE_NULL = "Statement can't be null or empty";
	private static final String ERROR_COLLECTION_CANT_BE_NULL = "Collection can't be null or empty";
	private static final String ERROR_DATA_CANT_BE_NULL = "Data can't be null or empty";
	private static final String ERROR_LIMIT = "Limit must be greater or equals to 1";

	@Override
	public String insert(String ontology, String schema, String instance) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(schema, ERROR_SCHEMA_CANT_BE_NULL);
			Assert.hasLength(instance, ERROR_INSTANCE_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			log.debug("ElasticSearchBasicOpsDBRepository : Loading content: {} into elasticsearch  {}",
					instance, ontology);
			List<? extends DBResult> output = null;
			final List<String> instances = new ArrayList<>();
			instances.add(instance);
			output = eSInsertService.load(ontology, ontology, instances, schema).getData();
			return ((BulkWriteResult) output.get(0)).getId();
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error inserting instances", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error inserting instance :" + instance + " into :" + ontology);
		}
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, String schema, List<String> instances, boolean order,
			boolean includeIds) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(schema, ERROR_SCHEMA_CANT_BE_NULL);
			Assert.notEmpty(instances, "Instances can't be null or empty");
			ontology = ontology.toLowerCase();
			return eSInsertService.load(ontology, ontology, instances, schema);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error inserting instances", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error inserting instances :" + instances + " into :" + ontology);
		}
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(updateStmt, ERROR_STATEMENT_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			log.debug("ElasticSearchBasicOpsDBRepository :Update Native");
			SearchResponse output = null;
			output = eSNativeService.updateByQuery(ontology, ontology, updateStmt);
			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(output.getHits().totalHits);
			return result;
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in operation", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in operation ES updateNative");
		}
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		try {
			Assert.hasLength(collection, ERROR_COLLECTION_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			Assert.hasLength(data, ERROR_DATA_CANT_BE_NULL);
			collection = collection.toLowerCase();
			SearchResponse output = null;
			output = eSNativeService.updateByQueryAndFilter(collection, collection, data, query);
			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(output.getHits().totalHits);
			return result;
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error operating", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in operation ES updateNative");
		}
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String ontology, String query, boolean includeIds) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return eSDeleteService.deleteByQuery(ontology, ontology, query, includeIds);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error deleting", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in operation ES delete native");
		}
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return eSDataService.findQueryData(query, ontology);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in queryNative", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query native");
		}
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			Assert.isTrue(offset >= 0, ERROR_OFFSET);
			Assert.isTrue(limit >= 1, ERROR_LIMIT);
			ontology = ontology.toLowerCase();
			return eSDataService.findAllByType(ontology, query, offset, limit);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in queryNative", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query native");
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return eSDataService.findQueryDataAsJson(query, ontology);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in queryNativeAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query native as json");
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			Assert.isTrue(offset >= 0, ERROR_OFFSET);
			Assert.isTrue(limit >= 1, ERROR_LIMIT);
			ontology = ontology.toLowerCase();
			return (eSDataService.findAllByTypeAsJson(ontology, offset, limit));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in queryNativeAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query native as json");
		}
	}

	@Override
	public String findById(String ontology, String objectId) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(objectId, ERROR_ID_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return (eSDataService.findByIndex(ontology, ontology, objectId));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error finding", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in finding by ID");
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			return elasticSearchSQLDbHttpConnector.queryAsJson(query,
					((Integer) resourcesServices.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
							.intValue());
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in querySQLAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query SQL as json");
		}
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			throw new DBPersistenceException(ERROR_IN_QUERY_AS_TABLE, new NotImplementedException(NOT_IMPLEMENTED));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error(ERROR_IN_QUERY_AS_TABLE, e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					ERROR_IN_QUERY_AS_TABLE);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			return elasticSearchSQLDbHttpConnector.queryAsJson(query, offset);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in querySQLAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in query SQL as json");
		}
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(query, ERROR_QUERY_CANT_BE_NULL);
			Assert.isTrue(offset >= 0, ERROR_OFFSET);
			throw new DBPersistenceException(ERROR_IN_QUERY_AS_TABLE, new NotImplementedException(NOT_IMPLEMENTED));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error(ERROR_IN_QUERY_AS_TABLE, e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					ERROR_IN_QUERY_AS_TABLE);
		}
	}

	@Override
	public String findAllAsJson(String ontology) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return (eSDataService.findAllByTypeAsJson(ontology, 200));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in findAllAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in finding all as json");
		}
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.isTrue(limit >= 0, "Limit must be greater or equals to 0");
			ontology = ontology.toLowerCase();
			return (eSDataService.findAllByTypeAsJson(ontology, limit));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in findAllAsJson", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in finding all as json");
		}
	}

	@Override
	public List<String> findAll(String ontology) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return (eSDataService.findAllByType(ontology));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in findAll", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in finding all");
		}
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.isTrue(limit >= 0, "Limit must be greater or equals to 0");
			ontology = ontology.toLowerCase();
			return (eSDataService.findAllByType(ontology, limit));
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in findAll", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in finding all");
		}
	}

	@Override
	public long count(String ontology) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			return eSCountService.getMatchAllQueryCountByType(ontology, ontology);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error counting", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in count");
		}
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			ontology = ontology.toLowerCase();
			final boolean all = eSDeleteService.deleteAll(ontology, ontology);

			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			if (all) {
				result.setCount(1);
			} else {
				result.setCount(-1);
			}
			return result;
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error deleting native", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in delete native");
		}
	}

	@Override
	public long countNative(String ontology, String jsonQueryString) {
		try {
			Assert.hasLength(ontology, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(jsonQueryString, "json can't be null or empty");
			ontology = ontology.toLowerCase();
			return eSCountService.getQueryCount(jsonQueryString, ontology);
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error counting native", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in count native");
		}
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		try {
			Assert.hasLength(ontologyName, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(objectId, ERROR_ID_CANT_BE_NULL);
			ontologyName = ontologyName.toLowerCase();
			final boolean all = eSDeleteService.deleteById(ontologyName, ontologyName, objectId);
			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			if (all) {
				result.setCount(1);
			} else {
				result.setCount(-1);
			}
			return result;
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error deleting native", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in delete native by id");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		try {
			Assert.hasLength(ontologyName, ERROR_ONTOLOGY_CANT_BE_NULL);
			Assert.hasLength(objectId, ERROR_ID_CANT_BE_NULL);
			Assert.hasLength(body, "Body can't be null or empty");
			ontologyName = ontologyName.toLowerCase();
			final boolean response = eSUpdateService.updateIndex(ontologyName, ontologyName, objectId, body);
			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			if (response) {
				result.setCount(1);
			} else {
				result.setCount(-1);
			}
			return result;
		} catch (final DBPersistenceException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error updating native", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.ELASTIC, e.getMessage()),
					"Error in update native");
		}

	}
}
