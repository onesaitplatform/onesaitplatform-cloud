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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Entity
@Table(name = "GADGET_MEASURE")
@Slf4j
public class GadgetMeasure extends AuditableEntityWithUUID implements Versionable<GadgetMeasure> {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "GADGET_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Gadget gadget;

	@ManyToOne
	@JoinColumn(name = "DATASOURCE_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private GadgetDatasource datasource;

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

	@JsonSetter("gadget")
	public void setGadgetJson(String id) {
		if (StringUtils.hasText(id)) {
			final Gadget g = new Gadget();
			g.setId(id);
			gadget = g;
		}
	}

	@JsonSetter("datasource")
	public void setDatasourceJson(String id) {
		if (StringUtils.hasText(id)) {
			final GadgetDatasource g = new GadgetDatasource();
			g.setId(id);
			datasource = g;
		}
	}

	@Override
	public String serialize() {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("gadget", gadget == null ? null : gadget.getId());
		node.put("datasource", datasource == null ? null : datasource.getId());
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
		final String g = gadget == null ? "" : gadget.getIdentification();
		final String ds = datasource == null ? "" : datasource.getIdentification();
		return (g.equals("") ? "" : g + "_") + (ds.equals("") ? "" : ds + "_") + getId() + ".yaml";
	}

	@Override
	@JsonIgnore
	public String getUserJson() {
		if(gadget != null) {
			return gadget.getUserJson();
		}
		if(datasource != null)
		{
			return datasource.getUserJson();
		}
		return null;
	}

	@Override
	public Versionable<GadgetMeasure> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<GadgetMeasure> gm =  Versionable.super.runExclusions(excludedIds, excludedUsers);
		if(gm != null) {
			if(gadget != null && !CollectionUtils.isEmpty(excludedIds.get(Gadget.class.getSimpleName()))
					&& excludedIds.get(Gadget.class.getSimpleName()).contains(gadget.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				gm = null;
			}
			if(datasource != null && !CollectionUtils.isEmpty(excludedIds.get(GadgetDatasource.class.getSimpleName()))
					&& excludedIds.get(GadgetDatasource.class.getSimpleName()).contains(datasource.getId()) ) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				gm = null;
			}
		}

		return gm;
	}
}
