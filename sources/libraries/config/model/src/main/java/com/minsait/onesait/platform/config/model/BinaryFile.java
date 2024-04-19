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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BINARY_FILE")
@Configurable
public class BinaryFile extends OPResource {

	private static final long serialVersionUID = 5923804579468183726L;

	public enum RepositoryType {
		MONGO_GRIDFS, FILE
	}

	@Column(name = "FILE_NAME", nullable = false)
	@Getter
	@Setter
	private String fileName;

	@Column(name = "FILE_EXTENSION", nullable = false)
	@Getter
	@Setter
	private String fileExtension;

	@Column(name = "METADATA", nullable = true)
	@Getter
	@Setter
	private String metadata;

	@Column(name = "PATH", nullable = true)
	@Getter
	@Setter
	private String path;

	@Column(name = "MIME", nullable = false)
	@Getter
	@Setter
	private String mime;

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "REPOSITORY")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RepositoryType repository;

	@OneToMany(mappedBy = "binaryFile", orphanRemoval = true)
	@Getter
	@Setter
	private Set<BinaryFileAccess> fileAccesses = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o instanceof BinaryFileAccess) {
			return (getId().equals(((BinaryFile) o).getId()));
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getId());
	}

}
