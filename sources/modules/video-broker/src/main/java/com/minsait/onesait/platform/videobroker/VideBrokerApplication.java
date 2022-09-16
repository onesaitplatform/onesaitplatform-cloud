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
package com.minsait.onesait.platform.videobroker;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.minsait.onesait.platform.config.repository")
@ComponentScan(basePackages = { "com.minsait.onesait.platform" })
@EnableAsync
@Slf4j
public class VideBrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideBrokerApplication.class, args);
	}

	@PostConstruct
	public void submitProcessing() {
		log.debug("Loading OpenCV engine");
		OpenCvLoader.loadOpenCV();
	}
}
