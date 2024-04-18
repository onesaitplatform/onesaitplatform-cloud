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
package org.apache.zeppelin.onesaitplatform.apimanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.zeppelin.onesaitplatform.base.Client;
import org.apache.zeppelin.onesaitplatform.enums.RestMethods;
import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ApiManagerClient extends Client {
    
	private String userAgent = "onesaitplatform.apimanager";
	private String apiManagerPath = "/api-manager/services/management";
	private String apiCallerPath = "/api-manager/server/api";
	private String token = null;

    private final Logger log = LoggerFactory.getLogger(ApiManagerClient.class);
    
    private final String FIND_TEMPLATE = "/apis?identificacion=%s&estado=%s&usuario=%s";
    private final String CREATE_TEMPLATE = "/apis";
    private final String DELETE_TEMPLATE = "/apis/%s/%s";
    private final String CALL_TEMPLATE = "/%s";
    private final String LIST_TEMPLATE = "/apis/user/%s";
    
    private final String IS_LIST_QUERY_STR = "/apis/user";
    private final String IS_FIND_QUERY_STR = "/apis?";
    
    private final String NOT_SETTED_TOKEN_MSG = "Not token setted. Please use setToken(<token>) before";
    private final String NOT_POSSIBLE_CONNECT_MSG = "Not possible to connect";
    private final String NOT_RESULTS_FOUND_MSG = "Response 404: Not results found";
    
    
    public ApiManagerClient(String host) {
    	this.host = host;
    	this.httpHost = generateHost(host, this.protocol);
        log.info("Connection Params: "  + this.host + " - " + this.apiManagerPath + " / " + this.apiCallerPath + " - "  
        		+ "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
        addDebugTrace("Connection Params: "  + this.host + " - " + this.apiManagerPath + " / " + this.apiCallerPath + " - "  
        		+ "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
    }
    
    public ApiManagerClient(String host, int port) {
    	this.host = host;
    	this.port = port;
    	this.httpHost = generateHost(host, this.protocol, port);
        log.info("Connection Params: "  + this.host + ":" + String.valueOf(this.port)  + " - " + this.apiManagerPath + " / " + this.apiCallerPath + " - "  
            	+ "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
        addDebugTrace("Connection Params: "  + this.host + ":" + String.valueOf(this.port)  + " - " + this.apiManagerPath + " / " + this.apiCallerPath + " - "  
            	+ "(" + protocol.name() +")" + " - avoid ssl: " + this.avoidSSLCertificate);
    }
    
    public void setApiManagerPath(String apiManagerPath) {
    	this.apiManagerPath = apiManagerPath;
    	log.info("Setted Api Manager Path: " + this.apiManagerPath);
        addDebugTrace("Setted Api Manager Path: " + this.apiManagerPath);
    }
    
    public void setApiCallerPath(String apiCallerPath) {
    	this.apiCallerPath = apiCallerPath;
    	log.info("Setted Api Caller Path: " + this.apiCallerPath);
        addDebugTrace("Setted Api Caller Path: " + this.apiCallerPath);
    }
    
    
    public void raiseExceptionIfNotToken() throws Exception {
    	addDebugTrace("Checking if token is setted");
		if (this.token == null) {
			String msg = "Error login, please connect to onesait platform api-manager with sentence: setToken(\"token\")";
			addDebugTrace("Error login: Token must be setted");
			throw new Exception(msg);
		}
		addDebugTrace("Token setted: " + this.token);
    }
    
    private void addHeaders(HttpRequest request) {
    	addDebugTrace("Setting headers for http request");
    	request.addHeader(X_OP_APIKey, this.token);
    	request.setHeader(ACCEPT_STR, APP_JSON);
    	request.setHeader(CONT_TYPE, APP_JSON);
    	request.addHeader(USER_AGENT, this.userAgent);
    	addDebugTrace("Headers added to the http request");
    }
    
    public String generateURLApiManager(String pathQuery) {
    	String hostTarget = addUrlProtocolFfNot(this.host, this.protocol, this.port);
    	addDebugTrace("Query Api Manager: " + hostTarget + this.apiManagerPath + pathQuery);
    	return hostTarget + this.apiManagerPath + pathQuery;
    }
    
    public String generateURLApis(String pathQuery) {
    	String hostTarget = addUrlProtocolFfNot(this.host, this.protocol, this.port);
    	addDebugTrace("Query Apis: " + hostTarget + this.apiCallerPath + pathQuery);
    	return hostTarget + this.apiCallerPath + pathQuery;
    }
    
    public String generateURLFind(String identification, String state, String user)  {
        String query = String.format(FIND_TEMPLATE, identification, state, user);
        log.info("Generated find query " + query);
        return generateURLApiManager(query);
    }
    
    public String generateURLCreate() {
        String query = CREATE_TEMPLATE;
        log.info("Generated create query " + query);
        return generateURLApiManager(query);
    }
    
    public String generateURLDelete(String identification, String version) {
        String query = String.format(DELETE_TEMPLATE, identification, version);
        log.info("Generated delete query " + query);
        return generateURLApiManager(query);
    }
    
    public String generateURLCall(String url) {
    	if (url.startsWith("")) {
    		url = url.substring(1);
    	}
        String query = String.format(CALL_TEMPLATE, url);
        log.info("Generated call query " + query);
        return generateURLApis(query);
    }
    
    public String generateURLList(String idUser) {
        String query = String.format(LIST_TEMPLATE, idUser);
        log.info("Generated list query " + query);
        return generateURLApiManager(query);
    }
    
    public boolean setToken(String token) {
    	
    	try {
    		addDebugTrace("Setting token to " + token);
    		this.token = token;
			this.client = generateHttpClient(this.avoidSSLCertificate);
		
	        log.info("Token " + this.token + " setted correctly!");
	        addDebugTrace("Token " + this.token + " setted correctly!");
	        return true;
	        
        
    	} catch (Exception e) {
    		log.error("Not possible to set token and create HttpClient: " + e.getMessage());
    		addDebugTrace("Not possible to set token and create HttpClient: " + e.getMessage());
    		return false;
    	}
    	
    }
    
    public List<String> find(String identification, String state, String user) throws Exception {
    	String resultResponse = null;

    	try{
    		log.info("Making find request, identification: "+identification+", state: "+state+", user: "+user);
        	addDebugTrace("Making find request, identification: "+identification+", state: "+state+", user: "+user);
        	
            resultResponse = call(generateURLFind(identification, state, user), RestMethods.GET);
            
            addDebugTrace("Maked find request, result: " + resultResponse);
            return parseResponseData(resultResponse);
        }
        catch(Exception e) {
            log.error("Error in query: " + e);
            addDebugTrace("Error making find request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    @Deprecated
    public List<String> doFind(String identification, String state, String user) throws Exception {
    	addDebugTrace("Making find request, identification: "+identification+", state: "+state+", user: "+user);
    	String resultResponse = null;

    	try{
        	String query = generateURLFind(identification, state, user);
            resultResponse = callRestAPI(query, "GET", null);
            
            addDebugTrace("Maked find request, result: "+resultResponse);
            return parseResponseData(resultResponse);
        }
        catch(Exception e) {
            log.error("Error in query: " + e);
            addDebugTrace("Error making find request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    public List<String> create(JsonArray body) throws Exception {
    	return create(body.toString());
    }
    
    public List<String> create(String body) throws Exception {
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
    		addDebugTrace("Making create request, body: "+ body);
            resultResponse = callRestAPI(generateURLCreate(), RestMethods.POST, body);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked create request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making create request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    @Deprecated
    public List<String> doCreate(String body) throws Exception {
    	addDebugTrace("Making create request, body: "+body);
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
        	String query = generateURLCreate();
            resultResponse = callRestAPI(query, "POST", body);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked create request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making create request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    public List<String> delete(String identification, String version) throws Exception {
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
    		addDebugTrace("Making delete request, identification: "+identification+", version: "+version);
        	String query = generateURLDelete(identification, version);
            resultResponse = callRestAPI(query, RestMethods.DELETE);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked delete request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making delete request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    @Deprecated
    public List<String> doDelete(String identification, String version) throws Exception {
    	addDebugTrace("Making delete request, identification: "+identification+", version: "+version);
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
        	String query = generateURLDelete(identification, version);
            resultResponse = callRestAPI(query, "DELETE", null);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked delete request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making delete request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    public String generateUrlRequest(String name, String version, List<String> pathParams, JsonObject queryParams) {
    	String url;
    	// version
    	if (version.startsWith("/")) {
    		url = version;
    	}
    	else if (version.toLowerCase().startsWith("v")) {
    		url = "/" + version;
    	}
    	else {
    		url = "/v" + version;
    	}
    	
    	// name
    	if (name.startsWith("/")) {
    		url += name;
    	}
    	else {
    		url = url + "/" + name;
    	}
    	
    	// path params
    	if (pathParams != null) {
	    	for (int i = 0; i < pathParams.size(); i++) {
	    		url = url + "/" + pathParams.get(i);
	    	}
    	}
    	
    	// query params
    	if (queryParams != null) {
	    	Set<Entry<String, JsonElement>> entrySet = queryParams.entrySet();
		   
	    	if (entrySet.size() > 0) {
	    		url += "?";
		    	for (Map.Entry<String,JsonElement> entry : entrySet){
		    		url = url + "&" + entry.getKey() + "=" + queryParams.get(entry.getKey()).getAsString();
				}
	    	}
    	}
    	
		return url;
    }

    public List<String> request(String method, String name, String version, 
    	List<String> pathParams, JsonObject queryParams,  Object body) throws Exception {
    	String url = generateUrlRequest(name, version, pathParams, queryParams);
    	
    	String parsedBody = null;
    	if (body instanceof JsonArray) {
    		JsonArray jsonBody = (JsonArray) body;
    		parsedBody = jsonBody.toString();
    	}
    	else if (body instanceof String) {
    		parsedBody = body.toString();
    	}
    	else if (body == null) {
    		parsedBody = "";
    	}
    	else {
    		String e = "Invalid body format: JsonArray and String allowed";
    		log.error(e);
    		throw new Exception(e);
    	}
    	
    	return request(url, method, parsedBody);
    }
    
    public List<String> request(String url, String method, JsonArray body) throws Exception {
    	String stringBody = "";
    	if (body != null) {
    		stringBody = body.toString();
    	}
    	return request(url, method, stringBody);
    }
    
    public List<String> request(String url, String method, String body) throws Exception {
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
    		addDebugTrace("Making find request, url: "+url+", method: "+method+", body: "+body);
            resultResponse = call(generateURLCall(url), method, body);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked call request, result: "+resultResponse);
            //return listResult;
            return parseResponseData(resultResponse);
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making call request, error: "+e);
            throw new Exception(resultResponse);
        }

    }
    
    @Deprecated
    public List<String> doCall(String url, String method, String body) throws Exception {
    	addDebugTrace("Making find request, url: "+url+", method: "+method+", body: "+body);
    	String resultResponse = null;
    	ArrayList<String> listResult = new ArrayList<String>();

    	try{
        	String query = generateURLCall(url);
            resultResponse = callRestAPI(query, method, body);
            listResult.add(resultResponse);
            
            addDebugTrace("Maked call request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + resultResponse);
            addDebugTrace("Error making call request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    public List<String> list(String idUser) throws Exception {
    	String resultResponse = null;
    	List<String> listResult = new ArrayList<String>();

    	try{
    		addDebugTrace("Making list request, idUser: "+idUser);
            resultResponse = call(generateURLList(idUser), RestMethods.GET);
            listResult.add(resultResponse);
            listResult = extractBasicInfoAPI(listResult);
            
            addDebugTrace("Maked list request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + e);
            addDebugTrace("Error making list request, error: "+e);
            throw new Exception(resultResponse);
        }
    }

    @Deprecated
    public List<String> doList(String idUser) throws Exception {
    	addDebugTrace("Making list request, idUser: "+idUser);
    	String resultResponse = null;
    	List<String> listResult = new ArrayList<String>();

    	try{
        	String query = generateURLList(idUser);
            resultResponse = callRestAPI(query, "GET", null);
            listResult.add(resultResponse);
            listResult = extractBasicInfoAPI(listResult);
            
            addDebugTrace("Maked list request, result: "+resultResponse);
            return listResult;
        }
        catch(Exception e) {
            log.error("Error in query: " + e);
            addDebugTrace("Error making list request, error: "+e);
            throw new Exception(resultResponse);
        }
    }
    
    private String call(String url, String method) {
    	return call(url, method, null);
    }
    
    private String call(String url, String method, String jsonData) {
    	if (this.token == null) {
    		log.info(NOT_SETTED_TOKEN_MSG);
    		addDebugTrace(NOT_SETTED_TOKEN_MSG);
    		return NOT_SETTED_TOKEN_MSG;
    	}
    	
    	String result = null;
    	HttpRequest http = null;
    	
    	try {
    		switch (method) {
            case RestMethods.GET:
            	http = new HttpGet(url);
                break;
            case RestMethods.POST:
                http = new HttpPost(this.httpHost + url);
                StringEntity entityPost = new StringEntity(jsonData);
                ((HttpPost) http).setEntity(entityPost);
                break;
            case RestMethods.PUT:
                http = new HttpPut(this.httpHost + url);
                StringEntity entityPut = new StringEntity(jsonData);
                ((HttpPut) http).setEntity(entityPut);
                break;
            case RestMethods.DELETE:
                http = new HttpDelete(this.httpHost + url);
                break;
            }
    		
    		addHeaders(http);
    		
    		HttpResponse httpResponse = this.client.execute((HttpUriRequest) http);
    		
    		if (httpResponse.getStatusLine().getStatusCode() == 401 && this.token != null) {
            	log.info("Response 401: " + NOT_POSSIBLE_CONNECT_MSG);
            	addDebugTrace("Response 401: " + NOT_POSSIBLE_CONNECT_MSG);
            	return NOT_POSSIBLE_CONNECT_MSG;
            }
    		
    		if (httpResponse.getStatusLine().getStatusCode() == 404 && this.token != null && (isList(url) || isFind(url))) {
    			log.info(NOT_RESULTS_FOUND_MSG);
            	addDebugTrace(NOT_RESULTS_FOUND_MSG);
            	result = NOT_RESULTS_FOUND_MSG;
            	return "[]";
    		}
    		
    		log.info(httpResponse.getStatusLine().toString());
            addDebugTrace(httpResponse.getStatusLine().toString());
            
            HttpEntity entityResponse = httpResponse.getEntity();
            result = EntityUtils.toString(entityResponse, "UTF-8");
            entityResponse.getContent().close();
    		
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
        } finally {
            log.info("Response of: " + result.length() + " chars");
            addDebugTrace("Response of: " + result.length() + " chars");
        }
    	
    	return result;
    }
    
    @Deprecated
    private String callRestAPI(String url, String method) {
    	return callRestAPI( url,  method, null);
    }
    
    @Deprecated
    private String callRestAPI(String url, String method, String bodyJson) {
        log.info("Call rest api in {}, method: {}, jsonData: {}, token: {}", url, method, bodyJson, this.token);
        addDebugTrace("Call rest api in "+url+", method: "+method+", jsonData: "+bodyJson+", token: "+this.token);
    	
        String result = null;
        
        try {
        	
        	method = method.toUpperCase();
        	
	        HttpClient client = HttpClientBuilder.create().build();
	        HttpUriRequest request = null;
	        
	        switch (method) {
	        case "GET":
	        	request = new HttpGet(url);
	        	break;
	        case "POST":
	        	request = new HttpPost(url);
	        	StringEntity entityPost = new StringEntity(bodyJson);
	        	((HttpPost) request).setEntity(entityPost);
	        	break;
	        case "PUT":
	        	request = new HttpPut(url);
	        	StringEntity entityPut = new StringEntity(bodyJson);
                ((HttpPut) request).setEntity(entityPut);
                break;
	        case "DELETE":
	        	request = new HttpDelete(url);
	        	break;
	        }
	        
	        addHeaders(request);
	        
	     // Execute HTTP request
	        HttpResponse httpResponse;
		
			httpResponse = client.execute(request);
			log.info("Http response: " + httpResponse.getStatusLine().toString());
			
			// Get hold of the response entity
            HttpEntity entityResponse = httpResponse.getEntity();
            result = EntityUtils.toString(entityResponse, "UTF-8");
			
        } catch (ClientProtocolException e) {
            // thrown by httpClient.execute(httpGetRequest)
        	result = "Error executing request: " + e.getMessage();
            log.error(result);
        } catch (IOException e) {
			result = "Error IO executing request: " + e.getMessage();
        	log.error(result);
		} finally {
            log.info("Response of: " + result.length() + " chars");
        }
        
    	
    	return result;
    	
    }
    
    @Deprecated
    public ArrayList<String> parseSSAPDataArray(String ssapResponse) {
        JsonParser jsonParser = new JsonParser(); 
        JsonArray jsonArray = jsonParser.parse(ssapResponse).getAsJsonArray();
        ArrayList<String> arrayResponse = new ArrayList<String>();
        for(int i=0;i < jsonArray.size(); i++){
            //arrayResponse.add(cleanOutput(jsonArray.get(i).toString()));
            arrayResponse.add(jsonArray.get(i).toString());
        }
        return arrayResponse;
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
    
    private List<String> extractBasicInfoAPI(List<String> apisInfos) {
    	String strApisInfos = apisInfos.get(0);
    	List<String> listResults = new LinkedList<String>();
    	
    	
    	JsonParser jsonParser = new JsonParser(); 
        JsonArray jsonArray = jsonParser.parse(strApisInfos).getAsJsonArray();
        
        for(int i=0;i < jsonArray.size(); i++){
        	
        	JsonElement apiInfo = jsonArray.get(i);
        	String identification = apiInfo.getAsJsonObject().getAsJsonPrimitive("identification").getAsString();
        	String version = apiInfo.getAsJsonObject().getAsJsonPrimitive("version").getAsString();
        	String type = apiInfo.getAsJsonObject().getAsJsonPrimitive("type").getAsString();
        	String category = apiInfo.getAsJsonObject().getAsJsonPrimitive("category").getAsString();
        	String endpoint = apiInfo.getAsJsonObject().getAsJsonPrimitive("endpoint").getAsString();
        	String description = apiInfo.getAsJsonObject().getAsJsonPrimitive("description").getAsString();
        	String status = apiInfo.getAsJsonObject().getAsJsonPrimitive("status").getAsString();
        	String userId = apiInfo.getAsJsonObject().getAsJsonPrimitive("userId").getAsString();
        	
        	JsonObject baseicInfoApi = new JsonObject();
        	baseicInfoApi.addProperty("identification", identification);
        	baseicInfoApi.addProperty("version", version);
        	baseicInfoApi.addProperty("status", status);
        	baseicInfoApi.addProperty("type", type);
        	baseicInfoApi.addProperty("category", category);
        	baseicInfoApi.addProperty("userId", userId);
        	baseicInfoApi.addProperty("endpoint", endpoint);
        	baseicInfoApi.addProperty("description", description);
        	
        	log.info("Basic info API extracted correctly for API:" + baseicInfoApi.toString());
        	listResults.add(baseicInfoApi.toString());
        }
    	
		return listResults;
    	
    }
    
    private boolean isList(String url) {
    	return url.contains(this.apiManagerPath + IS_LIST_QUERY_STR);
    }
    
    private boolean isFind(String url) {
    	return url.contains(this.apiManagerPath + IS_FIND_QUERY_STR);
    }
    
	
}
