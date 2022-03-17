/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
import javax.persistence.EntityListeners;
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
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "APP")
@Configurable
@EntityListeners(AuditEntityListener.class)
@ToString
public class App extends AppParent {

	/**
	 *
	 */
	private static final long serialVersionUID = 2199595602818161052L;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "PROJECT_ID")
	@Getter
	@Setter
	private Project project;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<AppRole> appRoles = new HashSet<>();

	@JoinTable(name = "app_associated", joinColumns = {
			@JoinColumn(name = "parent_app", referencedColumnName = "id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "child_app", referencedColumnName = "id", nullable = false) })
	@ManyToMany(fetch = FetchType.EAGER)
	@Getter
	@Setter
	@JsonIgnore
	private Set<AppChild> childApps;

	public App() {
	};

	public App(String id, String identification, String description, User user, String secret, String user_extra_fields,
			int tokenValiditySeconds, AppRole appRole, Date createAt, Date updateAt) {
		setId(id);
		setIdentification(identification);
		setDescription(description);
		setUser(user);
		setCreatedAt(createAt);
		setUpdatedAt(updateAt);
		setSecret(secret);
		setUserExtraFields(user_extra_fields);
		setTokenValiditySeconds(tokenValiditySeconds);
		final Set<AppRole> appRoles = new HashSet<AppRole>();
		if (appRole != null) {
			appRoles.add(appRole);
		}
		setAppRoles(appRoles);

		/*
		 * Set<App> childapps = new HashSet<App>(); if(childApp != null) {
		 * childApps.add(new App(childApp)); } this.setChildApps(childapps);
		 */
	};

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final App that = (App) obj;
		if (getIdentification() != null) {
			return getIdentification().equals(that.getIdentification());
		}
		return false;
	}

	@Override
	public int hashCode() {

		return java.util.Objects.hash(getIdentification());
	}

}