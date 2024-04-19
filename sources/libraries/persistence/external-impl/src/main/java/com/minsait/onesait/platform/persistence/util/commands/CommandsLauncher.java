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
package com.minsait.onesait.platform.persistence.util.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandsLauncher {

	public static String executeCommand(String command) throws CommandExecutionException {
		log.info("Command to be executed: " + command);

		StringBuffer output = new StringBuffer();
		String outputreturn = String.valueOf("");

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);

			/** any error message **/
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "INFO");

			/** any input? **/
			StreamGobbler inputGobbler = new StreamGobbler(p.getInputStream(), "INFO ");

			/** kick them off **/
			errorGobbler.start();
			inputGobbler.start();

			/** any error??? **/
			int exitVal = p.waitFor();

			StringBuffer stringBufferError = errorGobbler.getStringBufferOutPut();
			if (null != stringBufferError && stringBufferError.length() > 0) {
				output.append(stringBufferError);
			}

			StringBuffer stringBufferInput = inputGobbler.getStringBufferOutPut();
			if (null != stringBufferInput && stringBufferInput.length() > 0) {
				output.append(stringBufferInput);
			}

			log.info("Command output: " + command + " exitval: " + exitVal);

			if (output != null && output.length() > 1000) {
				outputreturn = output.substring(0, 1000);
			}

			if (p.exitValue() != 0) {

				throw new CommandExecutionException(outputreturn);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			throw new CommandExecutionException(e);
		}

		return outputreturn;
	}
}

@Slf4j
class StreamGobbler extends Thread {
	InputStream is;
	String type;
	StringBuffer output = new StringBuffer();

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line != null) {
					output.append(type + ">" + line + "\n");
				}
			}

		} catch (IOException ioe) {
			log.error("Error reading data", ioe);
		}
	}

	public StringBuffer getStringBufferOutPut() {

		return output;
	}
}
