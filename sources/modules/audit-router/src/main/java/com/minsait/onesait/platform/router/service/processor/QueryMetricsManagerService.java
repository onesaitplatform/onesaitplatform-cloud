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
package com.minsait.onesait.platform.router.service.processor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.minsait.onesait.platform.audit.query.dto.QueryMetric;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudService;
import com.minsait.onesait.platform.router.service.app.service.RouterCrudServiceException;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnExpression("'${onesaitplatform.router.metrics.query.enabled}'=='true'")
@Lazy(false)
@Slf4j
public class QueryMetricsManagerService {

	@Value("${onesaitplatform.router.metrics.query.extractor.pool:10}")
	private int queryMetricsThreadPoolSize;

	private ExecutorService queryMetricsExecutor;

	@Autowired
	@Qualifier("metricsQueue")
	private IQueue<String> metricsQueue;

	@Autowired
	private RouterCrudService routerCrudService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@PostConstruct
	public void init() {
		queryMetricsExecutor = Executors.newFixedThreadPool(queryMetricsThreadPoolSize);
		for (int i = 0; i < queryMetricsThreadPoolSize; i++) {
			queryMetricsExecutor.execute(() -> {
				while (true) {

					try {
						final String metric = metricsQueue.take();
						processMetric(metric);
					} catch (final Exception e) {
						log.error("Could not process metric", e);
					}

				}
			});
		}
	}

	private void processMetric(String metric) throws IOException, RouterCrudServiceException {

		final QueryMetric queryMetric = MAPPER.readValue(metric, QueryMetric.class);
		final OperationModel model = OperationModel
				.builder("QueryMetrics", OperationType.INSERT, queryMetric.getUser(),
						Source.valueOf(queryMetric.getSource()))
				.body(metric)
				.queryType(com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType.NONE)
				.cacheable(false).build();

		routerCrudService.insertWithNoAudit(model);
	}

}
