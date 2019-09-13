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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.minsait.onesait.platform.config.services.ontology.OntologyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OKPIJob {

	@Autowired
	private OntologyService ontologyConfigService;

	public void execute(JobExecutionContext context) throws IOException {

		final String user = context.getJobDetail().getJobDataMap().getString("userId");

		final String query = context.getJobDetail().getJobDataMap().getString("query");

		final String ontology = context.getJobDetail().getJobDataMap().getString("ontology");

		try {
			ontologyConfigService.executeKPI(user, query, ontology,
					context.getJobDetail().getJobDataMap().getString("postProcess"));
			log.debug("Job KPI ontology for ontology", ontology);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error generating the ontology instance for user:" + user + " and query:" + query, e);

		}
	}

}
