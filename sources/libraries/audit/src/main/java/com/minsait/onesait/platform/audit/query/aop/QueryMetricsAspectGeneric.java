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
package com.minsait.onesait.platform.audit.query.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.minsait.onesait.platform.audit.query.dto.QueryMetric;
import com.minsait.onesait.platform.audit.query.dto.QueryMetric.Status;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Lazy(false)
@Slf4j
@ConditionalOnExpression("'${onesaitplatform.router.metrics.query.enabled}'=='true'")
@ConditionalOnMissingClass(value = "com.minsait.onesait.platform.router.service.app.service.crud.RouterCrudServiceImpl")
@ConditionalOnClass(name = "com.minsait.onesait.platform.persistence.services.QueryToolServiceImpl")
public class QueryMetricsAspectGeneric {

	@Autowired(required = false)
	@Qualifier("metricsQueue")
	private IQueue<String> metricsQueue;

	@Autowired
	private Environment environment;

	private static final RestTemplate REST_CLIENT = new RestTemplate();

	private static final String AUDIT_ROUTER_URL = "http://auditrouter:20002/router";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Around(value = "args(user,ontology,query,..) && execution(* com.minsait.onesait.platform.persistence.services.QueryToolServiceImpl.querySQLAsJson(..))")
	public Object queryMetrics(ProceedingJoinPoint joinPoint, String user, String ontology, String query)
			throws Throwable {

		final Long startTime = System.currentTimeMillis();

		try {
			final Object result = joinPoint.proceed();

			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;
			sendQueryMetric(startTime, endTime, totalMs, user, query, "SQL", Status.OK, null, ontology);

			return result;
		} catch (final Throwable e) {
			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;

			sendQueryMetric(startTime, endTime, totalMs, user, query, "SQL", Status.ERROR, e.getMessage(), ontology);
			throw e;
		}

	}

	@Around(value = "args(user,ontology,query,..) && execution(* com.minsait.onesait.platform.persistence.services.QueryToolServiceImpl.queryNativeAsJson(..))")
	public Object queryMetricsNative(ProceedingJoinPoint joinPoint, String user, String ontology, String query)
			throws Throwable {

		final Long startTime = System.currentTimeMillis();

		try {
			final Object result = joinPoint.proceed();

			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;
			sendQueryMetric(startTime, endTime, totalMs, user, query, "NATIVE", Status.OK, null, ontology);

			return result;
		} catch (final Throwable e) {
			final Long endTime = System.currentTimeMillis();
			final Long totalMs = endTime - startTime;

			sendQueryMetric(startTime, endTime, totalMs, user, query, "NATIVE", Status.ERROR, e.getMessage(), ontology);
			throw e;
		}

	}

	private void sendQueryMetric(Long startMs, Long endMs, Long totalMs, String user, String query, String queryType,
			Status status, String errorMessage, String ontology) throws JsonProcessingException {
		if (metricsQueue != null && !"QueryMetrics".equals(ontology)) {
			final QueryMetric queryMetric = QueryMetric.builder().user(user).query(query)
					.source(getCurrentModule(environment.getProperty("spring.application.name"))).queryType(queryType)
					.startTime(startMs).endTime(endMs).totalMs(totalMs).errorMessage(errorMessage).status(status)
					.entity(ontology).datasource(getEntityDatasource(ontology)).build();

			metricsQueue.offer(MAPPER.writeValueAsString(queryMetric));
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

	private String getCurrentModule(String value) {
		if (value.contains("control")) {
			return "CONTROLPANEL";
		} else if (value.contains("dashboard")) {
			return "DASHBOARD_ENGINE";
		} else {
			log.debug("Module not recognised {}", value);
			return "QUERY_TOOL";
		}
	}

}
