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
package com.minsait.onesait.platform.onesaitplatform.plugin.manager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "PLUGIN", uniqueConstraints = @UniqueConstraint(columnNames = { "JAR_FILE", "MODULE" }))
@Data
public class Plugin {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID", nullable = false)
	private String id;

	@Column(name = "JAR_FILE", length = 512, nullable = false)
	private String jarFile;

	@Column(name = "LOADED", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean loaded = false;

	@Column(name = "PUBLISHER", length = 255, nullable = false)
	private String publisher;

	@Column(name = "MODULE", length = 255, nullable = false)
	@Enumerated(EnumType.STRING)
	private com.minsait.onesait.platform.plugin.Module module;

}
