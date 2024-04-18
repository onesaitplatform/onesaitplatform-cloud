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

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_refresh_token")
public class OAuthRefreshToken {

	@Id
	@Column(name = "token_id", unique = true, nullable = false)
	private String tokenId;

	@Lob
	@Column(name = "token")
	private Blob token;

	@Lob
	@Column(name = "authentication")
	private Blob authentication;

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Blob getToken() {
		return token;
	}

	public void setToken(Blob token) {
		this.token = token;
	}

	public Blob getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Blob authentication) {
		this.authentication = authentication;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OAuthRefreshToken))
			return false;

		OAuthRefreshToken that = (OAuthRefreshToken) o;

		boolean result = true;
		if (authentication != null ? !authentication.equals(that.authentication) : that.authentication != null)
			result = false;
		if (token != null ? !token.equals(that.token) : that.token != null)
			result = false;
		if (tokenId != null ? !tokenId.equals(that.tokenId) : that.tokenId != null)
			result = false;
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
