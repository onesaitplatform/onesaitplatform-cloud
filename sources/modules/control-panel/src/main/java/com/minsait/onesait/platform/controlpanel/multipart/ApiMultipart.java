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
package com.minsait.onesait.platform.controlpanel.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

public class ApiMultipart {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String swaggerJson;

	@Getter
	@Setter
	private Ontology ontology;

	@Getter
	@Setter
	private User user;

	@Getter
	@Setter
	private boolean sslCertificate;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private Integer numversion;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String category;

	@Getter
	@Setter
	private String endpoint;

	@Getter
	@Setter
	private String endpointExt;

	@Getter
	@Setter
	private String state;

	@Getter
	@Setter
	private String metaInf;

	@Getter
	@Setter
	private String imageType;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private Integer apicachetimeout;

	@Getter
	@Setter
	private Integer apilimit;

	@Getter
	@Setter
	private ApiType apiType;

	@Getter
	@Setter
	private Double assessment;

	@Getter
	@Setter
	private Date createdAt;

	@Getter
	@Setter
	private Date updatedAt;

	@Getter
	private MultipartFile image;

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public void setImage() {
		image = new MultipartFile() {
			@Override
			public void transferTo(File dest) throws IOException {
				// Method necessary as part of a MultipartFile but will not be used
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
