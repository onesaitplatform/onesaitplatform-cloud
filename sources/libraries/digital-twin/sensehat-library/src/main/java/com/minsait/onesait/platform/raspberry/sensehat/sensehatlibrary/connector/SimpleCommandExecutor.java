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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.CommandException;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.CommunicationException;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.InvalidSystemArchitectureException;

/**
 * Created by jcincera on 20/06/2017.
 */
public class SimpleCommandExecutor implements CommandExecutor {

	SimpleCommandExecutor() {
		if (!System.getProperty("os.arch").toLowerCase().contains("arm")) {
			throw new InvalidSystemArchitectureException(
					"System architecture is not supported for this command executor");
		}
	}

	@Override
	public CommandResult execute(Command command, String... args) {
		try {

			// Create command
			final String completeCommand = createCompleteCommand(command, args);

			// Call
			System.out.println("Command: " + command.name());
			ProcessBuilder pb = new ProcessBuilder("python", "-c", completeCommand);
			pb.redirectErrorStream(true);
			Process p = pb.start();

			// Read output
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;

			while ((line = output.readLine()) != null) {
				result.append(line);
				result.append(lineSeparator);
			}
			System.out.println("Command result: " + result.toString());

			// Handle result
			waitForCommand(p);
			checkCommandException(result);
			return new CommandResult(result.toString());
		} catch (Exception e) {
			System.err.println(e);

			if (e instanceof CommandException) {
				throw (CommandException) e;
			}

			throw new CommunicationException("Communication with Sense Hat failed!", e);
		}
	}

	private void checkCommandException(StringBuilder result) {
		if (result.toString().contains("Traceback") || result.toString().contains("Error")) {
			throw new CommandException("Command execution failed!\n" + result.toString());
		}
	}

	private String createCompleteCommand(Command command, String[] args) {
		String rawCommand = (args != null && args.length > 0) ? String.format(command.getCommand(), (Object[]) args)
				: command.getCommand();

		return Command.IMPORT_SENSE_HAT.getCommand() + ";" + Command.SENSE_OBJECT.getCommand() + ";" + rawCommand;
	}

	private void waitForCommand(Process p) {
		try {
			p.waitFor();
			Thread.sleep(100);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
}
