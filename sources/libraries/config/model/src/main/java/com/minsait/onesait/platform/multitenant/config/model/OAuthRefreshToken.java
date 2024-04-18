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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_refresh_token")
@Getter
@Setter
public class OAuthRefreshToken implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "token_id", unique = true, nullable = false)
	private String tokenId;

	//	@Lob
	@Column(name = "token", length = 16777217)
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] token;

	//	@Lob
	@Column(name = "authentication", length = 16777217)
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] authentication;


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OAuthRefreshToken)) {
			return false;
		}

		final OAuthRefreshToken that = (OAuthRefreshToken) o;

		boolean result = true;
		if (authentication != null ? !authentication.equals(that.authentication) : that.authentication != null) {
			result = false;
		}
		if (token != null ? !token.equals(that.token) : that.token != null) {
			result = false;
		}
		if (tokenId != null ? !tokenId.equals(that.tokenId) : that.tokenId != null) {
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode() {
		int result = tokenId != null ? tokenId.hashCode() : 0;
		result = 31 * result + (token != null ? token.hashCode() : 0);
		result = 31 * result + (authentication != null ? authentication.hashCode() : 0);
		return result;
	}
}
