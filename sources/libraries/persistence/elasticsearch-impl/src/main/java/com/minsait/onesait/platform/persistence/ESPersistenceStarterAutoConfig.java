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
package com.minsait.onesait.platform.persistence;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@Configuration
@ComponentScan
@Conditional(ElasticsearchEnabledCondition.class)
public class ESPersistenceStarterAutoConfig {
    

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client(IntegrationResourcesService resourcesService) {

        Map<String, Object> database = resourcesService.getGlobalConfiguration().getEnv().getDatabase();
        
        @SuppressWarnings("unchecked")
        Map<String, Object>  elasticsearch = (Map<String, Object>) database.get("elasticsearch");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> servers = (List<Map<String, Object>>) elasticsearch.get("servers");
        
        Node[] nodes = new Node[servers.size()];
        
        for (int i = 0; i < servers.size(); i++) {
            Map<String, Object> server = servers.get(i); 
            String host = (String) server.get("host");
            int port = (int) server.get("port");
            String protocol = (String) server.get("protocol");
            HttpHost httpHost = new HttpHost(host, port, protocol);
            nodes[i] = new Node(httpHost);
        }

        return  new RestHighLevelClient(RestClient.builder(nodes));
    }
    
    
}
