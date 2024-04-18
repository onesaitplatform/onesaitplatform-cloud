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
package com.minsait.onesait.platform.api.aop;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseAspect {

	private static ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();
	private static long statLogFrequency = 10;
	private static long methodWarningThreshold = 1000;

	public Method getMethod(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		return signature.getMethod();
	}

	public Object getMethodInvocation(JoinPoint joinPoint) {
		MethodInvocationProceedingJoinPoint point = (MethodInvocationProceedingJoinPoint) joinPoint;
		return point.getThis();
	}

	public String getClassName(JoinPoint joinPoint) {
		return this.getMethodSignatureClass(joinPoint).getName();
	}

	public Class<? extends MethodSignature> getClass(JoinPoint joinPoint) {
		return this.getMethodSignatureClass(joinPoint);
	}

	private Class<? extends MethodSignature> getMethodSignatureClass(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		return signature.getClass();
	}

	public void updateStats(String className, String methodName, long elapsedTime) {

		MethodStats stats = methodStats.computeIfAbsent(methodName, k -> new MethodStats(className, methodName));

		stats.count++;
		stats.totalTime += elapsedTime;
		if (elapsedTime > stats.maxTime) {
			stats.maxTime = elapsedTime;
		}

		if (elapsedTime > methodWarningThreshold) {
			log.warn("method warning: Class : {} Method: {}(), cnt = {}, lastTime = {}, maxTime = {}", className,
					methodName, stats.count, elapsedTime, stats.maxTime);
		}

		if (stats.count % statLogFrequency == 0) {
			long avgTime = stats.totalTime / stats.count;
			long runningAvg = (stats.totalTime - stats.lastTotalTime) / statLogFrequency;
			log.info(" Class: {}, Method: {} cnt: {}, lastTime: {}, avgTime: {}, runningAvg: {}, maxTime: {} ",
					className, stats.count, elapsedTime, avgTime, runningAvg, stats.maxTime);

			// reset the last total time
			stats.lastTotalTime = stats.totalTime;
		}

	}

	class MethodStats {
		private String methodName;
		private String className;
		private long count;
		private long totalTime;
		private long lastTotalTime;
		private long maxTime;

		public MethodStats(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public long getTotalTime() {
			return totalTime;
		}

		public void setTotalTime(long totalTime) {
			this.totalTime = totalTime;
		}

		public long getLastTotalTime() {
			return lastTotalTime;
		}

		public void setLastTotalTime(long lastTotalTime) {
			this.lastTotalTime = lastTotalTime;
		}

		public long getMaxTime() {
			return maxTime;
		}

		public void setMaxTime(long maxTime) {
			this.maxTime = maxTime;
		}
	}

}