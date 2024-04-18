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
package com.minsait.onesait.platform.multitenant.config.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.beans.factory.annotation.Configurable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "VERTICAL", uniqueConstraints = { @UniqueConstraint(columnNames = { "SCHEMA_DB" }),
		@UniqueConstraint(columnNames = { "NAME" }) })
@Configurable
@Getter
@Setter
public class VerticalLazy extends VerticalParent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "VERTICAL_TENANT", joinColumns = { @JoinColumn(name = "VERTICAL_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "TENANT_ID") }, uniqueConstraints = @UniqueConstraint(columnNames = { "VERTICAL_ID",
					"TENANT_ID" }))
	private Set<TenantLazy> tenants = new HashSet<>();

}
