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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "API_QUERY_PARAMETER")
public class ApiQueryParameter extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum HeaderType {
		BODY, FORMDATA, HEADER, PATH, QUERY;
	}

	public enum DataType {
		STRING, ARRAY, DATE, OBJECT, PASSWORD, BINARY, EMAIL, UUID, URI, HOSTNAME, NUMBER, FILE;
	}

	@ManyToOne
	@JoinColumn(name = "API_OPERATION_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private ApiOperation apiOperation;

	@Column(name = "NAME", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "QUERY_DATA_TYPE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private DataType dataType;

	@Column(name = "QUERY_DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "QUERY_VALUE")
	@Lob
	@Getter
	@Setter
	private String value;

	@Column(name = "QUERY_CONDITION", length = 50)
	@Getter
	@Setter
	private String condition;

	@Column(name = "QUERY_HEADER_TYPE", length = 50)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private HeaderType headerType;

}
