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
package com.minsait.onesait.platform.persistence;


import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.Client;
import org.opensearch.client.Node;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
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
@Conditional(OpensearchEnabledCondition.class)
public class OSPersistenceStarterAutoConfig {


	//TODO Create a separated component with preDestroy method closing OpenSearchClient._transport.close()
	@Bean//(destroyMethod = "close")
	@Primary
	public OpenSearchClient javaClient(IntegrationResourcesService resourcesService) {
		final Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();

		@SuppressWarnings("unchecked")
		final
		Map<String, Object>  opensearch = (Map<String, Object>) database.get("opensearch");
		
		@SuppressWarnings("unchecked")
		final
		List<Map<String, Object>> servers = (List<Map<String, Object>>) opensearch.get("servers");

		final Node[] nodes = new Node[servers.size()];

		final String username = opensearch.get("username") != null ? (String) opensearch.get("username") : "";
		final String password = opensearch.get("password") != null ? (String) opensearch.get("password") : "";

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
			final OpenSearchTransport transport = new RestClientTransport(RestClient.builder(nodes).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build(), new JacksonJsonpMapper()); 
			//final RestClientBuilder builder = RestClient.builder(nodes).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
			return new OpenSearchClient(transport);
			
		}
		final OpenSearchTransport transport = new RestClientTransport(RestClient.builder(nodes).build(), new JacksonJsonpMapper()); 
		return  new OpenSearchClient(transport);
	}
}
