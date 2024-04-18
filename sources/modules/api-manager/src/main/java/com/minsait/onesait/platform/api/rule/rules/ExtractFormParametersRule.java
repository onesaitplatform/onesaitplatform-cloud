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
package com.minsait.onesait.platform.api.rule.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;

import lombok.extern.slf4j.Slf4j;

@Component
@Rule
@Slf4j
public class ExtractFormParametersRule {

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean processRequest(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final HttpServletRequest request = (HttpServletRequest) data.get(ApiServiceInterface.REQUEST);
		return ServletFileUpload.isMultipartContent(request);
	}

	@Action
	public void extractFormParameters(Facts facts) {
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final HttpServletRequest request = (HttpServletRequest) data.get(ApiServiceInterface.REQUEST);
		final LinkedMultiValueMap<String, Object> parameterMap = new LinkedMultiValueMap<>();
		final MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		mRequest.getFileMap().entrySet().forEach(e -> {
			try {
				final File file = new File("/tmp/" + e.getValue().getOriginalFilename());
				Files.write(file.toPath(), e.getValue().getBytes());
				parameterMap.add(e.getKey(), new FileSystemResource(file));
			} catch (final IOException e1) {
				log.error("Could not create temp file for multipart request");
			}
		});
		mRequest.getParameterMap().entrySet().forEach(e -> {
			final String[] values = e.getValue();
			// extract string parameters if not an actual array
			if (values != null && values.length == 1)
				parameterMap.add(e.getKey(), values[0]);
			else
				parameterMap.add(e.getKey(), values);
		});

		data.put(ApiServiceInterface.FORM_PARAMETER_MAP, parameterMap);
	}
}
