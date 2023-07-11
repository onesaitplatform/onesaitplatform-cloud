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

import java.io.UnsupportedEncodingException;
import org.apache.zeppelin.onesaitplatform.apimanager.ApiManagerClient;
import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;
import org.apache.zeppelin.onesaitplatform.parser.ApiManagerParser;



public class ApiManagerParserTest {
	
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
	static String apiManagerPath = "/api-manager/services/management";
	static String apiCallerPath = "/api-manager/server/api";
	
	// Query variables
	static String apiExampleName = "RestaurantTestAPI";
	static String apiExampleStatus = "Created";
	
	// Basic expression examples
	static String setDebugModeSt = "setDebugMode(\"True\")";
	static String setTokenSt_local = "setToken(\""+localUserToken+"\")";
	static String setTokenSt_remote = "setToken(\""+remoteUserToken+"\")";
	static String findAPISt = "findAPI(\""+apiExampleName+"\", \""+apiExampleStatus+"\", \""+localUser+"\")";
	static String createAPISt = "createAPI(\"{\"body_value1\": \"body_value2\"}\")";
	static String deleteAPISt = "deleteAPI(\"todelete\", \"1\")";
	static String callApiSt = "callAPI(\"v1/"+apiExampleName+"/\", \"GET\", \"null\")";
	static String listAPISSt = "listAPIS(\"ebustos\")";
	static String zPutSt = "z.put(\"zKey\",\"zValue\")";
	
	// Complex expression examples
	static String setZDebugModeSt = "setDebugMode(\"True\")";
	static String setZTokenSt = "setToken(z.get(\"secret_token\"))";
	static String findZAPISt = "findAPI(z.get(\"secret_name\"), \""+apiExampleStatus+"\", \""+localUser+"\")";
	static String callZApiSt = "callAPI(z.get(\"secret_url\"), \"GET\", \"null\")";
	static String listZAPISSt = "listAPIS(z.get(\"secret_user\"))";
	static String zZPutSt = "z.put(\"zZKey\", z.get(\"secret_value\"))";
	static String zZPutSetTokenSt = "z.put(\"zKey\", "+localUserToken+")";
	static String zZPutSFindAPISt = "z.put(\"zKey\", "+findAPISt+")";
	
	static ApiManagerClient opc;
	
	public static void startLocal() {
		opc = new ApiManagerClient(localHost, localPort);
		opc.setProtocol(localProtocol);
		opc.avoidSSLCertificate(localAvoidSSLCertificate);
		opc.setApiManagerPath(apiManagerPath);
		opc.setApiCallerPath(apiCallerPath);
		opc.restartDebugTrace();
		ApiManagerParser.activateDevelopMode(); // Parser uses a HashMap as context
		
		ApiManagerParser.contextExample.put("secret_debug", "true");
		ApiManagerParser.contextExample.put("secret_token", localUserToken);
		ApiManagerParser.contextExample.put("secret_name", apiExampleName);
		ApiManagerParser.contextExample.put("secret_url", "v1/"+apiExampleName+"/");
		ApiManagerParser.contextExample.put("secret_value", "soy un valor suuuper secreto");
		ApiManagerParser.contextExample.put("secret_user", "analytics");
	}
	
	public static void startRemote() {
		opc = new ApiManagerClient(remoteHost, remotePort);
		opc.setProtocol(remoteProtocol);
		opc.avoidSSLCertificate(remoteAvoidSSLCertificate);
		opc.setApiManagerPath(apiManagerPath);
		opc.setApiCallerPath(apiCallerPath);
		opc.restartDebugTrace();
		ApiManagerParser.activateDevelopMode(); // Parser uses a HashMap as context
		
		ApiManagerParser.contextExample.put("secret_debug", "true");
		ApiManagerParser.contextExample.put("secret_token", localUserToken);
		ApiManagerParser.contextExample.put("secret_name", apiExampleName);
		ApiManagerParser.contextExample.put("secret_url", "v1/"+apiExampleName+"/");
		ApiManagerParser.contextExample.put("secret_value", "soy un valor suuuper secreto");
		ApiManagerParser.contextExample.put("secret_user", "analytics");
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
  
		System.out.println("*** Testing in local ***");
		startLocal();
		testSetToken(setTokenSt_local, setZTokenSt);
		testSetDebugMode();
		testFindAPI();
		testListAPIS();
		testCallAPI();
		testzPut();
		
		System.out.println("*** Testing in remote ***");
		startRemote();
		testSetToken(setTokenSt_remote, setTokenSt_remote);
		testSetDebugMode();
		testFindAPI();
		testListAPIS();
		testCallAPI();
		testzPut();
		
    }
	
	
	public static void testSetToken(String setTokenSt, String setZTokenSt) {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setTokenSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZTokenSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testSetDebugMode() {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setDebugModeSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZTokenSt));
			opc.setDebugMode(false);
			opc.restartDebugTrace();
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZTokenSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZDebugModeSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZTokenSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testFindAPI() {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, findAPISt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, findZAPISt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testListAPIS() {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, listAPISSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, listZAPISSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testCallAPI() {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, callApiSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, callZApiSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testzPut() {
		try {
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, setZTokenSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, zPutSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, zZPutSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, zZPutSetTokenSt));
			System.out.println(ApiManagerParser.parseAndExecute(null, opc, zZPutSFindAPISt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
    
}
