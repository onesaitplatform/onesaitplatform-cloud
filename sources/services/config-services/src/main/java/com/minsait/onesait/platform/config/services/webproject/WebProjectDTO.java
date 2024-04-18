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
package com.minsait.onesait.platform.config.services.webproject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.WebProject;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WebProjectDTO {

	@Parameter(required = true, example = "Web project demo")
	private String description;
	@Parameter(required = false, example = "index.html")
	private String mainFile;
	@Parameter(required = false)
	private MultipartFile zip;
	@Parameter(required = true)
	private String identification;
	private String created;
	private String updated;
	private String id;
	private String userId;
	@Parameter(required = false)
	private String gitToken;
	@Parameter(required = false)
	private String gitUrl;
	@Parameter(required = false)
	private Boolean npm;
	@Parameter(required = false)
	private String runCommand;
	@Parameter(required = false)
	private String targetDirectory;

	public static WebProjectDTO convert(WebProject wp) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return WebProjectDTO.builder().id(wp.getId()).description(wp.getDescription())
				.identification(wp.getIdentification()).mainFile(wp.getMainFile()).npm(wp.getNpm())
				.runCommand(wp.getRunCommand()).targetDirectory(wp.getTargetDirectory()).userId(wp.getUser() != null ? wp.getUser().getUserId() : null)
				.created(wp.getCreatedAt() != null ? dateFormat.format(wp.getCreatedAt()) : null)
				.updated(wp.getUpdatedAt() != null ? dateFormat.format(wp.getUpdatedAt()) : null).build();
	}

	public static WebProject convert(WebProjectDTO wpd, User user) {
		final WebProject wp = new WebProject();
		wp.setId(wpd.getId());
		wp.setIdentification(wpd.getIdentification());
		wp.setMainFile(wpd.getMainFile());
		wp.setDescription(wpd.getDescription());
		wp.setUser(user);
		wp.setNpm(wpd.getNpm());
		wp.setRunCommand(wpd.getRunCommand());
		wp.setTargetDirectory(wpd.getTargetDirectory());
		return wp;
	}
}
