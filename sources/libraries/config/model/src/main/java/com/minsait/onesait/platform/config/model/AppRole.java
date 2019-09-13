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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_ROLE_TYPE")
@Configurable
public class AppRole extends AuditableEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = -3041037657548992627L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter
	@Getter
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "app", nullable = false)
	@Getter
	@Setter
	private App app;

	@Column(name = "NAME", length = 24, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<AppUser> appUsers = new HashSet<>();

	@JoinTable(name = "app_associated_roles", joinColumns = {
			@JoinColumn(name = "parent_role", referencedColumnName = "id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "child_role", referencedColumnName = "id", nullable = false) })
	@ManyToMany(fetch = FetchType.LAZY)
	@Getter
	@Setter
	private Set<AppRole> childRoles;

}
