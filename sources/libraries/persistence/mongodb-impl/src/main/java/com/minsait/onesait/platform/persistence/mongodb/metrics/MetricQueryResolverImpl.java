/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.mongodb.metrics;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jline.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.metrics.util.WhereCerosCompletionExpressionVisitorAdapter;
import com.minsait.onesait.platform.persistence.mongodb.tools.sql.WhereExpressionVisitorAdapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@Component
@Slf4j
public class MetricQueryResolverImpl implements MetricQueryResolver {

	@Value("${onesaitplatform.database.timeseries.timezone:UTC}")
	private String timeZone;

	public static final String FORMAT_MINUTES_MILLIS = "yyyy-MM-dd'T'HH:mm:00.000'Z'";
	public static final String FORMAT_HOURS = "yyyy-MM-dd'T'HH:00:00'Z'";
	public static final String FORMAT_HOURS_MILLIS = "yyyy-MM-dd'T'HH:00:00.000'Z'";
	public static final String FORMAT_DAYS = "yyyy-MM-dd'T'00:00:00'Z'";
	public static final String FORMAT_DAYS_MILLIS = "yyyy-MM-dd'T'00:00:00.000'Z'";
	public static final String FORMAT_MONTHS = "yyyy-MM-01'T'00:00:00'Z'";
	public static final String FORMAT_MONTHS_MILLIS = "yyyy-MM-01'T'00:00:00.000'Z'";

	private static final int MINUTES_IN_HOUR = 60;
	private static final int HOURS_IN_DAY = 24;

	private static final String VALUE_STR = "value";
	private static final String VALUES_STR = "values";
	private static final String TIMESERIE_STR = "TimeSerie";
	private static final String TIMESTAMP_STR = "timestamp";
	private static final String TIMESERIE_DATE_STR = "$.TimeSerie.timestamp.$date";
	private static final String WINDOW_TYPE_STR = "windowType";
	private static final String DATE_STR = "$date";

	private long lastCerosHourlyInserted = 0;
	private long lastCerosDailyInserted = 0;
	private long lastCerosMonthlyInserted = 0;

	@Override
	public Map<String, String> buildMongoDBQueryStatement(String query) throws Exception {
		final CCJSqlParserManager parserManager = new CCJSqlParserManager();

		final PlainSelect statement = (PlainSelect) ((Select) parserManager.parse(new StringReader(query)))
				.getSelectBody();

		List<SelectItem> lSelectItems = statement.getSelectItems();
		if (lSelectItems.size() > 1) {
			throw new DBPersistenceException(
					"In Metrics Ontologies only one of \"*\" or \"count(*)\" clausules are allowed");
		}

		final String ontology = ((Table) statement.getFromItem()).getName();
		StringBuilder sbMongoDbWhere = new StringBuilder();
		StringBuilder sbMongoDbWhereCerosCompletion = new StringBuilder();

		final Expression where = statement.getWhere();
		if (null != where) {
			where.accept(new WhereExpressionVisitorAdapter(sbMongoDbWhere, false, 0, false, 0));
			where.accept(new WhereCerosCompletionExpressionVisitorAdapter(sbMongoDbWhereCerosCompletion, false, 0,
					false, 0));
		}
		String mongoDbWhere = sbMongoDbWhere.toString();
		if (sbMongoDbWhere.toString().endsWith(",")) {
			mongoDbWhere = mongoDbWhere.substring(0, mongoDbWhere.length() - 1);
		}

		String mongoDbWhereCerosCompletion = sbMongoDbWhereCerosCompletion.toString();
		if (sbMongoDbWhereCerosCompletion.toString().endsWith(",")) {
			mongoDbWhereCerosCompletion = mongoDbWhereCerosCompletion.substring(0,
					mongoDbWhereCerosCompletion.length() - 1);
		}

		Map<String, String> result = new HashMap<>();
		result.put(ONTOLOGY_NAME, ontology);
		result.put(STATEMENT, mongoDbWhere);
		result.put(STATEMENT_CEROS_COMPLETION, mongoDbWhereCerosCompletion);
		result.put(SELECT_ITEMS, lSelectItems.get(0).toString());

		return result;

	}

