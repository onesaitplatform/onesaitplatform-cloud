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
package com.indracompany.sofia2.examples.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@RestController
public class ApiRestController {

	private static final Logger LOG =   LoggerFactory.getLogger(ApiRestController.class);
	
	private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	
    @RequestMapping("/greeting")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping("/message")
    public String message(@RequestParam("msg") String msgReceived) {
    	
    	LOG.info("MSG RECIEVED: " +  msgReceived);
    	//String msg = processMsg(msgRecieved);
    	
    	String msg = postMsg(msgReceived);
    	
    	return msg;
    }
    
    private String postMsg(String msg) {

    	String url = "http://localhost:5003/ask_s4c_bot";
    	String requestJson = "{\"msg\":\""+msg+"\"}";
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);

    	HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);
    	String answer = restTemplate.postForObject(url, entity, String.class);
    	System.out.println(answer);
    	return answer;
    }

}
