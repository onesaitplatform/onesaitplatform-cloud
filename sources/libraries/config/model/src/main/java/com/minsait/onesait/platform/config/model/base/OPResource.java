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
package com.minsait.onesait.platform.config.model.base;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonIgnoreProperties(value = { "createdAt", "updatedAt" }, allowGetters = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class OPResource extends AuditableEntity {

	public enum Resources {
		API, CLIENTPLATFORM, DIGITALTWINDEVICE, DASHBOARD, FLOWDOMAIN, GADGET, NOTEBOOK, ONTOLOGY, DATAFLOW,
		GADGETDATASOURCE
	}

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

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		final OPResource that = (OPResource) obj;
		if (that instanceof Api && (getIdentification() != null && ((Api) this).getNumversion() != null)) {
			return (getIdentification().equals(that.getIdentification()) 
					&& ((Api) this).getNumversion().equals(((Api) that).getNumversion()));
		} 
		if (getIdentification() != null)
			return getIdentification().equals(that.getIdentification());
		return false;
	}

	
	@Override
	public int hashCode() {
		if (this instanceof Api) {
			return java.util.Objects.hash(this.getIdentification(), ((Api) this).getNumversion());
		}
		
		return java.util.Objects.hash(getIdentification());
	}	
}
