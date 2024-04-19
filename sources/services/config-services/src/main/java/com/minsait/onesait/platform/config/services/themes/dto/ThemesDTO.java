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
package com.minsait.onesait.platform.config.services.themes.dto;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

public class ThemesDTO {
	
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String identification;
	
	@Getter
	@Setter
	private String loginTitle;
	
	@Getter
	@Setter
	private String loginTitleEs;
	
	@Getter
	@Setter
	private MultipartFile image;
	
	@Getter
	@Setter
	private String image64;

	@Getter
	@Setter
	private MultipartFile headerImage;
	
	@Getter
	@Setter
	private String headerImage64;
	@Getter
	@Setter
	private JSONObject json;
	
	@Getter
	@Setter
	private String backgroundColor;
	
	@Getter
	@Setter
	private String footerText;
	
	@Getter
	@Setter
	private String footerTextEs;
	
	@Getter
	@Setter
	private String css;
	
	@Getter
	@Setter
	private String js;

}
