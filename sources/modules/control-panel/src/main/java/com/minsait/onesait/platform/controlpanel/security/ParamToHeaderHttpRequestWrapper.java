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
package com.minsait.onesait.platform.controlpanel.security;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StringUtils;

public class ParamToHeaderHttpRequestWrapper extends HttpServletRequestWrapper {

	public ParamToHeaderHttpRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getHeader(String name) {
		final String header = super.getHeader(name);

		if (!StringUtils.isEmpty(header)) {
			return header;
		} else {
			return super.getParameter(name);
		}
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		final List<String> names = Collections.list(super.getHeaderNames());
		names.addAll(Collections.list(super.getParameterNames()));
		return Collections.enumeration(names);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		final List<String> values = Collections.list(super.getHeaders(name));
		if (!values.contains(name) && Collections.list(super.getParameterNames()).contains(name)) {
			values.add(super.getParameter(name));
		}
		return Collections.enumeration(values);
	}

}
