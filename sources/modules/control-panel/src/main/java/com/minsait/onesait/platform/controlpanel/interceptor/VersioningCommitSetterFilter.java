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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.commons.http.MultiReadableHttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class VersioningCommitSetterFilter implements Filter {

	private static final String COMMIT_MSG_PARAM = "commit-msg-inputs";
	public static final String VERSIONING_ENABLED_ATT = "versioningEnabled";

	@Autowired
	private VersioningBusinessService versioningBusinessService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NO-OP

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		setSessionAttribute(req, versioningBusinessService.isActive());
		if (versioningBusinessService.isActive()) {
			VersioningCommitContextHolder.setProcessPostAllEvents(true);

			if (req.getParameter(COMMIT_MSG_PARAM) != null) {
				VersioningCommitContextHolder.setCommitMessage(req.getParameter(COMMIT_MSG_PARAM));
				chain.doFilter(request, response);
				VersioningCommitContextHolder.setCommitMessage(null);
			} else {
				if (req.getContentType() != null
						&& req.getContentType().toLowerCase().contains("multipart/form-data")) {
					final MultiReadableHttpServletRequest multiReadHttpServletRequest = new MultiReadableHttpServletRequest(
							req);
					final CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
					final MultipartHttpServletRequest multipartRequest = commonsMultipartResolver
							.resolveMultipart(multiReadHttpServletRequest);
					if (multipartRequest.getParameterMap().containsKey(COMMIT_MSG_PARAM)) {
						VersioningCommitContextHolder.setCommitMessage(multipartRequest.getParameter(COMMIT_MSG_PARAM));
					}
					chain.doFilter(multiReadHttpServletRequest, response);
				} else {
					chain.doFilter(request, response);
				}
				VersioningCommitContextHolder.setCommitMessage(null);
			}

		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
		// NO-OP

	}

	// Set session attribute for thymeleaf commit pop-up
	private void setSessionAttribute(HttpServletRequest req, Boolean isEnabled) {
		Optional.ofNullable(req.getSession(false)).ifPresent(s -> {
			final Boolean isActuallyEnabled = (Boolean) s.getAttribute(VERSIONING_ENABLED_ATT);
			if (isActuallyEnabled == null || isEnabled.compareTo(isActuallyEnabled) != 0) {
				s.setAttribute(VERSIONING_ENABLED_ATT, isEnabled);
			}
		});
	}

}
