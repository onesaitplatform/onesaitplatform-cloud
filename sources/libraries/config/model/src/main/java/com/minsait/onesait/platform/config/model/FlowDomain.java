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
package com.minsait.onesait.platform.config.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "FLOW_DOMAIN", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
public class FlowDomain extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum State {
		START, STOP
	}

	@NotNull
	@Getter
	@Setter
	@Column(name = "STATE", length = 20, nullable = false)
	private String state;

	@NotNull
	@Getter
	@Setter
	@Column(name = "PORT", nullable = false)
	private Integer port;

	@NotNull
	@Getter
	@Setter
	@Column(name = "SERVICE_PORT", nullable = false)
	private Integer servicePort;

	@NotNull
	@Getter
	@Setter
	@Column(name = "HOME", nullable = false)
	private String home;

	@NotNull
	@Getter
	@Setter
	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT")
	private Boolean active;

	@Getter
	@Setter
	@Column(name = "ACCESS_TOKEN", nullable = true)
	private String accessToken;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		FlowDomain fobj = (FlowDomain) obj;
		return (state.equals(fobj.getState()) && port == fobj.getPort() && servicePort == fobj.getServicePort()
				&& home.equals(fobj.getHome()) && active == fobj.getActive());
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, port, servicePort, home, active);
	}

}
