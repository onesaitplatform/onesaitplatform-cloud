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
package com.minsait.onesait.platform.controlpanel.controller.devicemanagement;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphDeviceDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String source;
	@Getter
	@Setter
	private String target;
	@Getter
	@Setter
	private String image;
	@Getter
	@Setter
	private String title;
	@Getter
	@Setter
	private String linkCreate;
	@Getter
	@Setter
	private String linkSource;
	@Getter
	@Setter
	private String linkTarget;

	@Getter
	@Setter
	private String classSource;
	@Getter
	@Setter
	private String classTarget;

	@Getter
	@Setter
	private String nameSource;
	@Getter
	@Setter
	private String nameTarget;

	@Getter
	@Setter
	private String status;

	@Getter
	@Setter
	private String connected;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	private Date updateAt;

	public GraphDeviceDTO(String source, String target, String linkSource, String linkTarget, String classSource,
			String classTarget, String nameSource, String nameTarget, String type, String image, String status,
			String connected, Date updateAt) {
		this.source = source;
		this.target = target;
		this.linkSource = linkSource;
		this.linkTarget = linkTarget;
		this.classSource = classSource;
		this.classTarget = classTarget;
		this.nameSource = nameSource;
		this.nameTarget = nameTarget;
		this.type = type;
		this.image = image;
		this.status = status;
		this.connected = connected;
		this.updateAt = updateAt;
	}

	public GraphDeviceDTO(String source, String target, String linkSource, String linkTarget, String classSource,
			String classTarget, String nameSource, String nameTarget, String type, String title, String linkCreate,
			String image, String status, String connected, Date updateAt) {
		super();
		this.source = source;
		this.target = target;
		this.linkSource = linkSource;
		this.linkTarget = linkTarget;
		this.classSource = classSource;
		this.classTarget = classTarget;
		this.title = title;
		this.linkCreate = linkCreate;
		this.nameSource = nameSource;
		this.nameTarget = nameTarget;
		this.type = type;
		this.image = image;
		this.status = status;
		this.connected = connected;
		this.updateAt = updateAt;
	}

	public static GraphDeviceDTO constructSingleNode(String source, String linkSource, String classSource,
			String nameSource, String image, String status, String connected) {
		return new GraphDeviceDTO(source, source, linkSource, linkSource, classSource, classSource, nameSource,
				nameSource, null, null, null, null, null);
	}

	public static GraphDeviceDTO constructSingleNodeWithTitleAndCreateLink(String source, String linkSource,
			String classSource, String nameSource, String title, String linkCreate, String image, String status,
			String connected) {
		return new GraphDeviceDTO(source, source, linkSource, linkSource, classSource, classSource, nameSource,
				nameSource, null, title, linkCreate, null, null, null, null);
	}

	@Override
	@JsonRawValue
	@JsonIgnore
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String result = null;
		try {
			result = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return result;

	}
}
