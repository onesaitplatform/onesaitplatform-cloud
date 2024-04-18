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
package com.minsait.onesait.examples.security.platform;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class OAuthAuthenticator {
	
	private static final String SCOPE = "openid";
	private String url;
	private String clientId;

	public OAuthAuthenticator(String url, String clientId) {
		this.url = url;
		this.clientId = clientId;
	}

	public OAuthAuthenticator(String clientId) {
		this.url = "http://localhost:21000/oauth-server/oauth";
		this.clientId = clientId;
	}
	
    public OAuthAuthorization authenticate(String token) throws IOException
    {
		HttpPost post = new HttpPost(url+"/tokenInfo");
		post.setEntity(new StringEntity(token, Charset.defaultCharset()));
		
		JsonObject jsonOauth = sendAuthentication(post);
		
		JsonObject info = null;
		
		
		
		if (jsonOauth.containsKey("oauthInfo") && jsonOauth.get("oauthInfo") != JsonValue.NULL)
			info = jsonOauth.getJsonObject("oauthInfo"); 
		
		if (info != null) 
			return new OAuthAuthorization(jsonOauth.getJsonObject("oauthInfo"), clientId);
		else
			return new OAuthAuthorization();
    }
    
    public OAuthAuthorization authenticate(String user, String password) throws IOException
    {
    	
    	String authStr = "Basic " + Base64.getEncoder()
                .encodeToString((user+":"+password).getBytes());
    	
		HttpPost post = new HttpPost(url);
		post.setHeader("content-type", "application/x-www-form-urlencoded");
		post.setHeader("authorization", authStr);
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("grant_type", "password"));
		urlParameters.add(new BasicNameValuePair("username", user));
		urlParameters.add(new BasicNameValuePair("password", password));
		urlParameters.add(new BasicNameValuePair("scope", OAuthAuthenticator.SCOPE));
		urlParameters.add(new BasicNameValuePair("client_id", clientId));

		post.setEntity(new UrlEncodedFormEntity(urlParameters, Charset.defaultCharset()));

		JsonObject jsonOauth = sendAuthentication(post);
		return new OAuthAuthorization(jsonOauth, clientId);
    }
    
	private JsonObject sendAuthentication(HttpPost post) throws IOException {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			
			HttpResponse response = httpClient.execute(post);
			
	    	if (response.getStatusLine().getStatusCode() != 200) {
	    		return Json.createObjectBuilder().build();
	    	}
	    	
			return Json.createReader(response.getEntity().getContent()).readObject();
    	}
	}
   
  

}