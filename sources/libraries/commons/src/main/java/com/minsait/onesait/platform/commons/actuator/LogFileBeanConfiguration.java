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
package com.minsait.onesait.platform.commons.actuator;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class LogFileBeanConfiguration {

	@Autowired
	private ConfigurableEnvironment configEnv;
	private static final String ROLLING_FILE_APPENDER_NAME = "rollingFileAppender";

	@Bean
	@Primary
	public LogFileWebEndpoint logFileWebEndpoint() {
		return new LogFileWebEndpoint(configEnv);
	}

	@Scheduled(fixedDelay = 600000)
	private void refreshLogFileProperty() {
		log.debug("Refreshing property file");
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		for (final Logger logger : context.getLoggerList()) {
			for (final Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
				Object enumElement = index.next();
				if (enumElement instanceof AsyncAppender) {
					final AsyncAppender asyc = (AsyncAppender) enumElement;
					enumElement = asyc.getAppender(ROLLING_FILE_APPENDER_NAME);
				}

				if (enumElement instanceof FileAppender) {
					final FileAppender<?> fileAppender = (FileAppender<?>) enumElement;
					final File file = new File(fileAppender.getFile());
					setNewPropertyValue(file.getAbsolutePath());

				}
			}
		}

	}

	private void setNewPropertyValue(String absolutePath) {
		final Map<String, Object> map = new HashMap<>();
		map.put("logging.file", absolutePath);
		log.debug("log file at {}", absolutePath);
		configEnv.getPropertySources().addFirst(new MapPropertySource("REFRESHABLE_MAP", map));
	}
}
