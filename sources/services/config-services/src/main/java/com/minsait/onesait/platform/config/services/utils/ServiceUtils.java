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
package com.minsait.onesait.platform.config.services.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceUtils {

	@Autowired
	private MessageSource messageSource;

	public static final String AUDIT_COLLECTION_NAME = "Audit_";

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public String getUserId() {
		final Authentication auth = getAuthentication();
		if (auth == null) {
			return null;
		}
		return auth.getName();
	}

	public String getRole() {
		final Authentication auth = getAuthentication();
		if (auth == null) {
			return null;
		}
		return auth.getAuthorities().toArray()[0].toString();
	}

	public String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Key:{} not found. Returns:{}", key, valueDefault);
			}
			return valueDefault;
		}
	}

	public void setSessionAttribute(HttpServletRequest request, String name, Object o) {
		WebUtils.setSessionAttribute(request, name, o);
	}

	public String jsonStringToString(String json) {

		final ObjectMapper objectMapper = new ObjectMapper();
		String formattedJson = null;

		try {
			final JsonNode tree = objectMapper.readValue(json, JsonNode.class);
			formattedJson = tree.toString();
			return formattedJson;
		} catch (final Exception e) {
			log.error("Exception reached {}", e.getMessage(), e);
			return null;
		}
	}

	public static String getAuditCollectionName(String userId) {
		if (userId.contains(".")) {
			final String replacedUser = userId.replace(".", "_");
			return AUDIT_COLLECTION_NAME + replacedUser;
		} else {
			return AUDIT_COLLECTION_NAME + userId;
		}

	}
}
