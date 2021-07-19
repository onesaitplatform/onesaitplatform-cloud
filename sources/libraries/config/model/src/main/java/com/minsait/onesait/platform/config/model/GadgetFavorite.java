/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "GADGET_FAVORITE", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))

public class GadgetFavorite extends OPResource {

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

}
