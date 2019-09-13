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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "DIGITAL_TWIN_TYPE")
public class DigitalTwinType extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum MainType {
		THING
	}

	@OneToMany(mappedBy = "typeId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<PropertyDigitalTwinType> propertyDigitalTwinTypes = new HashSet<>();

	@OneToMany(mappedBy = "typeId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<ActionsDigitalTwinType> actionDigitalTwinTypes = new HashSet<>();

	@OneToMany(mappedBy = "typeId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<EventsDigitalTwinType> eventDigitalTwinTypes = new HashSet<>();

	@OneToMany(mappedBy = "typeId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<DigitalTwinDevice> digitalTwinDevices = new HashSet<>();

	@Column(name = "NAME", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "TYPE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String type;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@Column(name = "JSON", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String json;

	@Column(name = "LOGIC", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String logic;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	public void setTypeEnum(DigitalTwinType.MainType type) {
		this.type = type.toString();
	}

	@PostLoad
	protected void trim() {
		if (name != null) {
			name = name.replaceAll(" ", "");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DigitalTwinType))
			return false;
		return getName() != null && getName().equals(((DigitalTwinType) o).getId());
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