	@Override
	public String buildUnifiedResponse(List<String> data, List<String> cerosInInterval, String selectItem)
			throws Exception {

		List<MetricsObject> lMetrics = extractMetrics(data);
		List<MetricsObject> lCeros = extractCeros(cerosInInterval);

		Map<Long, JSONArray> mDateGroups = new HashMap<>();
		lMetrics.forEach(metric -> {
			long timestamp = metric.getTimestamp();

			JSONArray grouped = mDateGroups.get(timestamp);
			if (null == grouped) {
				mDateGroups.put(timestamp, metric.getMetrics());
			} else {
				int length = grouped.length();
				JSONArray arrMetrics = metric.getMetrics();
				for (int i = 0; i < length; i++) {
					int currentValue = ((JSONObject) grouped.get(i)).getInt(VALUE_STR);
					int addValue = ((JSONObject) arrMetrics.get(i)).getInt(VALUE_STR);

					((JSONObject) grouped.get(i)).put(VALUE_STR, currentValue + addValue);
				}

			}
		});

		// Add ceros if not value detected
		lCeros.forEach(cero -> {
			long timestamp = cero.getTimestamp();

			if (!mDateGroups.containsKey(timestamp)) {
				mDateGroups.put(timestamp, cero.getMetrics());
			}

		});

		// LinkedHashMap preserve the ordering of elements in which they are inserted
		List<Long> sortedKeys = new LinkedList<>();

		mDateGroups.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEachOrdered(x -> sortedKeys.add(x.getKey()));

		JSONArray result = new JSONArray();

		sortedKeys.forEach(date -> {
			JSONArray current = mDateGroups.get(date);
			for (int i = 0; i < current.length(); i++) {
				result.put(current.get(i));
			}

		});

		if (selectItem.trim().equals("*")) {
			return result.toString();
		} else if (selectItem.trim().equals("count(*)")) {
			int count = 0;
			for (int i = 0; i < result.length(); i++) {
				count += ((JSONObject) result.get(i)).getInt(VALUE_STR);
			}
			return "{\"count\": " + count + "}";

		} else {
			throw new DBPersistenceException(
					"In Metrics Ontologies only one of \"*\" or \"count(*)\" clausules are allowed");
		}
	}

	@Override
	public void loadMetricsBase(MongoBasicOpsDBRepository mongodbRepository) {
		this.loadMetricsBaseHours(mongodbRepository);
		this.loadMetricsBaseDays(mongodbRepository);
		this.loadMetricsBaseMonths(mongodbRepository);
	}

	private void loadMetricsBaseHours(MongoBasicOpsDBRepository mongodbRepository) {
		try {

			SimpleDateFormat sdfHours = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_HOURS_MILLIS);

			Date nowHour = sdfHours.parse(sdfHours.format(new Date()));

			if (nowHour.getTime() > this.lastCerosHourlyInserted) {

				this.lastCerosHourlyInserted = nowHour.getTime();

				SimpleDateFormat sdfMinutes = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_MINUTES_MILLIS);

				SimpleDateFormat dateFormatGmt = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_HOURS_MILLIS);
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

				Calendar nowCalendar = Calendar.getInstance();
				nowCalendar.setTime(nowHour);

				Calendar lastDateCalendar = Calendar.getInstance();

				List<String> dataHours = mongodbRepository.queryNative(MongoBasicOpsDBRepository.METRICS_BASE,
						"db.getCollection('MetricsBase').find({'TimeSerie.windowType':'HOURS'}, {'TimeSerie.timestamp': 1 }).sort({'TimeSerie.timestamp': -1}).limit(1)");

				if (!dataHours.isEmpty()) {
					long lastDate = JsonPath.read(dataHours.get(0), TIMESERIE_DATE_STR);
					lastDateCalendar.setTime(sdfHours.parse(dateFormatGmt.format(new Date(lastDate))));

				} else {
					lastDateCalendar.setTime(sdfHours.parse("2018-12-31T23:00:00.000Z"));
				}

				// Create Hourly empty windows
				JSONObject dataHourly = new JSONObject();
				JSONObject rootHourly = new JSONObject();

