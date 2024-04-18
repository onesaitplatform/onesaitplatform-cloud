/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.config.services.webproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebProjectNPMHelperImpl implements WebProjectNPMHelper {

	private StringBuilder sbCurrentStatus;

	@Override
	public synchronized NPMCommandResult executeNPMInstall(String directory, String runCommand) {
		this.sbCurrentStatus = new StringBuilder();
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			this.sbCurrentStatus.append(
					"                                                                                                                -------------------------NPM INSTALL-------------------------")
					.append("\n");
			String[] installCommand = new String[] { "npm.cmd", "install" };

			executeNPMRun(installCommand, Optional.of(directory));
			this.sbCurrentStatus.append("\n").append(
					"                                                                                                                -------------------------NPM RUN-------------------------")
					.append("\n");

			String[] command = new String[] { "npm.cmd", "run", runCommand };
			return this.executeNPMRun(command, Optional.of(directory));
		} else {
			this.sbCurrentStatus.append(
					"                                                                                                                -------------------------NPM INSTALL-------------------------")
					.append("\n");
			String[] installCommand = new String[] { "npm", "install", directory };
			executeNPMRun(installCommand, Optional.of(directory));
			this.sbCurrentStatus.append("\n").append(
					"                                                                                                                -------------------------NPM RUN-------------------------")
					.append("\n");
			String[] command = new String[] { "npm", "run", runCommand };
			return this.executeNPMRun(command, Optional.of(directory));
		}

	}

	@Override
	public String getCurrentStatus() {
		if (null != sbCurrentStatus) {
			return sbCurrentStatus.toString();
		} else
			return "";

	}

	@Override
	public void deleteCurrentStatus() {
		sbCurrentStatus = new StringBuilder();

	}

	private NPMCommandResult executeNPMRun(String[] command, Optional<String> executionDirectory) {

		try {

			ProcessBuilder processBuilder = new ProcessBuilder();

			if (executionDirectory.isPresent()) {
				processBuilder.directory(new File(executionDirectory.get()));
			}

			processBuilder.command(command);

			Process process = processBuilder.start();

			BufferedReader readerInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

			StringBuilder sbCorrectResponse = new StringBuilder();

			String line;
			while ((line = readerInputStream.readLine()) != null) {
				sbCorrectResponse.append(line.replaceAll("\u001B\\[[;\\d]*m", "")).append(System.lineSeparator());
				sbCurrentStatus.append(line.replaceAll("\u001B\\[[;\\d]*m", "")).append(System.lineSeparator());
			}

			BufferedReader readerErrorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			StringBuilder sbErrorResponse = new StringBuilder();

			while ((line = readerErrorStream.readLine()) != null) {
				sbErrorResponse.append(line.replaceAll("\u001B\\[[;\\d]*m", "")).append("\n");
				sbCurrentStatus.append(line.replaceAll("\u001B\\[[;\\d]*m", "")).append("\n");
			}

			int exitCode = process.waitFor();

			if (exitCode == 0) {
				return new NPMCommandResult(NPMCommandResult.NPMCommandResultStatus.OK, sbCorrectResponse.toString());
			} else {

				return new NPMCommandResult(NPMCommandResult.NPMCommandResultStatus.ERROR, sbErrorResponse.toString());
			}

		} catch (Exception e) {
			log.error("Error", e);

		}
		return new NPMCommandResult(NPMCommandResult.NPMCommandResultStatus.ERROR, "");

	}

}
