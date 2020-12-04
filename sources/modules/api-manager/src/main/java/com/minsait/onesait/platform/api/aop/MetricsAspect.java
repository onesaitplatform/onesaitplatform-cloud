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
package com.minsait.onesait.platform.api.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Order // for many aspects order.
@Component
@Slf4j
public class MetricsAspect extends BaseAspect {

	private static final String METHOD = "method";
	private static final String COUNTER_CALLS = "counter.calls";
	@Autowired
	private MeterRegistry meterRegistry;

	@Around(value = "execution(* com.minsait.onesait.platform.api.rest.api.*.*(..))")
	public Object processTx(ProceedingJoinPoint joinPoint) throws java.lang.Throwable {
		meterRegistry.counter(COUNTER_CALLS, METHOD, getMethod(joinPoint).toGenericString()).increment();
		log.info("Controller @Around for {}, Interceptor Call {}", getMethod(joinPoint), joinPoint.getSignature());

		final long start = System.currentTimeMillis();
		Object proceed = null;

		try {
			proceed = joinPoint.proceed();

		} finally {
			updateStats(getClassName(joinPoint), getMethod(joinPoint).getName(), (System.currentTimeMillis() - start));
		}

		final long executionTime = System.currentTimeMillis() - start;
		log.info("Controller @Around for {}, Interceptor Called: {} executed in {} ms", getMethod(joinPoint),
				joinPoint.getSignature(), executionTime);

		return proceed;
	}

	@Before("execution(* com.minsait.onesait.platform.api.rest.api.*.*(..))")
	public void beforeSampleCreation(JoinPoint joinPoint) {
		meterRegistry.counter(COUNTER_CALLS, METHOD, "beforeSampleCreation").increment();
		;

		log.info("Controller @Before for {}, Method Invoked: {}", getMethod(joinPoint),
				joinPoint.getSignature().getName());

		if (joinPoint.getArgs() != null) {
			final int size = joinPoint.getArgs().length;
			if (size > 0) {
				log.info("Controller @Before for {}, Arguments Passed: {}", getMethod(joinPoint),
						joinPoint.getArgs()[0]);

			}
		}
	}

	@AfterReturning(pointcut = "execution(* com.minsait.onesait.platform.api.rest.api.*.*(..))", returning = "retVal")
	public void logServiceAccess(JoinPoint joinPoint, Object retVal) {
		meterRegistry.counter(COUNTER_CALLS, METHOD, "logServiceAccess").increment();
		;

		log.info("Controller @AfterReturning for {} Completed: {} ", getMethod(joinPoint), joinPoint);

		if (retVal != null) {
			log.debug("Controller @AfterReturning for {} Returned: {}", getMethod(joinPoint), retVal.toString());
		}

	}

	@AfterThrowing(pointcut = "execution(* com.minsait.onesait.platform.api.rest.api.*.*(..))", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, Exception ex) {

		log.info("After Throwing {} Method Invoked: {} ", getMethod(joinPoint), joinPoint.getSignature().getName());
		log.info(" Exception Message: {} ", ex.getMessage());

	}

}