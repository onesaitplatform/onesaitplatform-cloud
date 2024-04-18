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

import java.util.Base64;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DROOLS_RULE", uniqueConstraints = { @UniqueConstraint(columnNames = { "IDENTIFICATION" }),
		@UniqueConstraint(columnNames = { "USER_ID", "SOURCE_ONTOLOGY_ID" }) })
@Configurable
@Getter
@Setter
public class DroolsRule extends OPResource implements Versionable<DroolsRule> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ONTOLOGY, REST
	}

	public enum TableExtension {
		CSV, XLS, XLSX
	}

	@Lob
	@Column(name = "DRL", nullable = true)
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	private String DRL;

	// @Basic(fetch = FetchType.LAZY)
	@Column(name = "DECISION_TABLE")
	@Lob
	@org.hibernate.annotations.Type(type = "org.hibernate.type.ImageType")
	private byte[] decisionTable;

	@Column(name = "TYPE", nullable = true)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private Type type;

	@Column(name = "EXTENSION")
	@Enumerated(EnumType.STRING)
	private TableExtension extension;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinColumn(name = "TARGET_ONTOLOGY_ID", referencedColumnName = "ID")
	private Ontology targetOntology;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinColumn(name = "SOURCE_ONTOLOGY_ID", referencedColumnName = "ID")
	private Ontology sourceOntology;

	@Column(name = "ACTIVE", nullable = false)
	@org.hibernate.annotations.Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	private boolean active;

	@JsonGetter("targetOntology")
	public String getTargetOntologyJson() {
		return targetOntology == null ? null : targetOntology.getId();
	}

	@JsonGetter("sourceOntology")
	public String getSourceOntologyJson() {
		return sourceOntology == null ? null : sourceOntology.getId();
	}

	@JsonSetter("targetOntology")
	public void setTargetOntologyJson(String id) {
		if (!StringUtils.hasText(id)) {
			final Ontology s = new Ontology();
			s.setId(id);
			targetOntology = s;

		}
	}

	@JsonSetter("sourceOntology")
	public void setSourceOntologyJson(String id) {
		if (!StringUtils.hasText(id)) {
			final Ontology s = new Ontology();
			s.setId(id);
			sourceOntology = s;

		}
	}

	@JsonSetter("decisionTable")
	public void setImageJson(String imageBase64) {
		if (!StringUtils.hasText(imageBase64)) {
			try {
				decisionTable = Base64.getDecoder().decode(imageBase64);
			} catch (final Exception e) {

			}
		}
	}

	@JsonGetter("decisionTable")
	public String getImageJson() {
		if (decisionTable != null && decisionTable.length > 0) {
			try {
				return Base64.getEncoder().encodeToString(decisionTable);
			} catch (final Exception e) {

			}
		}
		return null;
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<DroolsRule> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		// TODO Auto-generated method stub
		Versionable<DroolsRule> v = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (v != null) {
			if (sourceOntology != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(sourceOntology.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				v = null;
			}
			if (targetOntology != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(targetOntology.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				v = null;
			}
		}

		return v;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
