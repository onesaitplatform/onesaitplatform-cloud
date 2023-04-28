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
package com.minsait.onesait.platform.metrics.manager;

import javax.annotation.PostConstruct;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.commons.model.MetricsPlatformDto;

@Component
@ConditionalOnProperty(name = "onesaitplatform.metrics.collector.endpoint", matchIfMissing = false)
@EnableScheduling
public class MetricsNotifier {

	@Value("${onesaitplatform.metrics.collector.endpoint:http://auditrouter:20002/router/metrics-collector/refresh}")
	private String metricsCollectorEndpoint;

	@Value("${onesaitplatform.metrics.enabled:false}")
	private boolean metricsEnabled;

	@Autowired
	private MetricsManager metricsManager;

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate();
	}

	@Scheduled(cron = "0 * * * * *")
	public void eachMinue() {

		if (metricsEnabled) {
			final long date = System.currentTimeMillis() - 60000;

			final MetricsPlatformDto dto = metricsManager.computeMetrics(date);
			try {
				if (dto.containsMetrics()) {
					restTemplate.postForLocation(metricsCollectorEndpoint, dto);
				}
			} catch (final Exception e) {
				Log.error("Error notifing metrics", e);
			}
		}
	}

}
