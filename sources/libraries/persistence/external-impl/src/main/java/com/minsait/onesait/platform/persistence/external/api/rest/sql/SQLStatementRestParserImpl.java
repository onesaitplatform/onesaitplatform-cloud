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
package com.minsait.onesait.platform.persistence.external.api.rest.sql;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.LexicalAnalyzer;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical.Token;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.SyntaxAnalyzer;
import com.minsait.onesait.platform.persistence.external.api.rest.sql.syntactical.util.QueryAnalysisResult;

@Component("SQLStatementRestParserImpl")
@Lazy
public class SQLStatementRestParserImpl implements SQLStatementRestParser {

	@Autowired
	private LexicalAnalyzer lexicalAnalyzer;
	@Autowired
	private SyntaxAnalyzer syntaxAnalyzer;

	@Override
	public QueryAnalysisResult parseRestSqlStatement(String query, List<String> pathParamNames,
			List<String> queryParamsNames) {

		List<Token> tokens = lexicalAnalyzer.lexicalAnalysis(query);
		return syntaxAnalyzer.runSyntaxAnalysis(tokens, pathParamNames, queryParamsNames);
	}

}
