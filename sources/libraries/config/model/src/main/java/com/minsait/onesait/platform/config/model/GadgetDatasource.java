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

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Entity
@Table(name = "GADGET_DATASOURCE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@Slf4j
public class GadgetDatasource extends OPResource implements Versionable<GadgetDatasource> {

	private static final long serialVersionUID = 1L;

	@Column(name = "MODE", length = 45, nullable = false)
	@Getter
	@Setter
	private String mode;

	@Column(name = "QUERY", nullable = false)
	@Type(type = "org.hibernate.type.TextType")
	@Lob
	@Getter
	@Setter
	private String query;

	@Column(name = "DBTYPE", length = 10, nullable = false)
	@Getter
	@Setter
	private String dbtype;

	@ManyToOne
	// @OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Ontology ontology;

	@Column(name = "REFRESH")
	@Getter
	@Setter
	private Integer refresh;

	@Column(name = "MAXVALUES", columnDefinition = "int default 10")
	@Getter
	@Setter
	private Integer maxvalues;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@Column(name = "CONFIG")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String config;

	@JsonSetter("config")
	public void setConfigJson(Object node) {
		try {
			config = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			config = null;
		}
	}

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			ontology = o;
		} else {
			ontology = null;
		}
	}

	@Override
	public String serialize() {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("ontology", ontology == null ? null : ontology.getId());
		try {
			node.set("config", mapper.readTree(config));
		} catch (final Exception e) {
			// NO-OP
		}
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			log.error("Could not serialize versionable of class {} with id {}", this.getClass(), getId());
			return null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<GadgetDatasource> runExclusions(Map<String, Set<String>> excludedIds,
			Set<String> excludedUsers) {
		Versionable<GadgetDatasource> gadgetds = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (gadgetds != null && ontology != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
				&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			gadgetds = null;
		}
		return gadgetds;
	}
}
