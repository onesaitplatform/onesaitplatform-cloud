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
package com.minsait.onesait.platform;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.minsait.onesait.platform.business.services.interceptor.MultitenancyInterceptor;
import com.minsait.onesait.platform.interceptor.CorrelationInterceptor;

@SpringBootApplication
@ComponentScan(basePackages = { "com.minsait.onesait.platform", "com.minsait.onesait.platform.cache" })

public class SparkLauncherApplication implements WebMvcConfigurer {

	@Autowired
	private CorrelationInterceptor logInterceptor;
	@Autowired
	private MultitenancyInterceptor multitenancyInterceptor;

	public static void main(String[] args) {
		SpringApplication.run(SparkLauncherApplication.class, args);
	}

	public static Throwable unwrap(Throwable ex) {
		if (ex != null && BeanCreationException.class.isAssignableFrom(ex.getClass())) {
			return unwrap(ex.getCause());
		} else {
			return ex;
		}

	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(logInterceptor);
		registry.addInterceptor(multitenancyInterceptor);
	}

}
