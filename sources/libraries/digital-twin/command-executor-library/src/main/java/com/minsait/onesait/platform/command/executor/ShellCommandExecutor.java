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
package com.minsait.onesait.platform.command.executor;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.minsait.onesait.platform.command.executor.dto.CommandResult;
import com.minsait.onesait.platform.command.executor.exception.CommandExecutorException;

@Component
@Qualifier("shellCommandExecutor")
public class ShellCommandExecutor implements CommandExecutor {

	public CommandResult executeCommand(String path, String password, String username, String host, Integer port,
			String command) throws CommandExecutorException {
		CommandResult result = new CommandResult();

		Session session = null;
		ChannelExec channel = null;
		JSch jsch = new JSch();

		try {
			jsch.addIdentity(path, password);
			session = jsch.getSession(username, host, port);

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
			channel.setOutputStream(responseStream);
			channel.connect();

			while (channel.isConnected()) {
				Thread.sleep(100);
			}

			result.setResult(new String(responseStream.toByteArray()));
			return result;
		} catch (JSchException e) {
			throw new CommandExecutorException(e.getMessage());
		} catch (InterruptedException e) {
			throw new CommandExecutorException(e.getMessage());
		} finally {
			if (session != null) {
				session.disconnect();
			}
			if (channel != null) {
				channel.disconnect();
			}
		}

	}

}
