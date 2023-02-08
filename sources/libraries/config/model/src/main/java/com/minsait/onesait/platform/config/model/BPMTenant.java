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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BPM_TENANT", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@Getter
@Setter
public class BPMTenant extends OPResource {

	private static final long serialVersionUID = 1L;

	public static final String TENANT_PREFIX = "tenant-";

	@OneToMany(mappedBy = "bpmTenant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<BPMTenantAuthorization> bpmTenantAuthorizations = new HashSet<>();
}
