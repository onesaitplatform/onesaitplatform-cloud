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
package com.minsait.onesait.platform.config.model.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.minsait.onesait.platform.multitenant.config.model.VerticalParent;

import lombok.Getter;
import lombok.Setter;

public class UserPrincipal extends User {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String verticalSchema;
	@Getter
	@Setter
	private String vertical;
	@Getter
	@Setter
	private String tenant;

	public UserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities,
			VerticalParent vertical, String tenant) {
		super(username, password, authorities);
		verticalSchema = vertical == null ? null : vertical.getSchema();
		this.vertical = vertical == null ? null : vertical.getName();
		this.tenant = tenant;

	}

	public UserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities,
			String verticalSchema, String tenant) {
		super(username, password, authorities);
		this.verticalSchema = verticalSchema;
		vertical = verticalSchema;
		this.tenant = tenant;

	}

}
