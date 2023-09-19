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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONFIGURATION")
@Configurable
//@JsonPropertyOrder({ "configParseType" })
@EqualsAndHashCode(callSuper = false)
public class Configuration extends OPResource implements Versionable<Configuration> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ENDPOINT_MODULES, TWITTER, MAIL, RTDB, MONITORING, SCHEDULING, GITLAB, RANCHER, OPENSHIFT, DOCKER, NGINX,
		OPEN_PLATFORM, JENKINS, GOOGLE_ANALYTICS, CUSTOM, EXPIRATIONUSERS, SQLENGINE, EXTERNAL_CONFIG, LINEAGE,
		VERSIONING, KAFKA_PROPERTIES, KAFKA_INTERNAL_CLIENT_PROPERTIES, DATACLASS, PRESTO_PROPERTIES, MAPS_PROJECT, BUNDLE_GIT
	}

	@Column(name = "YML_CONFIG", nullable = false)
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@NotNull
	@Lob
	@Getter
	@Setter
	private String ymlConfig;

	@Column(name = "ENVIRONMENT", length = 50)
	@Getter
	@Setter
	private String environment;

	@Column(name = "TYPE", length = 50)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

	// @Transient
	// @Getter
	// private ParseType configParseType;

	public enum ParseType {
		YAML, JSON, OTHER
	}

	// TO-DO Pretify ymlConfig
	// @JsonGetter("configParseType")
	// public Object getConfigParseTypeJson() {
	// getYmlConfigJson();
	// return configParseType;
	// }

	// @JsonGetter("ymlConfig")
	// public Object getYmlConfigJson() {
	// try {
	// configParseType = ymlConfig.startsWith("{") || ymlConfig.startsWith("[") ?
	// ParseType.JSON : ParseType.YAML;
	// return new YAMLMapper().readTree(ymlConfig);
	// } catch (final Exception e) {
	// configParseType = ParseType.OTHER;
	// return ymlConfig;
	// }
	// }

	// @JsonSetter("ymlConfig")
	// public void setJsonJson(Object node) {
	// try {
	// if (node != null) {
	// if (configParseType == null) {
	// configParseType = ParseType.OTHER;
	// }
	// switch (configParseType) {
	// case JSON:
	// ymlConfig = new ObjectMapper().writeValueAsString(node);
	// break;
	// case YAML:
	// ymlConfig = new YAMLMapper().writeValueAsString(node);
	// break;
	// case OTHER:
	// default:
	// ymlConfig = (@NotNull String) node;
	// break;
	// }
	// }
	// } catch (final Exception e) {
	// ymlConfig = (@NotNull String) node;
	// }
	// }

	@Override
	public String fileName() {
		return getIdentification() + "_" + getId() + ".yaml";
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
