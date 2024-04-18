/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MASTER_USER_HISTORIC")
@Configurable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterUserHistoric extends AuditableEntityWithUUID {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "MASTER_USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private MasterUser masterUser;

	@Column(name = "PASSWORD", length = 128, nullable = false)
	@NotNull
	@Convert(converter = JPAHAS256ConverterCustom.class)
	private String password;

	public String getPassword() {
		if (password != null && password.startsWith(JPAHAS256ConverterCustom.STORED_FLAG)) {
			return password.substring(JPAHAS256ConverterCustom.STORED_FLAG.length());
		} else {
			return password;
		}

	}

}
