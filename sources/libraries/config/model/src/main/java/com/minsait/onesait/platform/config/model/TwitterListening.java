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
@Table(name = "TWITTER_LISTENING")
@Configurable
public class TwitterListening extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private User user;

	@ManyToOne
	@JoinColumn(name = "CONFIGURATION_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private Configuration configuration;

	@ManyToOne
	@JoinColumn(name = "TOKEN_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private Token token;

	@Column(name = "IDENTIFICATOR", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identificator;

	@Column(name = "DATE_FROM", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private Date dateFrom;

	@Column(name = "DATE_TO", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private Date dateTo;

	@Column(name = "TOPICS", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String topics;

	@Column(name = "CRON", length = 100)
	@Getter
	@Setter
	private String cron;

	@Column(name = "JOB_NAME", length = 512)
	@Getter
	@Setter
	private String jobName;

}
