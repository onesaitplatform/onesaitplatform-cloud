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

public class OntologyServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public enum Error {
		GENERIC_ERROR, 
		PERMISSION_DENIED, NOT_FOUND, EXISTING_ONTOLOGY, EXISTING_DATA_MODEL,
		USER_NOT_FOUND, USER_ACCESS_NOT_FOUND, USER_IS_OWNER, USER_IS_ADMIN,
		MISSING_DATA_MODEL
	}
	
	@Getter
	private final Error error;

	public OntologyServiceException(String message) {
		super(message);
		this.error = Error.GENERIC_ERROR;
	}
	
	public OntologyServiceException(String message, Throwable e) {
		super(message, e);
		this.error = Error.GENERIC_ERROR;
	}
	
	public OntologyServiceException(String message, Error error) {
		super(message);
		this.error = error;
	}
	
	public OntologyServiceException(String message, Throwable e, Error error) {
		super(message, e);
		this.error = error;
	}
	
}
