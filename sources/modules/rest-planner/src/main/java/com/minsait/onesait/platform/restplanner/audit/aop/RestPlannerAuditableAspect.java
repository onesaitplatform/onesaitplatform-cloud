/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.restplanner.audit.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;

@Component
@Aspect
public class RestPlannerAuditableAspect extends BaseAspect {

	@Autowired
	private RestPlannerAuditProcessor auditProcessor;

	@Around("@annotation(auditable) && args(context,..) && execution(* executeJob(..))")
	public Object auditExecution(ProceedingJoinPoint joinPoint, RestPlannerAuditable auditable,
			JobExecutionContext context) throws Throwable {
		String response = null;
		final String identification = context.getJobDetail().getJobDataMap().getString("identification");
		final String user = context.getJobDetail().getJobDataMap().getString("userId");
		final String url = context.getJobDetail().getJobDataMap().getString("url");
		final String method = context.getJobDetail().getJobDataMap().getString("method");
		try {
			String message = "Job Rest Planner " + identification + " executed by user " + user + ": " + method + " "
					+ url;
			response = (String) joinPoint.proceed();
			eventProducer.publish(auditProcessor.getExecutionInfo(user, message, response));
		} catch (final Throwable e) {
			throw e;
		}
		return response;
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(context,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, RestPlannerAuditable auditable, JobExecutionContext context,
			Throwable ex) {
		final String identification = context.getJobDetail().getJobDataMap().getString("identification");
		final String user = context.getJobDetail().getJobDataMap().getString("userId");
		final String url = context.getJobDetail().getJobDataMap().getString("url");
		final String method = context.getJobDetail().getJobDataMap().getString("method");
		String message = "Job Rest Planner " + identification + " executed by user " + user + ": " + method + " " + url;
		final OPAuditError error = auditProcessor.getErrorEvent(user, message, (Exception) ex);
		eventProducer.publish(error);
	}

}
