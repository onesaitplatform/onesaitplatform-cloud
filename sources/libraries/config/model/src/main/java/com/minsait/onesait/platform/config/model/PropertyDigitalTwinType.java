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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "PROPERTY_DIGITAL_TWIN_TYPE")
public class PropertyDigitalTwinType extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Direction {
		IN, OUT, IN_OUT
	}

	@ManyToOne
	@JoinColumn(name = "TYPE_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private DigitalTwinType typeId;

	@Column(name = "NAME", length = 50, unique = false, nullable = false)
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

	@Column(name = "UNIT", length = 50)
	@Getter
	@Setter
	private String unit;

	@Column(name = "DIRECTION", length = 50)
	@Getter
	private String direction;

	@Column(name = "HREF", length = 500)
	@Getter
	@Setter
	private String href;

	public void setDirection(PropertyDigitalTwinType.Direction direction) {
		this.direction = direction.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PropertyDigitalTwinType))
			return false;
		return getName() != null && getName().equals(((PropertyDigitalTwinType) o).getName());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getName());
	}
}
