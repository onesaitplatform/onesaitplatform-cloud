/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "FLOW_DOMAIN", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class FlowDomain extends OPResource implements Versionable<FlowDomain> {

	private static final long serialVersionUID = 1L;

	public enum State {
		START, STOP
	}

	@NotNull
	@Getter
	@Setter
	@Column(name = "STATE", length = 20, nullable = false)
	private String state;

	@NotNull
	@Getter
	@Setter
	@Column(name = "PORT", nullable = false)
	private Integer port;

	@NotNull
	@Getter
	@Setter
	@Column(name = "SERVICE_PORT", nullable = false)
	private Integer servicePort;

	@NotNull
	@Getter
	@Setter
	@Column(name = "HOME", nullable = false)
	private String home;

	@NotNull
	@Getter
	@Setter
	@Column(name = "ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	private Boolean active;

	@Getter
	@Setter
	@Column(name = "ACCESS_TOKEN", nullable = true)
	private String accessToken;

	@Getter
	@Setter
	@Column(name = "AUTORECOVER", nullable = true)
	@Type(type = "org.hibernate.type.BooleanType")
	private Boolean autorecover;

	@Getter
	@Setter
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "THRESHOLDS", nullable = true)
	private String thresholds;

	@OneToMany(mappedBy = "flowDomain", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<Flow> flows = new HashSet<>();

	@JsonSetter("flows")
	public void setNodesJson(Set<Flow> flows) {
		flows.forEach(f -> {
			f.setFlowDomain(this);
			this.flows.add(f);
		});
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final FlowDomain fobj = (FlowDomain) obj;
		return state.equals(fobj.getState()) && port == fobj.getPort() && servicePort == fobj.getServicePort()
				&& home.equals(fobj.getHome()) && active == fobj.getActive();
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, port, servicePort, home, active);
	}

	@Override
	public String fileName() {
		return getIdentification() + File.separator + getIdentification() + ".yaml";
	}

	@Override
	public String serialize() throws IOException {
		final String v = Versionable.super.serialize();
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("FLOWS_DATA", "/usr/local/flows/");
		try {
			if (new File(contentsPath + getUser().getUserId()).exists()) {
				if (!new File(versionablePath).exists()) {
					new File(versionablePath).mkdirs();
				}
				VersioningUtils.zipFolder(new File(contentsPath + getUser().getUserId()),
						new File(versionablePath + File.separator + getIdentification() + ".zip"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	@Override
	public FlowDomain deserialize(String content) throws IOException {
		final FlowDomain fd = Versionable.super.deserialize(content);
		setIdentification(fd.getIdentification());
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("FLOWS_DATA", "/usr/local/flows/");
		final File zip = new File(versionablePath + File.separator + fd.getIdentification() + ".zip");
		if (zip.exists()) {
			try {
				final File target = new File(contentsPath + fd.getUser().getUserId());
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				target.mkdirs();
				VersioningUtils.unzipFolder(zip, target);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return fd;
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
		return list;
	}

	@Override
	public Versionable<FlowDomain> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<FlowDomain> domain = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (domain != null && !getFlows().isEmpty() && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))) {
			flows.forEach(f -> f.getNodes().removeIf(fn -> (fn.getOntology() != null
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(fn.getOntology().getId()))));
			domain = this;
		}
		return domain;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
