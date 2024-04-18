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
package com.minsait.onesait.platform.persistence.hadoop.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.ResultSetExtractor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultResultSetExtractor implements ResultSetExtractor<String> {

	@Override
	public String extractData(ResultSet rs) throws SQLException {
		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			int totalRows = rs.getMetaData().getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 0; i < totalRows; i++) {
				try {
					obj.put(rs.getMetaData().getColumnLabel(i + 1), rs.getObject(i + 1));
				} catch (JSONException e) {
					log.error("error parsing json ", e);
				}
			}
			jsonArray.put(obj);
		}
		return jsonArray.toString();
	}

}
