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

import java.util.ArrayList;

import org.apache.zeppelin.onesaitplatform.apimanager.ApiManagerClient;
import org.apache.zeppelin.onesaitplatform.enums.RestMethods;
import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;

import com.google.gson.JsonObject;

public class ApiManagerClientTest {
	
	// Client variables
	static RestProtocols localProtocol = TestingParams.LOCAL_PROTOCOL;
	static RestProtocols remoteProtocol = TestingParams.REMOTE_PROTOCOL;
	static String localHost = TestingParams.LOCAL_HOST;
	static String remoteHost = TestingParams.REMOTE_HOST;
	static int localPort = TestingParams.LOCAL_PORT_API_MANAGER;
	static int remotePort = TestingParams.REMOTE_PORT;
	static boolean localAvoidSSLCertificate = TestingParams.LOCAL_AVOID_SSL_CERTIFICATE;
	static boolean remoteAvoidSSLCertificate = TestingParams.REMOTE_AVOID_SSL_CERTIFICATE;
	
	// user client variables
	static String localUser = TestingParams.LOCAL_USER;
	static String localUserToken = TestingParams.LOCAL_USER_TOKEN;
	static String remoteUser = TestingParams.REMOTE_USER;
	static String remoteUserToken = TestingParams.REMOTE_USER_TOKEN;
	
	// example api
	static String apiName = "RestaurantTestApi";
	static String apiStatus = "Created";
	
	static ApiManagerClient opc;
		
	public static void startLocal() {
		opc = new ApiManagerClient(localHost, localPort);
		opc.setProtocol(localProtocol);
		opc.avoidSSLCertificate(localAvoidSSLCertificate);
	}
	
	public static void startRemote() {
		opc = new ApiManagerClient(remoteHost, remotePort);
		opc.setProtocol(remoteProtocol);
		opc.avoidSSLCertificate(remoteAvoidSSLCertificate);
	}	

	public static void main(String[] args) {
		
		System.out.println("*** Testing in local ***");
		testLocalList();
		testLocalFind();
		testLocalCall();
		System.out.println("*** Testing in remote ***");
		testRemoteList();
		testRemoteFind();
		testRemoteCall();
		
	}
	
	public static void testLocalCall() {
		
		try {
			startLocal();
			ArrayList<String> pathParams = new ArrayList<String>();
			//pathParams.add("path_param_1");
			//pathParams.add("path_param_2");
			
			JsonObject queryParams = new JsonObject();
			//queryParams.addProperty("query_param_1", "query_value_1");
			//queryParams.addProperty("query_param_2", "query_value_2");
			
			startLocal();
			System.out.println(opc.setToken(localUserToken));
			System.out.println(opc.request(RestMethods.GET, "RestaurantTestAPI", "1", pathParams, queryParams, null));
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocalList() {
		try {
			startLocal();
			System.out.println(opc.setToken(localUserToken));
			System.out.println(opc.list(localUser));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocalFind() {
		try {
			startLocal();
			System.out.println(opc.setToken(localUserToken));
			System.out.println(opc.find(apiName, apiStatus, localUser));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testRemoteCall() {
		
		try {
			ArrayList<String> pathParams = new ArrayList<String>();
			//pathParams.add("path_param_1");
			//pathParams.add("path_param_2");
			
			JsonObject queryParams = new JsonObject();
			//queryParams.addProperty("query_param_1", "query_value_1");
			//queryParams.addProperty("query_param_2", "query_value_2");
			
			startRemote();
			System.out.println(opc.setToken(remoteUserToken));
			System.out.println(opc.request(RestMethods.GET, "RestaurantTestApi", "1", pathParams, queryParams, null));
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteList() {
		try {
			startRemote();
			System.out.println(opc.setToken(remoteUserToken));
			System.out.println(opc.list(remoteUser));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteFind() {
		try {
			startRemote();
			System.out.println(opc.setToken(remoteUserToken));
			System.out.println(opc.find(apiName, apiStatus, remoteUser));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
