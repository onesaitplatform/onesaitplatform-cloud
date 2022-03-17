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
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.listener.EntityListener;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "USER_TOKEN")
@EntityListeners(EntityListener.class)
public class UserToken extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@Column(name = "TOKEN", nullable = false, unique = true)
	@NotNull
	@Getter
	@Setter
	String token;

	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@NotNull
	@Getter
	@Setter
	private User user;

}
