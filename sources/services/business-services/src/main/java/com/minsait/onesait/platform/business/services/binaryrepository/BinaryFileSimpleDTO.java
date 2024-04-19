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
package com.minsait.onesait.platform.business.services.binaryrepository;

import java.io.Serializable;

import com.minsait.onesait.platform.config.model.BinaryFile;

import lombok.Getter;
import lombok.Setter;

public class BinaryFileSimpleDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String link;
	
	@Getter
	@Setter
	private String fileName;
	
	@Getter
	@Setter
	private String metadata;
	
	@Getter
	@Setter
	private String contentType;
	
	@Getter
	@Setter
	private Boolean owned;

	public BinaryFileSimpleDTO(BinaryFile binaryFile, String urlBase) {
		id = binaryFile.getId();
		fileName = binaryFile.getFileName();
		metadata = binaryFile.getMetadata();
		contentType = binaryFile.getMime();
		link = urlBase + binaryFile.getId();
	}
}