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
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MIGRATION_DATA")
public class MigrationData extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum DataType {
		EXPORT, IMPORT, QUERY;
	}

	public enum Status {
		FINISHED, IN_PROGRESS, NO_STATUS, ERROR;
	}

	@Getter
	@Setter
	@Column(name = "DESCRIPTION")
	private String description;

	@Getter
	@Setter
	@Column(name = "DATA_TYPE")
	private DataType type;

	@Getter
	@Setter
	@Column(name = "STATUS")
	private Status status;

	@Getter
	@Setter
	@Column(name = "FILE_NAME")
	private String fileName;

	@Column(name = "MIGRATION_FILE", length = 100000000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] file;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MigrationData)) {
			return false;
		}
		final MigrationData that = (MigrationData) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

	@Override
	public String toString() {
		return getIdentification();
	}
}
