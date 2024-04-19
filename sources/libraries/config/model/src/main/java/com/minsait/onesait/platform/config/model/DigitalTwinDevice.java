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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.listener.EntityListener;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "DIGITAL_TWIN_DEVICE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))

@EntityListeners(EntityListener.class)
public class DigitalTwinDevice extends OPResource {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "TYPE_ID", referencedColumnName = "ID", nullable = false)
	// @OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private DigitalTwinType typeId;

	@Column(name = "URL", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String url;

	@Column(name = "URL_SCHEMA", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String urlSchema;

	@Column(name = "DIGITAL_KEY", length = 512, nullable = false)
	@Getter
	@Setter
	@NotNull
	private String DigitalKey;

	@Column(name = "INTERFACE", length = 512, nullable = false)
	@Getter
	@Setter
	@NotNull
	private String intrface;

	@Column(name = "IP", length = 512)
	@Getter
	@Setter
	private String ip;

	@Column(name = "IPV6", length = 512, nullable = false)
	@org.hibernate.annotations.Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	@NotNull
	private Boolean ipv6;

	@Column(name = "PORT", nullable = false)
	@Getter
	@Setter
	@NotNull
	private Integer port;

	@Column(name = "CONTEXT_PATH", length = 512, nullable = false)
	@Getter
	@Setter
	@NotNull
	private String contextPath;

	@Column(name = "LATITUDE", length = 512)
	@Getter
	@Setter
	private String latitude;

	@Column(name = "LONGITUDE", length = 512)
	@Getter
	@Setter
	private String longitude;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DigitalTwinDevice))
			return false;
		final DigitalTwinDevice that = (DigitalTwinDevice) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

}
