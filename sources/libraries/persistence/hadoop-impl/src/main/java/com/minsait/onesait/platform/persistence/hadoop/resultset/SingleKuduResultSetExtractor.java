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
package com.minsait.onesait.platform.persistence.hadoop.resultset;

import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_CLIENT_SESSION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_PREFIX;
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

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.hadoop.common.geometry.Geometry;
import com.minsait.onesait.platform.persistence.hadoop.common.geometry.GeometryType;
import com.minsait.onesait.platform.persistence.hadoop.util.HiveFieldType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SingleKuduResultSetExtractor implements ResultSetExtractor<String> {

	@Override
	public String extractData(ResultSet rs) throws SQLException {

		ObjectMapper mapper = new ObjectMapper();
		JSONObject obj = new JSONObject();

		while (rs.next()) {

			int total_rows = rs.getMetaData().getColumnCount();

			JSONObject contextData = new JSONObject();

			Geometry geometry = new Geometry();
			geometry.setType(GeometryType.POINT);

			for (int i = 0; i < total_rows; i++) {

				try {

					String columnName = rs.getMetaData().getColumnLabel(i + 1);

					if (isContexDataField(columnName)) {
						addToContextData(columnName, rs.getObject(i + 1), contextData);
					} else if (isTimeStamp(rs.getMetaData().getColumnType(i + 1))) {

						Timestamp val = rs.getTimestamp(i + 1);
						String value = getDateTimeFormatted(val);

						JSONObject date = new JSONObject().put("$date", value);
						obj.put(rs.getMetaData().getColumnLabel(i + 1), date);

					} else if (isGeometry(columnName)) {
						try {
							addToGeometry(columnName, rs.getDouble(i + 1), geometry);
							obj.put(getGeometryColumnName(columnName),
									new JSONObject(mapper.writeValueAsString(geometry)));

						} catch (JSONException | JsonProcessingException e) {
							log.error("", e);
						}

					} else {
						obj.put(columnName, rs.getObject(i + 1));
					}
				} catch (JSONException e) {
					log.error("error parsing json ", e);
				}
			}

			obj.put("contextData", contextData);

		}
		return obj.toString();
	}

	public boolean isContexDataField(String columnName) {
		return columnName.startsWith(CONTEXT_DATA_FIELD_PREFIX.toLowerCase());
	}

	public boolean isTimeStamp(int columnType) {
		return java.sql.Types.TIMESTAMP == columnType;
	}

	public boolean isGeometry(String columnName) {
		return columnName.toLowerCase().endsWith(HiveFieldType.LATITUDE_FIELD)
				|| columnName.toLowerCase().endsWith(HiveFieldType.LONGITUDE_FIELD);
	}

	public void addToContextData(String columnName, Object value, JSONObject contextData) {
		if (CONTEXT_DATA_FIELD_DEVICE_TEMPLATE.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_DEVICE_TEMPLATE, value);
		} else if (CONTEXT_DATA_FIELD_DEVICE.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_DEVICE, value);
		} else if (CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_DEVICE_TEMPLATE_CONNECTION, value);
		} else if (CONTEXT_DATA_FIELD_CLIENT_SESSION.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_CLIENT_SESSION, value);
		} else if (CONTEXT_DATA_FIELD_USER.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_USER, value);
		} else if (CONTEXT_DATA_FIELD_TIMEZONE_ID.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_TIMEZONE_ID, value);
		} else if (CONTEXT_DATA_FIELD_TIMESTAMP.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_TIMESTAMP, value);
		} else if (CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS.equalsIgnoreCase(columnName)) {
			contextData.put(FIELD_TIMESTAMP_MILLIS, value);
		}
	}

	public String getDateTimeFormatted(Timestamp val) {
		Date dateVal = new Date(val.getTime());
		String value = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(dateVal);
		return value;
	}

	public void addToGeometry(String columnName, double value, Geometry geometry) {
		if (columnName.toLowerCase().endsWith(HiveFieldType.LATITUDE_FIELD)) {
			geometry.getCoordinates()[0] = value;
		} else if (columnName.toLowerCase().endsWith(HiveFieldType.LONGITUDE_FIELD)) {
			geometry.getCoordinates()[1] = value;
		}
	}

	public String getGeometryColumnName(String columnName) {
		String geometryColumnName = columnName;
		geometryColumnName = geometryColumnName.replace(HiveFieldType.LATITUDE_FIELD, "");
		geometryColumnName = geometryColumnName.replace(HiveFieldType.LONGITUDE_FIELD, "");
		return geometryColumnName;
	}

}
