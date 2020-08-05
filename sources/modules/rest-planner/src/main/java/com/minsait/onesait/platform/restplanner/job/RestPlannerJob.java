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
package com.minsait.onesait.platform.restplanner.job;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.config.services.restplanner.RestPlannerService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.restplanner.audit.aop.RestPlannerAuditable;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestPlannerJob {

	@Autowired
	EventRouter eventRouter;
	@Autowired
	private RestPlannerService restPlannerService;

	@RestPlannerAuditable
	public String executeJob(JobExecutionContext context) throws IOException {

		final String id = context.getJobDetail().getJobDataMap().getString("id");
		final String identification = context.getJobDetail().getJobDataMap().getString("identification");
		final String user = context.getJobDetail().getJobDataMap().getString("userId");
		final String url = context.getJobDetail().getJobDataMap().getString("url");
		final String method = context.getJobDetail().getJobDataMap().getString("method");
		final String headers = context.getJobDetail().getJobDataMap().getString("headers");
		final String body = context.getJobDetail().getJobDataMap().getString("body");
		final String verticalSchema = context.getJobDetail().getJobDataMap()
				.getString(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING);

		final String tenant = context.getJobDetail().getJobDataMap().getString(Tenant2SchemaMapper.TENANT_KEY_STRING);
		String resp = null;

		if (!StringUtils.isEmpty(tenant) && !StringUtils.isEmpty(verticalSchema)) {
			MultitenancyContextHolder.setTenantName(tenant);
			MultitenancyContextHolder.setVerticalSchema(verticalSchema);
		}
		try {
			resp = restPlannerService.execute(user, url, method, body, headers);
			final String message = "Job Rest Planner " + identification + " by user " + user + ": " + method + " "
					+ url;
			log.info(message);
			log.info("Response: " + resp);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error generating the rest planner for user:" + user, e);
		}
		return resp;
	}

}
