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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CLIENT_PLATFORM_ONTOLOGY", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "CLIENT_PLATFORM_ID", "ONTOLOGY_ID" }) })
@Configurable
public class ClientPlatformOntology extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@Column(name = "ACCESS")
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private AccessType access;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CLIENT_PLATFORM_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private ClientPlatform clientPlatform;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Ontology ontology;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClientPlatformOntology))
			return false;
		final ClientPlatformOntology that = (ClientPlatformOntology) o;
		return getClientPlatform() != null && that.getClientPlatform() != null
				&& getClientPlatform().getIdentification() != null
				&& that.getClientPlatform().getIdentification() != null
				&& getClientPlatform().getIdentification().equals(that.getClientPlatform().getIdentification())
				&& null != getOntology() && null != that.getOntology() && null != getOntology().getIdentification()
				&& null != that.getOntology().getIdentification()
				&& getOntology().getIdentification().equals(that.getOntology().getIdentification());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getClientPlatform() == null || getClientPlatform().getIdentification() == null) ? 0
				: getClientPlatform().getIdentification().hashCode());
		result = prime * result + ((getOntology() == null || getOntology().getIdentification() == null) ? 0
				: getOntology().getIdentification().hashCode());
		return result;
	}

}
