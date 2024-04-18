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
package com.minsait.onesait.platform.persistence.mongodb.template;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.persistence.mongodb.MongoQueryAndParams;
import com.minsait.onesait.platform.persistence.mongodb.UtilMongoDB;
import com.minsait.onesait.platform.persistence.mongodb.config.MongoDbCredentials;
import com.minsait.onesait.platform.persistence.mongodb.index.MongoDbIndex;
import com.minsait.onesait.platform.persistence.mongodb.template.multitenant.MongoDBClientManager;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class MongoDbTemplateImpl implements MongoDbTemplate {

	private static final long serialVersionUID = 1L;

	@Autowired
	private UtilMongoDB util;

	private String writeConcern;

	@Autowired
	private MongoDbCredentials credentials;

	private List<ServerAddress> serverAddresses;

	@Autowired
	private MongoDBClientManager mongoDBClientManager;

	private ConcurrentHashMap<String, String> normalizedCollectionNames;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String ADMIN_DB = "admin";

	@PostConstruct
	public void init() throws DBPersistenceException {
		log.info("Initializing MongoDB connector...");
		normalizedCollectionNames = new ConcurrentHashMap<>();
		loadCentralMongoConfig();
	}

	@Override
	public MongoClient getConnection() {
		return mongoDBClientManager.electClient();
	}

	@Override
	public List<String> getCollectionNames(String database) throws DBPersistenceException {
		log.debug("Retrieving collection names. Database = {}.", database);
		try {
			return util.toJavaList(mongoDBClientManager.electClient().getDatabase(database).listCollectionNames());
		} catch (final Throwable e) {
			log.error("Unable to retrieve collection names. Database = {}, cause = {}, errorMessage = {}.", database,
					e.getCause(), e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public Boolean collectionExists(String database, String collection) {
		final MongoIterable<String> resultListCollectionNames = mongoDBClientManager.electClient().getDatabase(database)
				.listCollectionNames();
		if (null != resultListCollectionNames) {
			for (final String resultName : resultListCollectionNames) {
				if (resultName.equalsIgnoreCase(collection)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Collection<String> getDatabaseNames() throws DBPersistenceException {
		try {
			log.debug("Retrieving database names...");
			return util.toJavaCollection(mongoDBClientManager.electClient().listDatabaseNames());
		} catch (final Throwable e) {
			log.error("Unable to retrieve database names. Cause = {}, errorMessage = {}.", e.getCause(),
					e.getMessage());
			throw new DBPersistenceException("Unable to retrieve database names", e);
		}
	}

	@Override
	public Document getDatabaseStats(String database) throws DBPersistenceException {
		try {
			log.debug("Retrieving database stats. Database = {}.", database);
			return mongoDBClientManager.electClient().getDatabase(database).runCommand(new Document("dbstats", 1));
		} catch (final Throwable e) {
			log.error("Unable to retrieve database stats. Database = {}, cause = {}, errorMessage = {}.", database,
					e.getCause(), e.getMessage());
			throw new DBPersistenceException("Unable to retrieve database stats.", e);
		}
	}

	@Override
	public Document getCollectionStats(String database, String collection) throws DBPersistenceException {
		try {
			log.debug("Retrieving collection stats. Database = {}, collection = {}.", database, collection);
			return mongoDBClientManager.electClient().getDatabase(database)
					.runCommand(new Document("collStats", collection));
		} catch (final Throwable e) {
			log.error(
					"Unable to retrieve collection stats. Database = {}, collection = {}, cause = {}, errorMessage = {}.",
					database, collection, e.getCause(), e.getMessage());
			throw new DBPersistenceException("Unable to retrieve collection stats", e);
		}
	}

	@Override
	public List<String> getIndexesAsStrings(String database, String collection) throws DBPersistenceException {
		try {
			log.debug("Retrieving indexes. Database = {}, collection = {}.", database, collection);
			final ListIndexesIterable<Document> indexes = getCollection(database, collection, BasicDBObject.class)
					.listIndexes();
			final List<String> result = new ArrayList<>();
			for (final Document index_asDocument : indexes) {
				result.add(index_asDocument.toJson());
			}
			return result;
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to retrieve indexes. Database = %s, collection = %s, cause = %s, errorMessage = %s.",
					database, collection, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public List<MongoDbIndex> getIndexes(String database, String collection) throws DBPersistenceException {
		try {
			log.debug("Retrieving indexes. Database = {}, collection = {}.", database, collection);
			final ListIndexesIterable<Document> indexes = getCollection(database, collection, BasicDBObject.class)
					.listIndexes();
			final List<MongoDbIndex> result = new ArrayList<>();
			for (final Document index_asDocument : indexes) {
				result.add(MongoDbIndex.fromIndexDocument(index_asDocument));
			}
			return result;
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to retrieve indexes. Database = %s, collection = %s, cause = %s, errorMessage = %s.",
					database, collection, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public long count(String database, String collection, String query) throws DBPersistenceException {
		log.debug("Running count command. Database = {}, collection = {}, query = {}.", database, collection, query);
		try {
			if (!query.trim().equals("{}") && query.contains("count(")) {
				query = query.substring(query.indexOf("count(") + 6, query.indexOf(")"));
			}
			if (query != null && query != "" && !query.trim().equals("{}") && !query.isEmpty()) {
				final BasicDBObject queryObject = (BasicDBObject) JSON.parse(query);
				return getCollection(database, collection, BasicDBObject.class).count(queryObject);
			} else {
				return getCollection(database, collection, BasicDBObject.class).count();
			}
			// if (!query.trim().equals("{}")) {
			// BasicDBObject queryObject = (BasicDBObject) JSON.parse(query);
			// return getCollection(database, collection,
			// BasicDBObject.class).count(queryObject);
			// } else {
			// return getCollection(database, collection, BasicDBObject.class).count();
			// }
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to run count command. Database = %s, collection = %s, query = %s, cause = %s, errorMessage = %s.",
					database, collection, query, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void createCollection(String database, String collection) {
		log.debug("Creating the collection...Database = {}, Collection = {} .", database, collection);
		try {
			final String normalizedCollectionName = getNormalizedCollectionName(database, collection);
			final CreateCollectionOptions options = new CreateCollectionOptions();
			options.capped(false);
			options.autoIndex(true);
			mongoDBClientManager.electClient().getDatabase(database).createCollection(normalizedCollectionName,
					options);
		} catch (final Throwable e) {
			final String errorMessage = String
					.format("Unable to create the collection. Database = %s, collection = %s , "
							+ "cause = %s, errorMessage = %s.", database, collection, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public String createIndex(String database, String collection, MongoDbIndex index) {
		log.debug("Creating indexes. Database = {}, Collection = {}, Index = {}.", database, collection, index);
		try {
			final MongoCollection<?> dbCollection = getCollection(database, collection, BasicDBObject.class);
			final BasicDBObject indexKey = new BasicDBObject(index.getKey());
			IndexOptions nativeIndexOptions = null;
			if (index.getIndexOptions() != null) {
				nativeIndexOptions = index.getIndexOptions();
			}
			if (nativeIndexOptions == null) {
				return dbCollection.createIndex(indexKey);
			} else {
				return dbCollection.createIndex(indexKey, nativeIndexOptions);
			}
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to create indexes with the given keys. Database = %s , collection = %s,  index = %s, cause = %s , errorMessage = %s.",
					database, collection, index, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void createIndex(String database, String collection, Bson geo2dsphere) {
		log.debug("Creating geo2dsphere indexes. Database = {}, Collection = {}", database, collection);
		try {
			final MongoCollection<?> dbCollection = getCollection(database, collection, BasicDBObject.class);
			dbCollection.createIndex(geo2dsphere);
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to geo2dsphere create indexes with the given keys. Database = %s , collection = %s, cause = %s , errorMessage = %s.",
					database, collection, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MongoIterable<BasicDBObject> aggregate(String database, String collection, List<BasicDBObject> pipeline,
			boolean allowDiskUse) throws DBPersistenceException {
		log.debug("Running aggregate command. Database = {} , Collection = {} , Pipeline = {} ", database, collection,
				pipeline);
		try {
			if (pipeline == null || pipeline.isEmpty()) {
				throw new IllegalArgumentException(
						"The aggregation pipeline is required, and must contain at least one operation.");
			}
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			return dbCollection.aggregate(pipeline).allowDiskUse(allowDiskUse);
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to run aggregate command on the given collection. Database = %s , collection = %s , pipeline = %s, cause = %s , errorMessage = %s.",
					database, collection, pipeline, e.getCause(), e.getMessage());
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MongoIterable<BasicDBObject> aggregate(String database, String collection, List<Bson> pipeline,
			boolean allowDiskUse, long queryExecutionTimeoutMillis) throws DBPersistenceException {
		log.debug("Running aggregate command. Database = {} , Collection = {} , Pipeline = {} ", database, collection,
				pipeline);
		try {
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			return dbCollection.aggregate(pipeline).allowDiskUse(allowDiskUse).maxTime(queryExecutionTimeoutMillis,
					TimeUnit.MILLISECONDS);
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to run aggregate command on the given collection. Database = %s , collection = %s , pipeline = %s, cause = %s , errorMessage = %s.",
					database, collection, pipeline, e.getCause(), e.getMessage());
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public <T> MongoIterable<T> distinct(String database, String collection, String key, String query,
			Class<T> resultType) {
		log.debug(
				"Retrieving the distinct values from the given key/query. Database = {} , collection = {} , key = {} , query = {}.",
				database, collection, key, query);
		try {
			if (key == null || key.isEmpty()) {
				throw new IllegalArgumentException("The distinct field is required");
			}
			final MongoCollection<T> dbCollection = getCollection(database, collection, resultType);
			DistinctIterable<T> result = null;
			if (query == null) {
				dbCollection.distinct(key, resultType);
			} else {
				result = dbCollection.distinct(key, (BasicDBObject) JSON.parse(query), resultType);
			}
			return result;
		} catch (final JSONParseException e) {
			final String errorMessage = String.format(
					"Unable to parse JSON query. Query = %s, cause = %s, errorMessage = %s.", query, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to retrieve the values. Database = %s , collection = %s , key = %s , query =%s , cause = %S , errorMessage = %s.",
					database, collection, key, query, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void dropIndex(String database, String collection, MongoDbIndex index) {
		log.debug("Deleting the given index. Database = {} , collection = {} , index = {}.", database, collection,
				index);
		try {
			final MongoCollection<?> col = getCollection(database, collection, BasicDBObject.class);
			col.dropIndex(index.getName());
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to delete the entry. Database = %s , collection = %s , index = %s , cause = {} , errorMessage = %s.",
					database, collection, index, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public BasicDBObject findById(String database, String collection, String objectId) throws DBPersistenceException {
		// String
		// stmt="{db."+collection+".find({\"_id\":{\"$oid\":\""+objectId+"\"}})};";
		// { "_id" : { "$oid" : "5a326463a2b488aa28e8ed1c"}

		// MongoIterable<BasicDBObject> res= find(database, collection,
		// stmt,maxWaitTime);

		if (objectId.indexOf("_id") != -1) {
			objectId = objectId.replace("{", "");
			objectId = objectId.replace("}", "");
			objectId = objectId.replace("\"", "");
			objectId = objectId.replace("'", "");
			final String[] t = objectId.split(":");
			objectId = t[t.length - 1];
		}

		final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection, BasicDBObject.class);
		final FindIterable<BasicDBObject> res = dbCollection.find(new BasicDBObject("_id", new ObjectId(objectId)));
		if (res != null) {
			return res.first();
		}
		return null;
	}

	@Override
	public MongoIterable<BasicDBObject> findAll(String database, String collection, int skip, int limit,
			long queryExecutionTimeoutMillis) {
		log.debug(
				"Running query. Database = {} , collection = {} , query = {}, projection = {} , sort = {} , skip = {} , limit = {}, executionTimeOut = {}.",
				database, collection, skip, limit, queryExecutionTimeoutMillis);
		try {
			if (queryExecutionTimeoutMillis < 0) {
				throw new IllegalArgumentException("The query execution timeout must be greater than or equal to zero");
			}
			if (skip < 0) {
				throw new IllegalArgumentException("The skip value must be greater than or equal to zero");
			}
			if (limit < 0) {
				throw new IllegalArgumentException("The limit value must be greater than or equal to zero");
			}
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			FindIterable<BasicDBObject> result = null;
			result = dbCollection.find((Bson) JSON.parse("{}"));
			result = result.skip(skip);
			result = result.limit(limit);
			if (queryExecutionTimeoutMillis > 0) {
				result = result.maxTime(queryExecutionTimeoutMillis, TimeUnit.MILLISECONDS);
			}
			return result;
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to retrieve the required information. Database = %s , collection = %s , query = %s , projection = %s, "
							+ "sort = %s , skip = %s , limit = %s , queryExecutionTimeOut = %s, cause = %s, errorMessage = %s.",
					database, collection, skip, limit, queryExecutionTimeoutMillis, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MongoIterable<BasicDBObject> find(String database, String collection, MongoQueryAndParams mq,
			long queryExecutionTimeoutMillis) throws DBPersistenceException {
		if (mq.getAggregateQuery() != null) {
			return aggregate(database, collection, mq.getAggregateQuery(), mq.isAggregateAllowDiskUse(),
					queryExecutionTimeoutMillis);
		} else {
			return find(database, collection, mq.getFinalQuery(), mq.getProjection(), mq.getSort(), mq.getSkip(),
					mq.getLimit(), queryExecutionTimeoutMillis);
		}
	}

	@Override
	public MongoIterable<BasicDBObject> find(String database, String collection, Bson query, Bson projection, Bson sort,
			int skip, int limit, long queryExecutionTimeoutMillis) throws DBPersistenceException {
		log.debug(
				"Running query. Database = {} , collection = {} , query = {}, projection = {} , sort = {} , skip = {} , limit = {}, executionTimeOut = {}.",
				database, collection, query, projection, sort, skip, limit, queryExecutionTimeoutMillis);
		try {
			if (queryExecutionTimeoutMillis < 0) {
				throw new IllegalArgumentException("The query execution timeout must be greater than or equal to zero");
			}
			if (skip < 0) {
				throw new IllegalArgumentException("The skip value must be greater than or equal to zero");
			}
			if (limit < 0) {
				throw new IllegalArgumentException("The limit value must be greater than or equal to zero");
			}
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			FindIterable<BasicDBObject> result = null;
			if (projection == null) {
				result = dbCollection.find(query);
			} else {
				result = dbCollection.find(query).projection(projection);
			}
			if (sort != null) {
				result = result.sort(sort);
			}
			if (skip > 0) {
				result = result.skip(skip);
			}
			if (limit > 0) {
				result = result.limit(limit);
			}
			if (queryExecutionTimeoutMillis > 0) {
				result = result.maxTime(queryExecutionTimeoutMillis, TimeUnit.MILLISECONDS);
			}
			return result;
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to retrieve the required information. Database = %s , collection = %s , query = %s , projection = %s, "
							+ "sort = %s , skip = %s , limit = %s , queryExecutionTimeOut = %s, cause = %s, errorMessage = %s.",
					database, collection, query, projection, sort, skip, limit, queryExecutionTimeoutMillis,
					e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public ObjectId insert(String database, String collection, String data) throws DBPersistenceException {
		try {
			return insert(database, collection, (BasicDBObject) JSON.parse(data));
		} catch (final JSONParseException e) {
			final String errorMessage = String.format(
					"Unable to parse JSON data. Data = %s, cause = %s, errorMessage = %s.", data, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	private void fixPosibleNonCapitalizedGeometryPoint(BasicDBObject doc) {
		try {
			final Object geometry = doc.get("geometry");

			if (geometry instanceof BasicDBObject) {
				final BasicDBObject object = (BasicDBObject) geometry;
				if (object != null) {
					final String type = (String) object.get("type");
					if (type != null) {
						object.put("type", StringUtils.capitalize(type));
					}
				}

			} else if (geometry instanceof BasicDBList) {
				final BasicDBList object = (BasicDBList) geometry;
				final BasicDBObject coordinates = new BasicDBObject();
				coordinates.put("coordinates", object.copy());
				coordinates.put("type", "Point");
				doc.remove(geometry);
				doc.put("geometry", coordinates);

			}
		} catch (final Exception e) {
			log.error("" + e);
		}
	}

	@Override
	public ObjectId insert(String database, String collection, BasicDBObject doc) throws DBPersistenceException {
		log.debug(
				"Inserting the object into MongoDB. Database = {} , collection = {} , document = {}, writeConcern = {}.",
				database, collection, doc, writeConcern);

		fixPosibleNonCapitalizedGeometryPoint(doc);

		final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection, BasicDBObject.class);
		try {
			dbCollection.insertOne(doc);
			return doc.getObjectId("_id");
		} catch (final MongoException e) {
			final String errorMessage = String.format(
					"Unable to insert the object in MongoDB. Database = %s, collection = %s , document = %s , writeConcern = %s , cause = %s,"
							+ "errorMessage = %s.",
					database, collection, doc, writeConcern, e.getCause(), e.getMessage());
			log.error(errorMessage, e);

			if (e.getCode() == 11000) {
				throw new DBPersistenceException(e, new ErrorResult(ErrorResult.ErrorType.DUPLICATED,
						ErrorResult.PersistenceType.MONGO, e.getMessage()), e.getMessage());
			} else {
				throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.MONGO, e.getMessage()),
						e.getMessage());
			}
		}
	}

	@Override
	public List<BulkWriteResult> bulkInsert(String database, String collection, List<String> data, boolean orderedOp,
			boolean includeObjectIds) throws DBPersistenceException {

		final BulkWriteResult[] bwResults = new BulkWriteResult[data.size()];
		log.debug(
				"Performing bulk insert operation. Database= {}, collection = {} , data = {} , writeConcern = {} , orderedOperation = {}, includeObjectIds = {}.",
				database, collection, data, writeConcern, orderedOp, includeObjectIds);
		// mapa con indice de lDatos y Object
		final Map<Integer, BasicDBObject> mapDocs = new HashMap<>();

		final List<WriteModel<BasicDBObject>> bulkWrites = new ArrayList<>();
		try {
			String errorMsg;
			for (int i = 0; i < data.size(); i++) {
				errorMsg = null;
				BasicDBObject doc = null;
				boolean jsonParseError = false;
				try {
					doc = (BasicDBObject) JSON.parse(data.get(i));
				} catch (final JSONParseException e) {
					final String errorMessage = String.format(
							"Unable to parse JSON data. Data = %s, cause = %s, errorMessage = %s.", data, e.getCause(),
							e.getMessage());
					log.error(errorMessage);
					jsonParseError = true;
					errorMsg = "Not a valid JSON object: " + e.getMessage();
				} finally {
					if (!jsonParseError) {
						bulkWrites.add(new InsertOneModel<>(doc));
						mapDocs.put(i, doc);
					} else {
						final BulkWriteResult bwResult = new BulkWriteResult();
						bwResult.setErrorMessage(errorMsg);
						bwResult.setOk(false);
						bwResults[i] = bwResult;
					}
				}
			}

			final Map<Integer, String> errorsMap = new HashMap<>();
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			final BulkWriteOptions options = new BulkWriteOptions();
			options.ordered(orderedOp);
			try {
				dbCollection.bulkWrite(bulkWrites, options);
			} catch (final MongoBulkWriteException e) {
				final List<ErrorResult> errs = e.getWriteErrors().stream()
						.map(err -> new ErrorResult(
								err.getCode() == 11000 ? ErrorResult.ErrorType.DUPLICATED : ErrorResult.ErrorType.ERROR,
								ErrorResult.PersistenceType.MONGO, err.getMessage()))
						.collect(Collectors.toList());
				throw new DBPersistenceException(e, errs, "Bulk write operation error");

				/*
				 * final List<BulkWriteError> errors = e.getWriteErrors(); for (final
				 * BulkWriteError error : errors) { if(error.getCode() == 11000) { throw new
				 * DBPersistenceException(e, new ErrorResult(ErrorResult.ErrorType.DUPLICATED,
				 * ErrorResult.PersistanceType.MONGO, error.getMessage()), error.getMessage());
				 * } else { throw new DBPersistenceException(e, new
				 * ErrorResult(ErrorResult.PersistanceType.MONGO, error.getMessage()),
				 * error.getMessage()); } // errorsMap.put(error.getIndex(),
				 * error.getMessage()); }
				 */
			} catch (final MongoException e) {
				final ErrorResult err = new ErrorResult(
						e.getCode() == 11000 ? ErrorResult.ErrorType.DUPLICATED : ErrorResult.ErrorType.ERROR,
						ErrorResult.PersistenceType.MONGO, e.getMessage());
				throw new DBPersistenceException(e, err, "Bulk write operation error");
			} finally {
				for (final int i : mapDocs.keySet()) {
					final BulkWriteResult bwResult = new BulkWriteResult();
					if (errorsMap.containsKey(i)) {
						bwResult.setOk(false);
						bwResult.setErrorMessage(errorsMap.get(i));
						bwResults[i] = bwResult;
					} else {
						bwResult.setOk(true);
						if (includeObjectIds) {
							// bwResult.setId(util.getObjectIdString(mapDocs.get(i).getObjectId("_id")));
							bwResult.setId(mapDocs.get(i).getObjectId("_id").toString());
						}
						bwResults[i] = bwResult;
					}
				}
			}
			return new ArrayList<>(Arrays.asList(bwResults));
		} catch (final DBPersistenceException e) {
			final String errorMessage = String.format(
					"Unable to perform the bulkInsert operation. Database = %s, collection = %s, data = %s , writeConcern = %s , orderedOperation = %s"
							+ "cause = %s , erroMessage = %s.",
					database, collection, data, writeConcern, orderedOp, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw e;
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to perform the bulkInsert operation. Database = %s, collection = %s, data = %s , writeConcern = %s , orderedOperation = %s"
							+ "cause = %s , erroMessage = %s.",
					database, collection, data, writeConcern, orderedOp, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MultiDocumentOperationResult remove(String database, String collection, String query, boolean includeIds)
			throws DBPersistenceException {
		try {
			if (query.indexOf("db.") != -1) {
				query = util.getQueryContent(query);
			}
			return remove(database, collection, BasicDBObject.parse(query), includeIds);
		} catch (final JSONParseException e) {
			final String errorMessage = String.format(
					"Unable to parse JSON query. Query = %s, cause = %s, errorMessage = %s.", query, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MultiDocumentOperationResult remove(String database, String collection, BasicDBObject query,
			boolean includeIds) throws DBPersistenceException {
		log.debug("Removing from MongoDB...Database= {} , collection = {} , query = {}.", database, collection, query);

		DeleteResult dbResult = null;
		try {
			// mc.parseQuery(query, limit, offset);
			final MongoCollection<?> dbCollection = getCollection(database, collection, BasicDBObject.class);
			// find()

			final MultiDocumentOperationResult deleteResult = new MultiDocumentOperationResult();
			if (includeIds) {
				final List<String> ids = new ArrayList<>();
				int count = 0;
				synchronized (this) {
					final FindIterable<BasicDBObject> findIds = (FindIterable<BasicDBObject>) dbCollection.find(query);
					for (final BasicDBObject obj : findIds) {
						dbResult = dbCollection.deleteOne(obj);
						if (dbResult.getDeletedCount() > 0) {
							count++;
							ids.add(obj.get("_id").toString());
						}
					}
				}
				deleteResult.setCount(count);
				deleteResult.setIds(ids);

			} else {
				dbResult = dbCollection.deleteMany(query);
				deleteResult.setCount(dbResult.getDeletedCount());
			}

			return deleteResult;

		} catch (final MongoException e) {
			final String errorMessage = String.format(
					"Unable to delete from MongoDB. Database = %s, collection = %s , query = %s , writeConcern = %s , cause = %s , errorMessage = %s.",
					database, collection, query, writeConcern, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.MONGO, e.getMessage()),
					e.getMessage());
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to delete from MongoDB. Database = %s, collection = %s , query = %s , writeConcern = %s , cause = %s , errorMessage = %s.",
					database, collection, query, writeConcern, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void dropCollection(String database, String collection) throws DBPersistenceException {
		log.debug("Dropping collection. Database = {}, collection = {}.", database, collection);
		try {
			final MongoCollection<?> dbCollection = getCollection(database, collection, BasicDBObject.class);
			dbCollection.drop();
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to drop the collection from MongoDB. Database = %s, collection = %s , cause = %s , errorMessage = %s.",
					database, collection, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public MultiDocumentOperationResult update(String database, String collection, String query, String update,
			boolean multi, boolean includeIds) throws DBPersistenceException {
		log.info("Updating document. Database= {} , collection = {}, document = {} , update = {}.", database,
				collection, query, update);
		UpdateResult dbResult = null;
		try {

			final int endOfQuery = endOfQuery(update);

			String finalUpdate = update.substring(0, endOfQuery + 1);
			// allow $push queries to be executed
			if (finalUpdate.indexOf("$set") == -1 && finalUpdate.indexOf("$push") == -1
					&& finalUpdate.indexOf("$pull") == -1 && finalUpdate.indexOf("$inc") == -1
					&& finalUpdate.indexOf("$unset") == -1 && finalUpdate.indexOf("$addToSet") == -1) {
				finalUpdate = "{$set:" + finalUpdate + "}";
			}
			// query = updateQuery.substring(0, updateQuery.indexOf("},") + 1);

			String options = update.substring(endOfQuery + 1, update.length());
			options = options.substring(options.indexOf(',') + 1, options.length());
			if (options.indexOf('{') == -1) {
				options = "{" + options + "}";
			}

			final BasicDBObject parsedQuery = BasicDBObject.parse(query);
			final BasicDBObject parsedUpdate = BasicDBObject.parse(finalUpdate);
			final BasicDBObject parsedOptions = BasicDBObject.parse(options);

			final UpdateOptions updateOptions = new UpdateOptions().upsert(parsedOptions.getBoolean("upsert", false));

			// Bson filter = Filters.eq("user", "other");
			// Bson toUpdate = Filters.eq("user", "developer");

			final MongoCollection<?> dbCollection = getCollection(database, collection, BasicDBObject.class);

			final MultiDocumentOperationResult updateResult = new MultiDocumentOperationResult();
			if (includeIds) {
				final List<String> ids = new ArrayList<>();
				int count = 0;
				synchronized (this) {
					final FindIterable<BasicDBObject> findIds = (FindIterable<BasicDBObject>) dbCollection
							.find(parsedQuery);
					for (final BasicDBObject obj : findIds) {
						dbResult = dbCollection.updateOne(obj, parsedUpdate, updateOptions);
						if (dbResult.getModifiedCount() > 0) {
							count++;
							ids.add(obj.get("_id").toString());
						}
					}
				}
				updateResult.setCount(count);
				updateResult.setIds(ids);

			} else {
				if (multi) {
					dbResult = dbCollection.updateMany(parsedQuery, parsedUpdate, updateOptions);
				} else {
					dbResult = dbCollection.updateOne(parsedQuery, parsedUpdate, updateOptions);
				}
				updateResult.setCount(dbResult.getModifiedCount());
			}

			if (dbResult != null) {
				final int upserted = dbResult.getUpsertedId() != null ? 1 : 0;
				log.info("Executed update, with query {}, rows found and {} rows updated and {} rows upserted", query,
						dbResult.getMatchedCount(), dbResult.getModifiedCount(), upserted);
			}
			log.info("Executed update, with query {}, rows found and 0 rows updated and 0 rows upserted", query);

			return updateResult;

		} catch (final MongoException e) {
			final String errorMessage = String.format(
					"Unable to update the document. Database = %s, collection = %s, document = %s, update = %s, cause = %s , errorMessage = %s.",
					database, collection, query, update, e.getCause(), e.getMessage());
			log.error(errorMessage);
			if (e.getCode() == 11000) {
				throw new DBPersistenceException(e, new ErrorResult(ErrorResult.ErrorType.DUPLICATED,
						ErrorResult.PersistenceType.MONGO, e.getMessage()), e.getMessage());
			} else {
				throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.MONGO, e.getMessage()),
						e.getMessage());
			}
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to update the document. Database = %s, collection = %s, document = %s, update = %s, cause = %s , errorMessage = %s.",
					database, collection, query, update, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	private int endOfQuery(String query) {
		int i = -1;
		boolean found = false;
		int openBracketCount = 0;
		int closeBracketCount = 0;
		while (i < query.length() && !found) {
			i++;
			final char c = query.charAt(i);
			if (c == '{') {
				openBracketCount++;
			} else if (c == '}') {
				closeBracketCount++;
			}
			found = openBracketCount > 0 && openBracketCount == closeBracketCount;

		}
		if (!found) {
			throw new InvalidParameterException("Bad formed updated query");
		}
		return i;
	}

	@Override
	public void replace(String database, String collection, BasicDBObject oldDocument, BasicDBObject newDocument)
			throws DBPersistenceException {
		log.info("Updating document. Database= {} , collection = {}, oldDocument = {}.", database, collection,
				oldDocument);
		try {
			final MongoCollection<BasicDBObject> dbCollection = getCollection(database, collection,
					BasicDBObject.class);
			dbCollection.findOneAndReplace(oldDocument, newDocument);
		} catch (final Throwable e) {
			final String errorMessage = String.format(
					"Unable to replace document. Database = %s, collection = %s, oldDocument = %s, newDocument = %s, cause = %s , errorMessage = %s.",
					database, collection, oldDocument, newDocument, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public <T> List<T> convertQueryResults(MongoIterable<BasicDBObject> cursor, boolean keepObjectIds,
			boolean raiseExceptionsOnErrors, Class<T> targetQueryResultType) throws DBPersistenceException {
		try {
			final List<T> result = new ArrayList<>();
			final ObjectMapper objMapper = new ObjectMapper();
			for (final BasicDBObject obj : cursor) {
				if (!keepObjectIds && obj.get("_id") instanceof ObjectId) {
					obj.removeField("_id");
				}
				try {
					result.add(objMapper.readValue(JSON.serialize(obj), targetQueryResultType));
				} catch (final IOException e) {
					final String errorMessage = String.format(
							"Unable to deserialize query result. Object = %s, queryResultType = %s, cause = %s, errorMessage = %s.",
							obj, targetQueryResultType.getName(), e.getCause(), e.getMessage());
					log.error(errorMessage);
					if (raiseExceptionsOnErrors) {
						throw new DBPersistenceException(errorMessage, e);
					}
				}
			}
			return result;

		} catch (final Throwable e) {
			log.error("Unable to execute query. Cause = {}, errorMessage = {}.", e.getCause(), e.getMessage());
			throw new DBPersistenceException("Unable to execute query", e);
		}
	}

	@Override
	public <T> List<T> convertQueryResults(MongoIterable<BasicDBObject> cursor, Class<T> targetQueryResultType)
			throws DBPersistenceException {
		return convertQueryResults(cursor, false, true, targetQueryResultType);
	}

	@Override
	public boolean testConnection() {
		try {
			getDatabaseNames();
			return true;
		} catch (final DBPersistenceException e) {
			return false;
		}
	}

	@Override
	public ServerAddress getReplicaSetMaster() {
		if (mongoDBClientManager.electClient().getReplicaSetStatus() != null) {
			return mongoDBClientManager.electClient().getReplicaSetStatus().getMaster();
		} else {
			return mongoDBClientManager.electClient().getServerAddressList().get(0);
		}
	}

	@Override
	public MongoDbCredentials getCredentials() {
		return credentials;
	}

	@Override
	public String getNormalizedCollectionName(String database, String collectionName) {
		log.debug("Normalizing collection name. Database = {}, collection = {}.", database, collectionName);
		final String key = database + "::" + collectionName;
		String result = normalizedCollectionNames.get(key);
		if (result == null) {
			for (final String normalizedCollectionName : getCollectionNames(database)) {
				if (normalizedCollectionName.equalsIgnoreCase(collectionName)) {
					final String existingMapping = normalizedCollectionNames.putIfAbsent(key, normalizedCollectionName);
					if (existingMapping != null) {
						result = existingMapping;
					} else {
						result = normalizedCollectionName;
					}
					break;
				}
			}
			if (result == null) {
				result = normalizedCollectionNames.putIfAbsent(key, collectionName);
				if (result == null) {
					result = collectionName;
				}
			}
		}
		log.debug("The collection name has been normalized. Database = {}, collection = {}, result = {}.", database,
				collectionName, result);
		return result;
	}

	private <T> MongoCollection<T> getCollection(String database, String collectionName, Class<T> resultType) {
		return mongoDBClientManager.electClient().getDatabase(database)
				.getCollection(getNormalizedCollectionName(database, collectionName), resultType);
	}

	@Override
	public void dropDatabase(String database) {
		log.info("Dropping database. DatabaseName = {}.", database);
		mongoDBClientManager.electClient().getDatabase(database).drop();
	}

	@Override
	public GridFSBucket configureGridFSBucket(String database) {
		return GridFSBuckets.create(mongoDBClientManager.electClient().getDatabase(database));
	}

	private void loadCentralMongoConfig() {
		final Map<String, Object> databaseConfig = resourcesService.getGlobalConfiguration().getEnv().getDatabase();
		writeConcern = (String) databaseConfig.get("mongodb-write-concern");

	}

	@Override
	public Document getCurrentOps(int secsRunning) {
		final String command = "{\"currentOp\": 1, \"active\": true, \"secs_running\" : { \"$gte\" : " + secsRunning
				+ " }}";
		return mongoDBClientManager.electClient().getDatabase(ADMIN_DB).runCommand((Bson) JSON.parse(command));

	}

	@Override
	public Document killOp(long opID) {
		final String command = "{ \"killOp\": 1, \"op\": " + opID + " }";
		return mongoDBClientManager.electClient().getDatabase(ADMIN_DB).runCommand((Bson) JSON.parse(command));

	}

}
