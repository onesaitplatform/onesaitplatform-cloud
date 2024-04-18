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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP")
@Configurable
public class AppChildExport extends AppParent {

	/**
	 *
	 */
	private static final long serialVersionUID = 2199595602818161052L;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "PROJECT_ID")
	@Getter
	@Setter
	private ProjectExport project;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private UserExport user;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<AppRoleChildExport> appRoles = new HashSet<>();

	@JoinTable(name = "app_associated", joinColumns = {
			@JoinColumn(name = "parent_app", referencedColumnName = "id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "child_app", referencedColumnName = "id", nullable = false) })
	@ManyToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
	@Getter
	@Setter
	@JsonIgnore
	private Set<AppChildExport> childApps;

	public AppChildExport() {
	};

	public AppChildExport(String id, String identification, String description, UserExport user, String secret,
			String user_extra_fields, int tokenValiditySeconds, AppRoleChildExport appRole, Date createAt,
			Date updateAt) {
		this.setId(id);
		this.setIdentification(identification);
		this.setDescription(description);
		this.setUser(user);
		this.setCreatedAt(createAt);
		this.setUpdatedAt(updateAt);
		this.setSecret(secret);
		this.setUserExtraFields(user_extra_fields);
		this.setTokenValiditySeconds(tokenValiditySeconds);
		Set<AppRoleChildExport> appRoles = new HashSet<AppRoleChildExport>();
		if (appRole != null) {
			appRoles.add(appRole);
		}
		this.setAppRoles(appRoles);

		/*
		 * Set<App> childapps = new HashSet<App>(); if(childApp != null) {
		 * childApps.add(new App(childApp)); } this.setChildApps(childapps);
		 */
	};

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		final AppChildExport that = (AppChildExport) obj;
		if (getIdentification() != null)
			return getIdentification().equals(that.getIdentification());
		return false;
	}

	@Override
	public int hashCode() {

		return java.util.Objects.hash(getIdentification());
	}

}
