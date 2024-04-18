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
package com.minsait.onesait.platform.config.services.templates;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.QueryTemplate;
import com.minsait.onesait.platform.config.repository.QueryTemplateRepository;
import com.minsait.onesait.platform.config.services.exceptions.QueryTemplateServiceException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

@Slf4j
@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

	@Autowired
	private QueryTemplateRepository queryTemplateRepository;

	final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	@Override
	public PlatformQuery getTranslatedQuery(String ontology, String query) {

		final List<QueryTemplate> templates = new ArrayList<>(
				queryTemplateRepository.findByOntologyIdentification(ontology));
		templates.addAll(queryTemplateRepository.findByOntologyIdentificationIsNull());
		MatchResult result = new MatchResult();
		result.setResult(false);
		QueryTemplate template = null;

		try {
			for (int i = 0; i < templates.size() && !result.isMatch(); i++) {
				template = templates.get(i);
				result = SqlComparator.match(query, template.getQuerySelector());
			}

			if (result.isMatch()) {
				final String newStringQuery = processQuery(template, result);
				return new PlatformQuery(newStringQuery, template.getType());
			}
		} catch (ScriptException | JSQLParserException | NoSuchMethodException e) {
			log.error("Error matching query template", e);
			return null;
		}
		return null;

	}

	private String processQuery(QueryTemplate template, MatchResult result)
			throws NoSuchMethodException, ScriptException {
		String query = replaceVariables(template.getQueryGenerator(), result.getVariables());
		query = processQuery(query, template.getName());
		return query;
	}

	String replaceVariables(String queryGenerator, Map<String, VariableData> variables) {

		String newQuery = queryGenerator;
		final Set<String> variableNames = variables.keySet();
		for (final String variableName : variableNames) {
			final VariableData variable = variables.get(variableName);
			final String newValue = variable.getStringValue();
			newQuery = newQuery.replace("@" + variableName, newValue);
		}

		return newQuery;
	}

	String processQuery(String query, String templateName) throws ScriptException, NoSuchMethodException {

		try {
			final String scriptPostprocessFunction = "function postprocess(){ " + query + " }";
			final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
					scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
			engine.eval(new InputStreamReader(scriptInputStream));
			final Invocable inv = (Invocable) engine;
			Object result;
			result = inv.invokeFunction("postprocess");
			return result.toString();
		} catch (final ScriptException e) {
			log.trace("Error processing query in query template: {}", templateName, e);
			throw e;
		} catch (final NoSuchMethodException e) {
			log.trace("Error invoking processing function in query template: {}", templateName, e);
			throw e;
		}
	}

	@Override
	public List<QueryTemplate> getAllQueryTemplates() {
		return queryTemplateRepository.findAll();
	}

	@Override
	public QueryTemplate createQueryTemplate(QueryTemplate queryTemplate) {
		if (!queryTemplateExists(queryTemplate)) {
			log.debug("Query Template doesn't exist, creating...");
			return queryTemplateRepository.save(queryTemplate);
		} else {
			throw new QueryTemplateServiceException("Query Template already exists in Database");
		}
	}

	@Override
	public boolean queryTemplateExists(QueryTemplate queryTemplate) {
		return (queryTemplateRepository.findByName(queryTemplate.getName()) != null);
	}

	@Override
	public QueryTemplate getQueryTemplateById(String id) {
		return queryTemplateRepository.findById(id).orElse(null);
	}

	@Override
	public List<QueryTemplate> getQueryTemplateByCriteria(String name) {
		return queryTemplateRepository.findByNameContaining(name);
	}

	@Override
	public void updateQueryTemplate(QueryTemplate queryTemplate) {
		if (queryTemplateExists(queryTemplate)) {
			queryTemplateRepository.findById(queryTemplate.getId()).ifPresent(queryTemplateDB -> {
				queryTemplateDB.setName(queryTemplate.getName());
				queryTemplateDB.setDescription(queryTemplate.getDescription());
				queryTemplateDB.setOntology(queryTemplate.getOntology());
				queryTemplateDB.setQueryGenerator(queryTemplate.getQueryGenerator());
				queryTemplateDB.setQuerySelector(queryTemplate.getQuerySelector());
				queryTemplateDB.setType(queryTemplate.getType());
				queryTemplateRepository.save(queryTemplateDB);
			});

		} else
			throw new QueryTemplateServiceException("Cannot update a query template that does not exist");
	}

	@Override
	public void checkQueryTemplateSelectorExists(String templateId, String ontology, String query) {
		final List<QueryTemplate> templates = new ArrayList<>(
				queryTemplateRepository.findByOntologyIdentification(ontology));
		templates.addAll(queryTemplateRepository.findByOntologyIdentificationIsNull());
		MatchResult result = new MatchResult();
		result.setResult(false);
		QueryTemplate template = null;

		try {
			for (int i = 0; i < templates.size() && !result.isMatch(); i++) {
				template = templates.get(i);
				if (!template.getName().equals(templateId)) {
					result = SqlComparator.match(query, template.getQuerySelector());
				}
			}
		} catch (Exception e) {
			log.trace("Error processing Selector", e);
		}
		if (result.isMatch()) {
			String logmessage = "Query Template Selector already used";
			if (!ontology.equals("")) {
				logmessage = logmessage + " for entity: " + ontology;
			}
			log.trace(logmessage);
			throw new QueryTemplateServiceException(logmessage);
		}
	}

}
