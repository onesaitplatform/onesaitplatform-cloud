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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.CommandException;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.CommunicationException;

/**
 * Created by jcincera on 22/06/2017.
 */
public class CommandResult {

	private String value;

	public CommandResult(String value) {
		this.value = value;
	}

	public float getFloat() {
		return Float.valueOf(value);
	}

	public IMUData getIMUData() {
		final String[] result = value.split("@");

		if (result.length != 3) {
			throw new CommandException("Invalid orientation: " + value);
		}

		return new IMUData(Float.valueOf(result[0]), Float.valueOf(result[1]), Float.valueOf(result[2]));
	}

	public IMUDataRaw getIMUDataRaw() {
		final String[] result = value.split("@");

		if (result.length != 3) {
			throw new CommandException("Invalid orientation: " + value);
		}

		return new IMUDataRaw(Float.valueOf(result[0]), Float.valueOf(result[1]), Float.valueOf(result[2]));
	}

	public JoystickEvent getJoystickEvent() {
		final String[] result = value.split("@");

		if (result.length != 3) {
			throw new CommandException("Parsing joystick event failed: " + value);
		}

		return new JoystickEvent(result[0], result[1], result[2]);
	}

	public void checkEmpty() {
		if (this.value == null || !this.value.trim().equals("")) {
			throw new CommunicationException("Unexpected output: " + "'" + this.value + "'");
		}
	}
}
