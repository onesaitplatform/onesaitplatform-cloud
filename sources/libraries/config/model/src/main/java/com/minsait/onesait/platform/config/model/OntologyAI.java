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
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.commons.mindsdb.MindsDBPredictorGeneric;
import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configurable
@Entity
@Table(name = "ONTOLOGY_AI")
@ToString
@Getter
@Setter
public class OntologyAI extends AuditableEntityWithUUID implements Versionable<OntologyAI> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@OneToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@JsonIgnore
	private Ontology ontology;
	@Column(name = "PREDICTOR", length = 128, nullable = false, unique = true)
	private String predictor;
	@Column(name = "INPUT_PROPERTIES", length = 512, nullable = false)
	private String inputProperties;
	@Column(name = "TARGET_PROPERTIES", length = 512, nullable = false)
	private String targetProperties;
	@Column(name = "ORIGINAL_RTDB_DATASOURCE", length = 255, nullable = false)
	@Enumerated(EnumType.STRING)
	private RtdbDatasource originalDatasource;
	@Column(name = "SOURCE_ENTITY", length = 128)
	private String sourceEntity;
	@Column(name = "CONNECTION_NAME", length = 128, nullable = false)
	private String connectionName;
	@Column(name = "DB_SCHEMA", length = 128, nullable = true)
	private String dbSchema;

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (StringUtils.hasText(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			o.setOntologyAI(this);
			ontology = o;
		}
	}

	@JsonGetter("ontology")
	public String getOntologyJSON() {
		return ontology == null ? null : ontology.getId();
	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("ontology", ontology == null ? null : ontology.getId());
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public OntologyAI deserialize(String content) throws IOException {
		final OntologyAI o = Versionable.super.deserialize(content);

		try {
			final MindsDBPredictorGeneric service = BeanUtil.getBean(MindsDBPredictorGeneric.class);
			final PredictorDTO p = PredictorDTO.builder().targetFields(o.getTargetProperties())
					.inputFields(o.getInputProperties()).name(o.getPredictor()).ontology(o.getSourceEntity())
					.connName(o.getConnectionName()).user(o.getUserJson()).build();
			service.createPredictor(p);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return o;
	}

	@Override
	public String fileName() {
		return predictor + ".yaml";
	}

	@Override
	public String getUserJson() {
		if (ontology != null && ontology.getUser() != null) {
			return ontology.getUser().getUserId();
		}
		return null;
	}

	@Override
	public Versionable<OntologyAI> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<OntologyAI> o = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (o != null && ontology != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
				&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			o = null;
		}
		return o;
	}

	@Override
	public void setOwnerUserId(String userId) {
	}

}
