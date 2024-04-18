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
package com.minsait.onesait.platform.controlpanel.rest.management.bpm;

import java.io.IOException;

public interface BpmService {

	Object getAllProcessDef(String bearer);

	Object getAllProcessDefById(String id, String bearer);
	
	String getProcessDefXmlById(String id, String bearer);
	
	Object cloneProcessDef(BpmRequestDTO request, String bearer) throws IOException;
	
	Object getAllDeployments(String bearer);

	Object getDeploymentById(String id, String bearer);
	
	Object getAllProcessInstances(String bearer);

	Object getProcessInstanceById(String id, String bearer);

}
