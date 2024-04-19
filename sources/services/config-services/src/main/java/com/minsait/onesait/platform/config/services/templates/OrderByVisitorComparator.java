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
package com.minsait.onesait.platform.config.services.templates;

import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

public class OrderByVisitorComparator implements OrderByVisitor {

	private OrderByElement otherOrderByElement;
	private MatchResult result;

	public OrderByVisitorComparator(OrderByElement otherOrderByElement, MatchResult result) {
		this.otherOrderByElement = otherOrderByElement;
		this.result = result;
	}

	@Override
	public void visit(OrderByElement orderBy1) {
		boolean sameClass = true;
		OrderByElement orderBy2 = null;
		try {
			orderBy2 = (OrderByElement) otherOrderByElement;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			SqlComparator.matchOrderByElement(orderBy1, orderBy2, result);
		} else {
			result.setResult(false);
		}
	}

}
