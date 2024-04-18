/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config.services.ai;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.services.ai.provider.OpenAIServiceProvider;
import com.minsait.onesait.platform.config.services.exceptions.AIServiceException;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AIServiceImpl implements AIService {

	@Autowired
	OpenAIServiceProvider openAIServiceProvider; 
	
	private static final String OPENAI_NO_TOKEN_ERROR = "OpenAI Token not found, please provide it in centralized configuration or use this api with a valid OpenAI token"; 
	
	@Override
	public String textToAnswer(String userId, String text) throws AIServiceException {
		checkValidAIService();
		return openAIServiceProvider.textToAnswer(userId, text);
	}
	
	@Override
	public String textToSQL(String userId, String text) throws AIServiceException {
		checkValidAIService();
		return openAIServiceProvider.textToSQL(userId, text);
	}
	
	@Override
	public String textToAnswer(String userId, String text, String apikey) {
		return openAIServiceProvider.textToAnswer(userId, text, apikey);
	}
	
	@Override
	public String textToSQL(String userId, String text, String apikey) {
		return openAIServiceProvider.textToSQL(userId, text, apikey);
	}
	
	private void checkValidAIService() throws AIServiceException {
		if (!openAIServiceProvider.isOpenAIServiceEnable()) {
			Log.error(OPENAI_NO_TOKEN_ERROR);
			throw new AIServiceException(
					AIServiceException.Error.TOKEN_NOT_FOUND, 
					OPENAI_NO_TOKEN_ERROR);
		}
	} 
}
