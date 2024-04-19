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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthAccessTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Component("tokenStore")
@Slf4j
public class ThreadSafeJdbcTokenStore extends JdbcTokenStore {

	@Autowired
	private OAuthAccessTokenRepository tokenRepository;

	private final EnhancedAuthenticationKeyGenerator authenticationKeyGenerator = new EnhancedAuthenticationKeyGenerator();

	private final JdbcTemplate template;

	private static final String DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT = "select token_id, token from oauth_access_token where token_id = ?";
	private static final String DEFAULT_ACCESS_TOKEN_AUTHENTICATION_SELECT_STATEMENT = "select token_id, authentication from oauth_access_token where token_id = ?";
	private final String selectAccessTokenAuthenticationSql = DEFAULT_ACCESS_TOKEN_AUTHENTICATION_SELECT_STATEMENT;
	private final String selectAccessTokenSql = DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT;

	public ThreadSafeJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
		super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
		template = new JdbcTemplate(dataSource);
	}

	@Override
	public synchronized OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		OAuth2AccessToken accessToken = null;

		final String key = authenticationKeyGenerator.extractKey(authentication);
		try {

			final OAuthAccessToken tokenOauth = tokenRepository.findByAuthenticationId(key);
			if (tokenOauth != null) {
				accessToken = deserialize(tokenOauth.getToken().getBinaryStream());
			}

		} catch (final EmptyResultDataAccessException e) {
			log.debug("Failed to find access token for authentication " + authentication);
		} catch (final IllegalArgumentException e) {
			log.error("Could not extract access token for authentication " + authentication, e);
		} catch (final SQLException e) {
			log.error("Coudl not deserialize token BLOB", e);
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
	public synchronized void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {

		super.storeAccessToken(token, authentication);
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		OAuth2AccessToken accessToken = null;

		try {
			synchronized (this) {
				accessToken = template.queryForObject(selectAccessTokenSql,
						(RowMapper<OAuth2AccessToken>) (rs, rowNum) -> deserializeAccessToken(rs.getBytes(2)),
						extractTokenKey(tokenValue));
			}
			if (accessToken == null || accessToken.isExpired()) {
				accessToken = retryReadToken(tokenValue);
			}
		} catch (final EmptyResultDataAccessException e) {
			log.info("Failed to find access token for token " + tokenValue);
			log.debug("No token found on DB");
			accessToken = retryReadToken(tokenValue);

		} catch (final IllegalArgumentException e) {
			log.warn("Failed to deserialize access token for " + tokenValue, e);
			removeAccessToken(tokenValue);
		}

		return accessToken;
	}

	private synchronized OAuth2AccessToken retryReadToken(String tokenValue) {
		OAuth2AccessToken accessToken = null;
		try {
			log.warn("Token was not recognised or was expired, double checking...");
			wait(100);
			synchronized (this) {
				accessToken = template.queryForObject(selectAccessTokenSql,
						(RowMapper<OAuth2AccessToken>) (rs, rowNum) -> deserializeAccessToken(rs.getBytes(2)),
						extractTokenKey(tokenValue));
			}
			if (accessToken == null || accessToken.isExpired()) {
				log.warn("Double check failed for token, null: {}, isExpired: {}", accessToken == null,
						accessToken == null ? false : true);
				log.debug("Token was {}", tokenValue);
			} else {
				log.warn("Double check succeded, false positive.");
				log.debug("Token was {}", tokenValue);
			}
		} catch (final InterruptedException e) {
			log.error("Could not sleep thread for retry");
		} catch (final EmptyResultDataAccessException e) {
			log.warn("Double check failed for token, null: true");
			log.debug("Token was {}", tokenValue);
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
	public OAuth2Authentication readAuthentication(String token) {
		OAuth2Authentication authentication = null;

		try {
			synchronized (this) {
				authentication = template.queryForObject(selectAccessTokenAuthenticationSql,
						(RowMapper<OAuth2Authentication>) (rs, rowNum) -> deserializeAuthentication(rs.getBytes(2)),
						extractTokenKey(token));
			}
		} catch (final EmptyResultDataAccessException e) {
			log.info("Failed to find access token for token " + token);
		} catch (final IllegalArgumentException e) {
			log.warn("Failed to deserialize authentication for " + token, e);
			removeAccessToken(token);
		}

		return authentication;
	}

	public synchronized OAuth2AccessToken getAccessTokenUsingRefreshToken(String refreshToken) {
		return template.queryForObject("select token_id, token from oauth_access_token where refresh_token = ?",
				(RowMapper<OAuth2AccessToken>) (rs, rowNum) -> deserializeAccessToken(rs.getBytes(2)), new Object[] { extractTokenKey(refreshToken) });
	}
}
