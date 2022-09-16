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

import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configurable
@Getter
@Setter
@ToString(exclude= {"file"})
@Entity
@Table(name = "REPORT", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class Report extends OPResource implements Versionable<Report>{
	public enum ReportExtension {
		JRXML, JASPER;
	}

	private static final long serialVersionUID = -3383279797731473231L;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	private Boolean isPublic;

	@Column(name = "ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("true")
	@NotNull
	private Boolean active;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "FILE")
	@Lob
	@Type(type = "org.hibernate.type.ImageType")
	private byte[] file;

	@Column(name = "DATA_SOURCE_URL")
	private String dataSourceUrl;

	@Column(name = "EXTENSION")
	@Enumerated(EnumType.STRING)
	@NotNull
	private ReportExtension extension;

	@ManyToMany
	@JoinTable(name = "REPORT_RESOURCES", uniqueConstraints = @UniqueConstraint(columnNames = { "REPORT_ID",
	"RESOURCES_ID" }), joinColumns = @JoinColumn(name = "REPORT_ID"), inverseJoinColumns = @JoinColumn(name = "RESOURCES_ID"))
	@Getter
	@Setter
	private Set<BinaryFile> resources = new HashSet<>();

	@JsonSetter("file")
	public void setFileJson(String fileBase64) {
		if (!StringUtils.isEmpty(fileBase64)) {
			try {
				file = Base64.getDecoder().decode(fileBase64);
			} catch (final Exception e) {

			}
		}
	}
	@JsonGetter("file")
	public String getFileJson() {
		if (file != null && file.length > 0) {
			try {
				return Base64.getEncoder().encodeToString(file);
			} catch (final Exception e) {

			}
		}
		return null;
	}

	@JsonGetter("resources")
	public Object getResourcesJson() {
		final ArrayNode nu = new YAMLMapper().createArrayNode();
		resources.forEach(r -> nu.add(r.getId()));
		return nu;
	}

	@JsonSetter("resources")
	public void setResourcesJson(Set<String> resources) {
		resources.forEach(r -> {
			final BinaryFile bf = new BinaryFile();
			bf.setId(r);
			this.resources.add(bf);
		});
	}


	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<Report> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Report> r = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if(r !=null && !resources.isEmpty() && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(BinaryFile.class.getSimpleName()))) {
			resources.removeIf(bf -> excludedIds.get(BinaryFile.class.getSimpleName()).contains(bf.getId()));
			r = this;
		}
		return r;
	}

}
