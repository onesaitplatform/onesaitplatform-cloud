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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token.TokenType;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;

@Component("InsertSyntaxAnalyzer")
@Lazy
public class InsertSyntaxAnalyzer {
	
	public static final String TOKENSTRING = "  token.";
	public static final String NOTOKENSRECEIVED = "Expecting '(' but no tokens were received.";
	public static final String ERRORENTRY = "Expecting '(' but received ";
	
	public QueryAnalysisResult insertStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.INTO) {
				tokens.remove(0);
				return intoOntologyStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting INTO but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting Ontology name but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult intoOntologyStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.WORD) {
				result.setOntology(token.getContent());
				tokens.remove(0);
				return intoOperationStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting Field or * but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting Ontology name but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult intoOperationStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.OPERATION_SEPARATOR) {
				tokens.remove(0);
				// search for the operator name as a WORD
				if (!tokens.isEmpty()) {
					token = tokens.get(0);
					if (token.getType() == TokenType.WORD) {
						result.setOperation(token.getContent());
						tokens.remove(0);
						return valuesStatementProcessing(result, tokens);
					}
				}
				// Operation expected, nothing else found
				result.setErrorMessage("Expecting OPERATION name but received " + token.toString() + TOKENSTRING);
				result.setState(false);
				return result;
			}
		}
		/// send to process Where
		return valuesStatementProcessing(result, tokens);
	}

	private QueryAnalysisResult valuesStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.VALUES) {
				tokens.remove(0);
				return leftParenthesesFromValueProcessing(result, tokens);
			}
			result.setErrorMessage("Expecting VALUES but received " + token.toString() + TOKENSTRING);
		} else {
			result.setErrorMessage("Expecting VALUES but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult leftParenthesesFromValueProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.LEFT_PARETHESES) {
				tokens.remove(0);
				return jsonFromValueProcessing(result, tokens);
			}
			result.setErrorMessage(ERRORENTRY + token.toString() + TOKENSTRING);
		} else {
			result.setErrorMessage(NOTOKENSRECEIVED);
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult jsonFromValueProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.JSON) {
				result.setJsonObject(token.getContent());
				tokens.remove(0);
				return rightParenthesesFromValueProcessing(result, tokens);
			}
			result.setErrorMessage("Expecting a JSON but received " + token.toString() + TOKENSTRING);
		} else {
			result.setErrorMessage("Expecting a JSON but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult rightParenthesesFromValueProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.RIGHT_PARENTHESES) {
				tokens.remove(0);
				return endFromValueProcessing(result, tokens);
			}
			result.setErrorMessage(ERRORENTRY + token.toString() + TOKENSTRING);
		} else {
			result.setErrorMessage(NOTOKENSRECEIVED);
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult endFromValueProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.END) {
				tokens.remove(0);
				return result;
			}
			result.setErrorMessage(ERRORENTRY + token.toString() + TOKENSTRING);
			result.setState(false);
			return result;
		} else {
			result.setErrorMessage(NOTOKENSRECEIVED);
		}
		result.setState(true);
		return result;
	}
}
