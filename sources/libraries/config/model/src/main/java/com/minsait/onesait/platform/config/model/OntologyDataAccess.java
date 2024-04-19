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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ONTOLOGY_DATA_ACCESS")
@Configurable
public class OntologyDataAccess extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
	@Getter
	@Setter
	private User user;
	
	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "APP_ROLE", referencedColumnName = "ID")
	@Getter
	@Setter
	private AppRole appRole;
	
	@Column(name = "RULE", length = 256)
	@Getter
	@Setter
	private String rule;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OntologyDataAccess))
			return false;
		final OntologyDataAccess that = (OntologyDataAccess) o;
		return (((getUser() != null && that.getUser() != null && getUser().equals(that.getUser())) || 
				 (getAppRole() != null && that.getAppRole() != null && getAppRole().equals(that.getAppRole()))) &&
				(getOntology() !=null && that.getOntology() != null && getOntology().equals(that.getOntology())) &&
				(getRule() != null && that.getRule() != null && getRule().equals(that.getRule())));
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getId());
	}

	@Override
	public String toString() {
		final String space = "-";
		final StringBuilder sb = new StringBuilder();
		sb.append(getOntology());
		sb.append(space);
		if (getUser()!=null) {
			sb.append(getUser().getUserId());
		}
		if (getAppRole()!=null) {
			sb.append(getAppRole().getName());
		}
		sb.append(space);
		sb.append(getRule());
		return sb.toString();
	}

}
