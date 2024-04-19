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
package com.minsait.onesait.platform.persistence.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.Getter;
import lombok.Setter;

public class DBPersistenceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DBPersistenceException(String message) {
		super(message);
		this.detailedMessage = message;
	}

	public DBPersistenceException(String message, Throwable e) {
		super(message, e);
		this.detailedMessage = message;
	}

	public DBPersistenceException(Throwable e) {
		super(e);
		this.detailedMessage = e.getMessage();
	}

	public DBPersistenceException(ErrorResult errorResult) {
		super(errorResult.getOriginalMessage());
		this.detailedMessage = errorResult.getOriginalMessage();
		this.errorsResult.add(errorResult);
	}

	public DBPersistenceException(ErrorResult errorResult, String message) {
		super(message);
		this.detailedMessage = message;
		this.errorsResult.add(errorResult);
	}

	public DBPersistenceException(Throwable e, ErrorResult errorResult) {
		super(e);
		this.detailedMessage = e.getMessage();
		this.errorsResult.add(errorResult);
	}

	public DBPersistenceException(Throwable e, ErrorResult errorResult, String message) {
		super(e);
		this.detailedMessage = message;
		this.errorsResult.add(errorResult);
	}

	public DBPersistenceException(List<ErrorResult> errorsResult, String message) {
		super(message);
		this.detailedMessage = message;
		this.errorsResult.addAll(errorsResult);
	}

	public DBPersistenceException(Throwable e, List<ErrorResult> errorsResult) {
		super(e);
		this.detailedMessage = e.getMessage();
		this.errorsResult.addAll(errorsResult);
	}

	public DBPersistenceException(Throwable e, List<ErrorResult> errorsResult, String message) {
		super(e);
		this.detailedMessage = message;
		this.errorsResult.addAll(errorsResult);
	}

	@Getter
	@Setter
	private String detailedMessage;

	@Getter
	private List<ErrorResult> errorsResult = new ArrayList<>();

}
