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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "APP")
@Configurable
@EntityListeners(AuditEntityListener.class)
@ToString
public class App extends AppParent implements Versionable<App> {

	/**
	 *
	 */
	private static final long serialVersionUID = 2199595602818161052L;

	@OneToOne
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
	private Set<AppRole> appRoles = new HashSet<>();

	@JoinTable(name = "app_associated", joinColumns = {
			@JoinColumn(name = "parent_app", referencedColumnName = "id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "child_app", referencedColumnName = "id", nullable = false) })
	@ManyToMany(fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<App> childApps = new HashSet<>();

	public App() {
	}

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
	}

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

	@JsonSetter("user")
	public void setUserJson(String userId) {
		if (StringUtils.hasText(userId)) {
			final User u = new User();
			u.setUserId(userId);
			user = u;
		}
	}

	@Override
	@JsonGetter("user")
	public String getUserJson() {
		return user == null ? null : user.getUserId();
	}

	@JsonSetter("project")
	public void setProjectJson(String projectId) {
		// if (StringUtils.hasText(projectId)) {
		// final Project p = new Project();
		// p.setId(projectId);
		// p.setApp(this);
		// project = p;
		// }
	}

	@JsonGetter("project")
	public String getProjectJson() {
		return project == null ? null : project.getId();
	}

	@JsonSetter("appRoles")
	public void setAppRolesJson(Set<AppRole> appRoles) {
		appRoles.forEach(ar -> {
			ar.setApp(this);
			this.appRoles.add(ar);
		});
	}

	@JsonGetter("childApps")
	public Object getChidlAppsJson() {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode n = mapper.createArrayNode();
		childApps.forEach(a -> {
			n.add(a.getId());
		});
		return n;
	}

	// TO-DO version childApp???
	@JsonSetter("childApps")
	public void setChildAppsJson(Set<String> ids) {
		ids.forEach(i -> {
			final App ac = new App();
			ac.setId(i);
			childApps.add(ac);
		});
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<App> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<App> app = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (app != null) {
			if (project != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(Project.class.getSimpleName()))
					&& excludedIds.get(Project.class.getSimpleName()).contains(project.getId())) {
				setProject(null);
				app = this;
			}
			if (!appRoles.isEmpty() && !CollectionUtils.isEmpty(excludedUsers)) {
				appRoles.forEach(ar -> {
					ar.getAppUsers().removeIf(au -> excludedUsers.contains(au.getUser().getUserId()));
					ar.getChildRoles()
							.removeIf(r -> !CollectionUtils.isEmpty(excludedIds.get(App.class.getSimpleName()))
									&& excludedIds.get(App.class.getSimpleName()).contains(r.getApp().getId()));
				});

				app = this;
			}
			if (!childApps.isEmpty() && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(App.class.getSimpleName()))) {
				childApps.removeIf(a -> excludedIds.get(App.class.getSimpleName()).contains(a.getId()));
				app = this;
			}
		}
		return app;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
