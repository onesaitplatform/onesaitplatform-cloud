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
package com.minsait.onesait.platform.config.services.exceptions;

import lombok.Getter;

public class AIServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public enum Error {
		GENERIC_ERROR, PERMISSION_DENIED, TOKEN_NOT_FOUND
	}
	
	@Getter
	private final Error error;

	public AIServiceException(String message) {
		super(message);
		this.error = Error.GENERIC_ERROR;
	}
		
	public AIServiceException(String message, Throwable e) {
		super(message, e);
		this.error = Error.GENERIC_ERROR;
	}

	public AIServiceException(Error error) {
		super();
		this.error = error;
	}

	public AIServiceException(Error error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public AIServiceException(Error error, String message) {
		super(message);
		this.error = error;
	}

	public AIServiceException(Error error, Throwable cause) {
		super(cause);
		this.error = error;
	}
	
}
