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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "PROCESS_TRACE")
public class ProcessTrace extends OPResource {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "IS_ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private Boolean isActive;

	@Column(name = "IS_ORDERED", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private Boolean isOrdered;

	@Column(name = "IS_FILTERED", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private Boolean isFiltered;

	@Column(name = "CHECK_EXECUTIONS", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("true")
	@NotNull
	@Getter
	@Setter
	private Boolean checkExecutions;

	@Column(name = "CRON")
	@Getter
	@Setter
	private String cron;

	@Column(name = "DATE_FROM")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	@Getter
	@Setter
	private Date dateFrom;

	@Column(name = "DATE_TO")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	@Getter
	@Setter
	private Date dateTo;

	@Column(name = "JOB_NAME")
	@Getter
	@Setter
	private String jobName;

	@Column(name = "VERSION", nullable = false)
	@NotNull
	@Getter
	@Setter
	private Integer version;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "processTraceId", cascade = CascadeType.ALL, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	@JsonIgnore
	private Set<ProcessOperation> operations = new HashSet<>();

}
