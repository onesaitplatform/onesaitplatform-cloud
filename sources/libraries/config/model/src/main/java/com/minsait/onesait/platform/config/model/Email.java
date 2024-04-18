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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configurable
@Getter
@Setter
@ToString(exclude = { "file" })
@Entity
@Table(name = "EMAIL", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class Email extends OPResource {

	private static final long serialVersionUID = -3383279797731473231L;

	@Column(name = "DESCRIPTION")
	private String description;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "FILE")
	@Lob
	@Type(type = "org.hibernate.type.ImageType")
	private byte[] file;

	public String fileName() {
		return getIdentification() + ".docx";
	}

}
