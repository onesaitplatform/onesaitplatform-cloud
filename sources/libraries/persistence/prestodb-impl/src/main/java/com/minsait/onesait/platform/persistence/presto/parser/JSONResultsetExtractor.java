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
package com.minsait.onesait.platform.persistence.presto.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Slf4j
public class JSONResultsetExtractor implements ResultSetExtractor<List<String>> {

	private String statement;

	public JSONResultsetExtractor(String statement) {
		this.statement = statement;
	}

	@Override
	public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Select statement = null;
		try {
			statement = (Select) CCJSqlParserUtil.parse(this.statement);
		} catch (JSQLParserException e) {
			log.error("Error executing query. {}", e);
		}
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		String table = tablesNamesFinder.getTableList(statement).get(0);

		if (rs != null) {
			List<String> resultJson = new ArrayList<String>();
			ResultSetMetaData rsmd = rs.getMetaData();

			// Check hive resulset, that don't have getTableName method
			boolean hiveRS = rsmd instanceof org.apache.hive.jdbc.HiveResultSetMetaData;

			while (rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JSONObject obj = new JSONObject();

				for (int i = 1; i < numColumns + 1; i++) {
					String columnName = rsmd.getColumnLabel(i);
					String tableName = (hiveRS ? null : rsmd.getTableName(i));
					if (tableName == null || tableName.equals("")) {
						tableName = table;
					}
					obj = this.extractResultSetRowField(rs, tableName, rsmd.getColumnType(i), columnName, obj);
				}

				if (obj.length() > 0) {
					resultJson.add(obj.toString());
				}
			}
			return resultJson;
		}
		return new ArrayList<String>();

	}

	private JSONObject extractResultSetRowField(ResultSet rs, String table, int rowType, String columnName,
			JSONObject obj) throws JSONException, SQLException {

		if (rowType == java.sql.Types.VARCHAR) {
			String value = rs.getString(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.BOOLEAN) {
			Boolean value = rs.getBoolean(columnName);
			getRecurrence(columnName, obj, value, false);

		} else if (rowType == java.sql.Types.NUMERIC || rowType == java.sql.Types.BIGINT) {

			Double dValue = rs.getDouble(columnName);
			if (dValue != null) {
				if ((dValue == Math.floor(dValue)) && !Double.isInfinite(dValue) && Integer.MAX_VALUE >= dValue
						&& Integer.MIN_VALUE <= dValue) {
					int value = rs.getInt(columnName);
					getRecurrence(columnName, obj, value, false);
				} else {
					getRecurrence(columnName, obj, dValue.doubleValue(), false);
				}
			}
		} else if (rowType == java.sql.Types.INTEGER || rowType == java.sql.Types.SMALLINT) {
			Integer value = rs.getObject(columnName) != null ? rs.getInt(columnName) : null;
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.DOUBLE) {
			getRecurrence(columnName, obj, rs.getDouble(columnName), false);
		} else if (rowType == java.sql.Types.FLOAT) {
			getRecurrence(columnName, obj, rs.getFloat(columnName), false);
		} else if (rowType == java.sql.Types.DATE) {
			Date value = rs.getDate(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.TIMESTAMP) {
			String value = null;
			Timestamp t = rs.getTimestamp(columnName);
			if (t != null) {
				value = t.toInstant().toString();
			}
			getRecurrence(columnName, obj, value, true);
		} else if (rowType == java.sql.Types.CLOB) {
			Clob value = rs.getClob(columnName);
			getRecurrence(columnName, obj, new JSONObject(this.getContentFromInputStream(value.getAsciiStream())),
					false);
		} else if (rowType == java.sql.Types.BLOB) {
			Blob value = rs.getBlob(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.NVARCHAR) {
			String value = rs.getNString(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.TINYINT) {
			int value = rs.getInt(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.SMALLINT) {
			int value = rs.getInt(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.ARRAY) {
			getRecurrence(columnName, obj, rs.getArray(columnName), false);
		} else {
			Object value = rs.getObject(columnName);
			getRecurrence(columnName, obj, value, false);
		}

		return obj;
	}

	private String getContentFromInputStream(InputStream is) {

		StringBuilder sb = new StringBuilder();

		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return sb.toString();

	}

	private void getRecurrence(String columnName, JSONObject obj, Object value, boolean isDate) throws JSONException {

		JSONObject o = new JSONObject();

		if (value != null && isDate) {
			o.put(columnName, value);
		} else if (value != null) {
			o.put(columnName, value);
		} else {
			o.put(columnName, JSONObject.NULL);
		}
		checkifExistfield(o, obj);
	}

	private JSONObject checkifExistfield(JSONObject source, JSONObject target) throws JSONException {

		if (JSONObject.getNames(source) != null) {
			for (String key : JSONObject.getNames(source)) {
				Object value = source.get(key);
				if (!target.has(key)) {
					target.put(key, value);
				} else {
					if (value instanceof JSONObject) {
						JSONObject valueJson = (JSONObject) value;
						checkifExistfield(valueJson, target.getJSONObject(key));
					} else {
						target.put(key, value);
					}
				}
			}
		}
		return target;
	}
}
