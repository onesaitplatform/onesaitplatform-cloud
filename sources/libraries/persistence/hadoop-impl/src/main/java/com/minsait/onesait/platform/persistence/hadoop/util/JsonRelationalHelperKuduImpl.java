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
package com.minsait.onesait.platform.persistence.hadoop.util;

import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_CLIENT_SESSION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMESTAMP;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMEZONE_ID;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_USER;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_CLIENT_SESSION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_DEVICE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_DEVICE_TEMPLATE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_DEVICE_TEMPLATE_CONNECTION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_TIMESTAMP;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_TIMESTAMP_MILLIS;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_TIMEZONE_ID;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.FIELD_USER;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.kudu.Schema;
import org.apache.kudu.client.PartialRow;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.geometry.GeometryType;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduTable;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduTableGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JsonRelationalHelperKuduImpl {

	@Autowired
	private KuduTableGenerator kuduTableGenerator;
	
	private static final String DATE_OPERATOR="$date";

	public void instanceToPartialRow(Schema schema, String instance, PartialRow prow, String id, boolean onlyPKey) {
		JSONObject obj = new JSONObject(instance);
		Iterator<String> keys = obj.keys();

		if (!onlyPKey || (onlyPKey && schema.getColumn(JsonFieldType.PRIMARY_ID_FIELD).isKey())) {
			prow.addString(JsonFieldType.PRIMARY_ID_FIELD, id);
		}

		while (keys.hasNext()) {
			String key = keys.next();
			if (obj.get(key) instanceof JSONObject) {

				JSONObject o = obj.getJSONObject(key);
				if (JSONObject.NULL.equals(o)) {
					prow.setNull(key);
				} else if (isGeometry(o)) {
					JSONArray coordinates = o.getJSONArray("coordinates");
					if (!onlyPKey || (onlyPKey && schema.getColumn(key + HiveFieldType.LATITUDE_FIELD).isKey())) {
						prow.addDouble(key + HiveFieldType.LATITUDE_FIELD, coordinates.getDouble(0));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(key + HiveFieldType.LONGITUDE_FIELD).isKey())) {
						prow.addDouble(key + HiveFieldType.LONGITUDE_FIELD, coordinates.getDouble(1));
					}
				} else if (isTimestamp(o)) {
					if (!onlyPKey || (onlyPKey && schema.getColumn(key + HiveFieldType.LONGITUDE_FIELD).isKey())) {
						prow.addTimestamp(key, new Timestamp(
								ISODateTimeFormat.dateTimeParser().parseDateTime((String) o.get(DATE_OPERATOR)).getMillis()));
					}
				} else if (isContextData(key)) {
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE, (String) o.get(FIELD_DEVICE_TEMPLATE));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_DEVICE).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_DEVICE, (String) o.get(FIELD_DEVICE));
					}
					if (!onlyPKey
							|| (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION,
								(String) o.get(FIELD_DEVICE_TEMPLATE_CONNECTION));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_CLIENT_SESSION).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_CLIENT_SESSION, (String) o.get(FIELD_CLIENT_SESSION));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_USER).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_USER, (String) o.get(FIELD_USER));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_TIMEZONE_ID).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_TIMEZONE_ID, (String) o.get(FIELD_TIMEZONE_ID));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_TIMESTAMP).isKey())) {
						prow.addString(CONTEXT_DATA_FIELD_TIMESTAMP, (String) o.get(FIELD_TIMESTAMP));
					}
					if (!onlyPKey || (onlyPKey && schema.getColumn(CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS).isKey())) {
						prow.addLong(CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS, (Long) o.get(FIELD_TIMESTAMP_MILLIS));
					}
				}

			} else {
				if (!onlyPKey || (onlyPKey && schema.getColumn(key).isKey())) {
					Object o = obj.get(key);

					switch (schema.getColumn(key).getType()) {
					case STRING:
						prow.addString(key, (String) o);
						break;
					case UNIXTIME_MICROS:
						prow.addTimestamp(key, new Timestamp(
								ISODateTimeFormat.dateTimeParser().parseDateTime((String) o).getMillis()));
						break;
					case BOOL:
						prow.addBoolean(key, (boolean) o);
						break;
					case FLOAT:
						float f;
						if (o instanceof Integer) {
							f = (Integer) o;
						} else if (o instanceof Double) {
							f = (float) ((double) o);
						} else {
							f = (float) o;
						}
						prow.addFloat(key, f);
						break;
					case DOUBLE:
						prow.addDouble(key, (double) o);
						break;
					case DECIMAL:
						prow.addDecimal(key, (BigDecimal) o);
						break;
					case INT8:
					case INT16:
					case INT32:
					case INT64:
						prow.addInt(key, (int) o);
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public String getInsertStatement(String ontology, String schema, String instance, String id) {

		StringBuilder sqlInsert = new StringBuilder();
		StringBuilder sqlValues = new StringBuilder();

		sqlInsert.append("INSERT INTO " + ontology + " ( " + JsonFieldType.PRIMARY_ID_FIELD);
		sqlValues.append(" VALUES ('" + id + "' ");

		KuduTable table = kuduTableGenerator.builTable(ontology, schema, null);

		Map<String, String> columnTypes = table.getColumns().stream()
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getColumnType()));

		JSONObject obj = new JSONObject(instance);

		@SuppressWarnings("unchecked")
		Iterator<String> it = obj.keys();

		while (it.hasNext()) {

			String key = it.next();

			sqlInsert.append(", ");
			sqlValues.append(", ");

			if (obj.get(key) instanceof JSONObject) {

				JSONObject o = obj.getJSONObject(key);

				if (isGeometry(obj.getJSONObject(key))) {
					JSONArray coordinates = o.getJSONArray("coordinates");
					sqlInsert.append(key + HiveFieldType.LATITUDE_FIELD).append(", ")
							.append(key + HiveFieldType.LONGITUDE_FIELD);
					sqlValues.append(coordinates.getDouble(0)).append(",").append(coordinates.getDouble(1));
				} else if (isTimestamp(obj.getJSONObject(key))) {
					sqlInsert.append(key);
					sqlValues.append("'").append(o.get(DATE_OPERATOR)).append("'");
				} else if (isContextData(key)) {

					sqlInsert.append(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE).append(", ");
					sqlValues.append("'").append(o.get(FIELD_DEVICE_TEMPLATE)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_DEVICE).append(", ");
					sqlValues.append("'").append(o.get(FIELD_DEVICE)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION).append(", ");
					sqlValues.append("'").append(o.get(FIELD_DEVICE_TEMPLATE_CONNECTION)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_CLIENT_SESSION).append(", ");
					sqlValues.append("'").append(o.get(FIELD_CLIENT_SESSION)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_USER).append(", ");
					sqlValues.append("'").append(o.get(FIELD_USER)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_TIMEZONE_ID).append(", ");
					sqlValues.append("'").append(o.get(FIELD_TIMEZONE_ID)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_TIMESTAMP).append(", ");
					sqlValues.append("'").append(o.get(FIELD_TIMESTAMP)).append("', ");

					sqlInsert.append(CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS);
					sqlValues.append(o.get(FIELD_TIMESTAMP_MILLIS));

				}

			} else {
				String columnType = columnTypes.get(key);
				sqlInsert.append(key);

				if (HiveFieldType.STRING_FIELD.equals(columnType) || HiveFieldType.TIMESTAMP_FIELD.equals(columnType)) {
					sqlValues.append("'").append(obj.get(key)).append("'");
				} else {
					sqlValues.append(obj.get(key));
				}
			}

		}

		return sqlInsert.append(")").append(sqlValues).append(")").toString();
	}

	public boolean isGeometry(JSONObject o) {
		boolean result = false;

		try {
			if (o.has(JsonFieldType.TYPE_FIELD)) {
				String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);
				result = (GeometryType.POINT.name()).equalsIgnoreCase(jsonType);
			}

		} catch (Exception e) {
			log.error("error checking if a object is a geometry");
		}

		return result;
	}

	public boolean isTimestamp(JSONObject o) {
		return o.has(DATE_OPERATOR);
	}

	public boolean isContextData(String key) {
		return key.equalsIgnoreCase(JsonFieldType.CONTEXT_DATA_FIELD);
	}

	public Map<String, Object> transfromJSON(String json) {

		Map<String, Object> map = new LinkedHashMap<>();
		String nombreClave = "";

		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = new ObjectMapper().readValue(json, Map.class);

			Iterator<Entry<String, Object>> it = obj.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry<String, Object> e = it.next();
				nombreClave = e.getKey();
				map.put(nombreClave, e.getValue());
			}
		} catch (Exception e) {
			log.error("Error in transfromJSON", e);
			throw new DBPersistenceException(e);
		}

		return map;
	}
}
