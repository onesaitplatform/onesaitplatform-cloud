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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Primary
@Component("SQLHelperImpl")
public class SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SHOW TABLES";

	@Override
	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasLimit = (select.getLimit() != null);
		if(hasLimit) {
			final long oldLimit = ((LongValue) select.getLimit().getRowCount()).getValue();
			select.getLimit().setRowCount(new LongValue( Math.min(limit, oldLimit) ));
		} else {
			final Limit qLimit = new Limit();
			qLimit.setRowCount(new LongValue( Math.max(limit, 1) ));
			select.setLimit(qLimit);
		}

		return select;
	}

	@Override
	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);
		if(offset > 0) {
			final boolean hasOffset = (limitedSelect.getOffset() != null || (limitedSelect.getLimit() != null && limitedSelect.getLimit().getOffset() != null));
			if (hasOffset) {
				if (limitedSelect.getOffset() != null) {
					limitedSelect.getOffset().setOffset(offset);
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffset(offset);
				limitedSelect.setOffset(qOffset);
			}
		} else {
			limitedSelect.setOffset(null);
		}
		return limitedSelect;
	}

	@Override
	public String addLimit(final String query, final long limit) {
		final Optional<PlainSelect> selectStatement = parseQuery(query);
		if(selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit).toString();
		} else {
			return query;
		}
	}

	@Override
	public String addLimit(final String query, final long limit, final long offset) {
		final Optional<PlainSelect> selectStatement = parseQuery(query);
		if(selectStatement.isPresent()) {
			return addLimit(selectStatement.get(), limit, offset).toString();
		} else {
			return query;
		}
	}

	private Optional<PlainSelect> parseQuery(final String query){
		try {
			final Statement statement = CCJSqlParserUtil.parse(query);
			if (statement instanceof Select) {
				return Optional.ofNullable((PlainSelect) ((Select) statement).getSelectBody());
			} else {
				log.debug("The statement passed as argument is not a select query, returning the original");
				return Optional.empty();
			}
		} catch (final JSQLParserException e) {
			log.debug("Could not parse the query with JSQL returning the original. ", e);
			return Optional.empty();
		}
	}

}
