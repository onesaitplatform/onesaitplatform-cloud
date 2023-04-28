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
package com.minsait.onesait.platform.solver;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.dto.socket.querystt.FilterStt;
import com.minsait.onesait.platform.dto.socket.querystt.ProjectStt;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

@Component
@Qualifier("QuasarSolver")
public class SolverQuasarImpl extends SolverSQLImpl {

	private static final String SP_LIMIT_STR = " LIMIT ";

	@Override
	protected String addLimitOffset(PlainSelect select, int maxreg, long offset, long limit) {
		Limit querylimit = select.getLimit();

		Long min = (limit > 0 ? Math.min(maxreg, limit) : maxreg);

		if (querylimit != null) {
			min = Math.min(min, ((LongValue) querylimit.getRowCount()).getValue());
		}

		// Disable limit and added to end of the query
		select.setLimit(null);

		if (offset > 0) {
			Offset oaux = new Offset();
			oaux.setOffset(offset);
			select.setOffset(oaux);
		}

		return select.toString() + SP_LIMIT_STR + min;
	}

	private String setAliasQuotMarks(String orialias) {
		if (orialias.startsWith("`") && orialias.endsWith("`")) {
			return "`" + orialias + "`";
		} else {
			return orialias;
		}
	}

	@Override
	protected List<SelectItem> buildProjectV2(List<SelectItem> selectItem, List<ProjectStt> projections) {
		if (projections == null || projections.isEmpty()) {
			return selectItem;
		} else {
			List<SelectItem> selectItemOverwrite = new ArrayList<>();
			for (ProjectStt p : projections) {
				if (p.getAlias() != null) {// for quasar, always quoted select alias
					p.setAlias(setAliasQuotMarks(p.getAlias()));
				}
				selectItemOverwrite.add(generateSelectItemFromProject(p, selectItem));
			}
			return selectItemOverwrite;
		}
	}

	@Override
	protected List<Expression> buildGroupByV2(List<String> groups, String prefix, List<SelectItem> realproject,
			List<Expression> groupex) throws JSQLParserException {

		if (groups == null || groups.isEmpty()) {
			return groupex;
		} else {
			List<Expression> groupexaux = (groupex != null && !groupex.isEmpty() ? groupex
					: new ArrayList<Expression>());

			for (String group : groups) {
				Column col = new Column(findEndParamV2(group, realproject));
				groupexaux.add(col);
			}
			return groupexaux;
		}
	}

	@Override
	protected Expression buildExpFromFilter(FilterStt f, List<SelectItem> realproject, String prefix)
			throws JSQLParserException {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(findEndParamV2(f.getField(), realproject));
		sb.append(" ");
		sb.append(f.getOp());
		sb.append(" ");
		sb.append(f.getExp());
		Column col = new Column(sb.toString());
		return col;
	}

}
