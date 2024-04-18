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
package com.minsait.onesait.platform.persistence.hadoop.hive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.resultset.DefaultResultSetExtractor;
import com.minsait.onesait.platform.persistence.hadoop.util.HadoopQueryProcessor;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("HiveQueryAsTextDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
@ConditionalOnBean(name = NameBeanConst.HIVE_TEMPLATE_JDBC_BEAN_NAME)
public class HiveQueryAsTextDBRepository implements QueryAsTextDBRepository {

	@Autowired
	@Qualifier(NameBeanConst.HIVE_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private HadoopQueryProcessor hivePricessor;

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return jdbcTemplate.query(hivePricessor.parse(query), new DefaultResultSetExtractor());
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return queryNativeAsJson(ontology, query, -1, -1);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return queryNativeAsJson(ontology, query, offset, -1);
	}

}
