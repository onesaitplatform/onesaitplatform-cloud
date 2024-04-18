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
package com.minsait.onesait.platform.spark.launcher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.spark.dto.SparkLaunchJobModel;
import com.minsait.onesait.platform.spark.launcher.executor.SparkJobLauncherExecutor;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/")
@Slf4j
public class SparkLauncherController {
	@Autowired
	private SparkJobLauncherExecutor sparlLauncherExecutor;

	@PostMapping(value = "/launch")
	public ResponseEntity<?>  insert(@RequestBody SparkLaunchJobModel model) {
		final long dStart = System.currentTimeMillis();
		String state = "successfully";
		if (log.isDebugEnabled()) {
			log.debug("Launching Spark Job {}.", model.getJobName());
		}
		try {
			sparlLauncherExecutor.executeJob(model);
		} catch (final Exception e) {
			log.error("Error launching Spark Job {}. Cause= {}, Mesasge= {}", e.getCause(), e.getMessage());
			state = "with errors";
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Spark Job {} submited {} in {}.", model.getJobName(), state, dEnd - dStart);
		}

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

}
