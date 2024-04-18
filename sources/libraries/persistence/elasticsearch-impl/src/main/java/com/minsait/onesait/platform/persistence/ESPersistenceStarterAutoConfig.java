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
package com.minsait.onesait.platform.persistence;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan
@Conditional(ElasticsearchEnabledCondition.class)
public class ESPersistenceStarterAutoConfig {


	@Bean(destroyMethod = "close")
	@Primary
	public RestHighLevelClient client(IntegrationResourcesService resourcesService) {

		final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

		@SuppressWarnings("unchecked")
		final
		Map<String, Object>  elasticsearch = (Map<String, Object>) database.get("elasticsearch");

		@SuppressWarnings("unchecked")
		final
		List<Map<String, Object>> servers = (List<Map<String, Object>>) elasticsearch.get("servers");

		final Node[] nodes = new Node[servers.size()];

		final String username = elasticsearch.get("username") != null ? (String) elasticsearch.get("username") : "";
		final String password = elasticsearch.get("password") != null ? (String) elasticsearch.get("password") : "";

		for (int i = 0; i < servers.size(); i++) {
			final Map<String, Object> server = servers.get(i);
			final String host = (String) server.get("host");
			final int port = (int) server.get("port");
			final String protocol = (String) server.get("protocol");
			final HttpHost httpHost = new HttpHost(host, port, protocol);
			nodes[i] = new Node(httpHost);
		}

		if (!"".equals(username) && !"".equals(password)) {
			log.info("Setting OpenDistro basic authentication paramateres");
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

			final RestClientBuilder builder = RestClient.builder(nodes).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

			return new RestHighLevelClient(builder);
		}

		return  new RestHighLevelClient(RestClient.builder(nodes));

	}


}
