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
package com.minsait.onesait.platform.commons.exception;

public class GenericOPException extends Exception {

	private static final long serialVersionUID = 1L;

	public GenericOPException() {

	}

	public GenericOPException(String message) {
		super(message);
	}

	public GenericOPException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericOPException(Throwable cause) {
		super(cause);
	}

}
