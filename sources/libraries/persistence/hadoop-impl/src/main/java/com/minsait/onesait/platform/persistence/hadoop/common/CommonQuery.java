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
package com.minsait.onesait.platform.persistence.hadoop.common;

import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;

public class CommonQuery {

	public static final String LIST_TABLES = "show tables";
	public static final String DESCRIBE_TABLE = "describe %s";
	public static final String COUNT = "select count (*) from %s";
	public static final String FIND_ALL = "select * from %s";
	public static final String FIND_ALL_WITH_LIMIT = "select * from %s limit %s";
	public static final String FIND_BY_ID = "select * from %s where " + JsonFieldType.PRIMARY_ID_FIELD + " = '%s'";
	public static final String DELETE_ALL = "delete from %s";
	public static final String DELETE_BY_ID = "delete from %s where " + JsonFieldType.PRIMARY_ID_FIELD + " = '%s'";
	public static final String SHOW_CREATE_TABLE = "show create table %s";
	public static final String DROP_TABLE = "drop table if exists %s";

	private CommonQuery() {
		super();
	}

}
