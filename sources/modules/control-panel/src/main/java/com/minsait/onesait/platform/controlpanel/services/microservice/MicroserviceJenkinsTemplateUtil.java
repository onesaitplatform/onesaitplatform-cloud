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
package com.minsait.onesait.platform.controlpanel.services.microservice;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MicroserviceJenkinsTemplateUtil {

	@Value("classpath:static/microservices/templates/jenkins/pipeline.xml")
	private Resource pipeline;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-ssdd.xml")
	private Resource pipelineSSDD;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-nb.xml")
	private Resource notebookPipeline;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-nb-ssdd.xml")
	private Resource notebookPipelineSSDD;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-ml.xml")
	private Resource mlPipeline;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-ml-ssdd.xml")
	private Resource mlPipelineSSDD;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-dt.xml")
	private Resource digitalTwinPipeline;

	@Value("classpath:static/microservices/templates/jenkins/mlflow-model.xml")
	private Resource mlflowModel;

	@Value("classpath:static/microservices/templates/jenkins/mlflow-model-ssdd.xml")
	private Resource mlflowModelSSDD;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-dt-ssdd.xml")
	private Resource digitalTwinPipelineSSDD;

	@Value("classpath:static/microservices/templates/jenkins/pipeline-aa-ssdd.xml")
	private Resource pipelineArchetypeSSDD;

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	private static final String MICROSERVICE_NAME = "MICROSERVICE_NAME";
	private static final String MODEL_RUN_ID = "MODEL_RUN_ID";
	private static final String MLFLOW_TRACKING_URI = "MLFLOW_TRACKING_URI";
	private static final String SOURCES_PATH = "SOURCES_PATH";
	private static final String GIT_REPOSITORY = "GIT_REPOSITORY";
	private static final String DOCKER_PATH = "DOCKER_PATH";
	private static final String JENKINS_SSDD_URL = "jenkins-ssdd";
	private static final String JENKINS_SSDD_URL_2 = "jenkins.devops";

	public String compileXMLTemplate(MSConfig config, MicroserviceDTO microservice) throws IOException {

		String xml;
		final String jenkinsUrl = microservice.getJenkinsConfiguration().getJenkinsUrl();
		final boolean isSSDDJenkins = jenkinsUrl.contains(JENKINS_SSDD_URL) || jenkinsUrl.contains(JENKINS_SSDD_URL_2);

		switch (microservice.getTemplate()) {
		case ML_MODEL_ARCHETYPE:
			xml = isSSDDJenkins ? IOUtils.toString(mlPipelineSSDD.getInputStream())
					: IOUtils.toString(mlPipeline.getInputStream());
			break;
		case NOTEBOOK_ARCHETYPE:
			xml = isSSDDJenkins ? IOUtils.toString(notebookPipelineSSDD.getInputStream())
					: IOUtils.toString(notebookPipeline.getInputStream());
			break;

		case ARCHITECTURE_ARCHETYPE:
			xml = isSSDDJenkins ? IOUtils.toString(pipelineArchetypeSSDD.getInputStream())
					: IOUtils.toString(pipeline.getInputStream());
			break;

		case MLFLOW_MODEL:
			xml = isSSDDJenkins ? IOUtils.toString(mlflowModelSSDD.getInputStream())
					: IOUtils.toString(mlflowModel.getInputStream());
			break;

		case DIGITAL_TWIN:
		case IOT_CLIENT_ARCHETYPE:
		default:
			xml = isSSDDJenkins ? IOUtils.toString(pipelineSSDD.getInputStream())
					: IOUtils.toString(pipeline.getInputStream());
			break;
		}

		final String microserviceName = microservice.getName();
		final String gitlabRepo = microservice.getGitlab();

		final HashMap<String, Object> scopes = new HashMap<>();
		if (microservice.getTemplate().equals(TemplateType.NOTEBOOK_ARCHETYPE)) {
			scopes.put(MICROSERVICE_NAME, microserviceName.toLowerCase());
			scopes.put(SOURCES_PATH, "zeppelin-spark");
			scopes.put(GIT_REPOSITORY, gitlabRepo);
		}else if(microservice.getTemplate().equals(TemplateType.MLFLOW_MODEL)){
			scopes.put(MICROSERVICE_NAME, microserviceName.toLowerCase());
			scopes.put(MODEL_RUN_ID, microservice.getConfig().getModelRunId());
			final String baseURL = integrationResourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE);
			scopes.put(MLFLOW_TRACKING_URI, baseURL.endsWith("/") ? baseURL: baseURL+"/");
		} else {
			scopes.put(MICROSERVICE_NAME, microserviceName.toLowerCase());
			scopes.put(SOURCES_PATH, config.getSources());
			scopes.put(DOCKER_PATH, config.getDocker());
			scopes.put(GIT_REPOSITORY, gitlabRepo);
		}

		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(xml), microserviceName.toLowerCase());
		mustache.execute(writer, scopes);
		log.info("compiled jenkings template for {}", microservice.getTemplate());

		return writer.toString();

	}

}
