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
package com.minsait.onesait.platform.controlpanel.controller.scalability.msgs;

import lombok.Getter;

public class InjectorStatus {

	@Getter private final int id;
	@Getter private final int sent;
	@Getter private final int errors;
	@Getter private final float throughput;
	@Getter private final long timeAlive;
	@Getter private final float throughputPeriod;
	@Getter private final String protocol;
	
	public InjectorStatus(int id, int sent, int errors, float throughput, long timeAlive, float throughputPeriod, String protocol){
		this.id = id;
		this.sent = sent;
		this.errors = errors;
		this.throughput = throughput;
		this.timeAlive = timeAlive;
		this.throughputPeriod = throughputPeriod;
		this.protocol = protocol;
	}
}
