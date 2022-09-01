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
package com.minsait.onesait.platform.business.services.gadget;

public class GadgetDatasourceBusinessServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public static enum ErrorType {
		NOT_FOUND, UNAUTHORIZED, NOT_DATA
	};

	private ErrorType errorType;

	public GadgetDatasourceBusinessServiceException(String message) {
		super(message);
	}

	public GadgetDatasourceBusinessServiceException(ErrorType type, String message) {
		super(message);
		errorType = type;
	}

	public ErrorType getErrorType() {
		return errorType;
	}
}