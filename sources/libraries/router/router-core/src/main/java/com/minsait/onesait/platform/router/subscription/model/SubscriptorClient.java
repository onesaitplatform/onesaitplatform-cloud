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
package com.minsait.onesait.platform.router.subscription.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriptorClient implements Serializable {

	private static final long serialVersionUID = 1L;

	private String subscriptionGW;
	
	private String subscriptionId;
	
	private String clientId;

	private String callbackEndpoint;

	private String sessionKey;

	public SubscriptorClient(String subscriptionGW, String subscriptionId, String clientId, String callbackEndpoint, String sessionKey) {
		super();
		this.clientId = clientId;
		this.callbackEndpoint = callbackEndpoint;
		this.sessionKey = sessionKey;
		this.subscriptionId = subscriptionId;
		this.subscriptionGW=subscriptionGW;
	}
}
