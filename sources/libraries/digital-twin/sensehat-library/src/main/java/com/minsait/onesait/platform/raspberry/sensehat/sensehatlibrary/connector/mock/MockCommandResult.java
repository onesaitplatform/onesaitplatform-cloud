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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.mock;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.IMUData;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.IMUDataRaw;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.CommandException;

/**
 * Created by jcincera on 04/07/2017.
 */
public class MockCommandResult extends CommandResult {

	private Command command;

	public MockCommandResult(String value) {
		super(value);
	}

	@Override
	public float getFloat() {
		return 30.0f;
	}

	@Override
	public IMUData getIMUData() {
		return new IMUData(5.0f, 6.0f, 6.5f);
	}

	@Override
	public IMUDataRaw getIMUDataRaw() {
		return new IMUDataRaw(1.5f, 5.5f, 3.8f);
	}

	@Override
	public void checkEmpty() {
		if (this.command.getCommand().contains("print")) {
			throw new CommandException("Command expects some value!");
		}
	}

	public void setCommand(Command command) {
		this.command = command;
	}
}
