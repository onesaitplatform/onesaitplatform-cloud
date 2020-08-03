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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CLIENT_PLATFORM_INSTANCE", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "CLIENT_PLATFORM_ID", "IDENTIFICATION" }) })
@Configurable
public class ClientPlatformInstance extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public static enum StatusType {
		OK, ERROR, WARNING, COMPLETED, EXECUTED, UP, DOWN, CRITICAL
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CLIENT_PLATFORM_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private ClientPlatform clientPlatform;

	@Column(name = "IDENTIFICATION", length = 255, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "CONNECTED", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean connected;

	@Column(name = "STATUS", length = 255, unique = false, nullable = true)
	@Getter
	@Setter
	private String status;

	public void setAccesEnum(ClientPlatformInstance.StatusType status) {
		this.status = status.toString();
	}

	@Column(name = "JSON_ACTIONS", nullable = true)
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String jsonActions;

	@Column(name = "PROTOCOL", nullable = true)
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String protocol;

	@Column(name = "SESSION_KEY", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String sessionKey;

	@Column(name = "disabled", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean disabled;

	@Column(name = "tags")
	@Getter
	@Setter
	private String tags;

	@Column(name = "location")
	@Getter
	@Setter
	private double[] location;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClientPlatformInstance))
			return false;
		final ClientPlatformInstance that = (ClientPlatformInstance) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification())
				&& getClientPlatform() != null && getClientPlatform().equals(that.getClientPlatform());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification(), getClientPlatform());
	}

}
