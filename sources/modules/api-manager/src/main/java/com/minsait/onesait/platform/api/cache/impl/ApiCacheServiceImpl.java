/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.api.cache.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.cache.ApiCacheContent;
import com.minsait.onesait.platform.api.cache.ApiCacheKey;
import com.minsait.onesait.platform.api.cache.ApiCacheService;
import com.minsait.onesait.platform.api.rest.api.APiRestServiceImpl;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.OntologyDataAccess;
import com.minsait.onesait.platform.config.repository.OntologyDataAccessRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiCacheServiceImpl implements ApiCacheService {

	@Autowired()
	@Qualifier("ApiManagerCache")
	private Map<ApiCacheKey, ApiCacheContent> apiManagerCache;
	
	@Autowired()
	OntologyDataAccessRepository ontologyDataAccessRepository;
	
	@Override
	public Map<String, Object> getCache(Map<String, Object> data, Integer minutesCacheLimit) {
		Api api = (Api) data.get(Constants.API);
		List<OntologyDataAccess> dataAccesses = null;
		
		if (api.getOntology()!=null) {
			dataAccesses =  ontologyDataAccessRepository.findUserAccessByUser(data.get(Constants.USER).toString());
		}
		
		ApiCacheKey apiKey;
		if ((dataAccesses!=null) && (!dataAccesses.isEmpty())) {
			apiKey = new ApiCacheKey(((HttpServletRequest) data.get(Constants.REQUEST)).getRequestURI() + "?" + ((HttpServletRequest) data.get(Constants.REQUEST)).getQueryString(), api.getId(), data.get(Constants.USER).toString());
			
		} else {
			apiKey = new ApiCacheKey(((HttpServletRequest) data.get(Constants.REQUEST)).getRequestURI() + "?" + ((HttpServletRequest) data.get(Constants.REQUEST)).getQueryString(), api.getId(), null);
		}
		
		ApiCacheContent result = apiManagerCache.get(apiKey);
		
		if (result==null || ((new Date().getTime() - result.getInitdate().getTime())> minutesCacheLimit * 60 * 1000) && minutesCacheLimit!=0) {
			data.put(Constants.OUTPUT, null);
		} else {
			log.info("API cache: Data retrieved from cache");
			data.put(Constants.OUTPUT, result.getResult());
			
			if (result.getError()) {
				data.put(Constants.CONTENT_TYPE, result.getMediaType());
				data.put(Constants.STATUS, ChainProcessingStatus.STOP);
				data.put(Constants.HTTP_RESPONSE_CODE, result.getResponseCode());
				data.put(Constants.REASON, result.getErrorMessage());
			}
		}
		return data;
	}

	@Override
	public void putCache(Map<String, Object> data, Integer minutesCacheLimit) {
		Api api = (Api) data.get(Constants.API);
		List<OntologyDataAccess> dataAccesses = null;
		
		if (api.getOntology()!=null) {
			dataAccesses =  ontologyDataAccessRepository.findUserAccessByUser(data.get(Constants.USER).toString());
		}
		
		ApiCacheKey apiKey;
		if ((dataAccesses!=null) && (!dataAccesses.isEmpty())) {
			apiKey = new ApiCacheKey(((HttpServletRequest) data.get(Constants.REQUEST)).getRequestURI() + "?" + ((HttpServletRequest) data.get(Constants.REQUEST)).getQueryString(), api.getId(), data.get("USER").toString());
			
		} else {
			apiKey = new ApiCacheKey(((HttpServletRequest) data.get(Constants.REQUEST)).getRequestURI() + "?" + ((HttpServletRequest) data.get(Constants.REQUEST)).getQueryString(), api.getId(), null);
		}
		
		ApiCacheContent result = apiManagerCache.get(apiKey);
		if (result==null || ((new Date().getTime() - result.getInitdate().getTime())> minutesCacheLimit * 60 * 1000) && minutesCacheLimit!=0) {
			log.info("API cache: Data saved to cache");
			ApiCacheContent newCacheresult = new ApiCacheContent();
			newCacheresult.setInitdate(new Date());
			newCacheresult.setResult(data.get(Constants.OUTPUT));
			newCacheresult.setError(false);
			if (data.get(Constants.REASON)!=null) {
				newCacheresult.setError(true);
				newCacheresult.setErrorMessage(data.get(Constants.REASON).toString());
				
			}
			newCacheresult.setMediaType(data.get(Constants.CONTENT_TYPE_OUTPUT).toString());
			if (data.get(Constants.HTTP_RESPONSE_CODE) != null) {
				newCacheresult.setResponseCode(data.get(Constants.HTTP_RESPONSE_CODE).toString());
			}
			apiManagerCache.put(apiKey, newCacheresult);
		}
	}
	
	@Override
	public void cleanCache() {
		apiManagerCache.clear();
	}

	@Override
	public void cleanAPICache(String id) {
		
		for (Iterator<Entry<ApiCacheKey, ApiCacheContent>> it = apiManagerCache.entrySet().iterator(); it.hasNext();) {
			Entry<ApiCacheKey, ApiCacheContent> entry = it.next();
			if (entry.getKey().getIdApi().equals(id)) {
				apiManagerCache.remove(entry.getKey());
			}
		}
	}
}
