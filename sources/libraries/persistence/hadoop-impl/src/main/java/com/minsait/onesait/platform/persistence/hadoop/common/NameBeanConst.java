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
package com.minsait.onesait.platform.persistence.hadoop.common;

public class NameBeanConst {

	public static final String HIVE_DATASOURCE_BEAN_NAME = "HiveDatasource";
	public static final String HIVE_TEMPLATE_JDBC_BEAN_NAME = "HiveJdbcTemplate";

	public static final String IMPALA_DATASOURCE_BEAN_NAME = "ImpalaDatasource";
	public static final String IMPALA_TEMPLATE_JDBC_BEAN_NAME = "ImpalaJdbcTemplate";
	public static final String IMPALA_MANAGE_DB_REPO_BEAN_NAME = "ImpalaManageDBRepository";

	public static final String KUDU_QUERY_REPO_BEAN_NAME = "KuduQueryAsTextDBRepository";
	public static final String KUDU_MANAGE_DB_REPO_BEAN_NAME = "KuduManageDBRepository";
	public static final String KUDU_BASIC_OPS_BEAN_NAME = "KuduBasicOpsDBRepository";
	public static final String KUDU_CLIENT= "KuduClient";

	private NameBeanConst() {
		super();
	}
}
