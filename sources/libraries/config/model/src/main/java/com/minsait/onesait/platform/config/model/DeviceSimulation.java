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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DEVICE_SIMULATION")
@Configurable
public class DeviceSimulation extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Type {
		FIXED_INTEGER, FIXED_NUMBER, FIXED_STRING, FIXED_DATE, RANDOM_INTEGER, RANDOM_NUMBER, RANDOM_STRING,
		RANDOM_DATE, COSINE_NUMBER, SINE_NUMBER, FIXED_BOOLEAN, RANDOM_BOOLEAN, NULL
	}

	@Column(name = "IDENTIFICATION")
	@Getter
	@NotNull
	@Setter
	private String identification;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
	@NotNull
	@Getter
	@Setter
	private User user;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "CLIENT_PLATFORM_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private ClientPlatform clientPlatform;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "TOKEN_ID", referencedColumnName = "ID")
	@NotNull
	@Getter
	@Setter
	private Token token;

	@Column(name = "JSON")
	@NotNull
	@Lob
	@JsonRawValue
	@Getter
	@Setter
	private String json;

	@Column(name = "DATE_FROM")
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Date dateFrom;

	@Column(name = "DATE_TO")
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Date dateTo;

	@Column(name = "CRON")
	@Getter
	@Setter
	private String cron;

	@Column(name = "INTERVAL_SECONDS")
	@Getter
	@Setter
	private int interval;

	@Column(name = "ACTIVE", columnDefinition = "BIT")
	@Getter
	@Setter
	@NotNull
	private boolean active;

	@Column(name = "JOB_NAME")
	@Getter
	@Setter
	private String jobName;

}
