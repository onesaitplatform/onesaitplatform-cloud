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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.converters.CaasConfigurationConverter;
import com.minsait.onesait.platform.config.converters.GitlabConfigurationConverter;
import com.minsait.onesait.platform.config.converters.JenkinsConfigurationConverter;
import com.minsait.onesait.platform.config.converters.RancherConfigurationConverter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.git.GitlabConfiguration;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MICROSERVICE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@Getter
@Setter
public class Microservice extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum TemplateType {
		DIGITAL_TWIN, ARCHITECTURE_ARCHETYPE, IMPORT_FROM_GIT, IMPORT_FROM_ZIP, MLFLOW_MODEL
	}

	public enum CaaS {
		RANCHER, OPENSHIFT, RANCHER_2, KUBERNETES
	}

	@Getter
	@Setter
	@Column(name = "PORT", nullable = false)
	private Integer port;

	@Getter
	@Setter
	@Column(name = "CONTEXT_PATH", nullable = false)
	private String contextPath;

	@Column(name = "JENKINS_CONFIGURATION")
	@Convert(converter = JenkinsConfigurationConverter.class)
	private JenkinsConfiguration jenkinsConfiguration;

	@Column(name = "JENKINS_XML")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String jenkinsXML;

	@Column(name = "JENKINS_JOB_NAME")
	private String jobName;

	@Column(name = "JENKINS_JOB_URL")
	private String jobUrl;

	@Column(name = "GITLAB_CONFIGURATION")
	@Convert(converter = GitlabConfigurationConverter.class)
	private GitlabConfiguration gitlabConfiguration;

	@Column(name = "GITLAB_REPOSITORY")
	private String gitlabRepository;

	@Column(name = "CAAS")
	@Enumerated(EnumType.STRING)
	private CaaS caas;

	@Column(name = "RANCHER_CONFIGURATION")
	@Convert(converter = RancherConfigurationConverter.class)
	private RancherConfiguration rancherConfiguration;

	@Column(name = "OPENSHIFT_CONFIGURATION")
	@Convert(converter = CaasConfigurationConverter.class)
	private CaasConfiguration caasConfiguration;

	@Column(name = "RANCHER_ENVIRONMENT")
	private String rancherEnv;

	@Column(name = "RANCHER_STACK")
	private String rancherStack;

	@Column(name = "OPENSHIFT_NAMESPACE")
	private String openshiftNamespace;

	@Column(name = "OPENSHIFT_DEPLOYMENT_URL")
	private String openshiftDeploymentUrl;

	@Column(name = "JENKINS_QUEUE_ID")
	private Integer jenkinsQueueId;

	@Column(name = "DOCKER_IMAGE")
	private String dockerImage;

	@Column(name = "TEMPLATE_TYPE")
	private String templateType;

	@Column(name = "ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	private boolean active;

	@Column(name = "STRIP_ROUTE_PREFIX", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	private boolean stripRoutePrefix;

	public Object getCaaSConfiguration() {
		switch (caas) {
		case RANCHER:
			return rancherConfiguration;
		case OPENSHIFT:
		case RANCHER_2:
		case KUBERNETES:
		default:
			return caasConfiguration;
		}
	}

	public CaasConfiguration getOpenshiftConfiguration() {
		return caasConfiguration;
	}

	public void setOpenshiftConfiguration(CaasConfiguration caasConfiguration) {
		this.caasConfiguration = caasConfiguration;
	}

	public String getStackOrNamespace() {
		switch (caas) {
		case RANCHER:
			return rancherStack;
		case OPENSHIFT:
		case RANCHER_2:
		case KUBERNETES:
		default:
			return openshiftNamespace;
		}
	}
}
