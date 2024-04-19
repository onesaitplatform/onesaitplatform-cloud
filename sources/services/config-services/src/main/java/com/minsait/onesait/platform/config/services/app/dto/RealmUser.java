/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config.services.app.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealmUser  implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Status {
        ACTIVE, INACTIVE;
    }
	@Getter
	@Setter
	@NotNull
	private String username;
	@Getter
	@Setter
	@NotNull
	private String role;
	@Getter
	@Setter
	@NotNull
	private String fullName;
	@Getter
	@Setter
	@NotNull
	private String mail;
	@Getter
	@Setter
	@JsonInclude(Include.NON_NULL)
	private String password;
	@Getter
	@Setter
	@JsonInclude(Include.NON_NULL)
	private boolean active;
	@Getter
	@Setter
	@JsonInclude(Include.NON_NULL)
	private Date creationDate;
	@Getter
	@Setter
	@JsonInclude(Include.NON_NULL)
	private String extraFields;
	@JsonInclude(Include.NON_NULL)
	@Getter
	@Setter
	private byte[] avatar;
}
