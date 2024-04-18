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
package com.minsait.onesait.platform.controlpanel.rest.management.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Project.ProjectType;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectRestDTO {

	@ApiModelProperty(required = true)
	@NotNull
	private String identification;
	@ApiModelProperty(required = true)
	@NotNull
	private ProjectType type;
	@ApiModelProperty(required = true)
	@NotNull
	private String description;
	@Getter
	private transient MultipartFile image;

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public void setImage() {
		this.image = new MultipartFile() {
			@Override
			public void transferTo(File dest) throws IOException {
				// Do nothing because not necessary to implement
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 0;
			}

			@Override
			public String getOriginalFilename() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return new byte[0];
			}
		};
	}

}
