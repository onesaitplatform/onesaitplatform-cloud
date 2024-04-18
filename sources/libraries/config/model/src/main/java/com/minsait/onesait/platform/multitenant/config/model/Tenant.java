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
package com.minsait.onesait.platform.multitenant.config.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TENANT", uniqueConstraints = @UniqueConstraint(columnNames = { "NAME" }))
@Configurable
@Getter
@Setter
public class Tenant extends AuditableEntityWithUUID {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "NAME", length = 50, nullable = false)
	private String name;

	// TO-DO nivel intermedio Vertical-Tenant

	@OneToMany(mappedBy = "tenant", fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE,
			CascadeType.PERSIST }, orphanRemoval = true)
	private Set<MasterUser> users = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "tenants")
	private Set<Vertical> verticals = new HashSet<>();

}
