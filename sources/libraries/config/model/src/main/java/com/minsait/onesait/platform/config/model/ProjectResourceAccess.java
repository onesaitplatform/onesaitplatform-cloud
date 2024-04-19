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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.versioning.VersioningIOService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PROJECT_RESOURCE_ACCESS")
@Configurable
@NoArgsConstructor
public class ProjectResourceAccess extends ProjectResourceAccessParent {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "RESOURCE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private OPResource resource;

	@Fetch(FetchMode.JOIN)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Project project;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "APP_ROLE_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private AppRole appRole;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = true)
	@Getter
	@Setter
	private User user;

	public ProjectResourceAccess(User user, ResourceAccessType access, OPResource resource, Project project,
			AppRole appRole) {
		super(access);
		this.resource = resource;
		this.project = project;
		this.appRole = appRole;
		this.user = user;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final ProjectResourceAccess that = (ProjectResourceAccess) obj;
		if (that != null && getAppRole() != null && that.getAppRole() != null) {
			return getAppRole().getId().equals(that.getAppRole().getId())
					&& getResource().getId().equals(that.getResource().getId())
					&& getProject().getId().equals(that.getProject().getId());
		} else if (that != null && getUser() != null && that.getUser() != null) {
			return getUser().getUserId().equals(that.getUser().getUserId())
					&& getResource().getId().equals(that.getResource().getId())
					&& getProject().getId().equals(that.getProject().getId());
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if (getAppRole() != null) {
			return java.util.Objects.hash(getAppRole().getId(), getResource().getId(), getProject().getId());
		} else if (getUser() != null) {
			return java.util.Objects.hash(getUser().getUserId(), getResource().getId(), getProject().getId());
		} else {
			return super.hashCode();
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (resource != null && resource.getIdentification() != null) {
			sb.append(" Resource : " + resource.getIdentification());
		}
		if (getAccess() != null) {
			sb.append(" Access : " + getAccess().name());
		}
		if (user != null) {
			sb.append(" User : " + user.getUserId());
		}
		if (appRole != null) {
			sb.append(" AppRole : " + appRole.getName());
		}
		if (project != null) {
			sb.append(" Project : " + project.getIdentification());
		}

		return sb.toString();
	}

	@JsonSetter("user")
	public void setUserJson(String userId) {
		if (!StringUtils.isEmpty(userId)) {
			final User u = new User();
			u.setUserId(userId);
			user = u;
		}
	}
	@JsonGetter("user")
	public String getUserJson() {
		return user == null ? null : user.getUserId();
	}
	@JsonSetter("appRole")
	public void setAppRoleJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final AppRole ar = new AppRole();
			ar.setId(id);
			appRole = ar;
		}
	}
	@JsonGetter("appRole")
	public String getAppRoleJson() {
		return appRole == null ? null : appRole.getId();
	}

	@JsonSetter("project")
	public void setProjectJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final Project p = new Project();
			p.setId(id);
			project = p;
		}
	}

	@JsonGetter("project")
	public String getProjectJson() {
		return project == null ? null : project.getId();
	}

	@JsonSetter("resource")
	public void setResourceJson(String id) throws Exception {
		if (!StringUtils.isEmpty(id)) {
			final String[] parts = id.split("@");
			final OPResource p = (OPResource) Class.forName(VersioningIOService.CONFIG_MODEL_CLASS_PREFIX + parts[1]).newInstance();
			p.setId(parts[0]);
			resource = p;
		}
	}

	@JsonGetter("resource")
	public String getResourceJson() {
		return resource == null ? null : resource.getId()+"@"+resource.getClass().getSimpleName();
	}

}
