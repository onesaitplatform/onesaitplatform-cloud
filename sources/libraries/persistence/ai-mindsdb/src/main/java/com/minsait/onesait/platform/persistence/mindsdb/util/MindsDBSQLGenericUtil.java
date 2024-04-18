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
package com.minsait.onesait.platform.persistence.mindsdb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;



public class MindsDBSQLGenericUtil {

	private static final String POSTGRES_MATCHER = "jdbc:postgresql:\\/\\/([a-zA-Z\\d:.]+)\\/.*";
	private static final String MARIADB_MATCHER = "jdbc:mysql:\\/\\/([a-zA-Z\\d:.]+)\\/.*";

	public enum SERVER_PART {
		HOST, PORT
	}

	public static String getMongoDB(String var, SERVER_PART part) {
		if (StringUtils.hasText(var)) {
			return MindsDBSQLGenericUtil.extractHostPort(var, part);
		} else {
			return null;
		}
	}

	public static String getSQL(String var, SERVER_PART part) {
		if (StringUtils.hasText(var)) {
			String url = "";
			if (isPostgres(var)) {
				url = MindsDBSQLGenericUtil.getURLFromURI(var, POSTGRES_MATCHER);
			} else {
				url = MindsDBSQLGenericUtil.getURLFromURI(var, MARIADB_MATCHER);
			}
			return MindsDBSQLGenericUtil.extractHostPort(url, part);
		} else {
			return null;
		}
	}

	private static String extractHostPort(String var, SERVER_PART part) {

		if (SERVER_PART.HOST.equals(part)) {
			return var.split(",")[0].split(":")[0];
		} else {
			return var.split(",")[0].split(":")[1];
		}
	}

	private static String getURLFromURI(String uri, String pattern) {
		final Pattern p = Pattern.compile(pattern);
		final Matcher m = p.matcher(uri);
		if(m.matches()) {
			return m.group(1);
		}
		return null;
	}

	public static boolean isPostgres(String var) {
		return var.contains("postgresql");
	}

}
