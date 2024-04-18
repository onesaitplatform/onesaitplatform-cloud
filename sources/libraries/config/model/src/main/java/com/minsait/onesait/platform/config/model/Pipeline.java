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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pipeline", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class Pipeline extends OPResource implements Versionable<Pipeline> {

	private static final long serialVersionUID = 1L;

	public enum PipelineType {
		DATA_COLLECTOR, MICROSERVICE, DATA_COLLECTOR_EDGE
	}

	public enum PipelineStatus {
		EDITED, RUN_ERROR, STOPPED, FINISHED, RUNNING, START_ERROR, RUNNING_ERROR, DISCONNECTED, DISCONNECTING,
		CONNECTING, STOP_ERROR, INSTANCE_ERROR, CONNECT_ERROR, FINISHING, RETRY, STARTING, STARTING_ERROR, STOPPING,
		STOPPING_ERROR
	}

	@Column(name = "IDSTREAMSETS", length = 100, nullable = false)
	@Getter
	@Setter
	private String idstreamsets;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "ID_INSTANCE", nullable = false, referencedColumnName = "ID")
	@Getter
	@Setter
	private DataflowInstance instance;

	@Transient
	@Getter
	@Setter
	private PipelineUserAccessType.Type accessType;

	@Transient
	@Getter
	@Setter
	private PipelineType type;

	@Transient
	@Getter
	@Setter
	private PipelineStatus status;

	@OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<PipelineUserAccess> pipelineAccesses = new HashSet<>();

	@JsonSetter("pipelineAccesses")
	public void setAccessesJson(Set<PipelineUserAccess> accesses) {
		accesses.forEach(a -> {
			a.setPipeline(this);
			pipelineAccesses.add(a);
		});
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.getIdentification().hashCode();
		result = prime * result + super.getUser().hashCode();
		result = prime * result + idstreamsets.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pipeline other = (Pipeline) obj;
		if (super.getIdentification() == null) {
			if (other.getIdentification() != null) {
				return false;
			}
		} else if (!super.getIdentification().equals(other.getIdentification())) {
			return false;
		}
		if (super.getUser() == null) {
			if (other.getUser() != null) {
				return false;
			}
		} else if (!super.getUser().equals(other.getUser())) {
			return false;
		}
		if (idstreamsets == null) {
			if (other.idstreamsets != null) {
				return false;
			}
		} else if (!idstreamsets.equals(other.idstreamsets)) {
			return false;
		}
		return true;
	}

	@JsonSetter("instance")
	public void setInstanceJson(String id) {
		if (StringUtils.hasText(id)) {
			final DataflowInstance o = new DataflowInstance();
			o.setId(id);
			instance = o;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + File.separator + getIdentification() + ".yaml";
	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("instance", instance == null ? null : instance.getId());
		try {
			final String versionablePath = pathToVersionable(false);
			final String contentsPath = BeanUtil.getEnv().getProperty("DATAFLOWS_DATA", "/usr/local/dataflows/")
					+ "pipelines/";
			final String contentsPathRunInfo = BeanUtil.getEnv().getProperty("DATAFLOWS_DATA", "/usr/local/dataflows/")
					+ "runInfo/";
			try {
				if (new File(contentsPath + getIdstreamsets()).exists()) {
					if (!new File(versionablePath).exists()) {
						new File(versionablePath).mkdirs();
					}
					VersioningUtils.zipFolder(new File(contentsPath + getIdstreamsets()),
							new File(versionablePath + File.separator + getIdentification() + ".zip"));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
			try {
				if (new File(contentsPathRunInfo + getIdstreamsets()).exists()) {
					if (!new File(versionablePath).exists()) {
						new File(versionablePath).mkdirs();
					}
					VersioningUtils.zipFolder(new File(contentsPathRunInfo + getIdstreamsets()),
							new File(versionablePath + File.separator + getIdentification() + "_runInfo.zip"));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Pipeline deserialize(String content) throws IOException {
		final Pipeline p = Versionable.super.deserialize(content);
		setIdentification(p.getIdentification());
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("DATAFLOWS_DATA", "/usr/local/dataflows/")
				+ "pipelines/";
		final String contentsPathRunInfo = BeanUtil.getEnv().getProperty("DATAFLOWS_DATA", "/usr/local/dataflows/")
				+ "runInfo/";
		final File zip = new File(versionablePath + File.separator + p.getIdentification() + ".zip");
		if (zip.exists()) {
			try {
				final File target = new File(contentsPath + p.getIdstreamsets());
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				target.mkdirs();
				VersioningUtils.unzipFolder(zip, target);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		final File zipRunInfo = new File(versionablePath + File.separator + p.getIdentification() + "_runInfo.zip");
		if (zipRunInfo.exists()) {
			try {
				final File target = new File(contentsPathRunInfo + p.getIdstreamsets());
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				target.mkdirs();
				VersioningUtils.unzipFolder(zipRunInfo, target);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		return p;
	}

	@Override
	public String pathToVersionable(boolean toYamlFile) {
		final String path = Versionable.super.pathToVersionable(toYamlFile);
		if (toYamlFile) {
			return path;
		} else {
			return path + File.separator + getIdentification();
		}
	}

	@Override
	public List<String> zipFileNames() {
		final ArrayList<String> list = new ArrayList<>();
		list.add(pathToVersionable(false) + File.separator + getIdentification() + ".zip");
		list.add(pathToVersionable(false) + File.separator + getIdentification() + "_runInfo.zip");
		return list;
	}

	@Override
	public Versionable<Pipeline> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Pipeline> p = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (p != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(DataflowInstance.class.getSimpleName()))
				&& excludedIds.get(DataflowInstance.class.getSimpleName()).contains(instance.getId())) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			p = null;
		}
		return p;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}

}
