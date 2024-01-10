/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.command.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.command.executor.dto.CommandResult;
import com.minsait.onesait.platform.command.executor.exception.CommandExecutorException;

import lombok.extern.slf4j.Slf4j;

@Component
@Qualifier("ansibleCommandExecutor")
@Slf4j
public class AnsibleCommandExecutor implements CommandExecutor {

	public CommandResult executeCommand(String keyPath, String password, String username, String host, Integer port,
			String playbookPath) throws CommandExecutorException, IOException {
		CommandResult result = new CommandResult();

//		String hostPath = writeHostFile(host, username);

		StringBuilder output = new StringBuilder();
		try {

			ProcessBuilder builder = new ProcessBuilder("ansible-playbook", playbookPath,
					"--private-key=" + keyPath/*
												 * , "-i", hostPath
												 */);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				output.append(line);
				output.append(System.getProperty("line.separator"));
				if (log.isDebugEnabled()) {
					log.debug("output: {}", line);
				}				
			}
			log.debug("waitfor");
			p.waitFor();
			result.setResult(output.toString());
			if (log.isDebugEnabled()) {
				log.debug("Ansible command executed, output.toString(): {}", output.toString());
			}			
			return result;

		} catch (Exception e) {
			throw new CommandExecutorException(e.getMessage());
		} finally {
			removeFile(playbookPath);
//			removeFile(hostPath);
		}
	}

	private String writeHostFile(String ip, String user) throws IOException {
		File file = new File("/etc/ansible/host_" + UUID.randomUUID());
		Files.write(file.toPath(), (ip + " ansible_user=" + user).getBytes());
		if (log.isDebugEnabled()) {
			log.debug("hostPath: {}", file.getAbsolutePath());
		}		
		return file.getAbsolutePath();
	}

	private void removeFile(String path) throws IOException {
		Files.delete(new File(path).toPath());
	}

}