				rootHourly.put(WINDOW_TYPE_STR, "HOURS");
				dataHourly.put(TIMESERIE_STR, rootHourly);

				long currentIntancetime = lastDateCalendar.getTime().getTime();
				if (currentIntancetime < nowCalendar.getTime().getTime()) {
					lastDateCalendar.add(Calendar.HOUR, 1);
					while (currentIntancetime <= nowCalendar.getTime().getTime()) {

						JSONObject date = new JSONObject();
						date.put(DATE_STR, sdfHours.format(lastDateCalendar.getTime()));
						rootHourly.put(TIMESTAMP_STR, date);

						JSONArray metrics = new JSONArray();
						rootHourly.put(VALUES_STR, metrics);

						for (int secondIndex = 0; secondIndex < 60; secondIndex++) {
							JSONObject metric = new JSONObject();
							metric.put(VALUE_STR, 0);
							metric.put(TIMESTAMP_STR, sdfMinutes.format(lastDateCalendar.getTime()));
							lastDateCalendar.add(Calendar.MINUTE, 1);
							metrics.put(metric);
						}

						mongodbRepository.insert(MongoBasicOpsDBRepository.METRICS_BASE, dataHourly.toString());

						currentIntancetime = sdfHours.parse(sdfHours.format(lastDateCalendar.getTime())).getTime();
					}
				}
			}

		} catch (Exception e) {
			log.error("Error setting content on MetricsBase collection", e);
		}
	}

	private void loadMetricsBaseDays(MongoBasicOpsDBRepository mongodbRepository) {
		try {

			SimpleDateFormat sdfDays = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_DAYS_MILLIS);

			Date nowDay = sdfDays.parse(sdfDays.format(new Date()));

			if (nowDay.getTime() > this.lastCerosDailyInserted) {

				this.lastCerosDailyInserted = nowDay.getTime();

				SimpleDateFormat sdfHours = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_HOURS_MILLIS);

				SimpleDateFormat dateFormatGmt = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_DAYS_MILLIS);
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

				Calendar nowCalendar = Calendar.getInstance();
				nowCalendar.setTime(nowDay);

				Calendar lastDateCalendar = Calendar.getInstance();

				List<String> dataDays = mongodbRepository.queryNative(MongoBasicOpsDBRepository.METRICS_BASE,
						"db.getCollection('MetricsBase').find({'TimeSerie.windowType':'DAYS'}, {'TimeSerie.timestamp': 1 }).sort({'TimeSerie.timestamp': -1}).limit(1)");

				if (!dataDays.isEmpty()) {
					long lastDate = JsonPath.read(dataDays.get(0), TIMESERIE_DATE_STR);
					lastDateCalendar.setTime(sdfDays.parse(dateFormatGmt.format(new Date(lastDate))));

				} else {
					lastDateCalendar.setTime(sdfDays.parse("2018-12-31T00:00:00.000Z"));
				}

				// Create Hourly empty windows
				JSONObject dataDaily = new JSONObject();
				JSONObject rootDaily = new JSONObject();

				rootDaily.put(WINDOW_TYPE_STR, "DAYS");
				dataDaily.put(TIMESERIE_STR, rootDaily);

				long currentIntancetime = lastDateCalendar.getTime().getTime();
				if (currentIntancetime < nowCalendar.getTime().getTime()) {
					lastDateCalendar.add(Calendar.DATE, 1);

					while (currentIntancetime <= nowCalendar.getTime().getTime()) {

						JSONObject date = new JSONObject();
						date.put(DATE_STR, sdfDays.format(lastDateCalendar.getTime()));

						rootDaily.put(TIMESTAMP_STR, date);

						JSONArray metrics = new JSONArray();
						rootDaily.put(VALUES_STR, metrics);

						for (int hourIndex = 0; hourIndex < 24; hourIndex++) {
							JSONObject metric = new JSONObject();
							metric.put(VALUE_STR, 0);
							metric.put(TIMESTAMP_STR, sdfHours.format(lastDateCalendar.getTime()));
							lastDateCalendar.add(Calendar.HOUR, 1);
							metrics.put(metric);
						}

						mongodbRepository.insert(MongoBasicOpsDBRepository.METRICS_BASE, dataDaily.toString());

						currentIntancetime = sdfDays.parse(sdfDays.format(lastDateCalendar.getTime())).getTime();
					}
				}
			}

		} catch (Exception e) {
			log.error("loadMetricsBaseDays: Error setting content on MetricsBase collection", e);
		}

	}

	private void loadMetricsBaseMonths(MongoBasicOpsDBRepository mongodbRepository) {
		try {

			SimpleDateFormat sdfMonths = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_MONTHS_MILLIS);

			Date nowMonth = sdfMonths.parse(sdfMonths.format(new Date()));

			if (nowMonth.getTime() > this.lastCerosMonthlyInserted) {

				this.lastCerosMonthlyInserted = nowMonth.getTime();

				SimpleDateFormat sdfDays = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_DAYS_MILLIS);

				SimpleDateFormat dateFormatGmt = new SimpleDateFormat(MetricQueryResolverImpl.FORMAT_MONTHS_MILLIS);
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

				Calendar nowCalendar = Calendar.getInstance();
				nowCalendar.setTime(nowMonth);

				Calendar lastDateCalendar = Calendar.getInstance();

				List<String> dataMonths = mongodbRepository.queryNative(MongoBasicOpsDBRepository.METRICS_BASE,
						"db.getCollection('MetricsBase').find({'TimeSerie.windowType':'MONTHS'}, {'TimeSerie.timestamp': 1 }).sort({'TimeSerie.timestamp': -1}).limit(1)");

				if (!dataMonths.isEmpty()) {
					long lastDate = JsonPath.read(dataMonths.get(0), TIMESERIE_DATE_STR);
					lastDateCalendar.setTime(sdfMonths.parse(dateFormatGmt.format(new Date(lastDate))));

				} else {
					lastDateCalendar.setTime(sdfMonths.parse("2018-12-01T00:00:00.000Z"));
				}

				// Create Hourly empty windows
				JSONObject dataMonthly = new JSONObject();
				JSONObject rootMonthly = new JSONObject();

				rootMonthly.put(WINDOW_TYPE_STR, "MONTHS");
				dataMonthly.put(TIMESERIE_STR, rootMonthly);

				long currentIntancetime = lastDateCalendar.getTime().getTime();
				if (currentIntancetime < nowCalendar.getTime().getTime()) {
					lastDateCalendar.add(Calendar.MONTH, 1);
					while (currentIntancetime <= nowCalendar.getTime().getTime()) {

						JSONObject date = new JSONObject();
						date.put(DATE_STR, sdfMonths.format(lastDateCalendar.getTime()));

						rootMonthly.put(TIMESTAMP_STR, date);

						JSONArray metrics = new JSONArray();
						rootMonthly.put(VALUES_STR, metrics);

						int daysInMonth = lastDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						for (int dayIndex = 0; dayIndex < daysInMonth; dayIndex++) {
							JSONObject metric = new JSONObject();
							metric.put(VALUE_STR, 0);
							metric.put(TIMESTAMP_STR, sdfDays.format(lastDateCalendar.getTime()));
							lastDateCalendar.add(Calendar.DATE, 1);

							metrics.put(metric);
						}

						mongodbRepository.insert(MongoBasicOpsDBRepository.METRICS_BASE, dataMonthly.toString());

						currentIntancetime = sdfMonths.parse(sdfMonths.format(lastDateCalendar.getTime())).getTime();
					}
				}
			}

		} catch (Exception e) {
			log.error("loadMetricsBaseMonths: Error setting content on MetricsBase collection", e);
		}
	}

	@Data
	@AllArgsConstructor
	class MetricsObject {
		private long timestamp;
		private JSONArray metrics;

	}

	private List<MetricsObject> extractMetrics(List<String> data) {
		List<MetricsObject> lMetrics = new ArrayList<>();

		data.forEach(dataUnit -> {
			try {
				// La unidad de tiempo la tengo en la instancia
				Map<String, Integer> values = JsonPath.read(dataUnit, "$.TimeSerie.values.v");
				String windowType = JsonPath.read(dataUnit, "$.TimeSerie.windowType"); // Puede ser:
																						// HOURS --> 1 por minuto
																						// DAYS --> 1 por hora
																						// MONTHS --> 1 por dia

				long date = JsonPath.read(dataUnit, TIMESERIE_DATE_STR);

				JSONArray metrics = null;
				if (windowType.equals("HOURS")) {
					metrics = buildMetricsHours(date, values);

				} else if (windowType.equals("DAYS")) {
					metrics = buildMetricsDays(date, values);

				} else if (windowType.equals("MONTHS")) {
					metrics = buildMetricsMonths(date, values);
				}

				lMetrics.add(new MetricsObject(date, metrics));

			} catch (Exception e) {
				Log.error("Error processing metrics from date", e);
			}
		});
		return lMetrics;
	}

	private List<MetricsObject> extractCeros(List<String> data) {
		List<MetricsObject> lMetrics = new ArrayList<>();

		data.forEach(dataUnit -> {
			try {
				String ceros = JsonPath.read(dataUnit, "$.TimeSerie.values").toString();

				long date = JsonPath.read(dataUnit, TIMESERIE_DATE_STR);

				MetricsObject cero = new MetricsObject(date, new JSONArray(ceros));
				lMetrics.add(cero);

			} catch (Exception e) {
				Log.error("Error processing metrics from date", e);
			}
		});
		return lMetrics;
	}

	private JSONArray buildMetricsHours(long date, Map<String, Integer> values) throws Exception {
		JSONArray metrics = new JSONArray();

		// Prepare Initial time
		SimpleDateFormat sdfMinutes = new SimpleDateFormat(FORMAT_MINUTES_MILLIS);
		SimpleDateFormat sdfHours = new SimpleDateFormat(FORMAT_HOURS);

		sdfMinutes.setTimeZone(TimeZone.getTimeZone(timeZone));

		Calendar cal = Calendar.getInstance();
		cal.setTime(sdfHours.parse(sdfHours.format(new Date(date))));

		for (int i = 0; i < MINUTES_IN_HOUR; i++) {
			String timestamp = sdfMinutes.format(cal.getTime());

			int value = values.get(Integer.toString(i));

			JSONObject metric = new JSONObject();
			metric.put(TIMESTAMP_STR, timestamp);
			metric.put(VALUE_STR, value);

			metrics.put(metric);

			cal.add(Calendar.MINUTE, 1);
		}

		return metrics;

	}

	private JSONArray buildMetricsDays(long date, Map<String, Integer> values) throws Exception {
		JSONArray metrics = new JSONArray();

		// Prepare Initial time
		SimpleDateFormat sdfHours = new SimpleDateFormat(FORMAT_HOURS_MILLIS);
		SimpleDateFormat sdfDays = new SimpleDateFormat(FORMAT_DAYS);

		Calendar cal = Calendar.getInstance();
		cal.setTime(sdfDays.parse(sdfDays.format(new Date(date))));

		for (int i = 0; i < HOURS_IN_DAY; i++) {
			String timestamp = sdfHours.format(cal.getTime());

			int value = values.get(Integer.toString(i));

			JSONObject metric = new JSONObject();
			metric.put(TIMESTAMP_STR, timestamp);
			metric.put(VALUE_STR, value);

			metrics.put(metric);

			cal.add(Calendar.HOUR, 1);
		}

		return metrics;
	}

	private JSONArray buildMetricsMonths(long date, Map<String, Integer> values) throws Exception {
		JSONArray metrics = new JSONArray();

		// Prepare Initial time
		SimpleDateFormat sdfDays = new SimpleDateFormat(FORMAT_DAYS_MILLIS);
		SimpleDateFormat sdfMonths = new SimpleDateFormat(FORMAT_MONTHS);

		Calendar cal = Calendar.getInstance();
		cal.setTime(sdfMonths.parse(sdfMonths.format(new Date(date))));

		for (int i = 0; i < cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
			String timestamp = sdfDays.format(cal.getTime());

			int value = values.get(Integer.toString(i + 1));

			JSONObject metric = new JSONObject();
			metric.put(TIMESTAMP_STR, timestamp);
			metric.put(VALUE_STR, value);

			metrics.put(metric);
			cal.add(Calendar.DATE, 1);
		}

		return metrics;
	}

}
