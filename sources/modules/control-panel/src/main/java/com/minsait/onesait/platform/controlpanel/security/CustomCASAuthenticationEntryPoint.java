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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

public class CustomCASAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";

	private ServiceProperties serviceProperties;

	private String loginUrl;

	/**
	 * Determines whether the Service URL should include the session id for the
	 * specific user. As of CAS 3.0.5, the session id will automatically be
	 * stripped. However, older versions of CAS (i.e. CAS 2), do not automatically
	 * strip the session identifier (this is a bug on the part of the older server
	 * implementations), so an option to disable the session encoding is provided
	 * for backwards compatibility.
	 *
	 * By default, encoding is enabled.
	 */
	private boolean encodeServiceUrlWithSessionId = true;

	// ~ Methods
	// ========================================================================================================

	public void afterPropertiesSet() {
		Assert.hasLength(loginUrl, "loginUrl must be specified");
		Assert.notNull(serviceProperties, "serviceProperties must be specified");
		Assert.notNull(serviceProperties.getService(), "serviceProperties.getService() cannot be null.");
	}

	@Override
	public final void commence(final HttpServletRequest servletRequest, final HttpServletResponse response,
			final AuthenticationException authenticationException) throws IOException, ServletException {

		servletRequest.getSession().setAttribute(BLOCK_PRIOR_LOGIN, servletRequest.getServletPath());

		final String urlEncodedService = createServiceUrl(servletRequest, response);
		final String redirectUrl = createRedirectUrl(urlEncodedService);

		preCommence(servletRequest, response);

		response.sendRedirect(redirectUrl);
	}

	/**
	 * Constructs a new Service Url. The default implementation relies on the CAS
	 * client to do the bulk of the work.
	 *
	 * @param request
	 *            the HttpServletRequest
	 * @param response
	 *            the HttpServlet Response
	 * @return the constructed service url. CANNOT be NULL.
	 */
	protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
		return CommonUtils.constructServiceUrl(null, response, serviceProperties.getService(), null,
				serviceProperties.getArtifactParameter(), encodeServiceUrlWithSessionId);
	}

	/**
	 * Constructs the Url for Redirection to the CAS server. Default implementation
	 * relies on the CAS client to do the bulk of the work.
	 *
	 * @param serviceUrl
	 *            the service url that should be included.
	 * @return the redirect url. CANNOT be NULL.
	 */
	protected String createRedirectUrl(final String serviceUrl) {
		return CommonUtils.constructRedirectUrl(loginUrl, serviceProperties.getServiceParameter(), serviceUrl,
				serviceProperties.isSendRenew(), false);
	}

	/**
	 * Template method for you to do your own pre-processing before the redirect
	 * occurs.
	 *
	 * @param request
	 *            the HttpServletRequest
	 * @param response
	 *            the HttpServletResponse
	 */
	protected void preCommence(final HttpServletRequest request, final HttpServletResponse response) {
		// DO-NOTHING
	}

	/**
	 * The enterprise-wide CAS login URL. Usually something like
	 * <code>https://www.mycompany.com/cas/login</code>.
	 *
	 * @return the enterprise-wide CAS login URL
	 */
	public final String getLoginUrl() {
		return loginUrl;
	}

	public final ServiceProperties getServiceProperties() {
		return serviceProperties;
	}

	public final void setLoginUrl(final String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public final void setServiceProperties(final ServiceProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 *
	 * @param encodeServiceUrlWithSessionId
	 *            whether to encode the service url with the session id or not.
	 */
	public final void setEncodeServiceUrlWithSessionId(final boolean encodeServiceUrlWithSessionId) {
		this.encodeServiceUrlWithSessionId = encodeServiceUrlWithSessionId;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 *
	 * @return whether to encode the service url with the session id or not.
	 *
	 */
	protected boolean getEncodeServiceUrlWithSessionId() {
		return encodeServiceUrlWithSessionId;
	}
}
