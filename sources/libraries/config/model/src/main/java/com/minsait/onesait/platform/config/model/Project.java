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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PROJECT")
@Configurable
public class Project extends ProjectParent implements Versionable<Project> {

	private static final long serialVersionUID = 1L;

	public enum ProjectType {
		ENGINE, THINGS, INTELLIGENCE
	}

	@Fetch(FetchMode.JOIN)
	@ManyToMany( mappedBy = "projects", fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<User> users = new HashSet<>();

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private ProjectType type;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<ProjectResourceAccess> projectResourceAccesses = new HashSet<>();

	@OneToOne(mappedBy = "project")
	@Getter
	@Setter
	private App app;

	@JsonSetter("app")
	public void setAppJson(String appid) {
		if (!StringUtils.isEmpty(appid)) {
			final App a = new App();
			a.setId(appid);
			a.setProject(this);
			app = a;
		}
	}

	@JsonSetter("projectResourceAccesses")
	public void setProjectResourceAccessesJson(Set<ProjectResourceAccess> projectResourceAccesses) {
		projectResourceAccesses.forEach(pra -> {
			pra.setProject(this);
			this.projectResourceAccesses.add(pra);
		});
	}

	@JsonSetter("users")
	public void setUsersJson(Set<String> users) {
		users.forEach(s -> {
			final User u = new User();
			u.setUserId(s);
			this.users.add(u);
		});
	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("app", app == null ? null : app.getId());
		final ArrayNode nu = mapper.createArrayNode();
		users.forEach(pra -> nu.add(pra.getUserId()));
		node.set("users", nu);
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final Project that = (Project) obj;
		if(that.getId() != null && getId() != null) {
			return that.getId().equals(getId());
		}
		if(that.getIdentification()!= null && getIdentification() != null) {
			return that.getIdentification().equals(getIdentification());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification(), getId());
	}

	@Override
	public Versionable<Project> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Project> p = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if(p!=null) {
			if(app != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(App.class.getSimpleName()))
					&& excludedIds.get(App.class.getSimpleName()).contains(app.getId())) {
				setApp(null);
				projectResourceAccesses.removeIf(pra -> pra.getAppRole() != null);
				p = this;
			}
			if(!users.isEmpty() && !CollectionUtils.isEmpty(excludedUsers)) {
				users.removeIf(u -> excludedUsers.contains(u.getUserId()));
				projectResourceAccesses.removeIf(pra -> pra.getUser() != null && excludedUsers.contains(pra.getUser().getUserId()));
				p= this;
			}
			if(!projectResourceAccesses.isEmpty() && !CollectionUtils.isEmpty(excludedIds)) {
				projectResourceAccesses.removeIf(pra -> !CollectionUtils.isEmpty(excludedIds.get(pra.getResource().getClass().getSimpleName())) &&
						excludedIds.get(pra.getResource().getClass().getSimpleName()).contains(pra.getResource().getId()));
				p = this;
			}
		}
		return p;
	}
}
