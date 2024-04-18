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
package com.minsait.onesait.platform.spark.launcher.executor;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.spark.dto.SparkLaunchJobModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SparkJobLauncherExecutor {

	final static private String SPARK_HOME = "/opt/spark";
	final static private String JAVA_HOME = "/usr/local/openjdk-11";
	final static private String SPARK_DEPLOYMENT_MODE = "cluster";
	private String SPARK_MASTER = "spark://spark-master:7077";
	@Value("${onesaitplatform.database.minio.access-key:access-key}")
	private String accessKey;
	@Value("${onesaitplatform.database.minio.secret-key:secret-key}")
	private String secretKey;
	private String minioBaseUrl;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@PostConstruct
	private void init() {
		// load from centralized configuration
		Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());
		SPARK_MASTER = urls.getSpark().getMasterBase();
		this.minioBaseUrl = urls.getMinio().getBase();
	}

	public int executeJob(SparkLaunchJobModel sparkJob) throws IOException {
		SparkLauncher launcher = new SparkLauncher();

		// Job basic config
		launcher.setSparkHome(SPARK_HOME);
		launcher.setAppName(sparkJob.getJobName());
		launcher.setDeployMode(SPARK_DEPLOYMENT_MODE);
		launcher.setMaster(SPARK_MASTER);
		launcher.setMainClass(sparkJob.getJobMainClass());
		launcher.setJavaHome(JAVA_HOME);
		launcher.setAppResource(sparkJob.getJobS3Bucket());
		// adds params to spark main class invocation
		if (sparkJob.getJobParams() != null && sparkJob.getJobParams().size() > 0) {
			launcher.addAppArgs(sparkJob.getJobParams().toArray(new String[sparkJob.getJobParams().size()]));
		}

		// S3 MinIO config
		launcher.setConf("spark.hadoop.fs.s3a.access.key", accessKey);
		launcher.setConf("spark.hadoop.fs.s3a.secret.key", secretKey);
		launcher.setConf("spark.hadoop.fs.s3a.aws.credentials.provider",
				"org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
		launcher.setConf("spark.hadoop.fs.s3a.endpoint", this.minioBaseUrl);
		launcher.setConf("spark.hadoop.fs.s3a.connection.ssl.enabled", "false");
		launcher.setConf("spark.hadoop.fs.s3a.path.style.access", "true");

		// Spark cluster runtime Config
		if (sparkJob.getSparkJobConfig().getDriverExtraClassPath() != null) {
			launcher.setConf(SparkLauncher.DRIVER_EXTRA_CLASSPATH,
					sparkJob.getSparkJobConfig().getDriverExtraClassPath());
		}
		if (sparkJob.getSparkJobConfig().getDriverExtraJavaOpts() != null) {
			launcher.setConf(SparkLauncher.DRIVER_EXTRA_JAVA_OPTIONS,
					sparkJob.getSparkJobConfig().getDriverExtraJavaOpts());
		}
		if (sparkJob.getSparkJobConfig().getDriverExtraLibPath() != null) {
			launcher.setConf(SparkLauncher.DRIVER_EXTRA_LIBRARY_PATH,
					sparkJob.getSparkJobConfig().getDriverExtraLibPath());
		}
		launcher.setConf(SparkLauncher.DRIVER_MEMORY, sparkJob.getSparkJobConfig().getDriverMemory());
		launcher.setConf(SparkLauncher.EXECUTOR_CORES, sparkJob.getSparkJobConfig().getExecutorCores());
		launcher.setConf(SparkLauncher.EXECUTOR_MEMORY, sparkJob.getSparkJobConfig().getExecutorMemory());
		if (sparkJob.getSparkJobConfig().getExecutorExtraClassPath() != null) {
			launcher.setConf(SparkLauncher.EXECUTOR_EXTRA_CLASSPATH,
					sparkJob.getSparkJobConfig().getExecutorExtraClassPath());
		}
		if (sparkJob.getSparkJobConfig().getExecutorExtraJavaOpts() != null) {
			launcher.setConf(SparkLauncher.EXECUTOR_EXTRA_JAVA_OPTIONS,
					sparkJob.getSparkJobConfig().getExecutorExtraJavaOpts());
		}
		if (sparkJob.getSparkJobConfig().getExecutorExtraLibPath() != null) {
			launcher.setConf(SparkLauncher.EXECUTOR_EXTRA_LIBRARY_PATH,
					sparkJob.getSparkJobConfig().getExecutorExtraLibPath());
		}

		// Launches a sub-process that will start the configured Spark application.
		Process proc = launcher.launch();
		//
		InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(proc.getInputStream(),
				"input");
		Thread inputThread = new Thread(inputStreamReaderRunnable, "LogStreamReader input");
		inputThread.start();
		//
		InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(proc.getErrorStream(),
				"error");
		Thread errorThread = new Thread(errorStreamReaderRunnable, "LogStreamReader error");
		errorThread.start();
		//
		log.info("Waiting for finish...");
		int exitCode;
		try {
			exitCode = proc.waitFor();
		} catch (InterruptedException e) {
			log.error("Error while launching Spak job {}", sparkJob.getJobName());
			throw new SparkJobLauncherExecutorException("Error while launching Sparl Job.", e);
		}
		log.info("Finished! Exit code:" + exitCode);
		return exitCode;
	}
}
