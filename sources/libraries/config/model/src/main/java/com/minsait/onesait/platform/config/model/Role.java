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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ROLE_TYPE")
@Configurable
public class Role extends AuditableEntity {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ROLE_USER, ROLE_EDGE_USER, ROLE_DEVELOPER, ROLE_EDGE_DEVELOPER, ROLE_ADMINISTRATOR, ROLE_EDGE_ADMINISTRATOR, ROLE_PLATFORM_ADMIN, ROLE_DATASCIENTIST, ROLE_PARTNER, ROLE_OPERATIONS, ROLE_SYS_ADMIN, ROLE_DEVOPS, ROLE_DATAVIEWER, ROLE_PREVERIFIED_ADMINISTRATOR, ROLE_PREVERIFIED_TENANT_USER, ROLE_COMPLETE_IMPORT;
	}

	@Id
	@Column(name = "ID")
	@Getter
	@Setter
	private String id;

	public void setIdEnum(Role.Type role) {
		id = role.toString();
	}

	@OneToOne(cascade = CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ROLE_PARENT", unique = false, nullable = true, insertable = true, updatable = true)
	@Getter
	@Setter
	private Role roleParent;

	@Column(name = "NAME", length = 24, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

}
