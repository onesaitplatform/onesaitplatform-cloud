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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class OntologyResponseErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Error")
	@Getter
	@Setter
	private String error;

	@ApiModelProperty(value = "Error msg")
	@Getter
	@Setter
	private String msg;
	
	public OntologyResponseErrorDTO() {
	}
	
	public OntologyResponseErrorDTO(OntologyServiceException error) {
		this.error = error.getError().name();
		this.msg = error.getMessage();
	}

	public OntologyResponseErrorDTO(OntologyServiceException error, String msg) {
		this.error = error.getError().name();
		this.msg = msg;
	}
		
	public HttpStatus defaultHttpStatus() {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		
		if (this.error.equals(OntologyServiceException.Error.NOT_FOUND.name()) || 
				this.error.equals(OntologyServiceException.Error.USER_NOT_FOUND.name()) ||
				this.error.equals(OntologyServiceException.Error.USER_ACCESS_NOT_FOUND.name())
				) {
			httpStatus = HttpStatus.NOT_FOUND;
		}
		else if (this.error.equals(OntologyServiceException.Error.PERMISSION_DENIED.name()) ||
				this.error.equals(OntologyServiceException.Error.USER_IS_OWNER.name())
				) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		else if (this.error.equals(OntologyServiceException.Error.MISSING_DATA_MODEL.name())
				) {
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		else if (this.error.equals(OntologyServiceException.Error.EXISTING_ONTOLOGY.name()) ||
				this.error.equals(OntologyServiceException.Error.EXISTING_DATA_MODEL.name())
				) {
			httpStatus = HttpStatus.CONFLICT;
		}
		
		return httpStatus;
	}
	


}
