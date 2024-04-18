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
package com.minsait.onesait.platform.scheduler.config;

import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PREFIX;
import static com.minsait.onesait.platform.scheduler.PropertyNames.SCHEDULER_PROPERTIES_LOCATION;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = SCHEDULER_PREFIX)
@ConditionalOnResource(resources = SCHEDULER_PROPERTIES_LOCATION)
public class SchedulerConfig {
		
	private List<String> autoStartupSchedulers;

	public List<String> getAutoStartupSchedulers() {
		return autoStartupSchedulers;
	}

	public void setAutoStartupSchedulers(List<String> autoStartupSchedulers) {
		this.autoStartupSchedulers = autoStartupSchedulers;
	}
	
}
