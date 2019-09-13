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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.searchbox.core.DocumentResult;
import io.searchbox.core.Update;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESUpdateService {

	@Autowired
	ESBaseApi connector;

	public boolean updateIndex(String index, String type, String id, String jsonString) throws InterruptedException, ExecutionException {
		
		 try {
			 
			 String updater = "{\"doc\":"+jsonString+"}";
			 
			DocumentResult result = connector.getHttpClient().execute(new Update.Builder(updater).index(index).type(type).id(id).build());
			return result.isSucceeded();
		} catch (IOException e) {
			log.error("Error Updating document "+e);
			return false;
		}

    }
	
}