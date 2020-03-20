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
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component("SQLServerHelper")
@Slf4j
public class SQLServerHelper extends SQLHelperImpl implements SQLHelper {

	private final static String LIST_TABLES_QUERY = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES";

	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	public PlainSelect addLimit(final PlainSelect select, final long limit){
		final boolean hasTop = (select.getTop() != null);
		final boolean hasFetch = (select.getFetch() != null);
		if(hasFetch){
			final long oldFetch = select.getFetch().getRowCount();
			final long newFetch = Math.min( oldFetch, limit );
			select.getFetch().setRowCount(newFetch);
		} else if (hasTop) {
			final long oldTop = ((LongValue) select.getTop().getExpression()).getValue();
			final long newTop = Math.min(oldTop, limit);
			select.getTop().setExpression(new LongValue(newTop));
		} else {
			final Top top = new Top();
			top.setExpression(new LongValue(limit));
			select.setTop(top);
		}
		return select;
	}

	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);
		if(offset > 0){
			final boolean hasOrderBy = (limitedSelect.getOrderByElements() != null);
			final boolean hasTop = (limitedSelect.getTop() != null);
			final boolean hasOffset = (limitedSelect.getOffset() != null);

			// Order by mandatory to do offset
			if( !hasOrderBy ) {
				final OrderByElement orderByElement = new OrderByElement();
				orderByElement.setExpression(new LongValue(1));
				final List<OrderByElement> orderByList = new LinkedList<>();
				orderByList.add(orderByElement);
				limitedSelect.setOrderByElements(orderByList);
			}

			// Change to fetch to use offset
			if(hasTop){
				final long finalLimit = ((LongValue) select.getTop().getExpression()).getValue();
				limitedSelect.setTop(null);

				final Fetch fetch = new Fetch();
				fetch.setRowCount(finalLimit);
				fetch.setFetchParam("ROWS");
				limitedSelect.setFetch(fetch);
			}

			// Set new offset
			if(hasOffset) {
				limitedSelect.getOffset().setOffset(offset);
			} else {
				final Offset newOffset = new Offset();
				newOffset.setOffset(offset);
				newOffset.setOffsetParam("ROWS");
				limitedSelect.setOffset(newOffset);
			}
		} else {
			limitedSelect.setOffset(null);
		}
		return limitedSelect;
	}

}