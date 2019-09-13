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
package com.indracompany.sofia2.metricExample.endpoints;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

@Component
public class CustomEndpoint implements Endpoint<List<String>> {

	@Override
	public String getId() {
		return "CustomEndPoint";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isSensitive() {
		return false;
	}

	@Override
	public List<String> invoke() {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add("Hola 1");
		messages.add("Hola 2");
		return messages;
	}
	
	

}
