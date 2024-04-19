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
package com.minsait.onesait.platform.exception;

import lombok.Getter;

public class DashboardEngineException extends RuntimeException {

	private static final long serialVersionUID = 1432534144145L;

	public enum Error {
		PARSE_EXCEPTION, TIMEOUT_EXCEPTION, PERMISSION_DENIED, NOT_FOUND, GENERIC_EXCEPTION
	}
	
	@Getter
	private final Error error;
	
	public DashboardEngineException(String message) {
		super(message);
		this.error = Error.GENERIC_EXCEPTION;
	}
	
	public DashboardEngineException(String message, Throwable e) {
		super(message, e);
		this.error = Error.GENERIC_EXCEPTION;
	}

	public DashboardEngineException(Error error) {
		super();
		this.error = error;
	}

	public DashboardEngineException(Error error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public DashboardEngineException(Error error, String message) {
		super(message);
		this.error = error;
	}

	public DashboardEngineException(Error error, Throwable cause) {
		super(cause);
		this.error = error;
	}
}
