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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_USER_ACCES_TYPE")
public class OntologyUserAccessType extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ALL, QUERY, INSERT;
	}

	@JsonIgnore
	@OneToMany(mappedBy = "ontologyUserAccessType", fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<OntologyUserAccess> ontologyUserAccess;

	@Column(name = "NAME", length = 24, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	public void setNameEnum(OntologyUserAccessType.Type type) {
		this.name = type.toString();
	}

	@Column(name = "DESCRIPTION", length = 255)
	@Getter
	@Setter
	private String description;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OntologyUserAccessType))
			return false;
		return getName() != null && getName().equals(((OntologyUserAccessType) o).getName());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
