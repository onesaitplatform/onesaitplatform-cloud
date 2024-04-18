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
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_KPI")
public class OntologyKPI extends AuditableEntityWithUUID implements Versionable<OntologyKPI> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "CRON")
	@Getter
	@Setter
	private String cron;

	@Column(name = "DATE_FROM")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	@Getter
	@Setter
	private Date dateFrom;

	@Column(name = "DATE_TO")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	@Getter
	@Setter
	private Date dateTo;

	@Column(name = "QUERY", nullable = false)
	@Type(type = "org.hibernate.type.TextType")
	@Lob
	@Getter
	@Setter
	private String query;

	@Column(name = "JOB_NAME")
	@Getter
	@Setter
	private String jobName;

	@Column(name = "ACTIVE")
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	@NotNull
	private boolean active;

	@OneToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "POST_PROCESS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String postProcess;

	@JsonSetter("dateFrom")
	public void setDateFromJson(Long millis) {
		if (millis != null) {
			dateFrom = new Date(millis);
		}
	}

	@JsonGetter("dateFrom")
	public Long getDateFromJson() {
		return dateFrom == null ? null : dateFrom.getTime();
	}

	@JsonGetter("dateTo")
	public Long getDateToJson() {
		return dateTo == null ? null : dateTo.getTime();
	}

	@JsonSetter("dateTo")
	public void setDatetoJson(Long millis) {
		if (millis != null) {
			dateTo = new Date(millis);
		}
	}

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (StringUtils.hasText(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			o.setOntologyKPI(this);
			ontology = o;
		}
	}

	@JsonGetter("ontology")
	public String getOntologyJSON() {
		return ontology == null ? null : ontology.getId();
	}

	@Override
	@JsonGetter("user")
	public String getUserJson() {
		return user.getUserId();
	}

	@JsonSetter("user")
	public void setUserJson(String userId) {
		if (StringUtils.hasText(userId)) {
			final User u = new User();
			u.setUserId(userId);
			user = u;
		}
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
	public String fileName() {
		return ontology.getIdentification() + ".yaml";
	}

	@Override
	public Versionable<OntologyKPI> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<OntologyKPI> o = Versionable.super.runExclusions(excludedIds, excludedUsers);
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
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
