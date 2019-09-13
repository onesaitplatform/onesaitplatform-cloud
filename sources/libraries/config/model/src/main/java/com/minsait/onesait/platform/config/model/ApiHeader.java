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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "API_HEADER")
@Configurable
public class ApiHeader extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "API_OPERATION_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private ApiOperation apiOperation;

	@Column(name = "NAME", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "HEADER_TYPE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String header_type;

	@Column(name = "HEADER_DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String header_description;

	@Column(name = "HEADER_VALUE", length = 512)
	@Getter
	@Setter
	private String header_value;

	@Column(name = "HEADER_CONDITION", length = 50)
	@Getter
	@Setter
	private String header_condition;

}
