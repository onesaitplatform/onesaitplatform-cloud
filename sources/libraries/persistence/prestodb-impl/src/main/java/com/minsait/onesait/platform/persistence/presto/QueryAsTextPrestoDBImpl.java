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
package com.minsait.onesait.platform.persistence.presto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

@Component("QueryAsTextPrestoDBImpl")
@Scope("prototype")
@Slf4j
public class QueryAsTextPrestoDBImpl implements QueryAsTextDBRepository {

	@Autowired
	private PrestoOntologyOpsDBRepository prestoOntologyOps;

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		try {
			checkQueryIs4Ontology(ontology, query);
			return this.prestoOntologyOps.queryNativeAsJson(ontology, query, offset, limit);
		} catch (Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		try {
			checkQueryIs4Ontology(ontology, query);
			Statement statement = CCJSqlParserUtil.parse(query);

			if (statement instanceof Select) {
				return this.prestoOntologyOps.queryNativeAsJson(ontology, query);
			} else if (statement instanceof Insert) {
				return this.prestoOntologyOps.insertNative(ontology, query, false).toString();
			} else if (statement instanceof Update) {
				return this.prestoOntologyOps.updateNative(ontology, query, false).toString();
			} else if (statement instanceof Delete) {
				return this.prestoOntologyOps.deleteNative(ontology, query, false).toString();
			}

			return "";
		} catch (Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage(), e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		try {
			checkQueryIs4Ontology(ontology, query);
			return this.prestoOntologyOps.querySQLAsJson(ontology, query, offset);
		} catch (Exception e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		try {
			checkQueryIs4Ontology(ontology, query);
			return this.prestoOntologyOps.querySQLAsJson(ontology, query, offset, limit);
		} catch (Exception e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}
	
	private void checkQueryIs4Ontology(String ontology, String query) throws GenericOPException {
		query = query.replace("\n", "");
		query = StringUtils.normalizeSpace(query);
		final String selectQuery = "from " + ontology.toLowerCase();
				
		if (!query.toLowerCase().endsWith(selectQuery)
				&& !query.toLowerCase().endsWith(selectQuery+";")
				&& query.toLowerCase().indexOf(selectQuery + " ") == -1 
				&& query.toLowerCase().indexOf("insert into " + ontology.toLowerCase() + " ") == -1
				&& query.toLowerCase().indexOf("update " + ontology.toLowerCase() + " ") == -1
				&& (query.toLowerCase().indexOf(selectQuery) == -1 
				|| (query.toLowerCase().indexOf(selectQuery) != -1 && !query.endsWith(ontology.toLowerCase())))) {
			throw new GenericOPException("The query '" + query + "' is not for the ontology selected: " + ontology);
		}
	};
}
