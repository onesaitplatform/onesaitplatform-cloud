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
package com.minsait.onesait.platform.persistence.mongodb.timeseries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.jline.utils.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.TimeSeriesResult;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyDataType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.AggregationFunction;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.WindowType;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesPropertyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesWindowRepository;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.exception.TimeSeriesFrecuencyNotSupportedException;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.exception.TimeSeriesInsertLockTimeoutException;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.exception.TimeSeriesUnableToUpdateException;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.exception.WindowNotSupportedException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MongoDBTimeSeriesProcessorImpl implements MongoDBTimeSeriesProcessor {

	private static final String TIMESTAMP_PROPERTY = "timestamp";
	private static final String PROPERTY_NAME = "propertyName";
	private static final String WINDOW_TYPE = "windowType";
	private static final String WINDOW_FRECUENCY_UNIT = "windowFrecuencyUnit";
	private static final String WINDOW_FRECUENCY = "windowFrecuency";

	private static final String SDATE = "$date";
	private static final String CONTEXT_DATA = "contextData";

	private static final String FORMAT_WINDOW_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String FORMAT_WINDOW_MINUTES = "yyyy-MM-dd'T'HH:mm";
	private static final String FORMAT_WINDOW_HOURS = "yyyy-MM-dd'T'HH";
	private static final String FORMAT_WINDOW_DAYS = "yyyy-MM-dd";
	private static final String FORMAT_WINDOW_MONTHS = "yyyy-MM";
	private static final String FORMAT_WINDOW_YEARS = "yyyy";

	private static final String VALUE_STR = "value";
	private static final String WINDOW_TYPE_STR = "Window type ";
	private static final String NOT_SUPPORTED = " not supported";
	private static final String STATS_PREFIX = "Stats";
	private static final int MAX_DUPLICATE_RETRIES = 1;

	enum UPDATE_TYPE {
		SET("$set"), SUM("$inc");

		UPDATE_TYPE(String value) {
			this.value = value;
		}

		private final String value;
	}

	@Autowired(required = false)
	private HazelcastInstance hazelcastInstance;

	@Getter
	@Setter
	private long queryExecutionTimeout;

	@Value("${onesaitplatform.database.timeseries.timezone:UTC}")
	private String timeZone;

	@Value("${onesaitplatform.database.timeseries.mongodb.insert.lock.wait.check.millis:50}")
	private long waitBetweenChecks;
	@Value("${onesaitplatform.database.timeseries.mongodb.insert.lock.wait.timeout.millis:500}")
	private long waitChecksTimeout;

	@Autowired
	private OntologyTimeSeriesPropertyRepository ontologyTimeSeriesPropertyRepository;

	@Autowired
	private OntologyTimeSeriesWindowRepository ontologyTimeSeriesWindowRepository;

	@Autowired
	private MongoDbTemplate mongoDbConnector;

	@Autowired
	private OntologyRepository ontologyRepository;

	protected ObjectMapper objectMapper;
	private IMap<MongoDBTimeSeriesInstanceKey, String> timeseriesUpdateTransactionMap;

	private Map<String, TimeseriesConfigData> timeseriesConfigData;
	@Value("${onesaitplatform.database.timeseries.mongodb.config.cache.ttl:120000}")
	private long timeseriesConfigTtl;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		if (hazelcastInstance == null) {
			timeseriesUpdateTransactionMap = null;
		} else {
			timeseriesUpdateTransactionMap = hazelcastInstance.getMap("timeseriesUpdateTransaction");
		}
		timeseriesConfigData = new HashMap<>();
	}

	@Override
	public List<TimeSeriesResult> processTimeSerieBulk(String database, String ontology, List<String> instances)
			throws WindowNotSupportedException {
		log.info("Process TimeSerie bulk for ontology {}", ontology);

		// final List<TimeSeriesResult> result = new ArrayList<>();
		// get common info from ontology
		final TimeseriesConfigData timeseriesConfigData = getTimeseriesConfigData(ontology);
		// Get Properties declared for the Timeserie ontology
		final List<OntologyTimeSeriesProperty> lProperties = timeseriesConfigData.getProperties();
		// Get Windows declared for the Timeserie ontology
		final List<OntologyTimeSeriesWindow> lTimeSeriesWindows = timeseriesConfigData.getWindows();
		final Map<MongoDBTimeserieKey, List<MongoDBTimeseriesInstanceData>> groupedInstances = new HashMap<>();
		// Preprocess every record and window
		for (String instance : instances) {
			// group by key
			classifyByTimeserieKey(instance, lProperties, timeseriesConfigData, groupedInstances);
		}

		List<WriteModel<BasicDBObject>> bulkWrites = new ArrayList<>();
		// create final aggregated query
		List<String> finalInstances = new ArrayList<>();
		List<MongoDBTimeserieKey> keysInOrder = new ArrayList<>();
		for (Entry<MongoDBTimeserieKey, List<MongoDBTimeseriesInstanceData>> entry : groupedInstances.entrySet()) {
			bulkWrites
					.add(processbulkWindow(database, ontology, entry.getKey(), entry.getValue(), timeseriesConfigData));
			StringBuilder allInstancesFromKey = new StringBuilder().append("[");
			for (MongoDBTimeseriesInstanceData singleInstance : entry.getValue()) {
				if (allInstancesFromKey.length() > 1) {
					allInstancesFromKey.append(",");
				}
				allInstancesFromKey.append(singleInstance.getInstance());
			}
			allInstancesFromKey.append("]");
			finalInstances.add(allInstancesFromKey.toString());
			keysInOrder.add(entry.getKey());

		}

		// we need to retry DUPLICATE exceptions just because of how bulk multiupdates
		// work
		// In case a timeserie key is created and updated at the same time, this
		// exception will raise
		// so we need to retry the failures in order not to loose the update.
		List<BulkWriteResult> data = null;
		for (int i = 0; i <= MAX_DUPLICATE_RETRIES; i++) {
			try {

				// try executing bulk upsert
				data = mongoDbConnector.bulkUpsert(Tenant2SchemaMapper.getRtdbSchema(), ontology, bulkWrites, false,
						false, finalInstances);
				break;// if all is fine, exit the retry loop
			} catch (DBPersistenceException e) {
				boolean isDuplicated = false;
				for (ErrorResult errorResult : e.getErrorsResult()) {
					if (errorResult.getErrorType() == ErrorResult.ErrorType.DUPLICATED) {
						isDuplicated = true;
					}
				}
				if (isDuplicated) {
					// Throw original exception if it still is Duplicated
					if (i >= MAX_DUPLICATE_RETRIES) {
						throw e;
					}
				} else {
					// Throw original exception if is not DUPLICATE
					throw e;
				}
			}
		}

		// Change result type to TimeseriesResult, needed to parse response
		final List<TimeSeriesResult> result = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			final TimeSeriesResult partialResult = new TimeSeriesResult();
			if (data.get(i).isOk()) {
				partialResult.setFieldName(keysInOrder.get(i).getPropertyName());
				partialResult.setOk(true);
				partialResult.setWindowType(keysInOrder.get(i).getWindowType().name());
			} else {
				partialResult.setFieldName(keysInOrder.get(i).getPropertyName());
				partialResult.setOk(false);
				partialResult.setErrorMessage(data.get(i).getErrorMessage());
				partialResult.setWindowType(keysInOrder.get(i).getWindowType().name());
			}
			result.add(partialResult);
		}

		return result;

	}

	private UpdateManyModel processbulkWindow(String database, String ontology, MongoDBTimeserieKey TSKey,
			List<MongoDBTimeseriesInstanceData> oInstances, TimeseriesConfigData timeseriesConfigData)
			throws WindowNotSupportedException {

		// Check if the document has root to append it as prefix to all properties
		final String propertyPrefix = getDocumentBase(TSKey.getRootElement());

		/*
		 * Set QUERY for update statement
		 */

		// Build the query object with all tags, the concrete field and the timestamp
		// Append Tags to the query
		final BasicDBObject objQuery = new BasicDBObject();
		TSKey.getMtags().forEach((key, value) -> {
			if (key.getPropertyDataType().equals(PropertyDataType.OBJECT)) {
				objQuery.put(propertyPrefix.concat(key.getPropertyName()), BasicDBObject.parse(value.toString()));
			} else {
				objQuery.put(propertyPrefix.concat(key.getPropertyName()), value);
			}
		});

		// Append concrete Field to the query
		objQuery.put(propertyPrefix.concat(PROPERTY_NAME), TSKey.getPropertyName());

		// Append Timestamp to the query
		objQuery.put(propertyPrefix.concat(TIMESTAMP_PROPERTY), TSKey.getTimestamp());

		// Append window Type to the query
		objQuery.put(propertyPrefix.concat(WINDOW_TYPE), TSKey.getWindowType().name());

		// Append window Frequency to the query
		objQuery.put(propertyPrefix.concat(WINDOW_FRECUENCY), TSKey.getWindowFrecuency());
		objQuery.put(propertyPrefix.concat(WINDOW_FRECUENCY_UNIT), TSKey.getWindowFrecuencyUnit().name());

		/*
		 * Set default insert null for update statement
		 */
		List<BasicDBObject> updatePipelines = new ArrayList<>();
		Calendar timestamp = oInstances.get(0).getRealTimestamp();
		StringBuilder updateStatement = new StringBuilder();
		updateStatement.append("{\"$set\":{\"").append(propertyPrefix).append("values\": {\"$cond\": {")
				.append("\"if\": { \"$ne\": [{ \"$ifNull\": [\"$").append(propertyPrefix)
				.append("values\", null] }, null] },").append("\"then\": \"$").append(propertyPrefix)
				.append("values\",").append("\"else\": {\"v\": ")
				.append(buildBlankDocument(timestamp, TSKey.getWindowType(), TSKey.getWindowFrecuencyUnit(),
						TSKey.getWindowFrecuency(), null).toString())
				.append("}}}}}");
		final BasicDBObject blankDocPipeline = BasicDBObject.parse(updateStatement.toString());
		updatePipelines.add(blankDocPipeline);

		/*
		 * Set each element:
		 */

		for (MongoDBTimeseriesInstanceData oInstance : oInstances) {
			// If needed, insert in the STATS entity
			if (timeseriesConfigData.getOntologyStats() != null) {
				insertStats(database, ontology, TSKey, oInstance, timeseriesConfigData);
			}
			// for each document, set values.v
			final OntologyTimeSeriesWindow window = new OntologyTimeSeriesWindow();
			window.setFrecuency(TSKey.getWindowFrecuency());
			window.setFrecuencyUnit(TSKey.getWindowFrecuencyUnit());
			window.setWindowType(TSKey.getWindowType());

			final String updateStage = buildUpdate(propertyPrefix, oInstance.getField(),
					oInstance.getInformativeFields(), oInstance.getRealTimestamp(), window, UPDATE_TYPE.SET);
			final BasicDBObject parsedUpdate = BasicDBObject.parse(updateStage);
			updatePipelines.add(parsedUpdate);

		}
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		UpdateManyModel upsert = new UpdateManyModel(objQuery, updatePipelines, options);
		return upsert;
	}

	private void insertStats(String database, String ontology, MongoDBTimeserieKey key,
			MongoDBTimeseriesInstanceData oInstance, TimeseriesConfigData timeseriesConfigData) {
		final BasicDBObject objStat = new BasicDBObject();
		final ArrayList<BasicDBObject> tags = new ArrayList<>();
		for (final Entry<OntologyTimeSeriesProperty, Object> tag : key.getMtags().entrySet()) {
			final BasicDBObject tagDBObj = new BasicDBObject();
			tagDBObj.put("name", tag.getKey().getPropertyName());
			if (tag.getKey().getPropertyDataType().equals(PropertyDataType.OBJECT)) {
				tagDBObj.put(VALUE_STR, BasicDBObject.parse(tag.getValue().toString()));
			} else {
				tagDBObj.put(VALUE_STR, tag.getValue());
			}
			tags.add(tagDBObj);
		}
		objStat.put("tag", tags);
		objStat.put("field", key.getPropertyName());
		objStat.put(WINDOW_TYPE, key.getWindowType().name());
		objStat.put(WINDOW_FRECUENCY, key.getWindowFrecuency());
		objStat.put(WINDOW_FRECUENCY_UNIT, key.getWindowFrecuencyUnit().name());
		BasicDBObject lastValue;
		if (oInstance.getField().getKey().getPropertyDataType().equals(PropertyDataType.OBJECT)) {
			lastValue = new BasicDBObject(VALUE_STR, BasicDBObject.parse(oInstance.getField().getValue().toString()));
		} else {
			lastValue = new BasicDBObject(VALUE_STR, oInstance.getField().getValue());
		}
		objStat.put("lastValue", lastValue);
		final BasicDBObject sample = new BasicDBObject(STATS_PREFIX, objStat);
		mongoDbConnector.insert(database, timeseriesConfigData.getOntologyStats().getIdentification(), sample);
	}

	/**
	 * Parses data and groups it by timeseries key. This is needed for bulk updating
	 *
	 * @param instance
	 * @param lProperties
	 * @param timeseriesConfigData
	 * @param groupedInstances
	 */
	private void classifyByTimeserieKey(String instance, List<OntologyTimeSeriesProperty> lProperties,
			TimeseriesConfigData timeseriesConfigData,
			Map<MongoDBTimeserieKey, List<MongoDBTimeseriesInstanceData>> groupedInstances) {

		// Divide Root element and Data of the instance
		final JSONObject oInstance = new JSONObject(instance);
		JSONObject instanceData = oInstance;
		Optional<String> rootElement = Optional.empty();
		if ((oInstance.keySet().size() == 1)
				|| (oInstance.keySet().size() == 2 && oInstance.keySet().contains(CONTEXT_DATA))) {// Check if there is
																									// root
			// element
			rootElement = oInstance.keySet().stream().filter(p -> !((String) p).equals(CONTEXT_DATA)).findFirst();

			if (rootElement.isPresent())
				instanceData = (JSONObject) oInstance.get(rootElement.get());
		}

		final Optional<String> rootkey = rootElement;

		// Extract Tags from instance
		final Map<OntologyTimeSeriesProperty, Object> mTags = extractTags(lProperties, instanceData);

		final Set<String> tagNames = new HashSet<>();
		mTags.keySet().stream().forEach(prop -> tagNames.add(prop.getPropertyName()));

		// Extract Properties from instance
		final Map<OntologyTimeSeriesProperty, Object> mFields = extractFields(lProperties, instanceData);

		// Extract informative fields (mutable not part of the key or variables)
		final Map<OntologyTimeSeriesProperty, Object> mInformativeFields = extractInformativeFields(lProperties,
				instanceData);

		// Get Windows declared for the Timeserie ontology
		final List<OntologyTimeSeriesWindow> lTimeSeriesWindows = timeseriesConfigData.getWindows();
		for (Entry<OntologyTimeSeriesProperty, Object> field : mFields.entrySet()) {
			// one record per entry
			for (OntologyTimeSeriesWindow window : lTimeSeriesWindows) {
				try {
					final MongoDBTimeserieKey key = new MongoDBTimeserieKey();
					key.setPropertyName(field.getKey().getPropertyName());
					key.setTags(tagNames);
					key.setMtags(mTags);
					// Extract Timestamp from instance
					final JSONObject timestamp = (JSONObject) instanceData.get(TIMESTAMP_PROPERTY);
					final String formattedDate = (String) timestamp.get(SDATE);
					// Get Calendar with the maximum precission for this window
					// Validations and recover SimpleDateformat for the current window
					final SimpleDateFormat sdfInstancePrecision = validateWindowAndGetDateFormat(window);
					final SimpleDateFormat sdfSeconds = new SimpleDateFormat(FORMAT_WINDOW_SECONDS);
					final Calendar calendar = Calendar.getInstance();
					// real timestamp
					calendar.setTime(sdfSeconds.parse(formattedDate));
					sdfInstancePrecision.setTimeZone(TimeZone.getTimeZone(timeZone));
					// rounded to timeseries timestamp
					final Date dInstance = sdfInstancePrecision.parse(formattedDate);
					key.setTimestamp(dInstance);
					key.setWindowFrecuency(window.getFrecuency());
					key.setWindowFrecuencyUnit(window.getFrecuencyUnit());
					key.setWindowType(window.getWindowType());
					key.setRootElement(rootkey);
					// variable data within the key
					final MongoDBTimeseriesInstanceData data = new MongoDBTimeseriesInstanceData();
					data.setJsonInstance(oInstance);
					data.setInstance(instance);
					data.setField(field);
					data.setRealTimestamp(calendar);
					data.setInformativeFields(mInformativeFields);
					if (groupedInstances.get(key) == null) {
						List<MongoDBTimeseriesInstanceData> first = new ArrayList<>();
						first.add(data);
						groupedInstances.put(key, first);
					} else {
						groupedInstances.get(key).add(data);
					}
				} catch (TimeSeriesFrecuencyNotSupportedException | WindowNotSupportedException | ParseException e) {
					log.error("Error processing TimeSeries Window", e);
				}

			}
		}

	}

	private BasicDBObject buildBlankDocument(Calendar calendar, WindowType windowType, FrecuencyUnit frequencyUnit,
			Integer frequency, Object initialValue) throws WindowNotSupportedException {

		Map<String, Object> vMeasures;

		if (windowType == WindowType.MINUTES) {
			vMeasures = buildNewMinuteMap(frequency, initialValue);
		} else if (windowType == WindowType.HOURS) {
			vMeasures = buildNewHourMap(frequencyUnit, frequency, initialValue);
		} else if (windowType == WindowType.DAYS) {
			vMeasures = buildNewDayMap(frequencyUnit, frequency, initialValue);
		} else if (windowType == WindowType.MONTHS) {
			vMeasures = buildNewMonthMap(frequencyUnit, calendar, frequency, initialValue);
		} else if (windowType == WindowType.YEARS) {
			vMeasures = buildNewYearMap(frequencyUnit, calendar, frequency, initialValue);
		} else {
			throw new WindowNotSupportedException(WINDOW_TYPE_STR + windowType.name() + NOT_SUPPORTED);
		}

		return new BasicDBObject(vMeasures);
	}

	@Override
	public List<TimeSeriesResult> processTimeSerie(String database, String ontology, String instance) {

		log.info("Process TimeSerie instance for ontology {}", ontology);
		final List<TimeSeriesResult> result = new ArrayList<>();

		final TimeseriesConfigData timeseriesConfigData = getTimeseriesConfigData(ontology);
		// Get Properties declared for the Timeserie ontology
		final List<OntologyTimeSeriesProperty> lProperties = timeseriesConfigData.getProperties();

		// Get Windows declared for the Timeserie ontology
		final List<OntologyTimeSeriesWindow> lTimeSeriesWindows = timeseriesConfigData.getWindows();

		// Divide Root element and Data of the instance
		final JSONObject oInstance = new JSONObject(instance);
		JSONObject instanceData = oInstance;

		Optional<String> rootElement = Optional.empty();
		if ((oInstance.keySet().size() == 1)
				|| (oInstance.keySet().size() == 2 && oInstance.keySet().contains(CONTEXT_DATA))) {
			// Check if there is root element
			rootElement = oInstance.keySet().stream().filter(p -> !((String) p).equals(CONTEXT_DATA)).findFirst();

			if (rootElement.isPresent())
				instanceData = (JSONObject) oInstance.get(rootElement.get());
		}

		final Optional<String> rootkey = rootElement;

		// Extract Tags from instance
		final Map<OntologyTimeSeriesProperty, Object> mTags = extractTags(lProperties, instanceData);

		// Extract Properties from instance
		final Map<OntologyTimeSeriesProperty, Object> mFields = extractFields(lProperties, instanceData);

		// Extract informative fields (mutable not part of the key or variables)
		final Map<OntologyTimeSeriesProperty, Object> mInformativeFields = extractInformativeFields(lProperties,
				instanceData);

		// Extract Timestamp from instance
		final JSONObject timestamp = (JSONObject) instanceData.get(TIMESTAMP_PROPERTY);
		final String formattedDate = (String) timestamp.get(SDATE);

		// Process Instance for each declared window
		lTimeSeriesWindows.forEach(window -> {
			try {
				if (log.isDebugEnabled()) {
					log.debug("Process window {} for ontology {}", window.getWindowType().name(), ontology);
				}				
				result.addAll(manageWindow(database, ontology, rootkey, mTags, mFields, mInformativeFields,
						formattedDate, window, getWindowAggregationType(window)));
			} catch (TimeSeriesFrecuencyNotSupportedException | WindowNotSupportedException | ParseException e) {
				log.error("Error processing TimeSeries Window", e);
			}

		});

		return result;

	}

	private UPDATE_TYPE getWindowAggregationType(OntologyTimeSeriesWindow window) {
		if (window.getAggregationFunction().equals(AggregationFunction.SUM))
			return UPDATE_TYPE.SUM;
		else
			return UPDATE_TYPE.SET;

	}

	/**
	 * Extract Tag (Inmutable properties) properties for the instance
	 *
	 * @param lProperties
	 * @param oInstance
	 * @return
	 */
	private Map<OntologyTimeSeriesProperty, Object> extractTags(List<OntologyTimeSeriesProperty> lProperties,
			JSONObject oInstance) {
		// Extract Tags from instance
		final Map<OntologyTimeSeriesProperty, Object> mTags = new HashMap<>();
		lProperties.stream().filter(p -> p.getPropertyType() == PropertyType.TAG)
				.forEach(p -> mTags.put(p, oInstance.get(p.getPropertyName())));

		return mTags;
	}

	/**
	 * Extract Fields (Variable properties) properties for the instance
	 *
	 * @param lProperties
	 * @param oInstance
	 * @return
	 */
	private Map<OntologyTimeSeriesProperty, Object> extractFields(List<OntologyTimeSeriesProperty> lProperties,
			JSONObject oInstance) {
		final Map<OntologyTimeSeriesProperty, Object> mFields = new HashMap<>();
		lProperties.stream()
				.filter(p -> p.getPropertyType() == PropertyType.SERIE_FIELD && oInstance.has(p.getPropertyName()))
				.forEach(p -> mFields.put(p, oInstance.get(p.getPropertyName())));

		return mFields;
	}

	/**
	 * Extract Informative Fields (mutable fields: non key / fields) properties for
	 * the instance
	 *
	 * @param lProperties
	 * @param oInstance
	 * @return
	 */
	private Map<OntologyTimeSeriesProperty, Object> extractInformativeFields(
			List<OntologyTimeSeriesProperty> lProperties, JSONObject oInstance) {
		final Map<OntologyTimeSeriesProperty, Object> mInformativeFields = new HashMap<>();
		lProperties.stream()
				.filter(p -> p.getPropertyType() == PropertyType.FIELD_OPTIONAL && oInstance.has(p.getPropertyName()))
				.forEach(p -> mInformativeFields.put(p, oInstance.get(p.getPropertyName())));

		return mInformativeFields;
	}

	/**
	 * Process The instance for each Window
	 *
	 * @param ontology
	 * @param rootElement
	 * @param mTags
	 * @param mFields
	 * @param formattedDate
	 * @param window
	 * @throws TimeSeriesFrecuencyNotSupportedException
	 * @throws WindowNotSupportedException
	 * @throws ParseException
	 */
	private List<TimeSeriesResult> manageWindow(String database, String ontology, Optional<String> rootElement,
			Map<OntologyTimeSeriesProperty, Object> mTags, Map<OntologyTimeSeriesProperty, Object> mFields,
			Map<OntologyTimeSeriesProperty, Object> mInformativeFields, String formattedDate,
			OntologyTimeSeriesWindow window, UPDATE_TYPE updateType)
			throws TimeSeriesFrecuencyNotSupportedException, WindowNotSupportedException, ParseException {

		final TimeseriesConfigData timeseriesConfigData = getTimeseriesConfigData(ontology);
		// final Ontology stats = timeseriesConfigData.getOntologyStats();

		final List<TimeSeriesResult> result = new ArrayList<>();

		// Validations and recover SimpleDateformat for the current window
		final SimpleDateFormat sdfInstancePrecision = validateWindowAndGetDateFormat(window);

		// Check if the document has root to append it as prefix to all properties
		final String propertyPrefix = getDocumentBase(rootElement);

		// Get Calendar with the maximum precission for this window
		final SimpleDateFormat sdfSeconds = new SimpleDateFormat(FORMAT_WINDOW_SECONDS);
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(sdfSeconds.parse(formattedDate));

		sdfInstancePrecision.setTimeZone(TimeZone.getTimeZone(timeZone));

		final Date dInstance = sdfInstancePrecision.parse(formattedDate);

		for (final Entry<OntologyTimeSeriesProperty, Object> field : mFields.entrySet()) {
			MongoDBTimeSeriesInstanceKey timeserieInstanceKey = null;
			try {
				// Build the query object with all tags, the concrete field and the timestamp
				// Append Tags to the query
				final BasicDBObject objQuery = new BasicDBObject();
				mTags.forEach((key, value) -> {
					if (key.getPropertyDataType().equals(PropertyDataType.OBJECT)) {
						objQuery.put(propertyPrefix.concat(key.getPropertyName()),
								BasicDBObject.parse(value.toString()));
					} else {
						objQuery.put(propertyPrefix.concat(key.getPropertyName()), value);
					}
				});

				// Append concrete Field to the query
				objQuery.put(propertyPrefix.concat(PROPERTY_NAME), field.getKey().getPropertyName());

				// Append Timestamp to the query
				objQuery.put(propertyPrefix.concat(TIMESTAMP_PROPERTY), dInstance);

				// Append window Type to the query
				objQuery.put(propertyPrefix.concat(WINDOW_TYPE), window.getWindowType().name());

				// Append window Frequency to the query
				objQuery.put(propertyPrefix.concat(WINDOW_FRECUENCY), window.getFrecuency());
				objQuery.put(propertyPrefix.concat(WINDOW_FRECUENCY_UNIT), window.getFrecuencyUnit().name());

				// Append timestamp of the document to the query
				objQuery.put(propertyPrefix.concat(TIMESTAMP_PROPERTY), dInstance);

				// Add stats if allowed
				if (timeseriesConfigData.getOntologyStats() != null) {
					final BasicDBObject objStat = new BasicDBObject();
					final ArrayList<BasicDBObject> tags = new ArrayList<>();
					for (final Entry<OntologyTimeSeriesProperty, Object> tag : mTags.entrySet()) {
						final BasicDBObject tagDBObj = new BasicDBObject();
						tagDBObj.put("name", tag.getKey().getPropertyName());
						if (tag.getKey().getPropertyDataType().equals(PropertyDataType.OBJECT)) {
							tagDBObj.put(VALUE_STR, BasicDBObject.parse(tag.getValue().toString()));
						} else {
							tagDBObj.put(VALUE_STR, tag.getValue());
						}
						tags.add(tagDBObj);
					}
					objStat.put("tag", tags);
					objStat.put("field", field.getKey().getPropertyName());
					objStat.put(WINDOW_TYPE, window.getWindowType().name());
					objStat.put(WINDOW_FRECUENCY, window.getFrecuency());
					objStat.put(WINDOW_FRECUENCY_UNIT, window.getFrecuencyUnit().name());
					BasicDBObject lastValue;
					if (field.getKey().getPropertyDataType().equals(PropertyDataType.OBJECT)) {
						lastValue = new BasicDBObject(VALUE_STR, BasicDBObject.parse(field.getValue().toString()));
					} else {
						lastValue = new BasicDBObject(VALUE_STR, field.getValue());
					}
					objStat.put("lastValue", lastValue);
					final BasicDBObject sample = new BasicDBObject(STATS_PREFIX, objStat);
					mongoDbConnector.insert(database, timeseriesConfigData.getOntologyStats().getIdentification(),
							sample);
				}

				// Build update
				final String update = buildUpdate(propertyPrefix, field, mInformativeFields, calendar, window,
						updateType);
				if (log.isDebugEnabled()) {
					log.debug("Try to update TimeSeries ontology {} for window {}", ontology,
						window.getWindowType().name());
				}				

				// try to update the document --> Consider it exists, if not, it will be created
				UpdateResult updated = mongoDbConnector.updateWithOriginalTemplateResult(database, ontology, objQuery.toString(),
						update, false, false);
				
				if (updated.getModifiedCount() == 0 && updated.getModifiedCount() == 0) {
					// Document does not exist, as no match has occured
					log.debug("CREATING DOC TimeSeries ontology {} for window {}");
					timeserieInstanceKey = new MongoDBTimeSeriesInstanceKey();
					timeserieInstanceKey.setOntology(ontology);
					timeserieInstanceKey.setSignal(field.getKey().getPropertyName());
					timeserieInstanceKey.setTimestamp(dInstance);
					timeserieInstanceKey.setWindow(window.getId());
					final List<String> tags = new ArrayList<>();
					for (final Entry<OntologyTimeSeriesProperty, Object> tag : mTags.entrySet()) {
						final StringBuffer tagRecord = new StringBuffer().append(tag.getKey().getPropertyName())
								.append(";").append(tag.getValue().toString());
						tags.add(tagRecord.toString());
					}
					java.util.Collections.sort(tags);
					timeserieInstanceKey.setTags(tags);
					Thread currentThread = Thread.currentThread();// +UUID

					UUID uuid = UUID.randomUUID();
					StringBuffer value = new StringBuffer().append(currentThread.toString())
							.append(uuid.toString());
					log.debug("TSCON1_NOUPDATE {}", timeserieInstanceKey.printkeys());
					String retrievedValue = timeseriesUpdateTransactionMap.putIfAbsent(timeserieInstanceKey,
							value.toString());
					if (retrievedValue != null && !retrievedValue.equals(value.toString())) {
						log.debug("COLISION, waiting TimeSeries ontology {} for window {}");
						log.debug("TSCON1B_COLLISION {}", timeserieInstanceKey.printkeys());
						// wait for T ms to retry
						Long startTime = new Date().getTime();

						do {
							Thread.sleep(waitBetweenChecks);
							retrievedValue = timeseriesUpdateTransactionMap.get(timeserieInstanceKey);
						} while (startTime + waitChecksTimeout > new Date().getTime() && retrievedValue != null);

						if (retrievedValue != null) {
							// if exists, then timeout has passed and we have to discard the record -> ERROR
							// Throw timeout exception
							final StringBuffer errorMessage = new StringBuffer();
							errorMessage.append(
									"Error while waiting blocking insert to resolve. Timeout elapsed. Ontology: ")
									.append(ontology).append(" Field: ").append(timeserieInstanceKey.getSignal())
									.append(" Timestamp: ").append(timeserieInstanceKey.getTimestamp());
							log.error(errorMessage.toString());
							throw new TimeSeriesInsertLockTimeoutException(errorMessage.toString());
						} else {
							// if not exists, then the other process has created the insert and we can
							// update
							updated = mongoDbConnector.updateWithOriginalTemplateResult(database, ontology, objQuery.toString(), update, false,
									false);
							log.debug("TSCON1B_COLLISION_UPDATED {}", timeserieInstanceKey.printkeys());
							if (updated.getModifiedCount() == 0 && updated.getMatchedCount() == 0) {
								// if no record matches, then the previous insert did fail and the
								// document has not been persisted -> ERROR
								// Throw unable to update exception
								final StringBuffer errorMessage = new StringBuffer();
								errorMessage.append(
										"Error while waiting blocking insert to resolve. Unable to update. Ontology: ")
										.append(ontology).append(" Field: ").append(timeserieInstanceKey.getSignal())
										.append(" Timestamp: ").append(timeserieInstanceKey.getTimestamp());
								log.error(errorMessage.toString());
								throw new TimeSeriesUnableToUpdateException(errorMessage.toString());
							}
						}
					} else {
						log.debug("TSCON1A_UPDATE {}", timeserieInstanceKey.printkeys());
						// Retry update after locking key, in case any other thread did
						// lock-insert-unlock between this update error and the pending insert
						updated = mongoDbConnector.updateWithOriginalTemplateResult(database, ontology, objQuery.toString(), update, false,
								false);
						// if there was an updated record, an other thread would have created the key
						// first between the first update fail and the lock of this thread
						if (updated.getModifiedCount() == 0 && updated.getMatchedCount() == 0) {// Check if exists and previously created. If not, Build the
											// document
							if (log.isDebugEnabled()) {
								log.debug("Check if document exits for TimeSeries ontology {} for window {}", ontology,
									window.getWindowType().name());
								log.debug("TSCON1A_NOUPDATED {}", timeserieInstanceKey.printkeys());
							}

								final BasicDBObject timeIntance = buildDocument(rootElement, calendar, field, dInstance,
										mTags, mInformativeFields, window, updateType == UPDATE_TYPE.SUM ? 0 : null);
								mongoDbConnector.insert(database, ontology, timeIntance);
								timeseriesUpdateTransactionMap.remove(timeserieInstanceKey);
								if (log.isDebugEnabled()) {
									log.debug("Created new document for TimeSeries ontology {} for window {}", ontology,
										window.getWindowType().name());
									log.debug("TSCON1A_INSERTED {}", timeserieInstanceKey.printkeys());
								}
						} else {
							// TODO Delete this else after testing
							timeseriesUpdateTransactionMap.remove(timeserieInstanceKey);
							log.debug("TSCON1A_UPDATED {}", timeserieInstanceKey.printkeys());
						}
					}

				}

				final TimeSeriesResult partialResult = new TimeSeriesResult();
				partialResult.setFieldName(field.getKey().getPropertyName());
				partialResult.setOk(true);
				partialResult.setWindowType(window.getWindowType().name());

				result.add(partialResult);

			} catch (final Exception e) {
				Log.error("Error processing window for ontology {} and property {}", ontology,
						field.getKey().getPropertyName(), e);
				if (timeserieInstanceKey != null) {
					timeseriesUpdateTransactionMap.remove(timeserieInstanceKey);
				}
				final TimeSeriesResult partialResult = new TimeSeriesResult();
				partialResult.setFieldName(field.getKey().getPropertyName());
				partialResult.setOk(false);
				partialResult.setWindowType(window.getWindowType().name());
				partialResult.setErrorMessage(e.getMessage());

				result.add(partialResult);
			}
		}

		return result;

	}

	private SimpleDateFormat validateWindowAndGetDateFormat(OntologyTimeSeriesWindow window)
			throws TimeSeriesFrecuencyNotSupportedException, WindowNotSupportedException {
		final WindowType windowType = window.getWindowType();
		final FrecuencyUnit frecuencyUnit = window.getFrecuencyUnit();
		if (windowType == WindowType.MINUTES) {
			if (frecuencyUnit != FrecuencyUnit.SECONDS) {
				throw new TimeSeriesFrecuencyNotSupportedException(
						"In minutes Window only Second frecuency is supported");
			}
			return new SimpleDateFormat(FORMAT_WINDOW_MINUTES);

		} else if (windowType == WindowType.HOURS) {
			if (frecuencyUnit != FrecuencyUnit.SECONDS && frecuencyUnit != FrecuencyUnit.MINUTES) {
				throw new TimeSeriesFrecuencyNotSupportedException(
						"In hours Window only Seconds and Minutes frecuencies are supported");
			}
			return new SimpleDateFormat(FORMAT_WINDOW_HOURS);
		} else if (windowType == WindowType.DAYS) {
			if (frecuencyUnit != FrecuencyUnit.SECONDS && frecuencyUnit != FrecuencyUnit.MINUTES
					&& frecuencyUnit != FrecuencyUnit.HOURS) {
				throw new TimeSeriesFrecuencyNotSupportedException(
						"In days Window only Seconds, Minutes and Hours frecuencies are supported");
			}
			return new SimpleDateFormat(FORMAT_WINDOW_DAYS);

		} else if (windowType == WindowType.MONTHS) {
			if (frecuencyUnit != FrecuencyUnit.SECONDS && frecuencyUnit != FrecuencyUnit.MINUTES
					&& frecuencyUnit != FrecuencyUnit.HOURS && frecuencyUnit != FrecuencyUnit.DAYS) {
				throw new TimeSeriesFrecuencyNotSupportedException(
						"In months Window only Seconds, Minutes, Hours and Days frecuencies are supported");
			}
			return new SimpleDateFormat(FORMAT_WINDOW_MONTHS);
		} else if (windowType == WindowType.YEARS) {
			if (frecuencyUnit != FrecuencyUnit.SECONDS && frecuencyUnit != FrecuencyUnit.MINUTES
					&& frecuencyUnit != FrecuencyUnit.HOURS && frecuencyUnit != FrecuencyUnit.DAYS
					&& frecuencyUnit != FrecuencyUnit.MONTHS) {
				throw new TimeSeriesFrecuencyNotSupportedException(
						"In years Window only Seconds, Minutes, Hours, Days and Months frecuencies are supported");
			}
			return new SimpleDateFormat(FORMAT_WINDOW_YEARS);
		} else {
			throw new WindowNotSupportedException(WINDOW_TYPE_STR + windowType.name() + NOT_SUPPORTED);
		}
	}

	private String buildUpdate(String propertyPrefix, Entry<OntologyTimeSeriesProperty, Object> field,
			Map<OntologyTimeSeriesProperty, Object> mInformativeFields, Calendar calendar,
			OntologyTimeSeriesWindow window, UPDATE_TYPE updateType) throws WindowNotSupportedException {
		final WindowType windowType = window.getWindowType();
		final int frecuency = window.getFrecuency();

		String update = "{" + updateType.value + " :{\"" + propertyPrefix + "values.v.";
		if (windowType == WindowType.MINUTES) {
			update += Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency)
					+ "\": " + toJsonValue(field.getKey(), field.getValue()) + "}}";

			return update;
		} else if (windowType == WindowType.HOURS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				update += Integer.toString(calendar.get(Calendar.MINUTE)) + "."
						+ Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency)
						+ "\": ";
				break;

			case MINUTES:
				update += Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency)
						+ "\": ";
				break;
			default:
				break;
			}

		} else if (windowType == WindowType.DAYS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				update += Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE)) + "."
						+ Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency)
						+ "\": ";
				break;

			case MINUTES:
				update += Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency)
						+ "\": ";
				break;
			case HOURS:
				update += Integer.toString(
						calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency) + "\":";
				break;
			default:
				break;
			}

		} else if (windowType == WindowType.MONTHS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				update += Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE)) + "."
						+ Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency)
						+ "\": ";
				break;

			case MINUTES:
				update += Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency)
						+ "\": ";
				break;
			case HOURS:
				update += Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(
								calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency)
						+ "\":";
				break;
			case DAYS:
				update += Integer.toString(
						calendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) % frecuency) + "\":";
				break;
			default:
				break;
			}

		} else if (windowType == WindowType.YEARS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				update += (Integer.toString(calendar.get(Calendar.MONTH) + 1)) + "."
						+ Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE)) + "."
						+ Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency)
						+ "\": ";
				break;

			case MINUTES:
				update += (Integer.toString(calendar.get(Calendar.MONTH) + 1)) + "."
						+ Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "."
						+ Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency)
						+ "\": ";
				break;
			case HOURS:
				update += (Integer.toString(calendar.get(Calendar.MONTH) + 1)) + "."
						+ Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "."
						+ Integer.toString(
								calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency)
						+ "\":";
				break;
			case DAYS:
				update += (Integer.toString(calendar.get(Calendar.MONTH) + 1)) + "."
						+ Integer.toString(
								calendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) % frecuency)
						+ "\":";
				break;
			case MONTHS:
				update += Integer.toString(calendar.get(Calendar.MONTH) + 1 - calendar.get(Calendar.MONTH) % frecuency)
						+ "\":";
				break;
			default:
				break;
			}

		} else {
			throw new WindowNotSupportedException(WINDOW_TYPE_STR + windowType.name() + NOT_SUPPORTED);
		}

		// TODO SOPORTE PARA AGREGACIÓN
		// if(field.getValue() instanceof Integer || field.getValue() instanceof Long ||
		// field.getValue() instanceof Double) {
		// Object value;
		// switch (window.getAggregationFunction()) {
		// case AVG:
		// break;
		// case MAX:
		// break;
		// case MIN:
		// break;
		// case FIRST:
		// break;
		// case LAST:
		// break;
		// }
		// }

		update += toJsonValue(field.getKey(), field.getValue());

		if (mInformativeFields != null && !mInformativeFields.isEmpty()) {
			final String separator = ",";
			final StringBuffer informativeFields = new StringBuffer();
			mInformativeFields.forEach((key, value) -> {
				switch (key.getPropertyDataType()) {
				case OBJECT:
					final String val = value.toString();
					if (val.startsWith("{\"$date\":")) {

						informativeFields.append(separator).append("\"").append("TimeSerie.")
								.append(key.getPropertyName()).append("\":").append(val);
					} else {
						informativeFields.append(separator).append("\"").append("TimeSerie.")
								.append(key.getPropertyName()).append("\":").append(BasicDBObject.parse(value.toString()))
								.append("");
					}
					break;
				case INTEGER:
				case NUMBER:
					informativeFields.append(separator).append("\"").append("TimeSerie.").append(key.getPropertyName())
							.append("\":").append(value);
					break;
				case STRING:
				default:
					informativeFields.append(separator).append("\"").append("TimeSerie.").append(key.getPropertyName())
							.append("\":\"").append(value).append("\"");
					break;
				}
			});
			update += informativeFields.toString();
		}

		update += "}}";

		return update;
	}

	private BasicDBObject buildDocument(Optional<String> rootElement, Calendar calendar,
			Entry<OntologyTimeSeriesProperty, Object> field, Date dInstance,
			Map<OntologyTimeSeriesProperty, Object> mTags, Map<OntologyTimeSeriesProperty, Object> mInformativeFields,
			OntologyTimeSeriesWindow window, Object initialValue) throws WindowNotSupportedException {

		final WindowType windowType = window.getWindowType();

		Map<String, Object> vMeasures;

		if (windowType == WindowType.MINUTES) {
			vMeasures = buildNewMinuteMap(window.getFrecuency(), initialValue);
		} else if (windowType == WindowType.HOURS) {
			vMeasures = buildNewHourMap(window.getFrecuencyUnit(), window.getFrecuency(), initialValue);
		} else if (windowType == WindowType.DAYS) {
			vMeasures = buildNewDayMap(window.getFrecuencyUnit(), window.getFrecuency(), initialValue);
		} else if (windowType == WindowType.MONTHS) {
			vMeasures = buildNewMonthMap(window.getFrecuencyUnit(), calendar, window.getFrecuency(), initialValue);
		} else if (windowType == WindowType.YEARS) {
			vMeasures = buildNewYearMap(window.getFrecuencyUnit(), calendar, window.getFrecuency(), initialValue);
		} else {
			throw new WindowNotSupportedException(WINDOW_TYPE_STR + windowType.name() + NOT_SUPPORTED);
		}

		if (field.getKey().getPropertyDataType() == PropertyDataType.OBJECT)
			setFirstValueOfDocument(vMeasures, window, calendar, BasicDBObject.parse(field.getValue().toString()));
		else
			setFirstValueOfDocument(vMeasures, window, calendar, field.getValue());

		final BasicDBObject vArray = new BasicDBObject(vMeasures);

		final BasicDBObject v = new BasicDBObject();
		v.put("v", vArray);

		final BasicDBObject data = new BasicDBObject();
		data.put("values", v);

		mTags.forEach((key, value) -> {
			if (key.getPropertyDataType().equals(PropertyDataType.OBJECT))
				data.put(key.getPropertyName(), BasicDBObject.parse(value.toString()));
			else
				data.put(key.getPropertyName(), value);

		});

		mInformativeFields.forEach((key, value) -> {
			if (key.getPropertyDataType().equals(PropertyDataType.OBJECT))
				data.put(key.getPropertyName(), BasicDBObject.parse(value.toString()));
			else
				data.put(key.getPropertyName(), value);

		});

		data.put(TIMESTAMP_PROPERTY, dInstance);
		data.put(PROPERTY_NAME, field.getKey().getPropertyName());
		data.put(WINDOW_TYPE, windowType.name());
		data.put(WINDOW_FRECUENCY, window.getFrecuency());
		data.put(WINDOW_FRECUENCY_UNIT, window.getFrecuencyUnit().name());

		BasicDBObject timeIntance;
		if (rootElement.isPresent()) {
			timeIntance = new BasicDBObject(rootElement.get(), data);
		} else {
			timeIntance = data;
		}

		return timeIntance;
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> setFirstValueOfDocument(Map<String, Object> vMeasures, OntologyTimeSeriesWindow window,
			Calendar calendar, Object value) throws WindowNotSupportedException {
		final WindowType windowType = window.getWindowType();
		final int frecuency = window.getFrecuency();

		if (windowType == WindowType.MINUTES) {
			vMeasures.put(Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency),
					value);
		} else if (windowType == WindowType.HOURS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				((Map<String, Object>) vMeasures.get(Integer.toString(calendar.get(Calendar.MINUTE)))).put(
						Integer.toString(calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency),
						value);
				break;
			case MINUTES:
				vMeasures.put(
						Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency),
						value);
				break;
			default:
			}

		} else if (windowType == WindowType.DAYS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))))
						.get(Integer.toString(calendar.get(Calendar.MINUTE))))
						.put(Integer.toString(
								calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency), value);

				break;

			case MINUTES:
				((Map<String, Object>) vMeasures.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)))).put(
						Integer.toString(calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency),
						value);

				break;
			case HOURS:
				vMeasures.put(
						Integer.toString(
								calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency),
						value);
				break;
			default:
				break;
			}

		} else if (windowType == WindowType.MONTHS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))))
						.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))))
						.get(Integer.toString(calendar.get(Calendar.MINUTE))))
						.put(Integer.toString(
								calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency), value);

				break;

			case MINUTES:
				((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))))
						.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))))
						.put(Integer.toString(
								calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency), value);
				break;
			case HOURS:
				((Map<String, Object>) vMeasures.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)))).put(
						Integer.toString(
								calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency),
						value);
				break;
			case DAYS:
				vMeasures.put(
						Integer.toString(
								calendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) % frecuency),
						value);
				break;
			default:
				break;
			}

		} else if (windowType == WindowType.YEARS) {
			switch (window.getFrecuencyUnit()) {
			case SECONDS:
				((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.MONTH) + 1)))
						.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))))
						.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))))
						.get(Integer.toString(calendar.get(Calendar.MINUTE))))
						.put(Integer.toString(
								calendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND) % frecuency), value);

				break;

			case MINUTES:
				((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.MONTH) + 1)))
						.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))))
						.get(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))))
						.put(Integer.toString(
								calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % frecuency), value);
				break;
			case HOURS:
				((Map<String, Object>) ((Map<String, Object>) vMeasures
						.get(Integer.toString(calendar.get(Calendar.MONTH) + 1)))
						.get(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))))
						.put(Integer.toString(
								calendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY) % frecuency),
								value);
				break;
			case DAYS:
				((Map<String, Object>) vMeasures.get(Integer.toString(calendar.get(Calendar.MONTH) + 1))).put(
						Integer.toString(
								calendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) % frecuency),
						value);
				break;
			case MONTHS:
				vMeasures.put(
						Integer.toString(calendar.get(Calendar.MONTH) + 1 - calendar.get(Calendar.MONTH) % frecuency),
						value);
				break;
			default:
				break;
			}

		} else {
			throw new WindowNotSupportedException(WINDOW_TYPE_STR + windowType.name() + NOT_SUPPORTED);
		}

		return vMeasures;
	}

	private Map<String, Object> buildNewMinuteMap(int frecuency, Object value) {
		final Map<String, Object> vSeconds = new LinkedHashMap<>();
		for (int i = 0; i < 60; i = i + frecuency) {
			vSeconds.put(Integer.toString(i), value);
		}

		return vSeconds;
	}

	private Map<String, Object> buildNewHourMap(FrecuencyUnit frecuencyUnit, int frecuency, Object value) {
		final Map<String, Object> vMinutes = new LinkedHashMap<>();
		if (frecuencyUnit == FrecuencyUnit.MINUTES) {
			for (int i = 0; i < 60; i = i + frecuency) {
				vMinutes.put(Integer.toString(i), value);
			}

		} else {// Seconds
			for (int i = 0; i < 60; i++) {
				vMinutes.put(Integer.toString(i), buildNewMinuteMap(frecuency, value));
			}
		}

		return vMinutes;
	}

	private Map<String, Object> buildNewDayMap(FrecuencyUnit frecuencyUnit, int frecuency, Object value) {
		final Map<String, Object> vHours = new LinkedHashMap<>();
		if (frecuencyUnit == FrecuencyUnit.HOURS) {
			for (int i = 0; i < 24; i = i + frecuency) {
				vHours.put(Integer.toString(i), value);
			}
		} else {// Minutes or Seconds
			for (int i = 0; i < 24; i++) {
				vHours.put(Integer.toString(i), buildNewHourMap(frecuencyUnit, frecuency, value));
			}
		}

		return vHours;
	}

	private Map<String, Object> buildNewMonthMap(FrecuencyUnit frecuencyUnit, Calendar calendar, int frecuency,
			Object value) {
		final Map<String, Object> vDays = new LinkedHashMap<>();
		if (frecuencyUnit == FrecuencyUnit.DAYS) {
			for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i = i + frecuency) {
				vDays.put(Integer.toString(i), value);
			}
		} else {// Hours, Minutes or Seconds
			for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
				vDays.put(Integer.toString(i), buildNewDayMap(frecuencyUnit, frecuency, value));
			}
		}

		return vDays;
	}

	private Map<String, Object> buildNewYearMap(FrecuencyUnit frecuencyUnit, Calendar calendar, int frecuency,
			Object value) {
		final Map<String, Object> vMonths = new LinkedHashMap<>();
		if (frecuencyUnit == FrecuencyUnit.MONTHS) {
			for (int i = 1; i <= calendar.getActualMaximum(Calendar.MONTH) + 1; i = i + frecuency) {
				vMonths.put(Integer.toString(i), value);
			}
		} else {// DAYS, Hours, Minutes or Seconds
			for (int i = 1; i <= calendar.getActualMaximum(Calendar.MONTH) + 1; i++) {
				// change the month
				Calendar monthlyCal = (Calendar) calendar.clone();
				monthlyCal.set(Calendar.MONTH, i - 1);
				vMonths.put(Integer.toString(i), buildNewMonthMap(frecuencyUnit, monthlyCal, frecuency, value));
			}
		}

		return vMonths;
	}

	private String toJsonValue(OntologyTimeSeriesProperty property, Object value) {
		switch (property.getPropertyDataType()) {
		case INTEGER:
		case NUMBER:
			return value.toString();
		case STRING:
			return "\"" + value.toString() + "\"";
		case OBJECT:
			return "\"" + value.toString() + "\"";
		default:
			return "\"\"";

		}
	}

	private String getDocumentBase(Optional<String> rootElement) {
		String base = "";
		if (rootElement.isPresent()) {
			base = rootElement.get() + ".";
		}
		return base;
	}

	private TimeseriesConfigData getTimeseriesConfigData(String ontology) {
		TimeseriesConfigData result = timeseriesConfigData.get(ontology);
		if (result == null || result.getCreationTimestamp() + timeseriesConfigTtl < System.currentTimeMillis()) {
			// refresh al data
			result = new TimeseriesConfigData();
			result.setProperties(ontologyTimeSeriesPropertyRepository.findByOntologyIdentificaton(ontology));
			result.setWindows(ontologyTimeSeriesWindowRepository.findByOntologyIdentificaton(ontology));
			result.setOntologyStats(ontologyRepository.findByIdentification(ontology + "_stats"));
			result.setCreationTimestamp(System.currentTimeMillis());
			timeseriesConfigData.put(ontology, result);
		}
		return result;
	}

}
