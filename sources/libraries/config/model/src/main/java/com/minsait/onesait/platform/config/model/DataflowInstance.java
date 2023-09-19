/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "dataflow_instances", uniqueConstraints = @UniqueConstraint(name = "instance_ident_UQ", columnNames = {
		"IDENTIFICATION" }))
public class DataflowInstance implements Serializable, Versionable<DataflowInstance> {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUIDGenerator")
	@Column(name = "ID", length = 50)
	@Getter
	@Setter
	private String id;

	@Column(name = "IDENTIFICATION", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@OneToOne
	@Getter
	@Setter
	private User user;

	@Column(name = "URL", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String url;

	@Column(name = "ADMIN_CREDENTIALS", length = 50, nullable = false)
	@Getter
	@Setter
	private String adminCredentials;

	@Column(name = "USER_CREDENTIALS", length = 50, nullable = false)
	@Getter
	@Setter
	private String userCredentials;

	@Column(name = "GUEST_CREDENTIALS", length = 50, nullable = false)
	@Getter
	@Setter
	private String guestCredentials;

	@Column(name = "IS_DEFAULT", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean defaultInstance;

	@JsonGetter("user")
	public String getUserIdentification() {
		if (user != null) {
			return user.getUserId();
		} else {
			return null;
		}
	}

	public void setUser(User user) {
		this.user = user;
	}

	@JsonSetter("user")
	public void setUserByUserId(String userId) {
		if (StringUtils.hasText(userId)) {
			final User newUser = new User();
			newUser.setUserId(userId);
			user = newUser;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final DataflowInstance that = (DataflowInstance) obj;
		if (getIdentification() != null) {
			return getIdentification().equals(that.getIdentification());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@JsonIgnore
	@Override
	public String getUserJson() {
		return getUserIdentification();
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
