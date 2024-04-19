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
package com.minsait.onesait.platform.config.services.gadget.dto;

import java.io.Serializable;
import java.util.Date;

import com.minsait.onesait.platform.config.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GadgetDatasourceDTOForList implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identification;

	private String id;

	private String mode;

	private String query;

	private String dbtype;

	private String ontologyIdentification;

	private Integer refresh;

	private Integer maxvalues;

	private String description;

	private String config;
	private User user;
	private Date createdAt;
	private Date updatedAt;
}
