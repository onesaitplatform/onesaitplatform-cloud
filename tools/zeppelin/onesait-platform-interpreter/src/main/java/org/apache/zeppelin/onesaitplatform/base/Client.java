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
package org.apache.zeppelin.onesaitplatform.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;

import jline.internal.Log;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;


public class Client {
    
	protected String userAgent = "onesaitplatform.base";
	protected String host;
	protected int port = -1;
	protected RestProtocols protocol = RestProtocols.HTTP;
	protected boolean avoidSSLCertificate = false;
	protected int batchSize = 2000;
	
	protected HttpClient client;
	protected String httpHost;
	
	protected final String HTTPS = "https";
	protected final String HTTP = "http";
	protected final String X_OP_APIKey = "X-OP-APIKey";
	protected final String ACCEPT_STR = "Accept";
	protected final String APP_JSON = "application/json";
	protected final String CONT_TYPE  = "Content-type";
	protected final String USER_AGENT = "User-Agent";
	protected final String AUTH = "Authorization";
    
    boolean debugMode = false;
	protected String debugTemplate = "[%s] %s"; // [timestamp] (method) message
    protected LinkedList<String> debugTrace = new LinkedList<String>();
    

    public Client() {
    }
    
    public Client(String host) {
    	this.host = host;
    }
    
    public void avoidSSLCertificate(boolean avoid) {
    	this.avoidSSLCertificate = avoid;
    }
    
    public void setBatchSize(String batchSize) {
    	try {
    		this.batchSize = Integer.parseInt(batchSize);
    	}
    	catch (Exception e) {
    		Log.warn("Not possible to set batchSize from string");
    		addDebugTrace("Not possible to set batchSize from string");
    	}
    }
    
    public void setBatchSize(int batchSize) {
    	this.batchSize = batchSize;
    }
    
    public int getBatchSize() {
    	return this.batchSize;
    }
    
    public void setProtocol(RestProtocols protocol) {
    	this.protocol = protocol;
    	if (this.port < 0) {
    		this.httpHost = generateHost(this.host, protocol);
    	}
    	else {
    		this.httpHost = generateHost(this.host, protocol, this.port);
    	}
    	
    	if (protocol.name().equals(RestProtocols.HTTP.name())) {
    		this.avoidSSLCertificate(false);
    	}

    }
    
    public String addUrlProtocolFfNot(String url, RestProtocols protocol) {
    	return addUrlProtocolFfNot(url, protocol, -1);
    }
    
    public String addUrlProtocolFfNot(String url, RestProtocols protocol, int port) {
    	String resultUrl = url;
    	String port_str;
    	
    	if (!url.startsWith("http://") && !url.startsWith("https://")) {
    		if (protocol.name().equals(RestProtocols.HTTP.name())) {
    			resultUrl = "http://" + url;
    		}
    		else if (protocol.name().equals(RestProtocols.HTTPS.name())) {
    			resultUrl = "https://" + url;
    		}
    		
    	}
    	
    	if (port > 0) {
    		port_str = ":"+ String.valueOf(port);
    		resultUrl = resultUrl + port_str;
    		
    	}
    	
    	return resultUrl;
    }
    
    public String generateHost(String host, RestProtocols protocol, int port) {
    	return addUrlProtocolFfNot(host, protocol, port);
    }
    
    public String generateHost(String host, RestProtocols protocol) {
		return addUrlProtocolFfNot(host, protocol, -1);
    }

    public boolean isDebugMode() {
    	return this.debugMode;
    }
    
    public List<String> getDebugTrace() {
    	return this.debugTrace;
    }
    
    public List<String> setDebugMode(boolean debugMo) {
    	ArrayList<String> listResult = new ArrayList<String>();
        listResult.add("Debug mode: "+debugMo+"!");
        addDebugTrace("Debug mode: "+debugMo+"!");
    	
        this.debugMode = debugMo;
    	return listResult;
    }

    public void restartDebugTrace() {
    	this.debugTrace = new LinkedList<String>();
    }
    
    public void addDebugTrace(String message) {
    	if (this.debugMode) {
    		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    		String msgTemplate = String.format(debugTemplate, timestamp.toString(), message);
    		this.debugTrace.add(msgTemplate);
    	}
    }
    
    protected HttpClient generateHttpClient(boolean avoidSSLCertificate) throws Exception {
    	HttpClient httpClient;
    	
    	if (avoidSSLCertificate) {
	    	X509TrustManager manangerX509 = new X509TrustManager() {
	            public X509Certificate[] getAcceptedIssuers() {
	                    return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	    	};
	    	TrustManager[] manager = new TrustManager[]  {manangerX509};
	    	SSLContext sslContext = SSLContext.getInstance("SSL");
	    	sslContext.init(null, manager, new SecureRandom());
	    	httpClient = HttpClientBuilder.create().setSSLContext(sslContext).build();
			
    	} else {
    		httpClient = HttpClientBuilder.create().build();
    	}
    	
    	return httpClient;
    }
	
	
}
