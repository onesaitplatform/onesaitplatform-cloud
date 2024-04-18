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
package com.minsait.onesait.platform.router.service.app.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class OperationResultModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String result;

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private String errorCode;

	@Getter
	@Setter
	private String operation;

	@Getter
	@Setter
	private boolean status;

	@Override
	public String toString() {
		return "OperationResultModel [result=" + result + ", message=" + message + ", errorCode=" + errorCode
				+ ", operation=" + operation + "]";
	}

}