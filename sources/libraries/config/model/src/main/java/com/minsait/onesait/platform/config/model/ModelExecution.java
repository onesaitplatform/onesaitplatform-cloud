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

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "MODEL_EXECUTION")
public class ModelExecution extends OPResource {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID_EJECT", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String idEject;

	@Column(name = "PARAMETERS", length = 255, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String parameters;

	@Column(name = "ID_ZEPPELIN", length = 255, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String idZeppelin;

	@Column(name = "DESCRIPTION", length = 255, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "MODEL", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Model model;

}
