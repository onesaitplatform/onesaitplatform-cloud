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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

@Configurable
@MappedSuperclass
public abstract class ProjectParent extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

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
	
	/*public ProjectParent() {};
	
	public ProjectParent(String id, String identification, String description, User user, User userAllowed, ProjectType projectType, WebProject webProject, Date createAt, Date updateAt) {
		Set<User> suser = new HashSet<User>();
		if(userAllowed != null) {
			suser.add(userAllowed);
		}
		this.setId(id);
		this.setIdentification(identification);
		this.setDescription(description);
		this.setUser(user);
		this.setUsers(suser);
		this.setType(projectType);
		this.setWebProject(webProject);
		this.setCreatedAt(createAt);
		this.setUpdatedAt(updateAt);
		
	}*/
	
}
