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
package com.minsait.onesait.platform.config.services.spark;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.spark.dto.SparkLaunchJobModel;
import com.minsait.onesait.platform.config.services.spark.exception.SparkLaunchExecutorServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Lazy
public class SparkLauncherExecutorServiceImpl implements SparkLauncherExecutorService {

	private RestTemplate restTemplate;

	private String sparkLauncherBaseUrl;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.setErrorHandler(new ResponseErrorHandler() {// This error handler allow to handle 40X codes

			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}

			@Override
			public void handleError(ClientHttpResponse response) throws IOException {

			}
		});

		try {
			Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());

			this.sparkLauncherBaseUrl = urls.getSpark().getAnalyticsEngineLauncherManagerBase();

			if (!this.sparkLauncherBaseUrl.endsWith("/")) {
				this.sparkLauncherBaseUrl += "/";
			}

		} catch (Exception e) {
			log.error("Error reading Spark Launcher url.", e);
		}
	}

	@Override
	public Integer executeJob(SparkLaunchJobModel jobModel) throws SparkLaunchExecutorServiceException {

		HttpHeaders headers = new HttpHeaders();
		// headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
		HttpEntity<SparkLaunchJobModel> entity = new HttpEntity<>(jobModel, headers);

		ResponseEntity<Integer> response = restTemplate.exchange(this.sparkLauncherBaseUrl + "launch", HttpMethod.POST,
				entity, Integer.class);

		if (response.getStatusCode() != HttpStatus.ACCEPTED) {
			throw new SparkLaunchExecutorServiceException("Error executing Spark Job: " + jobModel.getJobName());
		}
		log.info("User created in MinIO");

		return response.getBody();
	}

}
