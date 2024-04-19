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
package com.minsait.onesait.platform.controlpanel.rest.management.models.model;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ModelsResponseErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Error")
	@Getter
	@Setter
	private String error;

	@ApiModelProperty(value = "Error msg")
	@Getter
	@Setter
	private String msg;
	
	public ModelsResponseErrorDTO(String msg) {
		this.error = ModelServiceException.Error.GENERIC_ERROR.name();
		this.msg = msg;
	}
	
	public ModelsResponseErrorDTO(ModelServiceException error) {
		this.error = error.getError().name();
		this.msg = error.getMessage();
	}

	public ModelsResponseErrorDTO(ModelServiceException error, String msg) {
		this.error = error.getError().name();
		this.msg = msg;
	}
		
	public HttpStatus defaultHttpStatus() {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		
		if (this.error.equals(ModelServiceException.Error.NOT_FOUND.name()) || 
				this.error.equals(ModelServiceException.Error.USER_NOT_FOUND.name())
				) {
			httpStatus = HttpStatus.NOT_FOUND;
		}
		else if (this.error.equals(ModelServiceException.Error.PERMISSION_DENIED.name())
				) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}		
		else if (this.error.equals(ModelServiceException.Error.MISSING_PARAMETER.name())
				) {
			httpStatus = HttpStatus.BAD_REQUEST;
		} else if (this.error.contentEquals(ModelServiceException.Error.DUPLICATE_MODEL_NAME.name()) ||
				this.error.contentEquals(ModelServiceException.Error.DUPLICATED_ID.name())
				) {
			httpStatus = HttpStatus.CONFLICT;
		}
		return httpStatus;
	}
	


}
