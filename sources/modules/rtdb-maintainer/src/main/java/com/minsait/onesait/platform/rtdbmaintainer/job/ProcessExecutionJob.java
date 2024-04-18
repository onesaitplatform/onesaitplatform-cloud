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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.services.processtrace.ProcessTraceService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.rtdbmaintainer.audit.aop.RtdbMaintainerAuditable;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessExecutionJob {

	@Autowired
	private ProcessTraceService processTraceService;

	@RtdbMaintainerAuditable
	public void execute(JobExecutionContext context) throws IOException {

		final String processId = context.getJobDetail().getJobDataMap().getString("processId");

		final String verticalSchema = context.getJobDetail().getJobDataMap()
				.getString(Tenant2SchemaMapper.VERTICAL_SCHEMA_KEY_STRING);

		final String tenant = context.getJobDetail().getJobDataMap().getString(Tenant2SchemaMapper.TENANT_KEY_STRING);

		if (!StringUtils.isEmpty(tenant) && !StringUtils.isEmpty(verticalSchema)) {
			MultitenancyContextHolder.setTenantName(tenant);
			MultitenancyContextHolder.setVerticalSchema(verticalSchema);
		}
		try {
			processTraceService.checkProcessExecution(processId);
			if (log.isDebugEnabled()) {
				log.debug("Process execution checked: {}", processId);
			}			
		} catch (final Exception e) {
			log.error("Error checking process execution: {}", processId, e);
		}
	}

}
