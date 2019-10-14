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
package com.minsait.onesait.platform.config.services.nginxmanage;

import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class NginxManageServiceImpl implements NginxManageService {
	
	@Value("${onesaitplatform.urls.nginx.service}")
	private String nginxServiceUrl;
	
	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();
	}

	
	@Override
	public String getNginx() {
		String nginxConf=this.restTemplate.getForObject(nginxServiceUrl+"/nginx", String.class);
		return nginxConf;
	}
	
	@Override
	public String setNginx(String nginx){	
		try {
			HttpEntity<String> entity=new HttpEntity<String>(nginx);
			this.restTemplate.exchange(nginxServiceUrl+"/nginx/test", HttpMethod.POST, entity, String.class);
			this.restTemplate.exchange(nginxServiceUrl+"/nginx/set", HttpMethod.POST, entity, String.class);			
			String nginxConf=this.restTemplate.getForObject(nginxServiceUrl+"/nginx", String.class);
			return nginxConf;
		}
		catch (HttpServerErrorException e) {
			String nginxConf= new String(e.getResponseBodyAsByteArray()) + "\n\nPress 'Previous Version' if you want undo the changes";
			return nginxConf;
		}
	}
	
	@Override
	public String testNginx(String nginx){
		try {
			HttpEntity<String> entity=new HttpEntity<String>(nginx);
			this.restTemplate.exchange(nginxServiceUrl+"/nginx/test", HttpMethod.POST, entity, String.class);
			
			String nginxConf=this.restTemplate.getForObject(nginxServiceUrl+"/nginx", String.class);
			return nginxConf;
		}
		catch (Exception e) {
			String nginxConf="Error. Reset para volver a la versión anterior";
			return nginxConf;
		}
	}
	
	@Override
	public String undoNginx() {
		//String nginxConf=this.restTemplate.putForObject(nginxServiceUrl+"/undo");
		this.restTemplate.put(nginxServiceUrl+"/nginx/undo", null);
		String nginxConf=this.restTemplate.getForObject(nginxServiceUrl+"/nginx", String.class);
		return nginxConf;
	}
	
	@Override
	public String resetNginx() {
		this.restTemplate.put(nginxServiceUrl+"/nginx/reset", null);
		String nginxConf=this.restTemplate.getForObject(nginxServiceUrl+"/nginx", String.class);
		return nginxConf;
	}	

}
