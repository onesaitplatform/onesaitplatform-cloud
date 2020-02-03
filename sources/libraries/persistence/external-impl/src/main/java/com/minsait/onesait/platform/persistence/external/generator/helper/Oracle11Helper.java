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
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("Oracle11Helper")
@Slf4j
public class Oracle11Helper extends SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM user_tables";
	private static final String ROWNUM_STR = "ROWNUM";
	private static final String ROWNUM_ALIAS_STR = "ROWNUMALIAS";

	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	public PlainSelect addLimit(final PlainSelect plainSelect, final long limit) {
		// Limit SELECT
		final PlainSelect limitSelect = new PlainSelect();
		limitSelect.setSelectItems(plainSelect.getSelectItems());
		// Limit WHERE
		final MinorThanEquals rowNumLimit = new MinorThanEquals();
		rowNumLimit.setLeftExpression(new Column(ROWNUM_STR));
		rowNumLimit.setRightExpression(new LongValue(limit));
		limitSelect.setWhere(rowNumLimit);
		// Adds the original query to the limit query using sub select
		final SubSelect limitSubSelect = new SubSelect();
		limitSubSelect.setSelectBody(plainSelect);
		limitSelect.setFromItem(limitSubSelect);

		return limitSelect;
	}

	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		if (offset <= 0) {
			return addLimit(select, limit);
		}

		final List<SelectItem> selectItems = select.getSelectItems();
		final boolean hasAllColumns = selectItems.stream()
				.anyMatch(expression -> expression instanceof AllColumns );

		if (hasAllColumns) {
			if(log.isWarnEnabled() ) {
				log.warn("The statement {} passed as argument has all columns (*) in the select items and thus offset can not be set, returning the just the query limited", select.toString());
			}
			return addLimit(select, limit);
		} else {
			// Gets the limited query an adds a column rownum with alias
			final PlainSelect selectWithLimit = addLimit(select, limit+offset);
			final List<SelectItem> limitSelectItems = new ArrayList<>(selectItems);
			final SelectExpressionItem rowNumColumn = new SelectExpressionItem(new Column(ROWNUM_STR));
			rowNumColumn.setAlias(new Alias(ROWNUM_ALIAS_STR));
			limitSelectItems.add(rowNumColumn);
			selectWithLimit.setSelectItems(limitSelectItems);

			final PlainSelect offsetSelect = new PlainSelect();
			offsetSelect.setSelectItems(selectItems);
			// Where expression
			final GreaterThan rowNumOffset = new GreaterThan();
			rowNumOffset.setLeftExpression(new Column(ROWNUM_ALIAS_STR));
			rowNumOffset.setRightExpression(new LongValue(offset));
			offsetSelect.setWhere(rowNumOffset);

			// Adds the limited query to the query with offset using sub select
			final SubSelect offsetSubSelect = new SubSelect();
			offsetSubSelect.setSelectBody(selectWithLimit);
			offsetSelect.setFromItem(offsetSubSelect);
			return offsetSelect;
		}
	}

}
