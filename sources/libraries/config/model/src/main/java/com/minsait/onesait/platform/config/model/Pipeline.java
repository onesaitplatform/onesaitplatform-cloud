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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pipeline", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
public class Pipeline extends OPResource {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "IDSTREAMSETS", length = 100, nullable = false)
	@Getter
	@Setter
	private String idstreamsets;

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (super.getIdentification().hashCode());
		result = prime * result + (user.hashCode());
		result = prime * result + (idstreamsets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pipeline other = (Pipeline) obj;
		if (super.getIdentification() == null) {
			if (other.getIdentification() != null)
				return false;
		} else if (!super.getIdentification().equals(other.getIdentification()))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (idstreamsets == null) {
			if (other.idstreamsets != null)
				return false;
		} else if (!idstreamsets.equals(other.idstreamsets))
			return false;
		return true;
	}

}
