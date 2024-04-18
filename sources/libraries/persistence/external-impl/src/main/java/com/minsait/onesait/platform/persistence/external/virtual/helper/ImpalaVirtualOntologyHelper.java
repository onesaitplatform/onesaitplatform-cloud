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
package com.minsait.onesait.platform.persistence.external.virtual.helper;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

@Component("ImpalaVirtualOntologyHelper")
@Slf4j
public class ImpalaVirtualOntologyHelper implements VirtualOntologyHelper {

	private static final String LIST_TABLES_QUERY = "SHOW TABLES";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}
	
	@Override
	public String addLimit(String query, final long limit) {
		try {
			Statement statement = CCJSqlParserUtil.parse(query);
			if(statement instanceof Select) {
				PlainSelect select = (PlainSelect) ((Select) statement).getSelectBody();
				final Boolean hasLimit = (select.getLimit() != null);
				
				if ( !hasLimit ) {
					Limit qlimit = new Limit();
					qlimit.setRowCount(new LongValue( (limit > 1 ? limit : 1) ));
 					select.setLimit(qlimit);
				} else {
					final long nlimit = Math.min( ((LongValue) select.getLimit().getRowCount()).getValue(), limit );
					select.getLimit().setRowCount(new LongValue( (nlimit > 1 ? nlimit : 1) ));
				}
				
			}

			return statement.toString();
		} catch (JSQLParserException e) {
			log.error("Error adding LIMIT with no offset to query, returning the original. "+e.getMessage(), e);
			return query;
		}
	}

	@Override
	public String addLimit(String query, final long limit, final long offset) {
		try {
			Statement statement = CCJSqlParserUtil.parse(query);
			if(statement instanceof Select) {
				PlainSelect select = (PlainSelect) ((Select) statement).getSelectBody();
				final Boolean hasLimit = (select.getLimit() != null);
				
				if ( !hasLimit ) {
					Limit qlimit = new Limit();
					qlimit.setRowCount(new LongValue( (limit > 1 ? limit : 1) ));
					qlimit.setOffset(new LongValue( (offset > 0 ? offset : 0) ));
 					select.setLimit(qlimit);
				} else {
					final long nlimit = Math.min( ((LongValue) select.getLimit().getRowCount()).getValue(), limit );
					select.getLimit().setRowCount(new LongValue( (nlimit > 1 ? nlimit : 1) ));
					if(select.getOffset() == null) {
						Offset offsetObj = new Offset();
						offsetObj.setOffset(offset >= 0l ? offset : 0l);
						select.setOffset(offsetObj);
					}
				}
				
			}

			return statement.toString();
		} catch (JSQLParserException e) {
			log.error("Error adding LIMIT with offset to query, returning the original. "+e.getMessage(), e);
			return query;
		}
	}
	
	

}
