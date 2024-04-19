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
package com.minsait.onesait.platform.serverless.exception;

import lombok.Getter;

public class ApplicationException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public enum Code {
		CONFLICT(409), NOT_FOUND(404), INTERNAL_ERROR(500), FORBIDDEN(403), BAD_REQUEST(400);

		private int code;

		private Code(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	@Getter
	private final Code code;

	public ApplicationException(String message, Code code) {
		super(message);
		this.code = code;
	}
}
