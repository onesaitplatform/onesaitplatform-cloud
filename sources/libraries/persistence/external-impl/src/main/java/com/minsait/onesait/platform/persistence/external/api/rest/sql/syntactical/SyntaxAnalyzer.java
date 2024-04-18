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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token.TokenType;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult.QueryType;

@Component("SyntaxAnalyzer")
@Lazy
public class SyntaxAnalyzer {

	@Autowired
	private final SelectSyntaxAnalyzer selectAnalyzer = new SelectSyntaxAnalyzer();
	@Autowired
	private final InsertSyntaxAnalyzer insertAnalyzer = new InsertSyntaxAnalyzer();
	@Autowired
	private final DeleteSyntaxAnalyzer deleteAnalyzer = new DeleteSyntaxAnalyzer();
	@Autowired
	private UpdateSyntaxAnalyzer updateAnalyzer;

	public QueryAnalysisResult runSyntaxAnalysis(List<Token> tokens, List<String> requestedPathParams,
			List<String> requestedQueryParams) {
		final QueryAnalysisResult parserResult = new QueryAnalysisResult(requestedPathParams, requestedQueryParams);
		parserResult.setTokens(tokens);
		return queryTypeProcessing(parserResult, tokens);
	}

	private QueryAnalysisResult queryTypeProcessing(QueryAnalysisResult result, List<Token> tokens) {

		if (!tokens.isEmpty()) {
			final Token token = tokens.get(0);
			if (token.getType() == TokenType.QUERY_TYPE) {
				switch (token.getContent().toUpperCase()) {
				case "SELECT":
					tokens.remove(0);
					result.setQueryType(QueryType.SELECT);
					return selectAnalyzer.selectStatementProcessing(result, tokens);
				case "INSERT":
					tokens.remove(0);
					result.setQueryType(QueryType.INSERT);
					return insertAnalyzer.insertStatementProcessing(result, tokens);
				case "UPDATE":
					tokens.remove(0);
					result.setQueryType(QueryType.UPDATE);
					return updateAnalyzer.updateStatementProcessing(result, tokens);
				case "DELETE":
					tokens.remove(0);
					result.setQueryType(QueryType.DELETE);
					return deleteAnalyzer.deleteStatementProcessing(result, tokens);
				default:
					break;
				}

			}
		}

		result.setErrorMessage("Expecting SELECT, INSERT, DELETE or UPDATE but no tokens where received.");
		result.setState(false);
		return result;
	}

}
