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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BINARY_FILE")
@Configurable
public class BinaryFile extends OPResource implements Versionable<BinaryFile> {

	private static final long serialVersionUID = 5923804579468183726L;

	public enum RepositoryType {
		MONGO_GRIDFS, FILE, MINIO_S3, GCP
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

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "REPOSITORY")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RepositoryType repository;

	@OneToMany(mappedBy = "binaryFile", orphanRemoval = true, cascade = CascadeType.ALL)
	@Getter
	@Setter
	private Set<BinaryFileAccess> fileAccesses = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof BinaryFileAccess) {
			return getId().equals(((BinaryFile) o).getId());
		} else {
			return false;
		}
	}

	@JsonSetter("fileAccesses")
	public void setFileAccessesJson(Set<BinaryFileAccess> fileAccesses) {
		fileAccesses.forEach(bfa -> {
			bfa.setBinaryFile(this);
			this.fileAccesses.add(bfa);
		});
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getId());
	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + "_" + getId() + ".yaml";
	}

	@Override
	public Versionable<BinaryFile> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<BinaryFile> binaryFile = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (binaryFile != null && !fileAccesses.isEmpty() && !CollectionUtils.isEmpty(excludedUsers)) {
			fileAccesses.removeIf(fa -> excludedUsers.contains(fa.getUser().getUserId()));
			binaryFile = this;

		}
		return binaryFile;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}

}
