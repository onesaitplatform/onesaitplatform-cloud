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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "VIEWER")
public class Viewer extends OPResource implements Versionable<Viewer>{

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@ManyToMany(mappedBy = "viewers", fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<Layer> layers = new HashSet<>();

	@Column(name = "IS_PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "BASE_LAYER", referencedColumnName = "ID")
	@Getter
	@Setter
	private BaseLayer baseLayer;

	@Column(name = "JS", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String js;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Viewer)) {
			return false;
		}
		final Viewer that = (Viewer) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

	@Column(name = "LATITUDE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String latitude;

	@Column(name = "LONGITUDE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String longitude;

	@Column(name = "HEIGHT", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String height;

	@JsonGetter("layers")
	private Set<String> getLayersJson(){
		return layers.stream().map(Layer::getId).collect(Collectors.toSet());
	}

	@JsonSetter("layers")
	private void setLayersJson(Set<String> list) {
		list.forEach(l -> {
			final Layer layer = new Layer();
			layer.setId(l);
			layer.getViewers().add(this);
			layers.add(layer);
		});
	}

	@JsonGetter("baseLayer")
	public String getBaseLayerJson() {
		if(baseLayer != null) {
			return baseLayer.getId();
		}
		return null;
	}

	@JsonSetter("baseLayer")
	public void setBaseLayerJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final BaseLayer baseLayer = new BaseLayer();
			baseLayer.setId(id);
			this.baseLayer =  baseLayer;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<Viewer> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Viewer> o = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if(o!=null && !layers.isEmpty() && !CollectionUtils.isEmpty(excludedIds.get(Layer.class.getSimpleName()))) {
			layers.removeIf(l -> excludedIds.get(Layer.class.getSimpleName()).contains(l.getId()));
			o = this;
		}
		return o;
	}

}
