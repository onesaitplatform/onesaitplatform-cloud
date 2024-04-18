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
package com.minsait.onesait.platform.controlpanel.rest.management.user.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserSimplified implements Comparable<UserSimplified> {
	@NotNull
	private String username;
	@JsonInclude(Include.NON_NULL)
	private String password;
	@NotNull
	private String mail;
	@NotNull
	private String fullName;
	@NotNull
	private String role;
	@JsonInclude(Include.NON_NULL)
	private String extraFields;
	@JsonInclude(Include.NON_NULL)
	private byte[] avatar;

	public UserSimplified(com.minsait.onesait.platform.config.model.User user) {
		username = user.getUserId();
		mail = user.getEmail();
		fullName = user.getFullName();
		role = user.getRole().getId();
		if (user.getAvatar() != null && user.getAvatar().length > 0)
			avatar = user.getAvatar();

		if (user.getExtraFields() != null)
			extraFields = user.getExtraFields();

	}

	@Override
	public int compareTo(UserSimplified other) {
		return username.compareTo(other.fullName);
	}

}
