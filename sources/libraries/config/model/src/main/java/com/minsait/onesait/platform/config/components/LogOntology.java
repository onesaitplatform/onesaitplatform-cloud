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
package com.minsait.onesait.platform.config.components;

import java.awt.geom.Point2D;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@JsonIgnoreProperties
@JsonDeserialize(using = LogOntologyDeserializer.class)
@Data
public class LogOntology {

	private String device;
	private Point2D.Double location;
	private String extraOptions;
	private String level;
	private String message;
	private String status;
	private Date timestamp;
	private String commandId;

	public LogOntology(String device, Point2D.Double location, String extraOptions, String level, String message,
			String status, Date timestamp, String commandId) {
		this.device = device;
		this.location = location;
		this.extraOptions = extraOptions;
		this.level = level;
		this.message = message;
		this.status = status;
		this.timestamp = timestamp;
		this.commandId = commandId;
	}

}
