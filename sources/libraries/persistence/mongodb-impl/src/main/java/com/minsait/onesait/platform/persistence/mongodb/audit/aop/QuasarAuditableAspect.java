/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.mongodb.audit.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.OPAuditError;

@Component
@Aspect
public class QuasarAuditableAspect extends BaseAspect {

	private static final String MAP_REDUCE = "mapreduce";

	@Autowired
	private QuasarAuditProcessor auditProcessor;

	@Around("@annotation(auditable) && args(collection,query,..) && execution(* compileQueryAsJson(..))")
	public Object auditSQLCompiledQueries(ProceedingJoinPoint joinPoint, QuasarAuditable auditable, String query,
			String collection) throws Throwable {
		String compiledQuery = null;
		try {
			compiledQuery = (String) joinPoint.proceed();
			if (compiledQuery.toLowerCase().contains(MAP_REDUCE))
				eventProducer.publish(auditProcessor.getWarningEvent(query, compiledQuery, collection));

		} catch (final Throwable e) {
			throw e;
		}
		return compiledQuery;

	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(collection, query,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, QuasarAuditable auditable, String collection, String query,
			Throwable ex) {

		final OPAuditError error = auditProcessor.getErrorEvent(query, collection, (Exception) ex);
		eventProducer.publish(error);
	}

}
