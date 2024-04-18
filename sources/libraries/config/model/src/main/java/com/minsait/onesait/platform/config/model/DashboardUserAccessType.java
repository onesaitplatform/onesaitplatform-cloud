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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "DASHBOARD_USER_ACCES_TYPE")
public class DashboardUserAccessType extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Type {
		VIEW, EDIT;
	}

	@JsonIgnore
	@OneToMany(mappedBy = "dashboardUserAccessType", fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<DashboardUserAccess> dashboardUserAccess;

	@Column(name = "NAME", length = 24, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	public void setNameEnum(DashboardUserAccessType.Type type) {
		this.name = type.toString();
	}

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DashboardUserAccessType))
			return false;
		return getName() != null && getName().equals(((DashboardUserAccessType) o).getName());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
