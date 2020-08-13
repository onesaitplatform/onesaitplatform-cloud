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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PROJECT_RESOURCE_ACCESS")
@Configurable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResourceAccessExport extends ProjectResourceAccessParent {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "RESOURCE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private OPResource resource;

	@JsonIgnore
	@Fetch(FetchMode.JOIN)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private ProjectExport project;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "APP_ROLE_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private AppRoleExport appRole;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = true)
	@Getter
	@Setter
	private UserExport user;

	@Builder
	public ProjectResourceAccessExport(UserExport user, ResourceAccessType access, OPResource resource,
			ProjectExport project, AppRoleExport appRole) {
		super(access);
		this.resource = resource;
		this.project = project;
		this.appRole = appRole;
		this.user = user;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		final ProjectResourceAccessExport that = (ProjectResourceAccessExport) obj;
		if (that != null && getAppRole() != null && that.getAppRole() != null)
			return (getAppRole().getId().equals(that.getAppRole().getId())
					&& getResource().getId().equals(that.getResource().getId())
					&& getProject().getId().equals(that.getProject().getId()));
		else if (that != null && getUser() != null && that.getUser() != null)
			return (getUser().getUserId().equals(that.getUser().getUserId())
					&& getResource().getId().equals(that.getResource().getId())
					&& getProject().getId().equals(that.getProject().getId()));
		else
			return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if (getAppRole() != null)
			return java.util.Objects.hash(getAppRole().getId(), getResource().getId(), getProject().getId());

		else if (getUser() != null)
			return java.util.Objects.hash(getUser().getUserId(), getResource().getId(), getProject().getId());
		else
			return super.hashCode();
	}

}
