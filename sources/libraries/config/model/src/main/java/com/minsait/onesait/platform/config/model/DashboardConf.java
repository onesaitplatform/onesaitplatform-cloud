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
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DASHBOARD_CONF")
@Configurable

public class DashboardConf extends AuditableEntityWithUUID implements Versionable<DashboardConf>{

	private static final long serialVersionUID = 1L;

	@Column(name = "IDENTIFICATION", length = 100, unique = true, nullable = false)
	@Getter
	@Setter
	private String identification;

	@Column(name = "DESCRIPTION", length = 100)
	@Getter
	@Setter
	private String description;

	@Column(name = "MODEL")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String model;

	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;

	@JsonGetter("model")
	public Object getModelJson() {
		try {
			return new ObjectMapper().readTree(model);
		} catch (final Exception e) {
			return model;
		}
	}
	@JsonSetter("model")
	public void setModelJson(ObjectNode node) {
		try {
			model = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			model = null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + "_" + getId() + ".yaml";
	}
	@Override
	@JsonIgnore
	public String getUserJson() {
		return null;
	}
}