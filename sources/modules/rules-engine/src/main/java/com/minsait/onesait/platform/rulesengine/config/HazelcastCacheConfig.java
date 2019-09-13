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
package com.minsait.onesait.platform.rulesengine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.minsait.onesait.platform.commons.model.HazelcastRuleDomainObject;
import com.minsait.onesait.platform.commons.model.HazelcastRuleObject;
import com.minsait.onesait.platform.rulesengine.service.RulesManagerService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HazelcastCacheConfig {

	@Autowired
	private HazelcastInstance hazelcastInstance;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_rules:topicRules}")
	private String topicRulesName;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_domains:topicDomains}")
	private String topicDomainsName;
	@Value("${onesaitplatform.rules-engine.hazelcast.topic_domains:topicAsyncComm}")
	private String topicAsyncComm;

	@Autowired
	private RulesManagerService rulesManagerService;

	@Bean(name = "topicChangedRules")
	public ITopic<String> topicChangedRules() {
		final ITopic<String> topicChangedRules = hazelcastInstance.getTopic(topicRulesName);
		topicChangedRules.addMessageListener(msg -> HazelcastRuleObject.fromJson(msg.getMessageObject())
				.ifPresent(hro -> rulesManagerService.manageRule(hro))

		);
		return topicChangedRules;
	}

	@Bean(name = "topicChangedDomains")
	public ITopic<String> topicChangedDomain() {
		final ITopic<String> topicChangedDomains = hazelcastInstance.getTopic(topicDomainsName);
		topicChangedDomains.addMessageListener(msg ->

		HazelcastRuleDomainObject.fromJson(msg.getMessageObject())
				.ifPresent(hrdo -> rulesManagerService.manageDomain(hrdo))

		);
		return topicChangedDomains;
	}

	@Bean(name = "topicAsyncComm")
	public ITopic<String> topicAsyncComm() {
		return hazelcastInstance.getTopic(topicAsyncComm);

	}

	@Bean
	CacheManager cacheManager() {
		if (hazelcastInstance != null) {
			final CacheManager manager = new HazelcastCacheManager(hazelcastInstance);
			log.info("Configured Local Cache Manager: Name : {} ", manager.toString());
			return manager;
		} else {
			return new NoOpCacheManager();
		}
	}
}
