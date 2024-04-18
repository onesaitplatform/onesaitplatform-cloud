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
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;
import com.minsait.onesait.platform.config.model.listener.EntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "USER")
@Configurable
@EntityListeners({ EntityListener.class, AuditEntityListener.class })
@ToString(exclude = { "projects" }, callSuper = true)
public class User extends UserParent implements Versionable<User> {

	private static final long serialVersionUID = 1L;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "USER_PROJECT", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	@Getter
	@Setter
	@JsonIgnore
	private Set<Project> projects = new HashSet<>();

	@Override
	public String fileName() {
		return getUserId() + ".yaml";
	}

	@Override
	public Object getId() {
		return getUserId();
	}

	@Override
	@JsonIgnore
	public String getUserJson() {
		return getUserId();
	}

	@Override
	public Versionable<User> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<User> u = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (u != null && !projects.isEmpty() && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(Project.class.getSimpleName()))) {
			projects.removeIf(p -> excludedIds.get(Project.class.getSimpleName()).contains(p.getId()));
			u = this;
		}
		return u;
	}

	@Override
	public void setOwnerUserId(String userId) {

	}
}
