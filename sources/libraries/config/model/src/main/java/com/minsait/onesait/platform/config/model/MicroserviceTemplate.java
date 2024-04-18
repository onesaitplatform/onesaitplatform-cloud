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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "MICROSERVICE_TEMPLATE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class MicroserviceTemplate extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum Language {
		Java8, Java17, Python, ML_MODEL_ARCHETYPE, IOT_CLIENT_ARCHETYPE, NOTEBOOK_ARCHETYPE
	}


	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@NotNull
	@Setter
	private String description;
	
	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;
	
	@Column(name = "GIT_REPOSITORY", nullable = false)
	@Getter
	@NotNull
	@Setter
	private String gitRepository;
	
	@Column(name = "GIT_USER")
	@NotNull
	@Getter
	@Setter
	private String gitUser;

	@Column(name = "GIT_PASSWORD")
	@NotNull
	@Setter
	@Getter
	private String gitPassword;

	@Getter
	@Setter
	@NotNull
	@Column(name = "GIT_BRANCH", nullable = false)
	private String gitBranch;
	
	@Getter
	@Setter
	@NotNull
	@Column(name = "RELATIVE_PATH", nullable = false)
	private String relativePath;
	
	@Getter
	@Setter
	@Column(name = "DOCKER_RELATIVE_PATH")
	private String dockerRelativePath;

	@Column(name = "LANGUAGE", nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter
	@NotNull
	@Setter
	private Language language;
	
	@Getter
	@Setter
	@Column(name = "GRAALVM")
	private boolean graalvm;

}
