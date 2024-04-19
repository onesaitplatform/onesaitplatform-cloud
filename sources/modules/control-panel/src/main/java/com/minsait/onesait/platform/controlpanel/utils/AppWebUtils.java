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
package com.minsait.onesait.platform.controlpanel.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.security.PasswordPatternMatcher;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.controlpanel.rest.management.login.LoginManagementController;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppWebUtils {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	@Lazy
	private SessionRegistry sessionRegistry;

	@Autowired(required = false)
	private LoginManagementController controller;

	@Autowired
	private PasswordPatternMatcher passwordPatternMatcher;

	private static final String MESSAGE_STR = "message";
	private static final String INFO_MESSAGE_STR = "info";
	private static final String OAUTH_TOKEN_SESS_ATT = "oauthToken";
	public static final String IDENTIFICATION_PATERN = "[a-zA-Z0-9_-]*";
	public static final String IDENTIFICATION_PATERN_SPACES = "[a-zA-Z 0-9_-]*";

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private RoleRepository roleRepository;

	private Tika tika = null;

	@Value("${onesaitplatform.binary-repository.mimeTypesNotAllowed:octet-stream,x-javascript,application/x-msdownload}")
	private String mimeTypesNotAllowed;

	@PostConstruct
	public void init() {
		tika = new Tika();
	}

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

	public boolean isAdministrator() {
		final Role role = roleRepository.findById(getRole()).orElse(null);

		return role != null && (role.getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()) || role.getRoleParent() != null
				&& role.getRoleParent().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()));
	}

	public boolean isDeveloper() {
		final Role role = roleRepository.findById(getRole()).orElse(null);

		return role != null && (role.getId().equals(Role.Type.ROLE_DEVELOPER.name()) || role.getRoleParent() != null
				&& role.getRoleParent().getId().equals(Role.Type.ROLE_DEVELOPER.name()));
	}

	public boolean isPlatformAdmin() {

		final Role role = roleRepository.findById(getRole()).orElse(null);

		return role != null
				&& (role.getId().equals(Role.Type.ROLE_PLATFORM_ADMIN.name()) || role.getRoleParent() != null
						&& role.getRoleParent().getId().equals(Role.Type.ROLE_PLATFORM_ADMIN.name()));

	}

	public boolean isAuthenticated() {
		final Authentication auth = getAuthentication();
		return auth != null;
	}

	public boolean isUser() {

		final Role role = roleRepository.findById(getRole()).orElse(null);

		return role != null && (role.getId().equals(Role.Type.ROLE_USER.name())
				|| role.getRoleParent() != null && role.getRoleParent().getId().equals(Role.Type.ROLE_USER.name()));
	}

	public boolean isDataViewer() {

		final Role role = roleRepository.findById(getRole()).orElse(null);

		return role != null && (role.getId().equals(Role.Type.ROLE_DATAVIEWER.name()) || role.getRoleParent() != null
				&& role.getRoleParent().getId().equals(Role.Type.ROLE_DATAVIEWER.name()));

	}

	public void addRedirectMessage(String messageKey, RedirectAttributes redirect) {
		final String message = getMessage(messageKey, "Error processing request:" + messageKey);
		redirect.addFlashAttribute(MESSAGE_STR, message);
	}

	public void addRedirectInfoMessage(String messageKey, RedirectAttributes redirect) {
		final String message = getMessage(messageKey, "Info:" + messageKey);
		redirect.addFlashAttribute(INFO_MESSAGE_STR, message);
	}

	public void addRedirectMessageWithParam(String messageKey, String param, RedirectAttributes redirect) {
		final String message = getMessage(messageKey, "Error processing request") + ":" + param;
		redirect.addFlashAttribute(MESSAGE_STR, message);
	}

	public void addRedirectException(Exception exception, RedirectAttributes redirect) {
		redirect.addFlashAttribute(MESSAGE_STR, exception.getMessage());

	}

	public String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {
			log.debug("Key:" + key + " not found. Returns:" + valueDefault);
			return valueDefault;
		}
	}

	public void setSessionAttribute(HttpServletRequest request, String name, Object o) {
		WebUtils.setSessionAttribute(request, name, o);
	}

	public String getCurrentUserOauthToken() {
		final Optional<HttpServletRequest> request = getCurrentHttpRequest();

		if (request.isPresent()) {
			if (WebUtils.getSessionAttribute(request.get(), OAUTH_TOKEN_SESS_ATT) != null) {
				return (String) WebUtils.getSessionAttribute(request.get(), OAUTH_TOKEN_SESS_ATT);
			} else if (request.get().getHeader(HttpHeaders.AUTHORIZATION) != null) {
				return request.get().getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
			} else if (SecurityContextHolder.getContext().getAuthentication() != null
					&& !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)
					&& controller != null) {
				return controller.postLoginOauthNopass(SecurityContextHolder.getContext().getAuthentication())
						.getValue();

			}

		}
		throw new GenericRuntimeOPException("No request currently active");

	}

	private static Optional<HttpServletRequest> getCurrentHttpRequest() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).filter(
				requestAttributes -> ServletRequestAttributes.class.isAssignableFrom(requestAttributes.getClass()))
				.map(requestAttributes -> ((ServletRequestAttributes) requestAttributes))
				.map(ServletRequestAttributes::getRequest);
	}

	public String validateAndReturnJson(String json) {
		final ObjectMapper objectMapper = new ObjectMapper();
		String formattedJson = null;
		try {
			final JsonNode tree = objectMapper.readValue(json, JsonNode.class);
			formattedJson = tree.toString();
		} catch (final Exception e) {
			log.error("Error reading JSON by:" + e.getMessage(), e);
		}
		return formattedJson;
	}

	public boolean paswordValidation(String data) {
		return passwordPatternMatcher.isValidPassword(data);
	}

	public String beautifyJson(String json) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}

	public Object getAsObject(String json) throws JsonProcessingException {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, Object.class);
		} catch (final Exception e) {
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

		pathSegmentEncode = UriUtils.encodePathSegment(pathSegment, enc);

		return pathSegmentEncode;
	}

	public void deactivateSessions(String userId) {
		sessionRegistry.getAllSessions(userId, true).forEach(SessionInformation::expireNow);
	}

	public boolean isFileExtensionForbidden(MultipartFile file) {
		try {
			if (getAllowedFileExtensions().stream().anyMatch(file.getOriginalFilename()::contains)) {
				return false;
			}
			final String contentType = tika.detect(file.getInputStream());
			final String[] arrayMimeTypes = mimeTypesNotAllowed.split(",");
			final boolean isForbidden = Arrays.stream(arrayMimeTypes).parallel().anyMatch(contentType::contains);
			if (!isForbidden) {
				// check additional by extension

			}
			return isForbidden;

		} catch (final Exception e) {
			log.error("Error detecting MIME Type...so file is not allowed");
			return true;
		}
	}

	public Long getMaxFileSizeAllowed() {
		return (Long) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("max-size");
	}

	private List<String> getAllowedFileExtensions() {
		try {
			return Arrays.asList(
					((String) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("allowed-extensions"))
							.split(","));

		} catch (final Exception e) {
			log.error("No allowed extensions stated on Global Configuration, update your database");
			return new ArrayList<>();
		}
	}

	public void renewOauth2AccessToken(HttpServletRequest request, Authentication authentication) {
		request.getSession().setAttribute("oauthToken", controller.postLoginOauthNopass(authentication).getValue());
	}

	public String getUserOauthTokenByCurrentHttpRequest() {
		final Optional<HttpServletRequest> request = getCurrentHttpRequest();

		if (request.isPresent()) {
			if (request.get().getHeader(HttpHeaders.AUTHORIZATION) != null) {
				return request.get().getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
			} else if (SecurityContextHolder.getContext().getAuthentication() != null
					&& !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)
					&& controller != null) {
				return controller.postLoginOauthNopass(SecurityContextHolder.getContext().getAuthentication())
						.getValue();

			}

		}
		throw new GenericRuntimeOPException("No request currently active");
	}

}
