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
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_ROLE_TYPE")
@Configurable
public class AppRole extends AppRoleParent{

	/**
	 *
	 */
	private static final long serialVersionUID = -3041037657548992627L;

	@ManyToOne
	@JoinColumn(name = "app", nullable = false)
	@Getter
	@Setter
	@JsonIgnore
	private App app;

	@JoinTable(name = "app_associated_roles", joinColumns = {
			@JoinColumn(name = "parent_role", referencedColumnName = "id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "child_role", referencedColumnName = "id", nullable = false) })
	@ManyToMany(fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<AppRole> childRoles = new HashSet<>();

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<AppUser> appUsers = new HashSet<>();

	@JsonSetter("appUsers")
	public void setAppUsersJson(Set<ObjectNode> appUsers) {
		appUsers.forEach(au -> {
			final AppUser appUser = new AppUser();
			appUser.setId(au.get("id").asText());
			final User u = new User();
			u.setUserId(au.get("user").asText());
			appUser.setUser(u);
			appUser.setRole(this);
			this.appUsers.add(appUser);
		});
	}


	@JsonGetter("childRoles")
	public Object getChidlRolesJson() {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode n = mapper.createArrayNode();
		childRoles.forEach(a -> {
			n.add(a.getId());
		});
		return n;
	}

	//TO-DO version childRole??
	@JsonSetter("childRoles")
	public void setChildRolesJson(Set<String> ids) {
		ids.forEach(i ->{
			final AppRole ar = new AppRole();
			ar.setId(i);
			childRoles.add(ar);
		});
	}



}
