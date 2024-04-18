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

public class Injector {
	
	public Injector(int injector, String dataToInsert) {
		this.injector = injector;
		this.dataToInsert = dataToInsert;
	}
	
	@Getter private int injector;
	@Getter private String dataToInsert;
	
	@Override
	public String toString() {
		return Integer.toString(injector);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Injector))
			return false;
		Injector that = (Injector) o;
		return this.injector == that.injector;
	}
	
	@Override
	public int hashCode() {
		return injector;
	}
}


