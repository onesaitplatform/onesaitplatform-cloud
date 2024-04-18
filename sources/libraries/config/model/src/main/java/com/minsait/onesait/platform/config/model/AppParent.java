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
package com.minsait.onesait.platform.config.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

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
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String userExtraFields;

	@Column(name = "PUBLIC_CLIENT")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@Getter
	@Setter
	private boolean publicClient;

	@JsonSetter("userExtraFields")
	public void setUserExtraFieldsJson(Object node) {
		if (node != null) {
			try {
				userExtraFields = new ObjectMapper().writeValueAsString(node);
			} catch (final Exception e) {
			}
		}
	}

	@JsonGetter("userExtraFields")
	public Object setUserExtraFieldsJson() {
		if (StringUtils.hasText(userExtraFields)) {
			try {
				return new ObjectMapper().readTree(userExtraFields);
			} catch (final Exception e) {
				return userExtraFields;
			}
		}
		return userExtraFields;
	}

	public AppParent() {
	}

	public AppParent(String id) {
		setId(id);
	}

	public AppParent(String id, String identification, String description, String secret, String user_extra_fields,
			int tokenValiditySeconds, AppRole appRole, Date createAt, Date updateAt) {
		setId(id);
		setIdentification(identification);
		setDescription(description);
		setCreatedAt(createAt);
		setUpdatedAt(updateAt);
		setSecret(secret);
		setUserExtraFields(user_extra_fields);
		setTokenValiditySeconds(tokenValiditySeconds);
		final Set<AppRole> appRoles = new HashSet<AppRole>();
		if (appRole != null) {
			appRoles.add(appRole);
		}
	}

}
