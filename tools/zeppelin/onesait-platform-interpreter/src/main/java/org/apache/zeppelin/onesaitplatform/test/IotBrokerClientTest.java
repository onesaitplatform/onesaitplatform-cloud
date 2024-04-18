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
import org.apache.zeppelin.onesaitplatform.iotbroker.IotBrokerClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class IotBrokerClientTest {
	
	// Client variables
	static RestProtocols localProtocol = TestingParams.LOCAL_PROTOCOL;
	static RestProtocols remoteProtocol = TestingParams.REMOTE_PROTOCOL;
	static String localHost = TestingParams.LOCAL_HOST;
	static String remoteHost = TestingParams.REMOTE_HOST;
	static int localPort = TestingParams.LOCAL_PORT_IOT_BROKER;
	static int remotePort = TestingParams.REMOTE_PORT;
	static boolean localAvoidSSLCertificate = TestingParams.LOCAL_AVOID_SSL_CERTIFICATE;
	static boolean remoteAvoidSSLCertificate = TestingParams.REMOTE_AVOID_SSL_CERTIFICATE;
	
	// Iot client variables
	static String localIotClientName = TestingParams.LOCAL_IOT_CLIENT_NAME;
	static String localIotClientToken = TestingParams.LOCAL_IOT_CLIENT_TOKEN;
	static String remoteIotClientName = TestingParams.REMOTE_IOT_CLIENT_NAME;
	static String remoteIotClientToken = TestingParams.REMOTE_IOT_CLIENT_TOKEN;
	
	static IotBrokerClient opc;
	
	public static void startLocal() {
		opc = new IotBrokerClient(localHost, localPort);
		opc.setProtocol(localProtocol);
		opc.avoidSSLCertificate(localAvoidSSLCertificate);
	}
	
	public static void startRemote() {
		opc = new IotBrokerClient(remoteHost, remotePort);
		opc.setProtocol(remoteProtocol);
		opc.avoidSSLCertificate(remoteAvoidSSLCertificate);
	}

	public static void main(String[] args) {
		
		System.out.println("*** Testing in local ***");
		testLocalQuery();
		testLocalInsert();
		testLocalQueryBatch();
		System.out.println("*** Testing in remote ***");
		testRemoteQuery();
		testRemoteInsert();
		testRemoteQueryBatch();

	}
	
	public static void testLocalQuery() {
		try {
			startLocal();
			
			System.out.println(opc.join(localIotClientName, localIotClientToken));
			System.out.println(opc.query("Restaurants", "select * from Restaurants limit 3", "SQL"));
			System.out.println(opc.query("RestaurantTest",  "select * from RestaurantTest limit 3", "SQL"));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocalInsert() {
		try {
			startLocal();
			System.out.println(opc.join(localIotClientName, localIotClientToken));
			String newRestaurants = "[{\"Restaurant\":{\"address\":{\"building\":\"666\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 666 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":2}],\"name\":\"Dj Reynolds 5\",\"restaurant_id\":\"30191843\"}}"
					+ ",{\"Restaurant\":{\"address\":{\"building\":\"666\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 666 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":2}],\"name\":\"Dj Reynolds 6\",\"restaurant_id\":\"30191842\"}}]";
			JsonParser jsonParser = new JsonParser();
	        JsonArray jsonArray = jsonParser.parse(newRestaurants).getAsJsonArray();
			//System.out.println(opc.insert("RestaurantTest", jsonArray));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocalQueryBatch() {
		try {
			startLocal();
			System.out.println(opc.join(localIotClientName, localIotClientToken));
			System.out.println(opc.queryBatch("RestaurantTest", "select * from RestaurantTest", "SQL", 2));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteQuery() {
		try {
			startRemote();
			
			System.out.println(opc.join(remoteIotClientName, remoteIotClientToken));
			System.out.println(opc.query("Restaurants", "select * from Restaurants limit 3", "SQL"));
			System.out.println(opc.query("RestaurantTest",  "select * from RestaurantTest limit 3", "SQL"));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteInsert() {
		try {
			startRemote();
			System.out.println(opc.join(remoteIotClientName, remoteIotClientToken));
			String newRestaurants = "[{\"Restaurant\":{\"address\":{\"building\":\"666\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 666 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":2}],\"name\":\"Dj Reynolds 5\",\"restaurant_id\":\"30191843\"}}"
					+ ",{\"Restaurant\":{\"address\":{\"building\":\"666\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 666 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":2}],\"name\":\"Dj Reynolds 6\",\"restaurant_id\":\"30191842\"}}]";
			JsonParser jsonParser = new JsonParser();
	        JsonArray jsonArray = jsonParser.parse(newRestaurants).getAsJsonArray();
			System.out.println(opc.insert("RestaurantTest", jsonArray));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteQueryBatch() {
		try {
			startRemote();
			System.out.println(opc.join(remoteIotClientName, remoteIotClientToken));
			System.out.println(opc.queryBatch("RestaurantTest", "select * from RestaurantTest", "SQL", 2));
			System.out.println(opc.leave());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
