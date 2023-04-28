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

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CLIENT_CONNECTION")
@Configurable
public class ClientConnection extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "CLIENT_PLATFORM_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@JsonIgnore
	private ClientPlatform clientPlatform;

	@Column(name = "IDENTIFICATION", length = 255, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "LAST_IP", length = 39)
	@Getter
	@Setter
	private String lastIp;

	@Column(name = "IP_STRICT", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean ipStrict;

	@Column(name = "LAST_CONNECTION")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "MM")
	@Getter
	@Setter
	private Calendar lastConnection;

	@Column(name = "STATIC_IP", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean staticIp;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ClientConnection)) {
			return false;
		}
		final ClientConnection that = (ClientConnection) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification())
				&& getClientPlatform().getIdentification() != null
				&& getClientPlatform().getIdentification().equals(that.getClientPlatform().getIdentification());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (getClientPlatform() == null ? 0 : getClientPlatform().getIdentification().hashCode());
		result = prime * result + (getIdentification() == null ? 0 : getIdentification().hashCode());
		return result;
	}



}
