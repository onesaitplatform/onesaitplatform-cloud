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
package com.minsait.onesait.platform.multitenant.config.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MASTER_USER")
@Configurable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasterUserLazy extends MasterUserParent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "TENANT_ID", referencedColumnName = "ID", nullable = false)
	private TenantLazy tenant;

	@Builder
	public MasterUserLazy(@NotNull @Size(min = 4, message = "user.userid.error") String userId,
			@NotNull String password,
			@NotNull @Pattern(regexp = "^[-A-Za-z0-9~!$%^&*_=+}{\\'?]+(\\.[-a-z0-9~!$%^&*_=+}{\\'?]+)*@([a-z0-9_][-a-z0-9_]*(\\.[-a-z0-9_]+)*\\.([a-z]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,5})?$", message = "user.create.empty.email") String email,
			@NotNull @Size(min = 4, message = "user.fullname.error") String fullName, String extraFields,
			@NotNull boolean active, Integer failedAtemps, Date lastLogin, Date lastPswdUpdate, Date resetPass,
			TenantLazy tenant) {

		super(userId, password, email, fullName, extraFields, active, failedAtemps, lastLogin, lastPswdUpdate,
				resetPass);
		this.tenant = tenant;

	}

}
