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
package com.minsait.onesait.platform.security;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minsait.onesait.platform.commons.security.PasswordPatternMatcher;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppWebUtils {

	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private PasswordPatternMatcher passwordPatternMatcher;

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public String getUserId() {
		Authentication auth = getAuthentication();
		if (auth == null)
			return null;
		return auth.getName();
	}

	public String getRole() {
		Authentication auth = getAuthentication();
		if (auth == null)
			return null;
		return auth.getAuthorities().toArray()[0].toString();
	}

	public boolean isAdministrator() {
		return (getRole().equals(ADMINISTRATOR));
	}

	public void addRedirectMessage(String messageKey, RedirectAttributes redirect) {
		String message = getMessage(messageKey, "Error processing request");
		redirect.addFlashAttribute("message", message);

	}

	public void addRedirectException(Exception exception, RedirectAttributes redirect) {
		redirect.addFlashAttribute("message", exception.getMessage());

	}

	public String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (Exception e) {
			log.debug("Key:" + key + " not found. Returns:" + valueDefault);
			return valueDefault;
		}
	}

	public void setSessionAttribute(HttpServletRequest request, String name, Object o) {
		WebUtils.setSessionAttribute(request, name, o);
	}

	public String validateAndReturnJson(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		String formattedJson = null;
		try {
			JsonNode tree = objectMapper.readValue(json, JsonNode.class);
			formattedJson = tree.toString();
		} catch (Exception e) {
			log.error("Error reading JSON by:" + e.getMessage(), e);
		}
		return formattedJson;
	}

	public boolean paswordValidation(String data) {
		return passwordPatternMatcher.isValidPassword(data);
	}

	public String beautifyJson(String json) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}

	public Object getAsObject(String json) throws JsonProcessingException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, Object.class);
		} catch (Exception e) {
			log.error("Impossible to convert to Object, returning the same");
			return json;
		}

	}

	public String encodeUrlPathSegment(final String pathSegment, final HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		String pathSegmentEncode = "";
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		try {
			pathSegmentEncode = UriUtils.encodePathSegment(pathSegment, enc);
		} catch (UnsupportedEncodingException uee) {
			log.warn("Error encoding path segment " + uee.getMessage());
		}
		return pathSegmentEncode;
	}

}
