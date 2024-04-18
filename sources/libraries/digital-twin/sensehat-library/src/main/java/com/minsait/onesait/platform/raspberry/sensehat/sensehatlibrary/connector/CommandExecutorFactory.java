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

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.mock.MockCommandExecutor;

/**
 * Created by jcincera on 04/07/2017.
 */
public class CommandExecutorFactory {

	private static final String SENSE_HAT_EXECUTOR_TYPE = "SENSE_HAT_EXECUTOR_TYPE";
	private static final String SIMPLE_COMMAND = "SIMPLE_COMMAND";
	private static final String MULTIPLE_COMMAND = "MULTIPLE_COMMAND";
	private static final String OS_ARCH = "os.arch";
	private static final String OS_ARCH_ARM = "arm";

	public static CommandExecutor get() {

		// Dev command executor for not ARM system (macOS etc.)
		String osArch = System.getProperty(OS_ARCH).toLowerCase();
		if (!osArch.contains(OS_ARCH_ARM)) {
			System.out.println("ARM platform not detected! Using mock command executor.");
			return new MockCommandExecutor();
		}

		// Command executor for Raspberry PI
		String senseHatExecutorType = System.getProperty(SENSE_HAT_EXECUTOR_TYPE);
		if (MULTIPLE_COMMAND.equals(senseHatExecutorType)) {
			return new MultipleCommandExecutor();
		} else if (SIMPLE_COMMAND.equals(senseHatExecutorType)) {
			return new SimpleCommandExecutor();
		} else {
			return new SimpleCommandExecutor();
		}
	}
}
