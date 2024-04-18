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
package com.minsait.onesait.platform.security.jwt.ri;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.dao.EmptyResultDataAccessException;
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

	public ThreadSafeJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
		super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
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
			if (log.isDebugEnabled()) {
				log.debug("Failed to find access token for authentication " + authentication);
			}
		} catch (final IllegalArgumentException e) {
			log.error("Could not extract access token for authentication " + authentication, e);
		} catch (final SQLException e) {
			log.error("Coudl not deserialize token BLOB");
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
}
