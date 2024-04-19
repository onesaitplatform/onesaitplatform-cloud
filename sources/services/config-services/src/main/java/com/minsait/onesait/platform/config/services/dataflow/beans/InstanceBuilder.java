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
package com.minsait.onesait.platform.config.services.dataflow.beans;

import com.minsait.onesait.platform.config.model.DataflowInstance;
import lombok.Setter;

import javax.validation.constraints.NotNull;

public class InstanceBuilder {
	@NotNull
	@Setter
	private DataflowInstance instance;

	@NotNull
	@Setter
	private InstanceCredentials credentials;

	public InstanceBuilder() {
	}

	public DataflowInstance buildInstance() {
		if(credentials.getAdminCredentials() != null){
			instance.setAdminCredentials(credentials.getAdminCredentials().getEncryptedCredentials());
		}

		if(credentials.getUserCredentials() != null){
			instance.setUserCredentials(credentials.getUserCredentials().getEncryptedCredentials());
		}

		if(credentials.getGuestCredentials() != null){
			instance.setGuestCredentials(credentials.getGuestCredentials().getEncryptedCredentials());
		}

		return instance;
	}
}
