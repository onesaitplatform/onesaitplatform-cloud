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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token.TokenType;

@Component("LexicalAnalizer")
@Lazy
public class LexicalAnalyzer {
	private static List<String> fieldOperators = Arrays.asList("=", "!=", "<", "<=", ">", ">=");
	private static List<String> twoCharsOperatorsBeginning = Arrays.asList("!", "<", ">");
	private static List<String> queryTypes = Arrays.asList("SELECT", "UPDATE", "DELETE", "INSERT");
	private static List<String> operators = Arrays.asList("AND", "OR");
	private static String tokenizerSeparators = " =!;,().{}\"";

	public List<Token> lexicalAnalysis(String statement) {

		List<Token> tokens = new ArrayList<>();
		//statement.replaceAll("\n", " ");
		StringTokenizer tokenizer = new StringTokenizer(statement, tokenizerSeparators, true);
		String prevToken = "";
		int jsonLeftBracesOpen = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			// check 2 chars operators
			if (isPotencialOperator(token)) {
				prevToken = token;
				token = tokenizer.nextToken();
				if (token.equals("=")) {
					token = prevToken + token;
				} else {
					// process previous token and token
					tokens.add(new Token(TokenType.FOP, prevToken));
				}
			}
			if (token.equals("{")) {
				String json = "{";
				jsonLeftBracesOpen++;
				while (tokenizer.hasMoreTokens() && jsonLeftBracesOpen != 0) {
					token = tokenizer.nextToken();
					json += token;
					if (token.equals("}"))
						jsonLeftBracesOpen--;
					if (token.equals("{"))
						jsonLeftBracesOpen++;
				}
				// read until all { are closed
				tokens.add(new Token(TokenType.JSON, json));
			} else if (token.equals("\"")) {
				String word = token;
				Boolean closedQuotes = false;
				while (tokenizer.hasMoreTokens() && !closedQuotes) {
					token = tokenizer.nextToken();
					word += token;
					if (token.equals("\""))
						closedQuotes = true;
				}
				tokens.add(new Token(TokenType.WORD, word));
			} else if (!token.equals(" "))
				tokens.add(processToken(token));
		}

		return tokens;
	}

	private static Token processToken(final String token) {
		if (fieldOperators.stream().anyMatch(str -> str.equalsIgnoreCase(token)))
			return new Token(TokenType.FOP, token);
		if (operators.stream().anyMatch(str -> str.equalsIgnoreCase(token)))
			return new Token(TokenType.OPERATOR, token);
		if (queryTypes.stream().anyMatch(str -> str.equalsIgnoreCase(token)))
			return new Token(TokenType.QUERY_TYPE, token);
		if ("WHERE".equalsIgnoreCase(token))
			return new Token(TokenType.WHERE, token);
		if ("FROM".equalsIgnoreCase(token))
			return new Token(TokenType.FROM, token);
		if ("INTO".equalsIgnoreCase(token))
			return new Token(TokenType.INTO, token);
		if ("VALUES".equalsIgnoreCase(token))
			return new Token(TokenType.VALUES, token);
		if ("SET".equalsIgnoreCase(token))
			return new Token(TokenType.SET, token);
		if (".".equalsIgnoreCase(token))
			return new Token(TokenType.OPERATION_SEPARATOR, token);
		if ("*".equalsIgnoreCase(token))
			return new Token(TokenType.ALL_FIELDS, token);
		if (",".equalsIgnoreCase(token))
			return new Token(TokenType.FIELD_SEPARATOR, token);
		if ("(".equalsIgnoreCase(token))
			return new Token(TokenType.LEFT_PARETHESES, token);
		if (")".equalsIgnoreCase(token))
			return new Token(TokenType.RIGHT_PARENTHESES, token);
		if (";".equalsIgnoreCase(token))
			return new Token(TokenType.END, token);
		if ("SKIP".equalsIgnoreCase(token))
			return new Token(TokenType.SKIP, token);
		if ("OFFSET".equalsIgnoreCase(token))
			return new Token(TokenType.OFFSET, token);
		if ("LIMIT".equalsIgnoreCase(token))
			return new Token(TokenType.LIMIT, token);
		if ("AS".equalsIgnoreCase(token))
			return new Token(TokenType.AS, token);
		return new Token(TokenType.WORD, token);
	}

	private static Boolean isPotencialOperator(String token) {
		for (String opIni : twoCharsOperatorsBeginning) {
			if (opIni.equals(token))
				return true;
		}
		return false;
	}
}
