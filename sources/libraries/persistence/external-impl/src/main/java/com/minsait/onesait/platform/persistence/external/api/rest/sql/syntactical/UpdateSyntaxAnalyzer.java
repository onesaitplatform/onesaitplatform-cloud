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
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.FilterElement;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;

@Component("UpdateSyntaxAnalyzer")
@Lazy
public class UpdateSyntaxAnalyzer {
	
	public static final String TOKENSTRING = "  token.";
	
	public QueryAnalysisResult updateStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.WORD) {
				result.setOntology(token.getContent());
				tokens.remove(0);
				return operationStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting Field or * but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting Ontology name but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult operationStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
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
						return setStatementProcessing(result, tokens);
					}
				}
				// Operation expected, nothing else found
				result.setErrorMessage("Expecting OPERATION name but received " + token.toString() + TOKENSTRING);
				result.setState(false);
				return result;
			}
		}
		/// send to process Where
		return setStatementProcessing(result, tokens);
	}

	public QueryAnalysisResult setStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.SET) {
				tokens.remove(0);
				return jsonStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting SET but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting SET but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	public QueryAnalysisResult jsonStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.JSON) {
				result.setJsonObject(token.getContent());
				tokens.remove(0);
				return whereStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting SET but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting SET but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult whereStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.WHERE) {
				tokens.remove(0);
				if (!tokens.isEmpty())
					return whereFilterStatementProcessing(result, tokens);
				// Something needed after WHERE clause
				result.setState(false);
				result.setErrorMessage("Expecting '(' or FIELD name but no tokens were recieved.");
				return result;
			} else if (token.getType() == TokenType.END) {
				result.setState(true);
				return result;
			}
			// WHERE clause expected, something else found
			result.setErrorMessage("Expecting WHERE clause or END token but received " + token.toString() + TOKENSTRING);
			result.setState(false);
			return result;
		}
		// no where clause, so it's all done
		result.setState(true);
		return result;
	}

	private QueryAnalysisResult whereFilterStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.WORD) {
				// Evaluate all sequence (FIELD + FOP + VALUE)
				FilterElement filterElement = new FilterElement();
				filterElement.setField(token.getContent());

				// Consume FIELD
				tokens.remove(0);
				if (!tokens.isEmpty()) {
					token = tokens.get(0);
					if (token.getType() == TokenType.FOP) {
						// Consume FIELD OPERATOR (FOP)
						filterElement.setFop(token.getContent());
						tokens.remove(0);
						if (!tokens.isEmpty()) {
							token = tokens.get(0);
							if (token.getType() == TokenType.WORD) {
								filterElement.setValue(token.getContent());
								// Consume VALUE
								tokens.remove(0);
								result.addFilterElement(filterElement);
								return closeParenthesesOrOpProcessing(result, tokens);
							}
							result.setErrorMessage("Expecting Value after field comparator but received "
									+ token.toString() + TOKENSTRING);
						} else {
							result.setErrorMessage(
									"Expecting Value after field comparator but no tokens were received.");
						}
					} else {
						result.setErrorMessage("Expecting Field comparator '=', '!=', '>',... but received "
								+ token.toString() + TOKENSTRING);
					}
				} else {
					result.setErrorMessage(
							"Expecting Field comparator '=', '!=', '>',... but no tokens were received.");
				}
				result.setState(false);
				return result;
			} else if (token.getType() == TokenType.LEFT_PARETHESES) {
				tokens.remove(0);
				result.openLeftParentheses();
				// Check again if starts a filter clause (field + FOP + value)
				return whereFilterStatementProcessing(result, tokens);
			} else if (token.getType() == TokenType.END && result.checkParenthesesCount()) {
				result.setState(true);
				return result;
			}
			result.setErrorMessage("Expecting '(' or FIELD name but no tokens were received.");
			result.setState(false);
			return result;
		}
		// If no more tokens then its OK == END
		result.setState(true);
		return result;
	}

	private QueryAnalysisResult closeParenthesesOrOpProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.RIGHT_PARENTHESES) {
				// check if its in place
				if (result.isRightPrenthesesAble()) {
					result.closeRightParentheses();
					tokens.remove(0);
					return closeParenthesesOrOpProcessing(result, tokens);
				} else {
					// not expected ), as there are not enough previous (
					result.setErrorMessage("Not expected ')' token.");
				}
			} else if (token.getType() == TokenType.OPERATOR) {
				result.addFilterOperation(token.getContent());
				tokens.remove(0);
				return whereFilterStatementProcessing(result, tokens);
			} else if (token.getType() == TokenType.END) {
				if (result.checkParenthesesCount()) {
					result.setState(true);
					return result;
				}
			} else {
				// Not expected token received
				result.setErrorMessage(
						"Expecting ')', 'AND' or 'OR' clause but received " + token.getType().toString() + TOKENSTRING);
			}
		} else {
			// if no left parentheses to close, its ok
			if (result.checkParenthesesCount()) {
				// END
				result.setState(true);
				return result;
			}
			result.setErrorMessage("Expecting ')' but no tokens were received.");
		}
		result.setState(false);
		return result;
	}
}
