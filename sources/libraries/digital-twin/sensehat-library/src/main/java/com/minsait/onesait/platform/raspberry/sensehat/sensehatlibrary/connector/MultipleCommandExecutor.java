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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.InvalidSystemArchitectureException;

/**
 * Created by jcincera on 27/06/2017.
 */
public class MultipleCommandExecutor implements CommandExecutor {

	MultipleCommandExecutor() {
		if (!System.getProperty("os.arch").toLowerCase().contains("arm")) {
			throw new InvalidSystemArchitectureException(
					"System architecture is not supported for this command executor");
		}
	}

	@Override
	public CommandResult execute(Command command, String... args) {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
