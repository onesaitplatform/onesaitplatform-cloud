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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "THEMES")
public class Themes extends AuditableEntity {

	private static final long serialVersionUID = 1L;

	public enum editItems {
		LOGIN_TITLE, LOGIN_IMAGE, HEADER_IMAGE, LOGIN_BACKGROUND_COLOR, LOGIN_TITLE_ES, FOOTER_TEXT, FOOTER_TEXT_ES,
		CSS, JS
	}

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUIDGenerator")
	@Column(name = "ID", length = 50)
	@Getter
	@Setter
	private String id;

	@Column(name = "IDENTIFICATION", length = 50, nullable = false, unique = true)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "JSON", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@JsonRawValue
	@Getter
	@Setter
	private String json;

	@Column(name = "ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	@ColumnDefault("0")
	@NotNull
	@Getter
	@Setter
	private boolean active;
}
