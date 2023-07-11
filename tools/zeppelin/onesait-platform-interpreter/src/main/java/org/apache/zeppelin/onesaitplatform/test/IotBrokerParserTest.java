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

import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;
import org.apache.zeppelin.onesaitplatform.iotbroker.IotBrokerClient;
import org.apache.zeppelin.onesaitplatform.parser.IotBrokerParser;


public class IotBrokerParserTest {
	
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
	
	// Expression examples
	static String setDebugModeSt = "setDebugMode(\"True\")";
	static String initConnectionSt_local = "initConnection(\""+localIotClientName+"\",\""+localIotClientToken+"\")";
	static String initConnectionSt_remote = "initConnection(\""+remoteIotClientName+"\",\""+remoteIotClientToken+"\")";
	
	static String dbNativeQuerySt = "db.Restaurants.find().limit(3)";
	static String dbSqlQuerySt = "select c from Restaurants as c limit 3";
	static String dbNativeBatchQuerySt = "paginatedQuery(db.Restaurants.find())";
	static String dbSqlBatchQuerySt = "paginatedQuery(select * from Restaurants)";
	
	static String insertSt = "insert(\"RestaurantTest\",{\"Restaurant\":{\"address\":{\"building\":\"668\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 668 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":\"2\"}],\"name\":\"Dj Reynolds 99\",\"restaurant_id\":\"30191899\"}})";
	static String inserListtSt = "insert(\"RestaurantTest\",[{\"Restaurant\":{\"address\":{\"building\":\"668\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 668 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":\"2\"}],\"name\":\"Dj Reynolds 99\",\"restaurant_id\":\"30191899\"}},"
														+"{\"Restaurant\":{\"address\":{\"building\":\"668\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 668 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":\"2\"}],\"name\":\"Dj Reynolds 99\",\"restaurant_id\":\"30191899\"}}])";
	static String zPutSt = "z.put(\"restaurants\", select c from Restaurants as c)";
	static String asZtableSt = "asZTable(select c from Restaurants as c limit 1)";
	
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
	
	public static void main(String[] args) throws UnsupportedEncodingException {
    	
		System.out.println("*** Testing in local ***");
		testLocalInitConnection();
		testLocalSetDebugMode();
		testQueries();
		testBatchQueries();
		testSqlQueries();
		testInsert();
		testZPut();
		testAsZTable();
		System.out.println("*** Testing in remote ***");
		testRemoteInitConnection();
		testRemoteSetDebugMode();
		testQueries();
		testBatchQueries();
		testSqlQueries();
		testInsert();
		testZPut();
		testAsZTable();

    }
	
	public static void testLocalInitConnection() {
		try {
			startLocal();
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, initConnectionSt_local));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocalSetDebugMode() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, setDebugModeSt));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, initConnectionSt_local));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteInitConnection() {
		try {
			startRemote();
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, initConnectionSt_remote));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testRemoteSetDebugMode() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, setDebugModeSt));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, initConnectionSt_remote));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testQueries() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, dbNativeQuerySt));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, dbSqlQuerySt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testBatchQueries() {
		try {
			opc.setBatchSize(10);
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, dbSqlBatchQuerySt).size());
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, dbNativeBatchQuerySt).size());
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testSqlQueries() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, "select * from RestaurantTest limit 3"));
			String entity = "{\"Restaurant\":{\"address\":{\"building\":\"668\",\"coord\":[-73.00513559999999,40.7676919],\"street\":\"West 668 Street\",\"zipcode\":\"10019\"},\"borough\":\"Manhattan\",\"cuisine\":\"Irish\",\"grades\":[{\"date\":\"2014-09-06T00:00:00Z\",\"grade\":\"A\",\"score\":\"2\"}],\"name\":\"Dj Reynolds 99\",\"restaurant_id\":\"30191899\"}}";
			String insert_query = "insert(\"RestaurantTest\", "+entity+")";
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, insert_query));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, "update RestaurantTest set Restaurant.name=\"Dj Reynolds 98\" where Restaurant.name=\"Dj Reynolds 99\""));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, "delete from RestaurantTest where Restaurant.name=\"Dj Reynolds 98\""));

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static void testInsert() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, insertSt));
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, inserListtSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testZPut() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, zPutSt));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testAsZTable() {
		try {
			System.out.println(IotBrokerParser.parseAndExecute(null, opc, asZtableSt));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
	
	
	
	
}
