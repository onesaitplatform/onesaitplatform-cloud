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
package com.minsait.onesait.platform.persistence.hadoop.config.missing;

import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.KUDU_BASIC_OPS_BEAN_NAME;
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.KUDU_MANAGE_DB_REPO_BEAN_NAME;
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.KUDU_QUERY_REPO_BEAN_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopDisabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class KuduConfiguration {

	@Autowired
	@Qualifier("defaultQueryAsTextDBRepository")
	private QueryAsTextDBRepository defaultQueryAsTextDBRepository;

	@Autowired
	@Qualifier("defaultManageDBRepository")
	private ManageDBRepository defaultManageDBRepository;

	@Autowired
	@Qualifier("defaultBasicOpsDBRepository")
	private BasicOpsDBRepository defaultBasicOpsDBRepository;

	@Bean(name = KUDU_QUERY_REPO_BEAN_NAME)
	@Conditional(HadoopDisabledCondition.class)
	public QueryAsTextDBRepository kuduDefaultQueryTextDBRepository() {
		return defaultQueryAsTextDBRepository;
	}

	@Bean(name = KUDU_MANAGE_DB_REPO_BEAN_NAME)
	@Conditional(HadoopDisabledCondition.class)
	public ManageDBRepository kuduDefaultManageDBRepository() {
		return defaultManageDBRepository;
	}

	@Bean(name = KUDU_BASIC_OPS_BEAN_NAME)
	@Conditional(HadoopDisabledCondition.class)
	public BasicOpsDBRepository kuduDefaultBasicOpsDBRepository() {
		return defaultBasicOpsDBRepository;
	}
}
