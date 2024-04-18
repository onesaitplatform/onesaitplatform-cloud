/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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

public class NotebookServiceException extends RuntimeException {

	private static final long serialVersionUID = 1432534144144L;

	public enum Error {
		DUPLICATE_NOTEBOOK_NAME, PERMISSION_DENIED, INVALID_FORMAT_NOTEBOOK, BAD_RESPONSE_FROM_NOTEBOOK_SERVICE, NOT_FOUND
	}
	
	@Getter
	private Error error;
	
	public NotebookServiceException(String message) {
		super(message);
	}
	
	public NotebookServiceException(String message, Throwable e) {
		super(message, e);
	}

	public NotebookServiceException(Error error) {
		super();
		this.error = error;
	}

	public NotebookServiceException(Error error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public NotebookServiceException(Error error, String message) {
		super(message);
		this.error = error;
	}

	public NotebookServiceException(Error error, Throwable cause) {
		super(cause);
		this.error = error;
	}
}
