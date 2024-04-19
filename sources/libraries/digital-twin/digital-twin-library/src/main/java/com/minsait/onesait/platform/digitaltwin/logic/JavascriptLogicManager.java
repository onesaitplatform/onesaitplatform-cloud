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
package com.minsait.onesait.platform.digitaltwin.logic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.logic.api.JavascriptAPI;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JavascriptLogicManager implements LogicManager {

	private final static String ENGINE_NAME = "nashorn";
	private final static String LOGIC_FILE = "static/js/logic.js";

	private Invocable invocable;

	@Autowired
	private List<JavascriptAPI> twinApis;

	@PostConstruct
	public void init() {
		for (JavascriptAPI twinApi : twinApis) {
			twinApi.init();
		}

		ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
		invocable = (Invocable) engine;
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			engine.eval(new InputStreamReader(classLoader.getResource(LOGIC_FILE).openStream()));

		} catch (ScriptException e1) {
			log.error("Execution logic for action", e1);
			this.invocable = null;
		} catch (FileNotFoundException e) {
			log.error("File logic.js not found.", e);
			this.invocable = null;
		} catch (IOException e) {
			log.error("File logic.js not found.", e);
			this.invocable = null;
		}
	}

	@Override
	public void invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
		if (null != this.invocable) {
			this.invocable.invokeFunction(name, args);
		} else {
			log.error("Cannot invoke function, invocable is null");
		}
	}

}
