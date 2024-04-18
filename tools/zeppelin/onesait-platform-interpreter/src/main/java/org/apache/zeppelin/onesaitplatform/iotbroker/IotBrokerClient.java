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
package org.apache.zeppelin.onesaitplatform.iotbroker;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.zeppelin.onesaitplatform.base.Client;
import org.apache.zeppelin.onesaitplatform.enums.QueryType;
import org.apache.zeppelin.onesaitplatform.enums.RestMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IotBrokerClient extends Client {
	
	private String userAgent = "onesaitplatform.iotbroker";
	private String iotBrokerPath = "/iot-broker";
    private String clientPlatform = null;
    private String clientPlatformId;
    private String token = null;
    private boolean connected = false;
    
    private String sessionKey;

    private static final Logger log = LoggerFactory.getLogger(IotBrokerClient.class);

    private final String JOIN_TEMPLATE = "/rest/client/join?token=%s&clientPlatform=%s&clientPlatformId=%s";
    private final String LEAVE_TEMPLATE = "/rest/client/leave";
    private final String INSERT_TEMPLATE = "/rest/ontology/%s";
    private final String QUERY_TEMPLATE = "/rest/ontology/%s?query=%s&queryType=%s";
    
    private final String IS_JOIN_QUERY_STR = "/rest/client/join";
    
    private final String NOT_CONNECTED_MSG = "Client is not connected, Use connect() or join() before.";
    private final String SESSIONKEY_EXP_MSG = "Expired sessionkey detected. Reconnecting...";
	

    public IotBrokerClient(String host) {
        this.host = host;
        this.httpHost = generateHost(host, this.protocol);
        log.info("Connection Params: " + this.host + "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
        addDebugTrace("Connection Params: " + this.host + "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
    }
    
    public IotBrokerClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.httpHost = generateHost(host, this.protocol, port);
        log.info("Connection Params: " + this.host + ":" + String.valueOf(this.port) + " - avoid ssl: " + this.avoidSSLCertificate);
        addDebugTrace("Connection Params: " + this.host + ":" + String.valueOf(this.port) + " - avoid ssl: " + this.avoidSSLCertificate);
    }
    
    public void setPath(String path) {
    	this.iotBrokerPath = path;
    	log.info("Setted Path: " + this.iotBrokerPath);
        addDebugTrace("Setted Path: " + this.iotBrokerPath);
    }
    
    public void setClientPlatformId(String clientPlatformId) {
    	this.clientPlatformId = clientPlatformId;
    }
    
    public boolean isConnected() {
    	return this.connected;
    }

    public boolean restart() {
    	leave();
    	return join(this.clientPlatform, this.token);
    }
    
    public boolean restart(String client, String token) {
    	leave();
    	return join(client, token);
    }
    
    public boolean connect(String client, String token) {
    	return join(client, token);
    }
    
    public boolean join(String client, String token) {
    	JsonParser jsonParser = new JsonParser();
    	
    	try {
    	
	    	this.token = token;
	    	this.clientPlatform = client;
	    	this.clientPlatformId  = client + ":JavaClient";
	    	log.info("Doing Join");
	        addDebugTrace("Doing Join");
	    	
	    	String joinUrl = this.iotBrokerPath + String.format(JOIN_TEMPLATE, this.token , this.clientPlatform, this.clientPlatformId);
	    	log.info("Join sentence: " + joinUrl);
	        addDebugTrace("Join sentence: " + joinUrl);
	    	
    		this.client = generateHttpClient(this.avoidSSLCertificate);
    		
			String resultStr = call(joinUrl, RestMethods.GET, "");
			log.info("Join response: " + resultStr);
            addDebugTrace("Join response: " + resultStr);
            
            JsonObject jsonTree = jsonParser.parse(resultStr).getAsJsonObject();
            this.sessionKey = jsonTree.get("sessionKey").getAsString();
            this.connected = true;
            log.info("Join ok - SessionKey is " + this.sessionKey);
            addDebugTrace("Join ok - SessionKey is " + this.sessionKey);
		
    	} catch (Exception e) {
    		log.error("Error in Join: " + e.getMessage());
            addDebugTrace("Error in Join: " + e.getMessage());
			return false;
		}
    	
    	return true;
    }
    
    @Deprecated
    public boolean doJoin(String client, String token) {
        try{
        	this.token = token;
        	this.clientPlatform = client;
            log.info("Doing Join");
            addDebugTrace("Doing Join");
            
            String join = this.iotBrokerPath + String.format(JOIN_TEMPLATE, this.token , this.clientPlatform, this.clientPlatform + ":Notebook");
            log.info("Join sentence: " + join);
            addDebugTrace("Join sentence: " + join);
            
            String resultStr = callRestAPI(join, "GET");
            log.info("Join response: " + resultStr);
            addDebugTrace("Join response: " + resultStr);
            
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonTree = jsonParser.parse(resultStr).getAsJsonObject();
            
            this.sessionKey = jsonTree.get("sessionKey").getAsString();

            log.info("SessionKey is " + this.sessionKey);
            addDebugTrace("SessionKey is " + this.sessionKey);
            log.info("Join Ok");
            addDebugTrace("Join Ok");
            this.connected = true;
        }
        catch(Exception e){
            log.error("Error in Join: " + e.getMessage());
            addDebugTrace("Error in Join: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean disconnect() {
    	return leave();
    }
    
    public boolean leave() {
    	try{
    		log.info("Doing Leave");
            String leave = this.iotBrokerPath + String.format(LEAVE_TEMPLATE);
            log.info("Leave sentence: " + leave);
            addDebugTrace("Leave sentence: " + leave);
            
            call(leave, RestMethods.GET, "");
            log.info("Leave Ok");
            addDebugTrace("Leave Ok");
        }
        catch(Exception e) {
        	log.error("Error en Leave: " + e.getMessage());
            addDebugTrace("Error en Leave: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Deprecated
    public boolean doLeave() {
        try{
            log.info("Doing Leave");
            
            String leave = this.iotBrokerPath + String.format(LEAVE_TEMPLATE);
            log.info("Leave sentence: " + leave);
            addDebugTrace("Leave sentence: " + leave);
            
            callRestAPI(leave, "GET");
            log.info("Leave Ok");
            addDebugTrace("Leave Ok");
        }
        catch(Exception e){
            log.error("Error en Leave: " + e.getMessage());
            addDebugTrace("Error en Leave: " + e.getMessage());
            return false;
        }
        return true;
    }

    private String generateURLQuery(String ontology, String query, String queryType) throws UnsupportedEncodingException {
        log.info("Generating Query");
        addDebugTrace("Generating Query");
        
        query = this.iotBrokerPath + String.format(QUERY_TEMPLATE, ontology, URLEncoder.encode(query, "UTF-8"), queryType);
        log.info("Query sentence: " + query);
        addDebugTrace("Query sentence: " + query);
        
        
        return query;
    }
    
    public ArrayList<String> query(String ontology, String query, String queryType) throws Exception {
    	String resultResponse = null;

    	try{
    		log.info("Doing Query: " + query);
            addDebugTrace("Doing Query: " + query);
            resultResponse = call(generateURLQuery(ontology, query, queryType), RestMethods.GET, "");
            return parseResponseData(resultResponse);
        }
        catch(Exception e){
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error in query: " + resultResponse);
            
            throw new Exception(resultResponse);
        }
    	
    }
    
    @Deprecated
    public ArrayList<String> doQuery(String query) throws Exception{
    	String resultResponse = null;
        try{
            log.info("Doing Query: " + query);
            addDebugTrace("Doing Query: " + query);
            
            resultResponse = callRestAPI(query, "GET");
            
            return parseResponseData(resultResponse);
        }
        catch(Exception e){
            log.error("Error en query: " + resultResponse);
            addDebugTrace("Error en query: " + resultResponse);
            
            throw new Exception(resultResponse);
        }
        
    }

    public ArrayList<String> queryBatch(String ontology, String query, QueryType queryType) throws Exception {
    	return queryBatch(ontology, query, queryType.name(), 0);
    }
    
    public ArrayList<String> queryBatch(String ontology, String query, String queryType) throws Exception {
    	return queryBatch(ontology, query, queryType, 0);
    }
    
    public ArrayList<String> queryBatch(String ontology, String query, QueryType queryType, int batchSize) throws Exception {
    	return queryBatch(ontology, query, queryType.name(), batchSize);
    }
    
    public ArrayList<String> queryBatch(String ontology, String query, String queryType, int batchSize) throws Exception {
    	ArrayList<String> result = new ArrayList<String>();

        if (batchSize == 0) { 
        	batchSize = this.batchSize;
        }
        
        log.info("Doing Batch Query: " + query + ", size: " + batchSize);
        addDebugTrace("Doing Batch Query: " + query + ", size: " + batchSize);

        int offset = 0;
        int limit = batchSize;
        int res_query_count = batchSize;
        
        while (res_query_count == batchSize) {

            res_query_count = 0;
            String step_query = queryBatchStr(query, offset, limit, queryType);
            ArrayList<String> res_query = query(ontology, step_query, queryType);
            boolean ok_query = !res_query.isEmpty();
            
            if (ok_query) { 
                res_query_count = res_query.size();
                result.addAll(res_query);
                offset += batchSize;
            }

        }
            
        return result;
    }
    
    private String queryBatchStr(String query, int offset, int limit, String query_type) {
        String step_query = null;
        
        if (query_type.equals(QueryType.SQL.name())) {
            step_query = query + " offset " + offset + " limit " + limit;
        }
        
        else if(query_type.equals(QueryType.NATIVE.name())) {
            step_query = query + ".skip(" + offset + ").limit(" + limit + ")";
        }
        
        return step_query;
    }

    public ArrayList<String> insert(String ontology, JsonArray instances) {
    	String instancesString = instances.toString();
    	return insert(ontology, instancesString);
    }
    
    public ArrayList<String> insert(String ontology, String instances) {
        try{    
            log.info("Doing Insert in " + ontology + " ,instances: " + instances);
            addDebugTrace("Doing Insert in " + ontology + " ,instances: " + instances);
            
            String resultStr = call(generateURLInsert(ontology, instances), RestMethods.POST, instances);
            log.info("Response: " + resultStr);
            addDebugTrace("Response: " + resultStr);
            ArrayList<String> l = new ArrayList<String>();
            l.add(resultStr);
            return l;
        }
        catch(Exception e){
            log.error("Error en query: " + e.getMessage());
            return null;
        }
    }
    
    @Deprecated
    public ArrayList<String> doInsert(String ontology, String instances) {
        try{    
            log.info("Doing Insert in " + ontology + " ,instances: " + instances);
            addDebugTrace("Doing Insert in " + ontology + " ,instances: " + instances);
            
            String resultStr = callRestAPI(generateURLInsert(ontology,instances), "POST", instances);
            log.info("Response: " + resultStr);
            addDebugTrace("Response: " + resultStr);
            ArrayList<String> l = new ArrayList<String>();
            l.add(resultStr);
            return l;
        }
        catch(Exception e){
            log.error("Error en query: " + e.getMessage());
            return null;
        }
    }
    
    private ArrayList<String> parseResponseData (String queryResponse) throws Exception{
    	if (queryResponse.toLowerCase().startsWith("{")) {
    		return parseJsonDataAsArray(queryResponse);
    	}
    	else if (queryResponse.toLowerCase().startsWith("[{")) {
    		return parseDataAsArray(queryResponse);
    	}
    	else if (queryResponse.toLowerCase().equals("[]")) {
    		return new ArrayList<String>();
    	}
    	else {
    		throw new Exception("Invalid response in query - " + queryResponse);
    	}
    }

    private ArrayList<String> parseDataAsArray(String queryResponse){
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(queryResponse).getAsJsonArray();
        ArrayList<String> arrayResponse = new ArrayList<String>();
        for(int i=0;i < jsonArray.size(); i++){
            //arrayResponse.add(cleanOutput(jsonArray.get(i).toString()));
            arrayResponse.add(jsonArray.get(i).toString());
        }
        return arrayResponse;
    }
    
    private ArrayList<String> parseJsonDataAsArray(String queryResponse) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonoObj = jsonParser.parse(queryResponse).getAsJsonObject();
        ArrayList<String> arrayResponse = new ArrayList<String>();
        //arrayResponse.add(cleanOutput(jsonoObj.toString()));
        arrayResponse.add(jsonoObj.toString());
        
        return arrayResponse;
    }
    
    @Deprecated
    private String cleanOutput(String response) {
    	response = response.replaceAll("\\\"\\{", "\\{");
    	response = response.replaceAll("\\}\\\"", "\\}");
    	response = response.replaceAll("\\\\\"", "\"");
    	return response;
    }

    private String generateURLInsert(String ontology, String instances){
        return this.iotBrokerPath + String.format(INSERT_TEMPLATE, ontology);
    }

    private void addAuthorizationHeader(HttpRequest http) {
    	http.addHeader(this.AUTH, this.sessionKey);
    	http.addHeader(this.USER_AGENT, this.userAgent);
    }


    private String call(String url, String method) {
    	return call(url, method, "");
    }

    private String call(String url, String method, String jsonData) {
    	if (!this.isConnected() && !isJoin(url)) {
    		log.info(NOT_CONNECTED_MSG);
    		return NOT_CONNECTED_MSG;
    	}
    	
    	String result = null;
    	HttpRequest http = null;
    	
    	try {
    		switch (method) {
            case RestMethods.GET:
                http = new HttpGet(this.httpHost + url);
                break;
            case RestMethods.POST:
                http = new HttpPost(this.httpHost + url);
                StringEntity entityPost = new StringEntity(jsonData);
                ((HttpPost) http).setEntity(entityPost);
                http.setHeader(ACCEPT_STR, APP_JSON);
                http.setHeader(CONT_TYPE, APP_JSON);
                break;
            case RestMethods.PUT:
                http = new HttpPut(this.httpHost + url);
                StringEntity entityPut = new StringEntity(jsonData);
                ((HttpPut) http).setEntity(entityPut);
                http.setHeader(ACCEPT_STR, APP_JSON);
                http.setHeader(CONT_TYPE, APP_JSON);
                break;
            case RestMethods.DELETE:
                http = new HttpDelete(this.httpHost + url);
                break;
            }
    		
    		addAuthorizationHeader(http);
    		
    		HttpResponse httpResponse = this.client.execute((HttpUriRequest) http);
    		
    		if(httpResponse.getStatusLine().getStatusCode() == 401 && !isJoin(url) && this.token != null) {
            	log.info("Response 401: " + SESSIONKEY_EXP_MSG);
            	addDebugTrace("Response 401: " + SESSIONKEY_EXP_MSG);
            	leave();
            	if(join(this.clientPlatform, this.token)) {
            		log.info("Reconnection OK");
            		addDebugTrace("Reconnection OK");
            	}
            }
    		
    		log.info(httpResponse.getStatusLine().toString());
            addDebugTrace(httpResponse.getStatusLine().toString());
            
            HttpEntity entityResponse = httpResponse.getEntity();
            result = EntityUtils.toString(entityResponse, "UTF-8");
            entityResponse.getContent().close();
    		
    	} catch (ClientProtocolException e) {
            // thrown by httpClient.execute(httpRequest)
        	result = "Error executing request: " + e.getMessage();
            log.error(result);
            addDebugTrace(result);
        } catch (IOException e) {
        	result = "Error IO executing request: " + e.getMessage();
        	log.error(result);
        	addDebugTrace(result);
        } catch (Exception e) {
        	result = "Error general executing request: " + e.getMessage();
        	log.error(result);
        	addDebugTrace(result);
        } finally {
            log.info("Response of: " + result.length() + " chars");
            addDebugTrace("Response of: " + result.length() + " chars");
        }
    	
    	return result;
    }
    
    @Deprecated
    private String callRestAPI(String targetURL, String method) throws Exception {
        return callRestAPI(targetURL, method, "");
    }
    
    @Deprecated
    @SuppressWarnings({ "deprecation", "static-access" })
    private String callRestAPI(String targetURL, String method, String jsonData) throws Exception {
        log.info("Call rest api in {}, method: {}, jsonData: {}", targetURL, method, jsonData);
        addDebugTrace("Call rest api in "+targetURL+", method: "+method+", jsonData: " + jsonData);
        
        //HttpClient httpClient = new DefaultHttpClient();
        HttpClient httpClient = generateHttpClient(true);
        String result = null;
        try {
            HttpRequest http = null;
            HttpHost httpHost = new HttpHost(this.host, this.port);
            log.info("Conection info, host: " + this.host + ", port: "+this.port);
            addDebugTrace("Conection info, host: " + this.host + ", port: "+this.port);
            StringEntity entity = new StringEntity(jsonData);
            //targetURL = URLEncoder.encode(targetURL, "UTF-8");
            
            if("".equals(targetURL)){
                targetURL = this.iotBrokerPath;
            }
            else{
                targetURL = this.iotBrokerPath + targetURL;
            }
            log.info("Enconded url:" + targetURL);
            addDebugTrace("Enconded url:" + targetURL);
            
            switch (method) {
            case "GET":
                http = new HttpGet(targetURL);
                break;
            case "POST":
                http = new HttpPost(targetURL);

                ((HttpPost) http).setEntity(entity);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");
                break;
            case "PUT":
                http = new HttpPut(targetURL);
                ((HttpPut) http).setEntity(entity);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");
                break;
            case "DELETE":
                http = new HttpDelete(targetURL);
                break;

            }
            
            addAuthorizationHeader(http);

            // Execute HTTP request
            HttpResponse httpResponse = httpClient.execute(httpHost, http);
            
            if(httpResponse.getStatusLine().getStatusCode() == 401 && !isJoin(targetURL) && this.token != null) {
            	log.info("Response 401: Session expired, reconnecting");
            	doLeave();
            	if(doJoin(this.clientPlatform, this.token)) {
            		log.info("Reconnection OK");
            	}
            }
            
            log.info("----------------------------------------");
            log.info(httpResponse.getStatusLine().toString());
            addDebugTrace(httpResponse.getStatusLine().toString());
            log.info("----------------------------------------");
            
            // Get hold of the response entity
            HttpEntity entityResponse = httpResponse.getEntity();
            result = EntityUtils.toString(entityResponse, "UTF-8");
            addDebugTrace("Result: " + result.toString());
        } catch (ClientProtocolException e) {
            // thrown by httpClient.execute(httpGetRequest)
        	result = "Error executing request: " + e.getMessage();
            log.error(result);
            addDebugTrace(result);
        } catch (IOException e) {
        	result = "Error IO executing request: " + e.getMessage();
        	log.error(result);
        	addDebugTrace(result);
        } catch (Exception e) {
        	result = "Error general executing request: " + e.getMessage();
        	log.error(result);
        	addDebugTrace(result);
        }finally {
            httpClient.getConnectionManager().shutdown();
            log.info("Response of: " + result.length() + " chars");
            addDebugTrace("Response of: " + result.length() + " chars");
            return result;
        }
    }
    
    private boolean isJoin(String url) {
    	return url.startsWith(this.iotBrokerPath + IS_JOIN_QUERY_STR);
    }
    
    
}
