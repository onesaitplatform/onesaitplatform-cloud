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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DASHBOARD", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
@Configurable

public class Dashboard extends OPResource {

	private static final long serialVersionUID = 1L;

	public static enum DashboardType {
		DASHBOARD, SYNOPTIC
	}

	@Column(name = "DESCRIPTION", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "JSON18N")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String jsoni18n;

	@Column(name = "CUSTOMCSS")
	@Getter
	@Setter
	private String customcss;

	@Column(name = "CUSTOMJS")
	@Getter
	@Setter
	private String customjs;

	@Column(name = "PUBLIC", columnDefinition = "BIT")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "MODEL")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String model;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "IMAGE", length = 100000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;

	@Column(name = "TYPE", length = 45)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private DashboardType type;

}