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
package com.minsait.onesait.platform.simulator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.simulator.job.utils.JsonUtils2;

@Service
public class FieldRandomizerServiceImpl implements FieldRandomizerService {

	@Autowired
	JsonUtils2 jsonUtils;
	private static final String PATH_PROPERTIES = "properties";
	private static final String FIXED_NUMBER = "FIXED_NUMBER";
	private static final String FIXED_STRING = "FIXED_STRING";
	private static final String FIXED_DATE = "FIXED_DATE";
	private static final String FIXED_INTEGER = "FIXED_INTEGER";
	private static final String COSINE_NUMBER = "COSINE_NUMBER";
	private static final String SINE_NUMBER = "SINE NUMBER";
	private static final String RANDOM_NUMBER = "RANDOM_NUMBER";
	private static final String RANDOM_INTEGER = "RANDOM_INTEGER";
	private static final String RANDOM_DATE = "RANDOM_DATE";
	private static final String RANDOM_STRING = "RANDOM_STRING";
	private static final String RANDOM_BOOLEAN = "RANDOM_BOOLEAN";
	private static final String FIXED_BOOLEAN = "FIXED_BOOLEAN";
	private static final String NULL = "NULL";
	private static final String VALUE = "value";

	private static final String DATE_STR = "$date";

	@Override
	public JsonNode randomizeFields(JsonNode json, JsonNode schema, JsonNode jsonSchema) {
		final ObjectMapper mapper = new ObjectMapper();

		final JsonNode map = schema;

		final Iterator<String> fields = json.fieldNames();
		while (fields.hasNext()) {
			final String field = fields.next();
			final String function = json.path(field).get("function").asText();
			String finalField = null;
			String path = "";
			if (!jsonUtils.refJsonSchema(jsonSchema).equals("")) {
				final String context = jsonSchema.get(PATH_PROPERTIES).fields().next().getKey();
				path = "/" + context;
			}
			if (field.contains(".")) {
				final String [] array = field.split("\\.");
				finalField = array[array.length - 1];
				for (int s = 0; s < array.length; s++) {
					if (map.at(path).isArray())
						path = path + "/0";
					else if (s != array.length - 1)
						path = path + "/" + array[s];

				}

			} else {
				finalField = field;

			}
			// if (map.at(path).isArray()) {
			//
			// ((ArrayNode) map.at(path)).remove(Integer.parseInt(finalField));
			// }

			switch (function) {
			case FIXED_NUMBER:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.valueOf(finalField).intValue(),
							json.path(field).get(VALUE).asDouble());
				else
					((ObjectNode) map.at(path)).put(finalField, json.path(field).get(VALUE).asDouble());
				break;
			case FIXED_INTEGER:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							json.path(field).get(VALUE).asInt());
				else
					((ObjectNode) map.at(path)).put(finalField, json.path(field).get(VALUE).asInt());
				break;
			case RANDOM_NUMBER:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							randomizeDouble(json.path(field).get("from").asDouble(),
									json.path(field).get("to").asDouble(), json.path(field).get("precision").asInt()));
				else
					((ObjectNode) map.at(path)).put(finalField, randomizeDouble(json.path(field).get("from").asDouble(),
							json.path(field).get("to").asDouble(), json.path(field).get("precision").asInt()));
				break;
			case RANDOM_INTEGER:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							randomizeInt(json.path(field).get("from").asInt(), json.path(field).get("to").asInt()));
				((ObjectNode) map.at(path)).put(finalField,
						randomizeInt(json.path(field).get("from").asInt(), json.path(field).get("to").asInt()));
				break;
			case COSINE_NUMBER:
				final double angleCos = Math.toRadians(json.path(field).get("angle").asDouble());
				final double multiplierCos = json.path(field).get("multiplier").asDouble();
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField), Math.cos(angleCos) * multiplierCos);
				else
					((ObjectNode) map.at(path)).put(finalField, Math.cos(angleCos) * multiplierCos);
				break;
			case SINE_NUMBER:
				final double angleSin = Math.toRadians(json.path(field).get("angle").asDouble());
				final double multiplierSin = json.path(field).get("multiplier").asDouble();
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField), Math.sin(angleSin) * multiplierSin);
				else
					((ObjectNode) map.at(path)).put(finalField, Math.sin(angleSin) * multiplierSin);
				break;
			case FIXED_STRING:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							json.path(field).get(VALUE).asText());
				else
					((ObjectNode) map.at(path)).put(finalField, json.path(field).get(VALUE).asText());
				break;
			case RANDOM_STRING:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							randomizeStrings(json.path(field).get("list").asText()));
				else
					((ObjectNode) map.at(path)).put(finalField,
							randomizeStrings(json.path(field).get("list").asText()));
				break;
			case FIXED_DATE:
				Date date;
				try {
					final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					date = df.parse(json.path(field).get(VALUE).asText());
				} catch (final ParseException e) {
					date = new Date();
				}
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				final JsonNode dateJson = mapper.createObjectNode();
				if (!map.at(path).path(finalField).path(DATE_STR).isMissingNode()) {
					((ObjectNode) dateJson).put(DATE_STR, df.format(date));

					((ObjectNode) map.at(path)).set(finalField, dateJson);
				} else {
					if (map.at(path).isArray())
						((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField), df.format(date));
					else
						((ObjectNode) map.at(path)).put(finalField, df.format(date));
				}

				break;
			case RANDOM_DATE:
				Date dateFrom;
				Date dateTo;
				Date dateRandom = new Date();

				try {
					final DateFormat dfr = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					dateFrom = dfr.parse(json.path(field).get("from").asText());
					dateTo = dfr.parse(json.path(field).get("to").asText());
					dateRandom = randomizeDate(dateFrom, dateTo);
				} catch (final ParseException e) {
					dateRandom = new Date();
				}
				df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				final JsonNode dateRandomJson = mapper.createObjectNode();
				if (!map.at(path).path(finalField).path(DATE_STR).isMissingNode()) {
					((ObjectNode) dateRandomJson).put(DATE_STR, df.format(dateRandom));
					((ObjectNode) map.at(path)).set(finalField, dateRandomJson);
				} else {
					if (map.at(path).isArray())
						((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField), df.format(dateRandom));
					else
						((ObjectNode) map.at(path)).put(finalField, df.format(dateRandom));
				}

				break;
			case FIXED_BOOLEAN:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField),
							json.path(field).get(VALUE).asBoolean(true));
				else
					((ObjectNode) map.at(path)).put(finalField, json.path(field).get(VALUE).asBoolean(true));
				break;
			case RANDOM_BOOLEAN:
				if (map.at(path).isArray())
					((ArrayNode) map.at(path)).insert(Integer.parseInt(finalField), randomizeBoolean());
				else
					((ObjectNode) map.at(path)).put(finalField, randomizeBoolean());
				break;
			case NULL:

				break;
			default:
				break;
			}

		}

		return map;
	}

	public String randomizeStrings(String list) {
		final List<String> words = new ArrayList<>(Arrays.asList(list.split(",")));
		if (!words.isEmpty()) {
			final int selection = randomizeInt(0, words.size() - 1);
			return words.get(selection);
		} else
			return list;

	}

	@Override
	public int randomizeInt(int min, int max) {
		final Random random = new Random();
		return random.nextInt((max - min) + 1) + min;
	}

	public double randomizeDouble(double min, double max, int precision) {
		final Random random = new Random();
		final Double randomDouble = min + (max - min) * random.nextDouble();
		return BigDecimal.valueOf(randomDouble).setScale(precision, RoundingMode.HALF_UP).doubleValue();
	}

	public Date randomizeDate(Date from, Date to) {

		final ThreadLocalRandom th = ThreadLocalRandom.current();
		return new Date(th.nextLong(from.getTime(), to.getTime()));

	}

	public boolean randomizeBoolean() {

		final Random random = new Random();
		return random.nextBoolean();
	}
}
