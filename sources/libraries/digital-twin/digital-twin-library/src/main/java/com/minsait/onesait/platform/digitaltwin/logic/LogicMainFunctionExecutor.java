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
package com.minsait.onesait.platform.digitaltwin.logic;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LogicMainFunctionExecutor {

	@Value("${device.logic.main.loop.delay.seconds:60}")
	private int mainFunctionDelay;

	@Autowired
	private LogicManager logicManager;

	@PostConstruct
	public void init() {
		new LogicMainFuncionExecutorThread().start();
	}

	class LogicMainFuncionExecutorThread extends Thread {

		@Override
		public void run() {

			try {
				logicManager.invokeFunction("init");
				while (true) {
					try {
						logicManager.invokeFunction("main");
					} catch (Exception e) {
						log.error("Error executing main function", e);
					}
					try {
						Thread.sleep(mainFunctionDelay * 1000l);
					} catch (Exception e) {
					}
				}

			} catch (Exception e) {
				log.error("Error executing main function", e);
			}

		}

	}

}
