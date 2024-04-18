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

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
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

@Entity
@Table(name = "CLIENT_PLATFORM_INSTANCE_SIMULATION")
@Configurable
@Slf4j
public class ClientPlatformInstanceSimulation extends OPResource
		implements Versionable<ClientPlatformInstanceSimulation> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		FIXED_INTEGER, FIXED_NUMBER, FIXED_STRING, FIXED_DATE, RANDOM_INTEGER, RANDOM_NUMBER, RANDOM_STRING,
		RANDOM_DATE, COSINE_NUMBER, SINE_NUMBER, FIXED_BOOLEAN, RANDOM_BOOLEAN, NULL
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "CLIENT_PLATFORM_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private ClientPlatform clientPlatform;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "TOKEN_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private Token token;

	@Column(name = "JSON")
	@NotNull
	@Lob
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String json;

	@Column(name = "DATE_FROM")
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Date dateFrom;

	@Column(name = "DATE_TO")
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Date dateTo;

	@Column(name = "CRON")
	@Getter
	@Setter
	private String cron;

	@Column(name = "INTERVAL_SECONDS")
	@Getter
	@Setter
	private int interval;

	@Column(name = "ACTIVE")
	@org.hibernate.annotations.Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	@NotNull
	private boolean active;

	@Column(name = "JOB_NAME")
	@Getter
	@Setter
	private String jobName;

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (StringUtils.hasText(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			ontology = o;
		}
	}

	@JsonSetter("clientPlatform")
	public void setClientPlatformJson(String id) {
		if (StringUtils.hasText(id)) {
			final ClientPlatform o = new ClientPlatform();
			o.setId(id);
			clientPlatform = o;
		}
	}

	@JsonSetter("token")
	public void setTokenJson(String id) {
		if (StringUtils.hasText(id)) {
			final Token o = new Token();
			o.setId(id);
			token = o;
		}
	}

	@JsonGetter("json")
	public Object getJsonJson() {
		try {
			return new ObjectMapper().readTree(json);
		} catch (final Exception e) {
			return json;
		}
	}

	@JsonSetter("json")
	public void setJsonJson(Object node) {
		try {
			json = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			// NO-OP
		}
	}

	@JsonGetter("dateFrom")
	public Long getdateFromJson() {
		return dateFrom == null ? null : dateFrom.getTime();
	}

	@JsonSetter("dateFrom")
	public void setdateFromJson(Long millis) {
		if (millis != null) {
			dateFrom = new Date(millis);
		}
	}

	@JsonGetter("dateTo")
	public Long getdateToJson() {
		return dateTo == null ? null : dateTo.getTime();
	}

	@JsonSetter("dateTo")
	public void setdateToJson(Long millis) {
		if (millis != null) {
			dateTo = new Date(millis);
		}
	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("ontology", ontology == null ? null : ontology.getId());
		node.put("clientPlatform", clientPlatform == null ? null : clientPlatform.getId());
		node.put("token", token == null ? null : token.getId());
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			log.error("Could not serialize versionable of class {} with id {}", this.getClass(), getId());
			return null;
		}
	}

	@Override
	public String fileName() {
		final String g = clientPlatform == null ? "" : clientPlatform.getIdentification();
		final String ds = ontology == null ? "" : ontology.getIdentification();
		return (g.equals("") ? "" : g + "_") + (ds.equals("") ? "" : ds + "_") + getId() + ".yaml";
	}

	@Override
	public Versionable<ClientPlatformInstanceSimulation> runExclusions(Map<String, Set<String>> excludedIds,
			Set<String> excludedUsers) {
		Versionable<ClientPlatformInstanceSimulation> client = Versionable.super.runExclusions(excludedIds,
				excludedUsers);
		if (client != null) {
			if (ontology != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				client = null;
			}
			if (clientPlatform != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(ClientPlatform.class.getSimpleName()))
					&& excludedIds.get(ClientPlatform.class.getSimpleName()).contains(clientPlatform.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				client = null;
			}
		}
		return client;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
