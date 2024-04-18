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
package com.minsait.onesait.platform.persistence.timescaledb.processor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.TimeSeriesResult;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesPropertyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.persistence.timescaledb.config.TimescaleDBConfiguration;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TimescaleDBTimeSeriesProcessorImpl implements TimescaleDBTimeSeriesProcessor {

	private static final String CONTEXT_DATA = "contextData";
	private static final String TIMESTAMP_PROPERTY = "timestamp";
	private static final String SDATE = "$date";
	private static final String DOT = ".";
	private static final String EQUALS = "=";
	private static final String COMMA = ",";
	private static final String COLON = ":";
	private static final String COMMA_BLANK = ", ";
	private static final String CLOSE_PARENTHESES = ")";
	private static final String EXCLUDED = "excluded.";

	private static final String FORMAT_WINDOW_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@Value("${onesaitplatform.database.timeseries.timezone:UTC}")
	private String timeZone;

	@Autowired
	private OntologyTimeSeriesPropertyRepository ontologyTimeSeriesPropertyRepository;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired(required = false)
	@Qualifier(TimescaleDBConfiguration.TIMESCALEDB_PARAMETER_TEMPLATE_JDBC_BEAN_NAME)
	private NamedParameterJdbcTemplate timescaleDBJdbcTemplate;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	@Override
	public List<TimeSeriesResult> processTimeSerie(String ontology, List<String> instances) {

		log.info("Process TimeSerie instance for ontology {}", ontology);
		final List<TimeSeriesResult> result = new ArrayList<>();

		// Get Properties declared for the Timeserie ontology
		final List<OntologyTimeSeriesProperty> lProperties = ontologyTimeSeriesPropertyRepository
				.findByOntologyIdentificaton(ontology);

		// Get timeserie ontology
		final OntologyTimeSeries timeSerieOntology = ontologyTimeSeriesRepository.findByOntologyIdentificaton(ontology);

		// GET fields and tags (just to have the order, we assume all instances have the
		// same structure as the first one)
		List<OntologyTimeSeriesProperty> timeserieTags = new ArrayList();
		List<OntologyTimeSeriesProperty> timeserieFields = new ArrayList();

		String sqlQuery = "";
		if (instances != null && instances.size() > 0) {
			final JSONObject oInstance = getJSONInstanceWithoutRootElement(instances.get(0));

			Map<OntologyTimeSeriesProperty, Object> mTags = extractTags(lProperties, oInstance);
			Map<OntologyTimeSeriesProperty, Object> mFields = extractFields(lProperties, oInstance);
			// Insert in ordered collection
			mTags.entrySet().forEach(tag -> timeserieTags.add(tag.getKey()));
			mFields.entrySet().forEach(field -> timeserieFields.add(field.getKey()));
			// Prepare statement
			sqlQuery = prepareTemplateStatement(timeSerieOntology, timeserieTags, timeserieFields, instances.size());
		}

		final int affected;
		final List<Map<String, Object>> keyList;

		final GeneratedKeyHolder holder = new GeneratedKeyHolder();

		Map<String, Object> parameters;
		try {
			parameters = generateParamMapForValues(timeserieTags, timeserieFields, instances, timeSerieOntology);
		} catch (TimeSeriesInvalidTimestampException e) {
			log.error("Error while parsing timestamp from instance while inserting bulk into TimeSerie {}",
					timeSerieOntology.getOntology().getIdentification());
			return result;
		}
		final MapSqlParameterSource sqlParams = new MapSqlParameterSource();
		parameters.forEach((k, v) -> sqlParams.addValue(k, v, java.sql.Types.OTHER));
		affected = timescaleDBJdbcTemplate.update(sqlQuery, sqlParams, holder);
		keyList = holder.getKeyList();

		if (keyList != null && !keyList.isEmpty()) {
			
			for (int i = 0; i < affected; i++) {
				final TimeSeriesResult singleReslt= new TimeSeriesResult();
				singleReslt.setOk(true);
				result.add(singleReslt);
			}
			/*return IntStream.range(0, affected).mapToObj(num -> new DBResult().setOk(true))
					.collect(Collectors.toList());*/
		}

		return result;
	}

	private String prepareTemplateStatement(OntologyTimeSeries timeserieOntology,
			List<OntologyTimeSeriesProperty> mTags, List<OntologyTimeSeriesProperty> mFields, int instancesNumber) {
		StringBuilder insertQuery = new StringBuilder().append("INSERT INTO ")
				.append(timeserieOntology.getOntology().getIdentification());

		StringBuilder fields = new StringBuilder().append("(timestamp");
		StringBuilder values = new StringBuilder().append("VALUES ");
		StringBuilder onConflict = new StringBuilder().append(" ON CONFLICT (timestamp");

		// Add tags
		for (OntologyTimeSeriesProperty tag : mTags) {
			fields.append(COMMA_BLANK).append(tag.getPropertyName());
			onConflict.append(COMMA_BLANK).append(tag.getPropertyName());
		}

		// Add signals to fields
		for (OntologyTimeSeriesProperty field : mFields) {
			fields.append(COMMA_BLANK).append(field.getPropertyName());
		}

		for (int i = 0; i < instancesNumber; i++) {
			// set values for tiestamp
			values.append("(").append(":timestamp").append(i);
			// Set values for tags
			for (OntologyTimeSeriesProperty tag : mTags) {
				values.append(COMMA_BLANK).append(COLON).append(tag.getPropertyName()).append(i);
			}
			// Set values for fields
			for (OntologyTimeSeriesProperty field : mFields) {
				values.append(COMMA_BLANK).append(COLON).append(field.getPropertyName()).append(i);
			}
			values.append(CLOSE_PARENTHESES).append(COMMA);
		}
		// remove last comma from values
		if (values.toString().endsWith(COMMA)) {
			values.deleteCharAt(values.length() - 1);
		}

		// Compose query
		insertQuery.append(fields).append(CLOSE_PARENTHESES).append(values);
		if (timeserieOntology.getTimeSeriesTimescaleProperties().getFrecuencyUnit() != FrecuencyUnit.NONE) {
			// If event freq (no aggr) then there is no on conflict clause
			onConflict.append(") DO UPDATE SET ");
			// Add each field with it's own aggregation function
			for (OntologyTimeSeriesProperty field : mFields) {
				fields.append(COMMA_BLANK).append(field.getPropertyName());
				String fieldConflictBehaviour = getSignalConflictAggregationFunction(
						timeserieOntology.getOntology().getIdentification(), field);
				onConflict.append(fieldConflictBehaviour);
			}
			// remove last comma from onConflict
			if (onConflict.toString().endsWith(COMMA)) {
				onConflict.deleteCharAt(onConflict.length() - 1);
			}
			insertQuery.append(onConflict);
		}

		insertQuery.append(";");
		return insertQuery.toString();
	}

	private Map<String, Object> generateParamMapForValues(List<OntologyTimeSeriesProperty> tags,
			List<OntologyTimeSeriesProperty> fields, List<String> instances, OntologyTimeSeries timeseriesOntology)
			throws TimeSeriesInvalidTimestampException {
		Map<String, Object> params = new HashMap<>();

		for (int index = 0; index < instances.size(); index++) {
			String instance = instances.get(index);
			// Divide Root element and Data of the instance
			JSONObject instanceData = getJSONInstanceWithoutRootElement(instance);

			// Extract Timestamp from instance
			final JSONObject timestamp = (JSONObject) instanceData.get(TIMESTAMP_PROPERTY);
			String formattedDate = (String) timestamp.get(SDATE);
			// TODO: Round date to frequency
			if (timeseriesOntology.getTimeSeriesTimescaleProperties().getFrecuencyUnit() != FrecuencyUnit.NONE
					&& timeseriesOntology.getTimeSeriesTimescaleProperties().getFrecuencyUnit() != FrecuencyUnit.NODUPS) {

				final SimpleDateFormat sdfSeconds = new SimpleDateFormat(FORMAT_WINDOW_SECONDS);
				final Calendar calendar = Calendar.getInstance();
				try {
					calendar.setTime(sdfSeconds.parse(formattedDate));
				} catch (ParseException e) {
					String errorMsg = new StringBuilder().append("Error while parsing timestamp ").append(formattedDate)
							.append(" for timeserie ").append(timeseriesOntology.getOntology().getIdentification())
							.toString();
					log.error(errorMsg);
					throw new TimeSeriesInvalidTimestampException(errorMsg);
				}
				Calendar newDate = parseTimestampToAggregate(calendar, timeseriesOntology);
				formattedDate = sdfSeconds.format(newDate.getTime());
			}
			// Process Instance
			try {
				if (log.isDebugEnabled()) {
					log.debug("Process TimescaleDB timeserie for ontology {}",
						timeseriesOntology.getOntology().getIdentification());
				}				
				DateTime dateTime = new DateTime(formattedDate);
				params.put(TIMESTAMP_PROPERTY + index, new Timestamp(dateTime.getMillis()));
				params.putAll(generateValueClauseForInstance(tags, fields, formattedDate, instanceData, index));
			} catch (Exception e) {
				log.error("Error processing TimescaleDB TimeSeries BULK {}. Cause={}, Message={}",
						timeseriesOntology.getOntology().getIdentification(), e.getCause(), e.getMessage());
			}
		}

		return params;

	}

	private Calendar parseTimestampToAggregate(Calendar calendar, OntologyTimeSeries timeseriesOntology) {
		String processedTimestamp = "";
		// TODO:
		final Integer freq = timeseriesOntology.getTimeSeriesTimescaleProperties().getFrecuency();
		switch (timeseriesOntology.getTimeSeriesTimescaleProperties().getFrecuencyUnit()) {
		case SECONDS:
			final int seconds = calendar.get(Calendar.SECOND) - (calendar.get(Calendar.SECOND) % freq);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case MINUTES:
			final int minutes = calendar.get(Calendar.MINUTE) - (calendar.get(Calendar.MINUTE) % freq);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case HOURS:
			final int hours = calendar.get(Calendar.HOUR) - (calendar.get(Calendar.HOUR) % freq);
			calendar.set(Calendar.HOUR, hours);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case DAYS:
			final int day = calendar.get(Calendar.DAY_OF_MONTH) - (calendar.get(Calendar.DAY_OF_MONTH) % freq);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		case MONTHS:
			final int month = calendar.get(Calendar.MONTH) - (calendar.get(Calendar.MONTH) % freq);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			break;
		default:
			// TODO: ERROR invalid?
		}
		return calendar;
	}

	private JSONObject getJSONInstanceWithoutRootElement(String instance) {
		// Divide Root element and Data of the instance
		final JSONObject oInstance = new JSONObject(instance);
		JSONObject instanceData = oInstance;

		Optional<String> rootElement = Optional.empty();
		if ((oInstance.keySet().size() == 1)
				|| (oInstance.keySet().size() == 2 && oInstance.keySet().contains(CONTEXT_DATA))) {
			// Check if there is root element
			rootElement = oInstance.keySet().stream().filter(p -> !p.equals(CONTEXT_DATA)).findFirst();
			if (rootElement.isPresent())
				instanceData = (JSONObject) oInstance.get(rootElement.get());
		}
		return instanceData;
	}

	private Map<String, String> generateValueClauseForInstance(List<OntologyTimeSeriesProperty> tags,
			List<OntologyTimeSeriesProperty> fields, String formattedDate, JSONObject instanceData, int index) {
		Map<String, String> params = new HashMap<>();
		// Add tags
		for (OntologyTimeSeriesProperty tag : tags) {
			StringBuilder key = new StringBuilder().append(tag.getPropertyName()).append(index);
			params.put(key.toString(), instanceData.get(tag.getPropertyName()).toString());
		}
		for (OntologyTimeSeriesProperty field : fields) {
			StringBuilder key = new StringBuilder().append(field.getPropertyName()).append(index);
			params.put(key.toString(), instanceData.get(field.getPropertyName()).toString());
		}

		return params;
	}


	private String getSignalConflictAggregationFunction(String tableName, OntologyTimeSeriesProperty field) {
		StringBuilder result = new StringBuilder();
		switch (field.getPropertyAggregationType()) {
		case NONE:
		case FIRST:
			// First commited value persist, ignoring new data
			result.append(field.getPropertyName()).append(EQUALS).append(tableName).append(".")
					.append(field.getPropertyName());
			break;
		case LAST:
			// new value is persisted, overwriting the previuos one
			result.append(field.getPropertyName()).append(EQUALS).append(EXCLUDED).append(field.getPropertyName());
			break;
		case MAX:
			result.append(field.getPropertyName()).append(EQUALS).append("GREATEST(").append(EXCLUDED)
					.append(field.getPropertyName()).append(COMMA).append(tableName).append(DOT)
					.append(field.getPropertyName()).append(CLOSE_PARENTHESES);
			break;
		case MIN:
			result.append(field.getPropertyName()).append(EQUALS).append("LEAST(").append(EXCLUDED)
					.append(field.getPropertyName()).append(COMMA).append(tableName).append(DOT)
					.append(field.getPropertyName()).append(CLOSE_PARENTHESES);
			break;
		case SUM:
			/*
			 * result.append(field.getPropertyName()).append(EQUALS).append("ADD(").append(
			 * EXCLUDED)
			 * .append(field.getPropertyName()).append(COMMA).append(tableName).append(DOT)
			 * .append(field.getPropertyName()).append(CLOSE_PARENTHESES);
			 */
			result.append(field.getPropertyName()).append(EQUALS).append(EXCLUDED).append(field.getPropertyName())
					.append("+").append(tableName).append(DOT).append(field.getPropertyName());
			break;
		case PUSH:
			result.append(field.getPropertyName()).append(EQUALS).append("array_append(").append(tableName).append(DOT)
					.append(field.getPropertyName()).append(COMMA).append(EXCLUDED)
					.append(field.getPropertyPushSignal()).append(CLOSE_PARENTHESES);
			break;
		default:
			// TODO: Throw exception not valid aggregation type

		}
		result.append(COMMA);
		return result.toString();
	}

	private Map<OntologyTimeSeriesProperty, Object> extractTags(List<OntologyTimeSeriesProperty> lProperties,
			JSONObject oInstance) {
		// Extract Tags from instance
		final Map<OntologyTimeSeriesProperty, Object> mTags = new HashMap<>();
		lProperties.stream().filter(p -> p.getPropertyType() == PropertyType.TAG)
				.forEach(p -> mTags.put(p, oInstance.get(p.getPropertyName())));

		return mTags;
	}

	private Map<OntologyTimeSeriesProperty, Object> extractFields(List<OntologyTimeSeriesProperty> lProperties,
			JSONObject oInstance) {
		final Map<OntologyTimeSeriesProperty, Object> mFields = new HashMap<>();
		lProperties.stream()
				.filter(p -> p.getPropertyType() == PropertyType.SERIE_FIELD && oInstance.has(p.getPropertyName()))
				.forEach(p -> mFields.put(p, oInstance.get(p.getPropertyName())));

		return mFields;
	}
}
