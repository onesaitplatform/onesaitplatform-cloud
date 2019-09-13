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
package com.minsait.onesait.platform.flowengine.nodered;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClient;
import com.minsait.onesait.platform.flowengine.nodered.sync.NodeRedDomainSyncMonitor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NodeRedLauncher {

	@Value("${onesaitplatform.flowengine.node.path}")
	private String nodePath;
	@Value("${onesaitplatform.flowengine.launcher.path}")
	private String nodeRedLauncherPath;
	@Value("${onesaitplatform.flowengine.launcher.job}")
	private String nodeRedJob;
	@Value("${onesaitplatform.flowengine.launcher.failsbeforestop.max:10}")
	private int maxFailsNumber;
	@Value("${onesaitplatform.flowengine.launcher.failsbeforestop.time.millis:60000}")
	private int failsBeforeStopMillis;
	@Value("${onesaitplatform.flowengine.launcher.reboot.delay.seconds:10}")
	private int rebootDelay;
	@Value("${onesaitplatform.flowengine.launcher.logging.active:true}")
	private Boolean loggingEnabled;
	@Value("${onesaitplatform.flowengine.launcher.logging.log:/tmp/log/flowEngine}")
	private String debugLog;
	@Value("${onesaitplatform.flowengine.launcher.logging.retain.days:5}")
	private int debugRatainDays;
	@Value("${onesaitplatform.flowengine.launcher.logging.append: false}")
	private Boolean logAppend;

	private ExecutorService exService = Executors.newSingleThreadExecutor();

	@Autowired
	private NodeRedAdminClient nodeRedAdminClient;

	@Autowired
	private NodeRedDomainSyncMonitor nodeRedMonitor;

	@PostConstruct
	public void init() {

		// Stop the flow engine in case it is still up
		try {
			nodeRedAdminClient.stopFlowEngine();
		} catch (Exception e) {
			log.warn("Could not stop Flow Engine.");
		}

		if (null != nodePath && null != nodeRedLauncherPath) {
			NodeRedLauncherExecutionThread launcherThread = new NodeRedLauncherExecutionThread(this.nodePath,
					this.nodeRedJob, this.nodeRedLauncherPath, this.maxFailsNumber, this.failsBeforeStopMillis,
					this.loggingEnabled, this.debugLog);

			exService.execute(launcherThread);
		}
	}

	@PreDestroy
	public void destroy() {
		this.exService.shutdown();
	}

	private class NodeRedLauncherExecutionThread implements Runnable {

		private String nodePath;
		private String workingPath;
		private String launcherJob;
		private int maxFailsNumber;
		private int failsBeforeStopMillis;

		private boolean stop;
		private long lastFailTimestamp;
		private int consecutiveFails;
		private Boolean loggingEnabled;
		private String logPath;

		public NodeRedLauncherExecutionThread(String nodePath, String launcherJob, String workingPath,
				int maxFailsNumber, int failsBeforeStopMillis, Boolean enableDebugging, String logPath) {
			this.nodePath = nodePath;
			this.workingPath = workingPath;
			this.launcherJob = launcherJob;
			this.maxFailsNumber = maxFailsNumber;
			this.failsBeforeStopMillis = failsBeforeStopMillis;

			this.stop = false;
			this.lastFailTimestamp = 0;
			this.consecutiveFails = 0;
			this.loggingEnabled = enableDebugging;
			this.logPath = logPath;
		}

		public void stop() {
			this.stop = true;
			nodeRedMonitor.stopMonitor();
		}

		@Override
		public void run() {

			CommandLine commandLine = new CommandLine(this.nodePath);
			commandLine.addArgument(this.launcherJob);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(0);
			executor.setWorkingDirectory(new File(this.workingPath));

			while (!stop) {
				try {
					nodeRedAdminClient.resetSynchronizedWithBDC();
					nodeRedMonitor.stopMonitor();
					TimeUnit.SECONDS.sleep(rebootDelay);

					ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
					executor.setWatchdog(watchDog);

					DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler() {
						@Override
						public void onProcessComplete(final int exitValue) {
							super.onProcessComplete(exitValue);
						}

						@Override
						public void onProcessFailed(final ExecuteException e) {
							super.onProcessFailed(e);
							processFail();
						}
					};
					if (loggingEnabled) {
						executor.setStreamHandler(new PumpStreamHandler(
								new RolloverFileOutputStream(logPath + File.separator + "yyyy_mm_dd.debug.log", logAppend, debugRatainDays)));
					}
					executor.execute(commandLine, handler);
					nodeRedMonitor.startMonitor();
					handler.waitFor();
				} catch (Exception e) {
					log.error("Error arrancando NodeRED", e);
					this.processFail();
				}
			}

		}

		private void processFail() {
			long currentTimestamp = System.currentTimeMillis();
			// Hace mas de 1 minuto del ultimo fallo
			if (currentTimestamp > lastFailTimestamp + this.failsBeforeStopMillis) {
				this.consecutiveFails = 1;
			} else {
				this.consecutiveFails++;
			}
			lastFailTimestamp = System.currentTimeMillis();
			if (this.consecutiveFails > this.maxFailsNumber) {
				this.stop();
			}
		}

	}

}
