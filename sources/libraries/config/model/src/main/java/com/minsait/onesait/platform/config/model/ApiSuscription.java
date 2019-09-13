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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "API_SUSCRIPTION")
public class ApiSuscription extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "API_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Api api;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "INIT_DATE")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	@Getter
	@Setter
	private Date initDate;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	@Getter
	@Setter
	private Date endDate;

	@Column(name = "ACTIVE", columnDefinition = "BIT default 1")
	@Getter
	@Setter
	private Boolean isActive;

}
