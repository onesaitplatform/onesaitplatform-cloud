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
package com.minsait.onesait.platform.digitaltwin.action.execute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.logic.LogicManager;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActionExecutor {

	@Value("${device.javascript.enabled:true}")
	private boolean javascriptEnabled;

	@Autowired
	private LogicManager logicManager;

	@Autowired(required = false)
	private ActionJavaListener actionJavaListener;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@PreDestroy
	public void destroy() {
		this.executor.shutdown();
	}

	public void executeAction(String name, String data) {

		if (null != actionJavaListener) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						actionJavaListener.executeAction(name, data);
					} catch (Exception e) {
						log.error("Error executing Java Action", e);
					}
				}
			});
		}

		if (javascriptEnabled) {
			try {
				log.info("Invoques Javascript function");
				this.logicManager.invokeFunction("onAction" + name.substring(0, 1).toUpperCase() + name.substring(1),
						data);

			} catch (ScriptException e1) {
				log.error("Execution logic for action " + name + " failed", e1);
			} catch (NoSuchMethodException e2) {
				log.error("Action " + name + " not found", e2);
			}
		}
	}

}
