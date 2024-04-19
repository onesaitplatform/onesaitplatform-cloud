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
package com.minsait.onesait.platform.security.jwt.ri;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class ResponseToken implements Serializable {
	private static final long serialVersionUID = 1L;

	private String token;
	private Date expirationTimestamp;
	private String expirationFormatted;
	
	private Set<String> authorities;

	private OAuth2AccessToken oauthInfo;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public OAuth2AccessToken getOauthInfo() {
		return oauthInfo;
	}

	public void setOauthInfo(OAuth2AccessToken oauthInfo) {
		this.oauthInfo = oauthInfo;
	}

	public Date getExpirationTimestamp() {
		return expirationTimestamp;
	}

	public void setExpirationTimestamp(Date expirationTimestamp) {
		this.expirationTimestamp = expirationTimestamp;
	}

	public String getExpirationFormatted() {
		return expirationFormatted;
	}

	public void setExpirationFormatted(String expirationFormatted) {
		this.expirationFormatted = expirationFormatted;
	}
	
	public Set<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}

	@Override
	public String toString() {
		return "Sofia2ResponseToken [token=" + token + ", expirationTimestamp=" + expirationTimestamp
				+ ", expirationFormatted=" + expirationFormatted + ", oauthInfo=" + oauthInfo + "]";
	}

}
