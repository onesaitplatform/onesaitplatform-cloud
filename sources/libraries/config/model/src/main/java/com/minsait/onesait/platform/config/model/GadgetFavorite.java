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

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "GADGET_FAVORITE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class GadgetFavorite extends OPResource implements Versionable<GadgetFavorite>{

	private static final long serialVersionUID = 1L;

	@Column(name = "TYPE", length = 100)
	@Getter
	@Setter
	private String type;

	@ManyToOne(optional = true)
	@JoinColumn(name = "GADGET_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Gadget gadget;

	@ManyToOne(optional = true)
	@JoinColumn(name = "GADGET_TEMPLATE_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private GadgetTemplate gadgetTemplate;

	@ManyToOne(optional = true)
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

	@Column(name = "METAINF", length = 1024, nullable = true)
	@Getter
	@Setter
	private String metainf;

	@JsonGetter("config")
	public Object getConfigJson() {
		try {
			return new ObjectMapper().readTree(config);
		} catch (final Exception e) {
			return config;
		}
	}
	@JsonSetter("config")
	public void setConfigJson(Object node) {
		try {
			config = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			config = null;
		}
	}


	@JsonGetter("gadget")
	public String getGadgetJson() {
		return gadget == null ? null: gadget.getId();
	}
	@JsonSetter("gadget")
	public void setGadgetJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final Gadget g = new Gadget();
			g.setId(id);
			gadget = g;
		}
	}

	@JsonGetter("gadgetTemplate")
	public String getGadgetTemplateJson() {
		return gadgetTemplate == null ? null: gadgetTemplate.getId();
	}
	@JsonSetter("gadgetTemplate")
	public void setGadgetTemplateJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final GadgetTemplate g = new GadgetTemplate();
			g.setId(id);
			gadgetTemplate = g;
		}
	}
	@JsonGetter("datasource")
	public String getDatasourceJson() {
		return datasource == null ? null: datasource.getId();
	}
	@JsonSetter("datasource")
	public void setDatasourceJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final GadgetDatasource g = new GadgetDatasource();
			g.setId(id);
			datasource = g;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<GadgetFavorite> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<GadgetFavorite> gf =  Versionable.super.runExclusions(excludedIds, excludedUsers);
		if(gf != null) {
			if(gadget != null && !CollectionUtils.isEmpty(excludedIds.get(Gadget.class.getSimpleName()))
					&& excludedIds.get(Gadget.class.getSimpleName()).contains(gadget.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				gf = null;
			}
			if(gadgetTemplate != null && !CollectionUtils.isEmpty(excludedIds.get(GadgetTemplate.class.getSimpleName()))
					&& excludedIds.get(GadgetTemplate.class.getSimpleName()).contains(gadgetTemplate.getId()) ) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				gf = null;
			}
			if(datasource != null && !CollectionUtils.isEmpty(excludedIds.get(GadgetDatasource.class.getSimpleName()))
					&& excludedIds.get(GadgetDatasource.class.getSimpleName()).contains(datasource.getId()) ) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				gf = null;
			}
		}

		return gf;
	}

}
