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
package com.minsait.onesait.platform.config.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PROJECT")
@Configurable
public class Project extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum ProjectType {
		ENGINE, THINGS, INTELLIGENCE
	}

	@Fetch(FetchMode.JOIN)
	@ManyToMany(cascade = { CascadeType.ALL }, mappedBy = "projects", fetch = FetchType.LAZY)
	@Getter
	@Setter
	@JsonIgnore
	private Set<User> users = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "WEB_PROJECT_ID", referencedColumnName = "ID")
	@JsonIgnore
	@Getter
	@Setter
	private WebProject webProject;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private ProjectType type;

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

	@OneToOne(mappedBy = "project")
	@Getter
	@Setter
	private App app;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<ProjectResourceAccess> projectResourceAccesses = new HashSet<>();

}
