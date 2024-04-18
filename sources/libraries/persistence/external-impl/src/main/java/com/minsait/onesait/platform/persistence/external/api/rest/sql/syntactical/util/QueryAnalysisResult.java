/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryAnalysisResult {
	public enum QueryType {
		SELECT, INSERT, UPDATE, DELETE
	}

	@Getter
	@Setter
	private QueryType queryType;
	@Getter
	@Setter
	private List<Token> tokens;
	@Getter
	@Setter
	private List<String> projectionFields = new ArrayList<>();
	@Getter
	@Setter
	private String ontology;
	@Getter
	@Setter
	private String operation;
	@Getter
	@Setter
	private String tableAlias;
	@Getter
	@Setter
	private Boolean state;
	@Getter
	private String errorMessage;
	@Getter
	@Setter
	private String jsonObject;
	@Getter
	@Setter
	private int parenthesesToBeClosed;
	@Getter
	@Setter
	private Map<String, String> pathParams = new HashMap<>();
	@Getter
	@Setter
	private Map<String, String> queryParams = new HashMap<>();
	@Getter
	@Setter
	private String jsonPathfilter;
	@Getter
	@Setter
	private List<String> requestedPathParams;
	@Getter
	@Setter
	private List<String> requestedQueryParams;
	@Getter
	private long limit;
	@Getter
	private long skip;
	@Getter
	private Boolean hasLimit;
	@Getter
	private Boolean hasSkip;
	private Boolean skipNextFilterOperation;

	public QueryAnalysisResult(List<String> requestedPathParams, List<String> requestedQueryParams) {
		parenthesesToBeClosed = 0;
		state = true;
		this.requestedPathParams = requestedPathParams;
		this.requestedQueryParams = requestedQueryParams;
		jsonPathfilter = "";
		this.hasLimit = false;
		this.hasSkip = false;
		this.skipNextFilterOperation = false;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		log.error(errorMessage);
	}

	public void openLeftParentheses() {
		this.parenthesesToBeClosed++;
		this.jsonPathfilter += "(";
	}

	public void closeRightParentheses() {
		this.parenthesesToBeClosed--;
		this.jsonPathfilter += ")";
	}

	public Boolean checkParenthesesCount() {
		return parenthesesToBeClosed == 0 ? true : false;
	}

	public Boolean isRightPrenthesesAble() {
		return parenthesesToBeClosed > 0 ? true : false;
	}

	public void addFilterOperation(String operation) {
		if(!this.skipNextFilterOperation){
			String newOper = "";
			if ("AND".equalsIgnoreCase(operation))
				newOper = "&&";
			else if ("OR".equalsIgnoreCase(operation))
				newOper = "||";
			this.jsonPathfilter += " " + newOper + " ";
		}
	}

	public void addFieldToProjection(String field) {
		this.projectionFields.add(field);
	}

	public void addFilterElement(FilterElement filter) {

		if (requestedPathParams.stream().anyMatch(pathParam -> pathParam.equalsIgnoreCase(filter.getField()))) {
			pathParams.put(filter.getField(), filter.getValue());
			this.skipNextFilterOperation = true;
		} else if (requestedQueryParams.stream()
				.anyMatch(queryParam -> queryParam.equalsIgnoreCase(filter.getField()))) {
			queryParams.put(filter.getField(), filter.getValue());
			this.skipNextFilterOperation = true;
		} else {
			// TODO write the JsonPath filter clause
			String fop = filter.getFop();
			String value = filter.getValue();
			if (fop.equals("="))
				fop = "==";
			if (value.startsWith("\"") && value.endsWith("\"")) {
				// change " for '
				value = "'" + value.substring(1, value.length() - 1) + "'";
			}
			this.jsonPathfilter += "@." + filter.getField() + fop + value;
			this.skipNextFilterOperation = false;
		}

	}

	public Boolean isSelectAll() {
		if (this.getProjectionFields().size() == 1) {
			return this.getProjectionFields().get(0).equals("*");
		}
		return false;
	}

	public void setLimit(long limit) {
		this.limit = limit;
		this.hasLimit = true;
	}

	public void setSkip(long skip) {
		this.skip = skip;
		this.hasSkip = true;
	}

}
