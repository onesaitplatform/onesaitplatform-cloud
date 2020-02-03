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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CLIENT_PLATFORM", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
@Configurable
public class ClientPlatform extends OPResource {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "clientPlatform", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<ClientPlatformOntology> clientPlatformOntologies = new HashSet<>();

	@OneToMany(mappedBy = "clientPlatform", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<Token> tokens = new HashSet<>();

	@OneToMany(mappedBy = "clientPlatform", fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<ClientConnection> clientConnections;

	@OneToMany(mappedBy = "clientPlatform", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<ClientPlatformInstance> devices = new HashSet<>();

	@Column(name = "ENCRYPTION_KEY", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String encryptionKey;

	@Column(name = "METADATA")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String metadata;

	@Column(name = "DESCRIPTION", length = 256)
	@Getter
	@Setter
	private String description;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClientPlatform))
			return false;
		final ClientPlatform that = (ClientPlatform) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification()) && getUser() != null
				&& getUser().getUserId().equals(that.getUser().getUserId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUser() == null) ? 0 : getUser().getUserId().hashCode());
		result = prime * result + ((getIdentification() == null) ? 0 : getIdentification().hashCode());
		return result;

	}

}
