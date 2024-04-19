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
package com.minsait.onesait.platform.persistence.timescaledb.util;

import lombok.Getter;
import lombok.Setter;

public class TimescaleDBPersistantException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum ErrorType {
		AGGREGATE_CREATION, AGGREGATE_POLICY, GENERAL
	}

	@Getter
	@Setter
	private ErrorType errorType;

	@Getter
	@Setter
	private String detailedMessage;

	public TimescaleDBPersistantException(String message, Throwable e) {
		super(e);
		this.errorType = ErrorType.GENERAL;
		this.detailedMessage = message;
	}

	public TimescaleDBPersistantException(String message, Throwable e, ErrorType errorType) {
		super(e);
		detailedMessage = message;
		this.errorType = errorType;
	}

	public TimescaleDBPersistantException(String message) {
		super(message);
		this.errorType = ErrorType.GENERAL;
		this.detailedMessage = message;
	}
}
