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
package com.minsait.onesait.platform.config.services.exceptions;

import lombok.Getter;

public class SecurityServiceException extends RuntimeException {

	private static final long serialVersionUID = 1432534144144L;

	public enum Error {
		METHOD_NOT_FOUND, REPOSITORY_NOT_FOUND, SERVICE_NOT_FOUND, WRONG_INPUT_LIST, SECURITY_ERROR, INVOKE_METHOD_ERROR
	}
	
	@Getter
	private Error error;
	
	public SecurityServiceException(String message) {
		super(message);
	}
	
	public SecurityServiceException(String message, Throwable e) {
		super(message, e);
	}

	public SecurityServiceException(Error error) {
		super();
		this.error = error;
	}

	public SecurityServiceException(Error error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public SecurityServiceException(Error error, String message) {
		super(message);
		this.error = error;
	}

	public SecurityServiceException(Error error, Throwable cause) {
		super(cause);
		this.error = error;
	}
}
