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
package com.minsait.onesait.platform.business.services.datasources.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutputMessage {

	private String data;
	private String time;
	private Long startTime;
	private boolean error;
	private int code;

	public OutputMessage(final String data, final String time, final Long startTime, final boolean error, int code) {
		this.data = data;
		this.time = time;
		this.startTime = startTime;
		this.error = error;
		this.code = code;
	}
}