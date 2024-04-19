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
package com.minsait.onesait.platform.multitenant.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MASTER_CONFIGURATION")
@Configurable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterConfiguration extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Type {
		RTDB
	}

	@Column(name = "YML_CONFIG", nullable = false)
	@NotNull
	@Lob
	private String ymlConfig;

	@Column(name = "TYPE", length = 50, unique = true)
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name = "DESCRIPTION", length = 255)
	private String description;
}
