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
package com.minsait.onesait.platform.config.services.ai.provider;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenAIServiceProviderImpl implements OpenAIServiceProvider {

	OpenAiService service;

	private static final Duration DEFAULT_TIMEOUT_OPENAI = Duration.ofSeconds(120);

	@Autowired
	OntologyService ontologyService;

	@Autowired
	ConfigurationService configurationService;

	OpenAIModel textToSQLModel;
	Double textToSQLModelTemperature;
	List<String> textToSQLModelStops = List.of("#", ";");

	OpenAIModel textToAnswerModel;
	Double textToAnswerModelTemperature;
	List<String> textToAnswerModelStops = null;

	private static final String NO_OPENAI_TOKEN_VALUE = "(openai_apikey)";

	@PostConstruct
	public void init() {
		final Map<String, Object> aiConfObject = configurationService.getAIConfiguration().getProviders().get("OpenAI");
		if (aiConfObject != null && aiConfObject.get("security.apikey") != null
				&& !aiConfObject.get("security.apikey").equals(NO_OPENAI_TOKEN_VALUE)) {
			service = generateOpenAiService((String) aiConfObject.get("security.apikey"));
			log.info("Onesait AI by OpenAI Enabled");
		} else {
			log.info("Onesait AI by OpenAI Disabled");
		}
		if (OpenAIModel.getByName((String) aiConfObject.get("texttosql.model.name")) != null) {
			textToSQLModel = OpenAIModel.getByName((String) aiConfObject.get("texttosql.model.name"));
		} else {
			textToSQLModel = OpenAIModel.GPT_TURBO;
		}
		if (aiConfObject.get("texttosql.model.temperature") != null) {
			textToSQLModelTemperature = (Double) aiConfObject.get("texttosql.model.temperature");
		} else {
			textToSQLModelTemperature = 0.0d;
		}
		if (OpenAIModel.getByName((String) aiConfObject.get("texttoanswer.model.name")) != null) {
			textToAnswerModel = OpenAIModel.getByName((String) aiConfObject.get("texttoanswer.model.name"));
		} else {
			textToAnswerModel = OpenAIModel.GPT_TURBO;
		}
		if (aiConfObject.get("texttoanswer.model.temperature") != null) {
			textToAnswerModelTemperature = (Double) aiConfObject.get("texttoanswer.model.temperature");
		} else {
			textToAnswerModelTemperature = 0.0d;
		}
	}

	@Override
	public String textToAnswer(String userId, String prompt) {
		return textToAnswer(userId, prompt, service);
	}

	@Override
	public String textToSQL(String userId, String text) {
		return textToSQL(userId, text, service);
	}

	@Override
	public String textToAnswer(String userId, String prompt, String apikey) {
		return textToAnswer(userId, prompt, generateOpenAiService(apikey));
	}

	@Override
	public String textToSQL(String userId, String text, String apikey) {
		return textToSQL(userId, text, generateOpenAiService(apikey));
	}

	@Override
	public boolean isOpenAIServiceEnable() {
		return service != null;
	}

	private String textToAnswer(String userId, String prompt, OpenAiService service) {
		final StringBuilder answerText = new StringBuilder();

		log.debug("Open AI prompt:  ");
		log.debug(prompt);

		if (textToAnswerModel.isChatAPI()) {
			final ChatCompletionRequest chatCompletionRequest = buildChatRequest(prompt,
					textToAnswerModel.getMaxTokens(), textToAnswerModelTemperature, textToAnswerModel.getName(),
					textToAnswerModelStops);
			service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choices -> {
				final String answer = choices.getMessage().getContent();
				log.debug("Open AI Chat answer:  ");
				log.debug(answer);
				answerText.append(answer);
			});
		} else {
			final CompletionRequest completionRequest = buildLegacyAPIRequest(prompt, textToAnswerModel.getMaxTokens(),
					textToAnswerModelTemperature, textToAnswerModel.getName(), textToAnswerModelStops);
			service.createCompletion(completionRequest).getChoices().forEach(choices -> {
				final String answer = choices.getText();
				log.debug("Open AI answer:  ");
				log.debug(answer);
				answerText.append(answer);
			});
		}
		return answerText.toString();
	}

	private String textToSQL(String userId, String text, OpenAiService service) {
		final StringBuilder sql = new StringBuilder();

		final String prompt = generateRequestTextToSQLOpenAIStructure(userId, text);

		log.debug("Open AI prompt:  ");
		log.debug(prompt);

		if (textToSQLModel.isChatAPI()) {
			final ChatCompletionRequest chatCompletionRequest = buildChatRequest(prompt, textToSQLModel.getMaxTokens(),
					textToSQLModelTemperature, textToSQLModel.getName(), textToSQLModelStops);
			service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choices -> {
				final String answer = choices.getMessage().getContent();
				log.debug("Open AI Chat answer:  ");
				log.debug(answer);
				sql.append("SELECT " + answer);
			});
		} else {
			final CompletionRequest completionRequest = buildLegacyAPIRequest(prompt, textToSQLModel.getMaxTokens(),
					textToSQLModelTemperature, textToSQLModel.getName(), textToSQLModelStops);
			service.createCompletion(completionRequest).getChoices().forEach(choices -> {
				final String answer = choices.getText();
				log.debug("Open AI answer:  ");
				log.debug(answer);
				sql.append(answer.replace(prompt, "SELECT"));
			});
		}
		return sql.toString();
	}

	private OpenAiService generateOpenAiService(String apikey) {
		return new OpenAiService(apikey, DEFAULT_TIMEOUT_OPENAI);
	}

	private CompletionRequest buildLegacyAPIRequest(String request, int maxTokens, Double temperature, String modelId,
			List<String> stops) {
		final CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder().prompt(request);
		builder.temperature(temperature).frequencyPenalty(0.0).presencePenalty(0.0).model(modelId);
		if (stops != null) {
			builder.stop(stops);
		}
		return builder.build();
	}

	private ChatCompletionRequest buildChatRequest(String request, int maxTokens, Double temperature, String modelId,
			List<String> stops) {
		final ChatMessage message = new ChatMessage("user", request);
		final ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder()
				.messages(Collections.singletonList(message));

		builder.temperature(temperature).frequencyPenalty(0.0).presencePenalty(0.0).model(modelId);
		if (stops != null) {
			builder.stop(stops);
		}
		return builder.build();
	}

	/***
	 * Generate structure for OpenAI to provide information of the schema of all
	 * entities and their structure
	 */
	private String generateRequestTextToSQLOpenAIStructure(String userId, String text) {
		final StringBuilder request = new StringBuilder();
		request.append(
				"### SQL tables with sensitive names, with their multilevel properties separated by ,. In select, if field is subpath of other field only select one:\n");
		request.append("#\n");
		request.append(generateEntitiesStructureOfUser(userId));
		request.append("#\n");
		request.append("###").append(text).append("\n");
		request.append("SELECT");
		return request.toString();
	}

	private String generateEntitiesStructureOfUser(String userId) {
		final StringBuilder openAIEntities = new StringBuilder();

		ontologyService.getAllIdentificationsByUser(userId).forEach(ontology -> {
			try {
				openAIEntities.append("# ").append(ontology).append("(")
						.append(fieldsMapToString(ontologyService.getOntologyFieldsQueryTool(ontology, userId)))
						.append(");\n");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});
		return openAIEntities.toString();
	}

	private String fieldsMapToString(Map<String, String> fieldsMap) {
		final Set<String> keys = fieldsMap.keySet();
		return keys.stream() // Keep only not number (arrays) fields
				.filter(field -> Arrays.stream(field.split("\\.")).allMatch(path -> !path.matches("\\d+")))
				.collect(Collectors.joining(","));
	}

}
