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
package com.minsait.onesait.platform.multitenant.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_access_token")
@Getter
@Setter
public class OAuthAccessToken implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "token_id", unique = true, nullable = false)
	private String tokenId;

	//	@Lob
	@Column(name = "token", length = 167772170)
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] token;

	@Column(name = "authentication_id", unique = true)
	private String authenticationId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "client_id")
	private String clientId;

	//	@Lob
	@Column(name = "authentication", length = 167772170)
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] authentication;

	@Column(name = "refresh_token")
	private String refreshToken;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (authentication == null ? 0 : authentication.hashCode());
		result = prime * result + (authenticationId == null ? 0 : authenticationId.hashCode());
		result = prime * result + (clientId == null ? 0 : clientId.hashCode());
		result = prime * result + (refreshToken == null ? 0 : refreshToken.hashCode());
		result = prime * result + (token == null ? 0 : token.hashCode());
		result = prime * result + (tokenId == null ? 0 : tokenId.hashCode());
		result = prime * result + (userName == null ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OAuthAccessToken)) {
			return false;
		}
		return getTokenId() != null && getTokenId().equals(((OAuthAccessToken) o).getTokenId());
	}
}
