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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@MappedSuperclass
public abstract class ProjectParent extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "WEB_PROJECT_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private WebProject webProject;

	@Column(name = "IDENTIFICATION", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "DESCRIPTION", nullable = false)
	@Getter
	@Setter
	private String description;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@JsonGetter("user")
	public String getUserJson() {
		return user.getUserId();
	}

	@JsonSetter("user")
	public void setUserJson(String userId) {
		final User u = new User();
		u.setUserId(userId);
		user = u;
	}

	@JsonGetter("webProject")
	public String getWebProjectJson() {
		return webProject == null ? null : webProject.getId();
	}

	@JsonSetter("webProject")
	public void setWebProjectJson(String id) {
		if (StringUtils.hasText(id)) {
			final WebProject w = new WebProject();
			w.setId(id);
			webProject = w;
		}
	}

}
