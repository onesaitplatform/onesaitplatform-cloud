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
package com.minsait.onesait.platform.controlpanel.services.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.queue.QueueItem;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.repository.MicroserviceRepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class JenkinsBuildWatcher implements Runnable {

	@Autowired
	private MicroserviceRepository microserviceRepository;

	@Setter
	private JenkinsClient client;
	@Setter
	private int jenkinsQueueId;
	@Setter
	private Microservice microservice;

	@Override
	public void run() {
		try {
			QueueItem queueItem = client.api().queueApi().queueItem(jenkinsQueueId);
			while (true) {
				if (queueItem.cancelled() || queueItem.executable() != null) {
					if (queueItem.cancelled()) {
						log.warn("Build was cancelled");
					} else {
						if (log.isDebugEnabled()) {
							log.debug("Build is executing with build number: {}", queueItem.executable().number());
						}						
					}
					break;
				}

				Thread.sleep(10000);
				queueItem = client.api().queueApi().queueItem(jenkinsQueueId);
			}

			// Get the build info of the queue item being built and poll until it is done
			BuildInfo buildInfo = client.api().jobsApi().buildInfo(null, microservice.getJobName(),
					queueItem.executable().number());
			while (buildInfo.result() == null) {
				Thread.sleep(10000);
				buildInfo = client.api().jobsApi().buildInfo(null, microservice.getJobName(),
						queueItem.executable().number());
			}
			if (log.isDebugEnabled()) {
				log.debug("Build status: {}", buildInfo.result());
			}			
		} catch (final Exception e) {
			log.error("Something happened, could not wait until pipeline", e);

		} finally {
			microserviceRepository.updateMicroserviceSetJenkinsQueueIdNullByMicroserviceId(microservice.getId());
		}

	}

}
