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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "CATEGORIZATION_USER", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"CATEGORIZATION_ID", "USER_ID"} )})
public class CategorizationUser extends AuditableEntity {
	
	private static final long serialVersionUID = 1L;
	
	public enum Type {
		OWNER, GUEST;
	}
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUIDGenerator")
	@Column(name = "ID", length = 50)
	@Getter
	@Setter
	private String id;
	
	@JsonIgnore
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CATEGORIZATION_ID", referencedColumnName = "ID", nullable = false, unique = false)
	@Getter
	@Setter
	private Categorization categorization;
	
	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;
	
	@Column(name = "ACTIVE", nullable = false)
	@org.hibernate.annotations.Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean active;
	
	@Column(name = "AUTHORIZATION_TYPE", length = 24)
	@NotNull
	@Getter
	@Setter
	private String authorizationType;

	public void setAuthorizationTypeEnum(CategorizationUser.Type type) {
		this.authorizationType = type.toString();
	}

}
