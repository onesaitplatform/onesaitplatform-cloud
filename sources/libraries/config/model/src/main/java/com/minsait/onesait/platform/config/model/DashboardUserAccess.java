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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DASHBOARD_USER_ACCESS", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "DASHBOARD_ID", "USER_ID" }) })
@Configurable
public class DashboardUserAccess extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "DASHBOARD_USER_ACCESS_TYPE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private DashboardUserAccessType dashboardUserAccessType;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DASHBOARD_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Dashboard dashboard;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DashboardUserAccess))
			return false;
		DashboardUserAccess that = (DashboardUserAccess) o;
		return getDashboardUserAccessType() != null
				&& getDashboardUserAccessType().equals(that.getDashboardUserAccessType()) && getDashboard() != null
				&& getDashboard().equals(that.getDashboard()) && getUser() != null && getUser().equals(that.getUser());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getDashboardUserAccessType(), getDashboard(), getUser());
	}

	@Override
	public String toString() {
		String space = "-";
		StringBuilder sb = new StringBuilder();
		sb.append(getDashboard());
		sb.append(space);
		sb.append(getUser());
		sb.append(space);
		sb.append(getDashboardUserAccessType());
		return sb.toString();
	}

}
