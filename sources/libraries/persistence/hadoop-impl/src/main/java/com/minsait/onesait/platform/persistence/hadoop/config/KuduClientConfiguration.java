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
package com.minsait.onesait.platform.persistence.hadoop.config;

import java.util.Arrays;
import java.util.List;

import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduClient.KuduClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class KuduClientConfiguration {

	//Master kudu address only for 1 master server
	@Value("${onesaitplatform.database.kudu.address:localhost:7051}")
	private String kuduURLs;
	
	@Value("${onesaitplatform.database.kudu.client.bossThreadPool:1}")
	private int bossThreadPoolSize;

	@Value("${onesaitplatform.database.kudu.client.workerThreadPool:4}")
	private int workerThreadPoolSize;
	
	@Value("${onesaitplatform.database.kudu.client.operationTimeout:30000}")
	private int operationTimeout;
	
	@Value("${onesaitplatform.database.kudu.client.adminOperationTimeout:30000}")
	private int adminOperationTimeout;
	
	@Value("${onesaitplatform.database.kudu.client.socketReadTimeout:10000}")
	private int socketReadTimeout;
	
	public KuduClient createKuduClient() {

		List<String> urls = Arrays.asList(kuduURLs.split(","));
		
		KuduClientBuilder builder = new KuduClient.KuduClientBuilder(urls)
			      .defaultOperationTimeoutMs(operationTimeout)
			      .defaultAdminOperationTimeoutMs(adminOperationTimeout)
			      .defaultSocketReadTimeoutMs(socketReadTimeout)
			      .bossCount(bossThreadPoolSize)
			      .workerCount(workerThreadPoolSize);

		log.info("Initialized kudu connector for " + kuduURLs);

		return builder.build();
	}

	@Bean(name = NameBeanConst.KUDU_CLIENT)
	public KuduClient kuduClientBean() {
		return createKuduClient();
	}

}
