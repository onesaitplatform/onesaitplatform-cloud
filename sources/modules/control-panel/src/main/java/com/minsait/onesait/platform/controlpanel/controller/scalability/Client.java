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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import com.minsait.onesait.platform.client.exception.MqttClientException;

public interface Client {
	public void createClient(String url) throws MqttClientException;

	public void connect(String token, String clientPlatform, String clientPlatformInstance, boolean avoidSSLValidation)
			throws MqttClientException;

	public void insertInstance(String ontology, String instance) throws MqttClientException;

	public void disconnect() throws MqttClientException;

	public String getProtocol();
}
