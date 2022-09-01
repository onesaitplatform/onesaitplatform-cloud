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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.Category.Type;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "CATEGORY_RELATION")
public class CategoryRelation extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@Column(name = "TYPE_ID", length = 50, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String typeId;

	@Column(name = "TYPE", length = 255)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name = "CATEGORY", length = 50, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String category;

	@Column(name = "SUBCATEGORY", length = 50, unique = false, nullable = true)
	@NotNull
	@Getter
	@Setter
	private String subcategory;

}
