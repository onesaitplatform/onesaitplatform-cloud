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
package org.apache.zeppelin.onesaitplatform.test;

import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;

public class TestingParams {
	
		// Client variables
		static final RestProtocols LOCAL_PROTOCOL = RestProtocols.HTTP;
		static final  RestProtocols REMOTE_PROTOCOL = RestProtocols.HTTPS;
		static final  String LOCAL_HOST = "localhost";
		static final  String REMOTE_HOST = "development.onesaitplatform.com";
		static final  int REMOTE_PORT = 443;
		static final  boolean LOCAL_AVOID_SSL_CERTIFICATE = false;
		static final  boolean REMOTE_AVOID_SSL_CERTIFICATE = true;
		
		// Iot client variables
		static final  int LOCAL_PORT_IOT_BROKER = 19000;
		static final  String LOCAL_IOT_CLIENT_NAME = "RestaurantTutorialClient";
		static final  String LOCAL_IOT_CLIENT_TOKEN = "fcbaa77ea07546b9a3daee59c5853c87";
		static final  String REMOTE_IOT_CLIENT_NAME = "RestaurantTestClient";
		static final  String REMOTE_IOT_CLIENT_TOKEN = "0a42c0ba12254a2db048e9d927a61f47";
		
		// Api client variables
		static final  int LOCAL_PORT_API_MANAGER = 19100;
		static final  String LOCAL_USER = "analytics";
		static final  String LOCAL_USER_TOKEN = "177cc6febace456eb60bbc50eed8ab2c";
		static final  String REMOTE_USER = "analytics";
		static final  String REMOTE_USER_TOKEN = "b32522cd73e84ddda519f1dff9627f40";

}

