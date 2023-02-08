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
package com.minsait.onesait.platform.scheduler.config;

import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES;
import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES_LOCATION;
import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES_SINGLE_THREAD;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnResource(resources = SCHEDULER_PROPERTIES_LOCATION)
@Slf4j
public class QuartzConfig {

	@Bean(name = "quartzProperties")
	public Properties quartzProperties() {
		final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource(SCHEDULER_PROPERTIES));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (final IOException e) {
			log.error("Cannot load " + SCHEDULER_PROPERTIES + "by:" + e.getMessage());
		}

		return properties;
	}

	@Bean(name = "quartzPropertiesSingleThread")
	public Properties quartzPropertiesSingleThread() {
		final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource(SCHEDULER_PROPERTIES_SINGLE_THREAD));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (final IOException e) {
			log.error("Cannot load " + SCHEDULER_PROPERTIES_SINGLE_THREAD + "by:" + e.getMessage());
		}

		return properties;
	}

}
