/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.commons.model;

public class DBResult {

	protected static final String OK_PROPERTY = "ok";
	protected static final String ERROR_MESSAGE_PROPERTY = "errorMessage";

	protected boolean ok;
	protected String errorMessage;
	private String id;

	public boolean isOk() {
		return ok;
	}

	public DBResult setOk(final boolean ok) {
		this.ok = ok;
		return this;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public DBResult setErrorMessage(final String errorMessage) {

		this.errorMessage = errorMessage;
		return this;
	}

	public String getId() {
		return id;
	}

	public DBResult setId(String id) {
		this.id = id;
		return this;
	}
}
