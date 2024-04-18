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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.minsait.onesait.platform.business.services.interceptor.InterceptorCommon;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.controlpanel.security.Securityhandler;
import com.minsait.onesait.platform.controlpanel.security.X509CertService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class X509CertFilter implements Filter {

	private static final String CERT_HEADER = "X509-Cert";
	private static final String API_PREFIX = "/api/";
	private static final String LOGOUT_PREFIX = "/logout";
	private final ConfigDBDetailsService detailsService;
	private final X509CertService x509Service;
	private final Securityhandler successHandler;

	public X509CertFilter() {
		x509Service = BeanUtil.getBean(X509CertService.class);
		detailsService = BeanUtil.getBean(ConfigDBDetailsService.class);
		successHandler = BeanUtil.getBean(Securityhandler.class);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init filter X509CertFilter");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		if (requiresAuthentication(req, true)) {
			log.debug("Detected header {} in API request, loading temp autenthication", CERT_HEADER);
			InterceptorCommon.setPreviousAuthenticationOnSession(req.getSession());
			try {
				prepareX509Authentication(req);
				chain.doFilter(request, response);

			} catch (final Exception e) {
				log.error("Error on X509 REST authentication", e);

			} finally {
				log.debug("Clearing authentication contexts");
				InterceptorCommon.clearContexts(
						(Authentication) req.getSession().getAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH),
						req.getSession());
			}
		} else if (requiresAuthentication(req, false)) {
			try {
				final Authentication auth = prepareX509Authentication(req);
				if (auth != null)
					successHandler.onAuthenticationSuccess(req, (HttpServletResponse) response, auth);
				chain.doFilter(request, response);
			} catch (final Exception e) {
				log.error("Error on X509 authentication", e);
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	private boolean requiresAuthentication(HttpServletRequest req, boolean isAPI) {
		if (!isAPI) {
			return !req.getServletPath().startsWith(LOGOUT_PREFIX) && req.getHeader(CERT_HEADER) != null
					&& SecurityContextHolder.getContext().getAuthentication() == null;
		} else {
			return req.getServletPath().startsWith(API_PREFIX) && req.getHeader(CERT_HEADER) != null;
		}
	}

	private Authentication prepareX509Authentication(HttpServletRequest req)
			throws CertificateException, UnsupportedEncodingException, GenericOPException {
		x509Service.extractUserNameFromCert(req.getHeader(CERT_HEADER)).ifPresent(id -> {
			final UserDetails details = detailsService.loadUserByUsername(id);
			if (details != null) {
				final Authentication auth = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
						details.getAuthorities());
				InterceptorCommon.setContexts(auth);
				log.debug("Loaded authentication for user {}", auth.getName());
			}
		});
		return SecurityContextHolder.getContext().getAuthentication();

	}

	@Override
	public void destroy() {
		log.debug("destroying filter X509CertFilter");

	}

}
