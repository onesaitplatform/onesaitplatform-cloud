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
package com.minsait.onesait.platform.persistence.mongodb.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoNativeManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.UtilMongoDB;
import com.minsait.onesait.platform.persistence.mongodb.tools.sql.Sql2NativeTool;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component("QueryAsTextMongoDBRepository")
@Scope("prototype")
@Slf4j
public class QueryAsTextMongoDBImpl implements QueryAsTextDBRepository {

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	MongoBasicOpsDBRepository mongoRepo = null;

	@Autowired
	@Qualifier("MongoManageDBRepository")
	MongoNativeManageDBRepository manageRepo = null;

	@Autowired
	UtilMongoDB utils = null;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String ERROR_QUERYSQLASJSON = "Error querySQLAsJson:";

	private void checkQueryIs4Ontology(String ontology, String query, boolean sql) throws GenericOPException {
		query = query.replace("\n", "");
		while (query.contains("  ")) {
			query = query.replace("  ", " ");
		}
		if (sql) {
			if (query.toLowerCase().indexOf("from ") == -1 && query.toLowerCase().indexOf("update ") == -1
					&& query.toLowerCase().indexOf("join ") == -1) {
				throw new QueryNativeFormatException("Malformed SQL Query");
			} else if (query.toLowerCase().indexOf("from " + ontology.toLowerCase()) == -1
					&& query.toLowerCase().indexOf("update " + ontology.toLowerCase()) == -1
					&& query.toLowerCase().indexOf("join " + ontology.toLowerCase()) == -1) {
				throw new GenericOPException("The query '" + query + "' is not for the ontology selected: " + ontology);
			}
		} else {
			if (query.indexOf("db.") == -1) {
				return;
			}
			if (query.indexOf("." + ontology + ".") == -1) {
				throw new DBPersistenceException(
						"The query " + query + " is not for the ontology selected:" + ontology);
			}
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		try {
			checkQueryIs4Ontology(ontology, query, false);
			return mongoRepo.queryNativeAsJson(ontology, query, offset, limit);
		} catch (final Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		String queryContent = null;
		try {
			checkQueryIs4Ontology(ontology, query, false);
			queryContent = utils.getQueryContent(query);
			if (query.indexOf(".createIndex(") != -1) {
				manageRepo.createIndex(query);
				return "Created index indicated in the query:" + query;
			} else if (query.indexOf(".getIndexes(") != -1) {
				return manageRepo.getIndexes(ontology);
			} else if (query.indexOf(".insert(") != -1) {
				return "Inserted row with id:" + mongoRepo.insert(ontology, queryContent);
			} else if (query.indexOf(".update(") != -1) {
				return mongoRepo.updateNative(ontology, queryContent, false).toString();
			} else if (query.indexOf(".remove(") != -1) {
				return mongoRepo.deleteNative(ontology, queryContent, false).toString();
			} else if (query.indexOf(".dropIndex(") != -1) {
				query = query.substring(query.indexOf(".dropIndex(") + 11, query.length());
				query = query.replace("\"", "");
				query = query.replace("'", "");
				final String indexName = query.substring(0, query.indexOf(')'));
				manageRepo.dropIndex(ontology, indexName);
				return "Dropped index indicated in the query:" + query;
			} else if (query.indexOf(".getIndexes()") != -1) {
				return manageRepo.getIndexes(ontology);
			} else if (query.indexOf(".count(") != -1) {
				return "" + mongoRepo.countNative(ontology, queryContent);
			} else if (query.indexOf(".drop") != -1) {
				return "Drop a collection from QueryTool not supported.";
			} else {
				return mongoRepo.queryNativeAsJson(ontology, query);
			}
		} catch (final QueryNativeFormatException e) {
			throw e;
		} catch (final DBPersistenceException e) {
			log.error(ERROR_QUERYSQLASJSON + e.getDetailedMessage());
			throw e;

		} catch (final Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage(), e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		try {
			checkQueryIs4Ontology(ontology, query, true);
			if (query.trim().toLowerCase().startsWith("update") || query.trim().toLowerCase().startsWith("delete")
					|| query.trim().toLowerCase().startsWith("select") && !useQuasar()) {
				return surroundNativeWithValueNode(
						this.queryNativeAsJson(ontology, Sql2NativeTool.translateSql(query)));
			} else {
				return mongoRepo.querySQLAsJson(ontology, query, offset);
			}
		} catch (final DBPersistenceException e) {
			log.error(ERROR_QUERYSQLASJSON + e.getMessage(), e);
			throw e;
		} catch (final Exception e) {
			log.error(ERROR_QUERYSQLASJSON + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	// Surround with value like {"value":nativeValue} when return type is not json
	// or jsonarray
	private String surroundNativeWithValueNode(String resultQuery) {
		if (!resultQuery.isEmpty()) {
			final String firstChar = resultQuery.trim().substring(0, 1);
			if (firstChar.equals("[") || firstChar.equals("{")) {// fast check
				return resultQuery;
			} else {
				return "[{\"value\":" + resultQuery + "}]";
			}
		} else {
			return resultQuery;
		}
	}

	private boolean useQuasar() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-quasar")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}

}
