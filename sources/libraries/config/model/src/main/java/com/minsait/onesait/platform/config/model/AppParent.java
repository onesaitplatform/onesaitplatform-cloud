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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;


@Configurable
@MappedSuperclass
public abstract class AppParent extends AuditableEntityWithUUID {

	/**
	 *
	 */
	private static final long serialVersionUID = 7199595602818161052L;
	
	@Column(name = "IDENTIFICATION", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "ID", nullable = false)
	@Getter
	@Setter
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

	@Column(name = "TOKEN_VALIDITY_SECONDS")
	@Getter
	@Setter
	private Integer tokenValiditySeconds;

	@Column(name = "SECRET", length = 128)
	@Getter
	@Setter
	private String secret;

	@Column(name = "user_extra_fields", nullable = true)
	@Lob
	@JsonRawValue
	@Getter
	@Setter
	private String userExtraFields;
	
	public AppParent() {};
	
	public AppParent(String id) {
		this.setId(id);
	};
	
	public AppParent(String id, String identification, String description, User user, String secret, String user_extra_fields, int tokenValiditySeconds, AppRole appRole, Date createAt, Date updateAt) {
		this.setId(id);
		this.setIdentification(identification);
		this.setDescription(description);
		this.setUser(user);
		this.setCreatedAt(createAt);
		this.setUpdatedAt(updateAt);
		this.setSecret(secret);
		this.setUserExtraFields(user_extra_fields);
		this.setTokenValiditySeconds(tokenValiditySeconds);
		Set<AppRole> appRoles = new HashSet<AppRole>();
		if(appRole != null) {
			appRoles.add(appRole);
		}
		//this.setAppRoles(appRoles);
		
		/*Set<App> childapps = new HashSet<App>();
		if(childApp != null) {
			childApps.add(new App(childApp));
		}
		this.setChildApps(childapps);*/
	};
}
