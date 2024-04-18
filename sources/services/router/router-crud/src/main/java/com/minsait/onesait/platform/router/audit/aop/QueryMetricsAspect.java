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
package com.minsait.onesait.platform.router.audit.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.minsait.onesait.platform.audit.query.dto.QueryMetric;
import com.minsait.onesait.platform.audit.query.dto.QueryMetric.Status;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@ConditionalOnExpression("'${onesaitplatform.router.metrics.query.enabled}'=='true'")
public class QueryMetricsAspect {

	@Autowired
	@Qualifier("metricsQueue")
	private IQueue<String> metricsQueue;

	private static final RestTemplate REST_CLIENT = new RestTemplate();

	private static final String AUDIT_ROUTER_URL = "http://auditrouter:20002/router";

	@Around(value = "args(operationModel) && execution(* com.minsait.onesait.platform.router.service.app.service.crud.RouterCrudServiceImpl.query(*))")
	public Object queryMetrics(ProceedingJoinPoint joinPoint, OperationModel operationModel) throws Throwable {

		final Long startTime = System.currentTimeMillis();

		try {
			final OperationResultModel result = (OperationResultModel) joinPoint.proceed();

			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;
			if (!"QueryMetrics".equals(operationModel.getOntologyName())) {
				final QueryMetric queryMetric = QueryMetric.builder().user(operationModel.getUser())
						.query(operationModel.getQueryType().name()).source(operationModel.getSource().name())
						.query(operationModel.getBody()).startTime(startTime).endTime(endTime).totalMs(totalMs)
						.status(Status.OK).entity(operationModel.getOntologyName())
						.datasource(getEntityDatasource(operationModel.getOntologyName())).build();

				final ObjectMapper objectMapper = new ObjectMapper();

				metricsQueue.offer(objectMapper.writeValueAsString(queryMetric));
			}

			return result;
		} catch (final Throwable e) {
			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;
			if (!"QueryMetrics".equals(operationModel.getOntologyName())) {
				final QueryMetric queryMetric = QueryMetric.builder().user(operationModel.getUser())
						.query(operationModel.getQueryType().name()).source(operationModel.getSource().name())
						.query(operationModel.getBody()).startTime(startTime).endTime(endTime).totalMs(totalMs)
						.status(Status.ERROR).errorMessage(e.getMessage()).entity(operationModel.getOntologyName())
						.datasource(getEntityDatasource(operationModel.getOntologyName())).build();

				final ObjectMapper objectMapper = new ObjectMapper();

				metricsQueue.offer(objectMapper.writeValueAsString(queryMetric));
			}
			throw e;
		}

	}

	private String getEntityDatasource(String entity) {
		try {
			final ResponseEntity<String> response = REST_CLIENT.exchange(
					AUDIT_ROUTER_URL + "/entity/" + entity + "/datasource", HttpMethod.GET, null, String.class);
			return response.getBody();
		} catch (final HttpClientErrorException e) {
			log.error("Could not get entity datasource for entity {}, error: {}, status: {}", entity,
					e.getResponseBodyAsString(), e.getRawStatusCode());
		} catch (final Exception e) {
			log.error("Could not get entity datasource for entity {}, error: {}", entity, e.getMessage());
		}
		return null;
	}

}
