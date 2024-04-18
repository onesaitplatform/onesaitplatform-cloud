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
package com.minsait.onesait.platform.persistence.external.virtual.parser;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
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
	public List<String> extractData(ResultSet rs) throws SQLException {
		Select statementData = null;
		try {
			statementData = (Select) CCJSqlParserUtil.parse(this.statement);
		} catch (JSQLParserException e) {
			log.error("Error executing query. {}", e);
		}
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		String table = tablesNamesFinder.getTableList(statementData).get(0);

		if (rs != null) {
			ResultSetMetaData rsmd = rs.getMetaData();
			return extractResultSetAsJson(rs, table, rsmd);
		}
		return new ArrayList<>();

	}

	private List<String> extractResultSetAsJson(ResultSet rs, String table, ResultSetMetaData rsmd)
			throws SQLException {
		List<String> resultJson = new ArrayList<>();
		// Check hive resulset, that don't have getTableName method
		boolean hiveRS = rs instanceof org.apache.hive.jdbc.HiveQueryResultSet;

		while (rs.next()) {
			int numColumns = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();

			for (int i = 1; i < numColumns + 1; i++) {
				String columnName = rsmd.getColumnName(i);
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

	private JSONObject extractResultSetRowField(ResultSet rs, String table, int rowType, String columnName,
			JSONObject obj) throws SQLException {

		if (rowType == java.sql.Types.VARCHAR) {
			String value = rs.getString(columnName);
			getRecurrence(columnName, obj, value, false);
		} else if (rowType == java.sql.Types.BOOLEAN) {
			Boolean value = rs.getBoolean(columnName);
			getRecurrence(columnName, obj, value, false); // ***

		} else if (rowType == java.sql.Types.NUMERIC || rowType == java.sql.Types.BIGINT) {
			int value = rs.getInt(columnName);
			Double dValue = rs.getDouble(columnName);
			if (dValue != null) {
				if ((dValue == Math.floor(dValue)) && !Double.isInfinite(dValue)) {
					getRecurrence(columnName, obj, value, false);
				} else {
					getRecurrence(columnName, obj, dValue, false);
				}
			}
		} else if (rowType == java.sql.Types.INTEGER || rowType == java.sql.Types.SMALLINT
				|| rowType == java.sql.Types.TINYINT) {
			int value = rs.getInt(columnName);
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
				value = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date(t.getTime()));
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

	private void getRecurrence(String columnName, JSONObject obj, Object value, boolean isDate) {

		JSONObject o2 = new JSONObject();

		if (value != null && isDate) {
			JSONObject aux = new JSONObject().put("$date", value);
			o2 = new JSONObject().put(columnName, aux);
		} else if (value != null) {
			o2.put(columnName, value);
		} else {
			o2.put(columnName, "null");
		}
		checkifExistfield(o2, obj);
	}

	private JSONObject checkifExistfield(JSONObject source, JSONObject target) {

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
