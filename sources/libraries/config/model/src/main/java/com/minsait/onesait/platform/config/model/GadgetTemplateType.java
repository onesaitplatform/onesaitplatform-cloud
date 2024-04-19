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
package com.minsait.onesait.platform.config.model;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Configurable
@Entity
@Table(name = "GADGET_TEMPLATE_TYPE", uniqueConstraints = @UniqueConstraint(name = "UK_ID", columnNames = {
		"ID" }))

public class GadgetTemplateType extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@Getter
	@Setter
	private String id;

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 200, nullable = true)
	private String identification;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "TEMPLATE")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String template;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "TEMPLATEJS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String templateJS;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;
}
