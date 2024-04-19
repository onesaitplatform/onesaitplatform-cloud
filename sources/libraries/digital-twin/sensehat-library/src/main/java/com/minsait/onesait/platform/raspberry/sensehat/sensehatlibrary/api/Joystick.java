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

import java.util.List;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.JoystickEvent;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.utils.PythonUtils;

/**
 * Created by jcincera on 20/06/2017.
 */
public class Joystick extends APIBase {

	Joystick() {
	}

	/**
	 * Blocks execution until a joystick event occurs
	 *
	 * @return event type which occured
	 */
	public JoystickEvent waitForEvent() {
		return waitForEvent(false);
	}

	/**
	 * Blocks execution until a joystick event occurs
	 *
	 * @param emptyBuffer
	 *            can be used to flush any pending events before waiting for new
	 *            events
	 * @return event type which occured
	 */
	public JoystickEvent waitForEvent(boolean emptyBuffer) {
		return execute(Command.WAIT_FOR_EVENT_EMPTY_BUFFER, PythonUtils.toBoolean(emptyBuffer)).getJoystickEvent();
	}

	/**
	 * Returns a list of events representing all events that have occurred since the
	 * last call to getEvents or waitForEvent
	 *
	 * @return list of events
	 */
	public List<JoystickEvent> getEvents() {
		throw new UnsupportedOperationException("Not supported yet!");
	}
}
