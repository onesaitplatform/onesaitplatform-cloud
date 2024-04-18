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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token.TokenType;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.FilterElement;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;

@Component("SelectSyntaxAnalyzer")
@Lazy
public class SelectSyntaxAnalyzer {
	
	public static final String TOKENSTRING = "  token.";
	public static final String TOKENSRECEIVED = " token was received";
	public static final String ERRORPARENTHESES = "Where clause parentheses are not closd properly.";
	
	public QueryAnalysisResult selectStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.ALL_FIELDS || token.getType() == TokenType.WORD) {
				result.addFieldToProjection(token.getContent());
				tokens.remove(0);
				return fromStatementProcessing(result, tokens);
			}
			result.setErrorMessage("Expecting Field or * but received " + token.toString() + TOKENSTRING);
		} else {
			result.setErrorMessage("Expecting Field or * but no tokens where received.");
		}

		result.setState(false);
		return result;
	}

	private QueryAnalysisResult fromStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.FIELD_SEPARATOR) {
				// consume separator, expecting more WORDs as FIELDS
				tokens.remove(0);
				return selectStatementProcessing(result, tokens);
			} else if (token.getType() == TokenType.FROM) {
				// Check table and op
				tokens.remove(0);
				return fromOntologyStatementProcessing(result, tokens);
			}
			result.setErrorMessage("Expecting Field or FROM but received: " + token.toString());
		} else {
			result.setErrorMessage("Expecting Field or FROM but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult fromOntologyStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.WORD) {
				result.setOntology(token.getContent());
				tokens.remove(0);
				return fromOperationStatementProcessing(result, tokens);
			} else {
				result.setErrorMessage("Expecting Field or * but received " + token.toString() + TOKENSTRING);
			}
		} else {
			result.setErrorMessage("Expecting Ontology name but no tokens were received.");
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult fromOperationStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
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
						return fromAliasStatementProcessing(result, tokens);
					}
				}
				// Operation expected, nothing else found
				result.setErrorMessage("Expecting OPERATION name but received " + token.toString() + TOKENSTRING);
				result.setState(false);
				return result;
			}
		}
		/// send to process Where
		return fromAliasStatementProcessing(result, tokens);
	}

	private QueryAnalysisResult fromAliasStatementProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.AS) {
				tokens.remove(0);
				if (!tokens.isEmpty()) {
					token = tokens.get(0);
					if (token.getType() == TokenType.WORD) {
						result.setTableAlias(token.getContent());
						tokens.remove(0);
						return whereStatementProcessing(result, tokens);
					}
					result.setErrorMessage("Expecting alias name but received " + token.toString() + TOKENSTRING);
				} else {
					result.setErrorMessage("Expecting alias name name but no token was received.");
				}
				result.setState(false);
				return result;

			}
		}
		/// send to process Where
		return whereStatementProcessing(result, tokens);
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
			} else if (token.getType() == TokenType.LIMIT || token.getType() == TokenType.SKIP) {
				return skipOrLimitProcessing(result, tokens);
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
			} else if (token.getType() == TokenType.END) {
				if (result.checkParenthesesCount()) {
					result.setState(true);
					return result;
				}
				result.setErrorMessage(ERRORPARENTHESES);
			} else if (token.getType() == TokenType.LIMIT || token.getType() == TokenType.SKIP) {
				if (result.checkParenthesesCount()) {
					return skipOrLimitProcessing(result, tokens);
				}
				result.setErrorMessage(ERRORPARENTHESES);
			}
			result.setErrorMessage(
					"Expecting '(' , FIELD, LIMIT or SKIP name but " + token.toString() + " was received.");
			result.setState(false);
			return result;
		}
		// If no more tokens then its OK == END
		result.setState(true);
		return result;
	}

	private QueryAnalysisResult fieldFilterProcessing(QueryAnalysisResult result, List<Token> tokens) {
		// Evaluate all sequence (FIELD + FOP + VALUE)
		Token token = tokens.get(0);
		String aliasOrField = token.getContent();
		FilterElement filterElement = new FilterElement();

		// Consume FIELD
		tokens.remove(0);
		if (!tokens.isEmpty()) {
			token = tokens.get(0);
			if (token.getType() == TokenType.FOP) {
				// Consume FIELD OPERATOR (FOP)
				filterElement.setField(aliasOrField);
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
					result.setErrorMessage(
							"Expecting Value after field comparator but received " + token.toString() + TOKENSTRING);
				} else {
					result.setErrorMessage("Expecting Value after field comparator but no tokens were received.");
				}
			} else {
				result.setErrorMessage(
						"Expecting Field comparator '=', '!=', '>',... but received " + token.toString() + TOKENSTRING);
			}
		} else if (token.getType() == TokenType.OPERATION_SEPARATOR) {
			// it is an alias
			tokens.remove(0);
			if (!aliasOrField.equalsIgnoreCase(result.getTableAlias())) {
				result.setErrorMessage("Field alias does not match with origin alias.");
			} else if (!tokens.isEmpty()) {
				token = tokens.get(0);
				if (token.getType() == TokenType.WORD) {
					return fieldFilterProcessing(result, tokens);
				} else {
					result.setErrorMessage(
							"Expecting Field after alias name but received " + token.toString() + " token.");
				}
			} else {
				result.setErrorMessage("Expecting Field after alias name but no tokens were received.");
			}
		} else {
			result.setErrorMessage("Expecting Field comparator '=', '!=', '>',... but no tokens were received.");
		}
		result.setState(false);
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
				result.setErrorMessage(ERRORPARENTHESES);
			} else if (token.getType() == TokenType.LIMIT || token.getType() == TokenType.SKIP
					|| token.getType() == TokenType.OFFSET) {
				if (result.checkParenthesesCount()) {
					return skipOrLimitProcessing(result, tokens);
				}
				result.setErrorMessage(ERRORPARENTHESES);
			} else {
				// Not expected token received
				result.setErrorMessage("Expecting ')', 'AND', 'OR', 'LIMIT' or 'SKIP' clause but received "
						+ token.toString() + TOKENSTRING);
			}
		} else {
			// if no left parentheses to close, its ok
			if (result.checkParenthesesCount()) {
				// END
				result.setState(true);
				return result;
			}
			result.setErrorMessage(ERRORPARENTHESES);
		}
		result.setState(false);
		return result;
	}

	private QueryAnalysisResult skipOrLimitProcessing(QueryAnalysisResult result, List<Token> tokens) {
		if (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token.getType() == TokenType.LIMIT) {
				tokens.remove(0);
				token = tokens.get(0);
				if (token.getType() == TokenType.WORD) {
					try {
						result.setLimit(Long.parseLong(token.getContent()));
						tokens.remove(0);
						return skipOrLimitProcessing(result, tokens);
					} catch (NumberFormatException e) {
						result.setErrorMessage("LIMIT value must be a non negative number.");
					}
				} else {
					result.setErrorMessage("Expexting LIMIT value but " + token.toString() + TOKENSRECEIVED);
				}
			} else if (token.getType() == TokenType.SKIP || token.getType() == TokenType.OFFSET) {
				tokens.remove(0);
				token = tokens.get(0);
				if (token.getType() == TokenType.WORD) {
					try {
						result.setSkip(Long.parseLong(token.getContent()));
						tokens.remove(0);
						return skipOrLimitProcessing(result, tokens);
					} catch (NumberFormatException e) {
						result.setErrorMessage("SKIP value must be a non negative number.");
					}
				} else {
					result.setErrorMessage("Expexting SKIP value but " + token.toString() + TOKENSRECEIVED);
				}
			}
			else {
				result.setErrorMessage(
						"Expexting LIMIT or SKIP clause but " + token.toString() + TOKENSRECEIVED);
			}

			result.setState(false);
			return result;
		}
		result.setState(true);
		return result;
	}
}
