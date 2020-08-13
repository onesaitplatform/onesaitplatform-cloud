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
package com.minsait.onesait.platform.controlpanel.config.hazelcast;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.minsait.onesait.platform.controlpanel.controller.user.UserPendingValidation;
import com.minsait.onesait.platform.controlpanel.security.twofactorauth.Verification;

@Configuration
public class HazelcastCacheConfig {

	@Autowired
	private HazelcastInstance hazelcastInstance;
	@Value("${onesaitplatform.videobroker.hazelcast.queue}")
	private String videoQueueName;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_rules:topicRules}")
	private String topicRulesName;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_domains:topicDomains}")
	private String topicDomainsName;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_domains:topicAsyncComm}")
	private String topicAsyncComm;

	@Bean(name = "videoQueue")
	public IQueue<String> hazelcastVideoQueue() {
		return hazelcastInstance.getQueue(videoQueueName);

	}

	@Bean(name = "topicChangedRules")
	public ITopic<String> topicChangedRules() {
		return hazelcastInstance.getTopic(topicRulesName);

	}

	@Bean(name = "topicChangedDomains")
	public ITopic<String> topicChangedDomain() {
		return hazelcastInstance.getTopic(topicDomainsName);

	}

	@Bean(name = "topicAsyncComm")
	public ITopic<String> topicAsyncComm() {
		return hazelcastInstance.getTopic(topicAsyncComm);

	}

	@Bean(name = "cachePendingRegistryUsers")
	public Map<String, UserPendingValidation> pendingRegistryUsers() {
		return hazelcastInstance.getMap("cachePendingRegistryUsers");
	}

	@Bean(name = "purgatoryCache")
	public Map<String, Verification> purgatoryCache() {
		return hazelcastInstance.getMap("purgatoryCache");
	}

	@Bean(name = "cachePendingResetPassword")
	public Map<String, String> pendingResetPassword() {
		return hazelcastInstance.getMap("cachePendingResetPassword");
	}

}