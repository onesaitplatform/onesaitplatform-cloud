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


@Component("OracleHelper")
@Slf4j
public class OracleHelper extends SQLHelperImpl implements SQLHelper {

	private static final String LIST_TABLES_QUERY = "SELECT table_name FROM user_tables";

	public String getAllTablesStatement() {
		return LIST_TABLES_QUERY;
	}

	public PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasFetch = select.getFetch() != null;
		if(hasFetch){
			final long oldFetch = select.getFetch().getRowCount();
			select.getFetch().setRowCount(Math.min(oldFetch, limit));
		} else {
			final Fetch qFetch = new Fetch();
			qFetch.setFetchParam("ROWS");
			qFetch.setRowCount(Math.max(limit, 1));
			select.setFetch(qFetch);
		}
		return select;
	}

	public PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);
		if(offset > 0){
			final boolean hasOffset = limitedSelect.getOffset() != null;
			if(hasOffset){
				if(limitedSelect.getOffset() != null){
					limitedSelect.getOffset().setOffset(offset);
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffsetParam("ROWS");
				qOffset.setOffset(Math.max(offset, 0));
				limitedSelect.setOffset(qOffset);
			}
		} else {
			limitedSelect.setOffset(null);
		}
		return limitedSelect;
	}

}
