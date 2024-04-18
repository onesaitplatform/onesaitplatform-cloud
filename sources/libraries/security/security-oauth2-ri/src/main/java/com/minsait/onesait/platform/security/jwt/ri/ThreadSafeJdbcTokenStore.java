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
package com.minsait.onesait.platform.security.jwt.ri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;
import com.minsait.onesait.platform.multitenant.config.model.OAuthRefreshToken;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthAccessTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthRefreshTokenRepository;

import lombok.Getter;

@Component("tokenStore")
public class ThreadSafeJdbcTokenStore extends JdbcTokenStore {

	@Autowired
	private OAuthAccessTokenRepository tokenRepository;
	@Autowired
	private OAuthRefreshTokenRepository oAuthRefreshTokenRepository;
	@Getter
	private final EnhancedAuthenticationKeyGenerator authenticationKeyGenerator = new EnhancedAuthenticationKeyGenerator();

	private final JdbcTemplate template;

	public ThreadSafeJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
		super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
		template = new JdbcTemplate(dataSource);
	}

	@Override
	public synchronized OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		OAuth2AccessToken accessToken = null;

		final String key = authenticationKeyGenerator.extractKey(authentication);

		final OAuthAccessToken tokenOauth = tokenRepository.findByAuthenticationId(key);
		if (tokenOauth != null) {
			accessToken = deserialize(new ByteArrayInputStream(tokenOauth.getToken()));
		}

		if (accessToken != null) {
			final OAuth2Authentication auth = readAuthentication(accessToken.getValue());
			if (auth != null && !key.equals(authenticationKeyGenerator.extractKey(auth))) {
				removeAccessToken(accessToken.getValue());
				// Keep the store consistent (maybe the same user is represented by this
				// authentication but the details have
				// changed)
				storeAccessToken(accessToken, authentication);
			}
		}

		return accessToken;

	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}
		final OAuthAccessToken t = new OAuthAccessToken();
		t.setTokenId(extractTokenKey(token.getValue()));

		t.setToken(serializeAccessToken(token));
		t.setAuthentication(serializeAuthentication(authentication));

		t.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
		t.setUserName(authentication.isClientOnly() ? null : authentication.getName());
		t.setClientId(authentication.getOAuth2Request().getClientId());
		t.setRefreshToken(extractTokenKey(refreshToken));
		tokenRepository.save(t);
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		OAuth2AccessToken accessToken = null;

		final OAuthAccessToken t = getJPAAccessToken(tokenValue);
		if (t != null) {
			accessToken = deserialize(new ByteArrayInputStream(t.getToken()));
		}

		return accessToken;
	}

	public static <T> T deserialize(InputStream is) {
		ObjectInputStream oip = null;
		try {
			oip = new ConfigurableObjectInputStream(is, Thread.currentThread().getContextClassLoader());
			@SuppressWarnings("unchecked")
			final T result = (T) oip.readObject();
			return result;
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		} finally {
			if (oip != null) {
				try {
					oip.close();
				} catch (final IOException e) {
					// eat it
				}
			}
		}
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		OAuth2Authentication authentication = null;

		final OAuthAccessToken t = getJPAAccessToken(token);
		if (t != null) {
			authentication = deserialize(new ByteArrayInputStream(t.getAuthentication()));
		}

		return authentication;
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(String refreshToken) {
		tokenRepository.deleteByRefreshToken(extractTokenKey(refreshToken));
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		removeAccessToken(token.getValue());
	}

	@Override
	public void removeAccessToken(String tokenValue) {
		final OAuthAccessToken t = getJPAAccessToken(tokenValue);
		tokenRepository.deleteByTokenId(t.getTokenId(), t.getAuthenticationId());
	}

	public synchronized OAuth2AccessToken getAccessTokenUsingRefreshToken(String refreshToken) {
		return template.queryForObject("select token_id, token from oauth_access_token where refresh_token = ?",
				(RowMapper<OAuth2AccessToken>) (rs, rowNum) -> deserializeAccessToken(rs.getBytes(2)),
				extractTokenKey(refreshToken));
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		final OAuthRefreshToken t = new OAuthRefreshToken();
		t.setTokenId(extractTokenKey(refreshToken.getValue()));
		t.setToken(serializeRefreshToken(refreshToken));
		t.setAuthentication(serializeAuthentication(authentication));
		oAuthRefreshTokenRepository.save(t);
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		removeRefreshToken(token.getValue());
	}

	@Override
	public void removeRefreshToken(String token) {
		oAuthRefreshTokenRepository.deleteByTokenId(extractTokenKey(token));
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		return readAuthenticationForRefreshToken(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
		OAuth2Authentication authentication = null;
		final OAuthRefreshToken t = getJPARefreshToken(value);
		if (t != null) {
			authentication = deserialize(new ByteArrayInputStream(t.getAuthentication()));
		}

		return authentication;

	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String token) {
		OAuth2RefreshToken refreshToken = null;
		final OAuthRefreshToken t = getJPARefreshToken(token);
		if (t != null) {
			refreshToken = deserialize(new ByteArrayInputStream(t.getToken()));
		}

		return refreshToken;
	}

	public OAuthAccessToken getJPAAccessToken(String tokenValue) {
		return tokenRepository.findByTokenId(extractTokenKey(tokenValue));
	}

	public OAuthRefreshToken getJPARefreshToken(String tokenValue) {
		return oAuthRefreshTokenRepository.findByTokenId(extractTokenKey(tokenValue));
	}
}
