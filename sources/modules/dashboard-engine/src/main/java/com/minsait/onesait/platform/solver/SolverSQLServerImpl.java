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
package com.minsait.onesait.platform.solver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;

import net.sf.jsqlparser.expression.LongValue;

import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;

@Component
@Qualifier("SQLServerSolver")
public class SolverSQLServerImpl extends SolverSQLImpl {
	
	@Autowired
	@Qualifier("SQLServerHelper")
	private SQLHelper sqlHelper;
	
	@Override
	protected String addLimitOffset(PlainSelect select, int maxreg, long offset, long limit) {
		Limit querylimit = select.getLimit();
		
		Long min = (limit > 0 ? Math.min(maxreg, limit) : maxreg);
 
		if(querylimit != null) {
			min = Math.min(min, ((LongValue) querylimit.getRowCount()).getValue());
		}
		
		//Disable limit and added to end of the query
		select.setLimit(null);
		
		if(offset > 0) {
			return sqlHelper.addLimit(select.toString(), min, offset);
		}
		else {
			return sqlHelper.addLimit(select.toString(), min);
		}
	}

}
