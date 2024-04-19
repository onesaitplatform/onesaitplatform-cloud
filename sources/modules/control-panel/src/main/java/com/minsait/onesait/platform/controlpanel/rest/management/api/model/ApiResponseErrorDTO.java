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
package com.minsait.onesait.platform.controlpanel.rest.management.api.model;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ApiResponseErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description="Error")
	@Getter
	@Setter
	private String error;

	@Schema(description="Error msg")
	@Getter
	@Setter
	private String msg;

	public ApiResponseErrorDTO() {
	}

	public ApiResponseErrorDTO(ApiManagerServiceException error) {
		this.error = error.getError().name();
		msg = error.getMessage();
	}

	public ApiResponseErrorDTO(ApiManagerServiceException error, String msg) {
		this.error = error.getError().name();
		this.msg = msg;
	}

	public HttpStatus defaultHttpStatus() {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		if (error.equals(ApiManagerServiceException.Error.NOT_FOUND.name()) ||
				error.equals(ApiManagerServiceException.Error.USER_NOT_FOUND.name()) ||
				error.equals(ApiManagerServiceException.Error.USER_ACCESS_NOT_FOUND.name())
				) {
			httpStatus = HttpStatus.NOT_FOUND;
		}
		else if (error.equals(ApiManagerServiceException.Error.PERMISSION_DENIED.name()) ||
				error.equals(ApiManagerServiceException.Error.USER_IS_OWNER.name())
				) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		else if (error.equals(ApiManagerServiceException.Error.MISSING_ONTOLOGY.name()) ||
				error.equals(ApiManagerServiceException.Error.MISSING_API_IDENTIFICATION.name()) ||
				error.equals(ApiManagerServiceException.Error.MISSING_OPERATIONS.name()) ||
				error.equals(ApiManagerServiceException.Error.INVALID_API_STATE.name())
				) {
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		else if (error.equals(ApiManagerServiceException.Error.EXISTING_API.name())
				) {
			httpStatus = HttpStatus.CONFLICT;
		}

		return httpStatus;
	}



}
