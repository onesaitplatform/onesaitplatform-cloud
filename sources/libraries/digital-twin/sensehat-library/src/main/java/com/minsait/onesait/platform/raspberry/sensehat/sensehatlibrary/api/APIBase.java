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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.CommandExecutor;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.CommandExecutorFactory;

/**
 * Created by jcincera on 22/06/2017.
 */
public abstract class APIBase {

	private CommandExecutor commandExecutor;

	protected APIBase() {
		this.commandExecutor = CommandExecutorFactory.get();
	}

	protected CommandResult execute(Command command, String... args) {
		return commandExecutor.execute(command, args);
	}
}
