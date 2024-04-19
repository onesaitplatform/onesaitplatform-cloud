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
package com.minsait.onesait.platform.multitenant.config.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "oauth_client_details")
public class OAuthClientDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "client_id", unique = true, nullable = false)
	private String clientId;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "resource_ids")
	private String resourceIds;

	@Column(name = "scope")
	private String scope;

	@Column(name = "authorities")
	private String authorities;

	@Column(name = "authorized_grant_types")
	private String authorizedGrantTypes;

	@Column(name = "web_server_redirect_uri")
	private String registeredRedirectUri;

	@Column(name = "access_token_validity")
	private Integer accessTokenValiditySeconds;

	@Column(name = "refresh_token_validity")
	private Integer refreshTokenValiditySeconds;

	@Column(name = "additional_information")
	private String additionalInformation;

	public OAuthClientDetails() {
	}

	public OAuthClientDetails(String clientId, String resourceIds, String scopes, String grantTypes,
			String authorities) {
		this(clientId, resourceIds, scopes, grantTypes, authorities, null);
	}

	public OAuthClientDetails(String clientId, String resourceIds, String scopes, String grantTypes, String authorities,
			String redirectUris) {

		this.clientId = clientId;

		if (StringUtils.hasText(resourceIds) && !resourceIds.isEmpty()) {
				this.resourceIds = resourceIds;
		}

		if (StringUtils.hasText(scopes) && !scopes.isEmpty()) {
				this.scope = scopes;
		}

		if (StringUtils.hasText(grantTypes)) {
			this.authorizedGrantTypes = grantTypes;
		} else {
			this.authorizedGrantTypes = "authorization_code, refresh_token";
		}

		if (StringUtils.hasText(authorities)) {
			this.authorities = authorities;
		}

		if (StringUtils.hasText(redirectUris)) {
			this.registeredRedirectUri = redirectUris;
		}
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Set<String> getResourceIds() {
		if (resourceIds != null && !resourceIds.isEmpty()) {
			String[] split = resourceIds.split(",");
			List<String> strings = Arrays.asList(split);
			return new LinkedHashSet<>(strings);
		} else {
			return Collections.<String>emptySet();
		}
	}

	public void setResourceIds(String resourceIds) {
		if (!resourceIds.isEmpty()) {
			this.resourceIds = resourceIds;
		}
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Set<String> getScope() {
		if (scope != null && !scope.isEmpty()) {
			String[] split = scope.split(",");
			List<String> strings = Arrays.asList(split);
			return new LinkedHashSet<>(strings);
		} else {
			return Collections.<String>emptySet();
		}
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Set<String> getAuthorizedGrantTypes() {
		if (authorizedGrantTypes != null && !authorizedGrantTypes.isEmpty()) {
			String[] split = authorizedGrantTypes.split(",");
			List<String> strings = Arrays.asList(split);
			return new HashSet<>(strings);
		} else {
			return Collections.<String>emptySet();
		}
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public Collection<GrantedAuthority> getAuthorities() {
		if (authorities != null && !authorities.isEmpty()) {
			return new ArrayList<>(AuthorityUtils.createAuthorityList(authorities));
		} else {
			return Collections.emptyList();
		}
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	public Set<String> getRegisteredRedirectUri() {
		if (registeredRedirectUri != null && !registeredRedirectUri.isEmpty()) {
			String[] split = registeredRedirectUri.split(",");
			List<String> strings = Arrays.asList(split);
			return new LinkedHashSet<>(strings);
		} else {
			return Collections.<String>emptySet();
		}
	}

	public void setRegisteredRedirectUri(String registeredRedirectUris) {
		this.registeredRedirectUri = registeredRedirectUris;
	}

	public Map<String, Object> getAdditionalInformation() {
		if (additionalInformation != null && !additionalInformation.isEmpty()) {
			Map<String, Object> myMap = new HashMap<>();
			String[] pairs = additionalInformation.split(",");
			for (int i = 0; i < pairs.length; i++) {
				String pair = pairs[i];
				String[] keyValue = pair.split(":");
				myMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
			}
			return myMap;
		} else {
			return Collections.<String, Object>emptyMap();
		}

	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	@Transient
	public boolean isSecretRequired() {
		return this.clientSecret != null;
	}

	@Transient
	public boolean isScoped() {
		return this.scope != null && !this.scope.isEmpty();
	}
}
