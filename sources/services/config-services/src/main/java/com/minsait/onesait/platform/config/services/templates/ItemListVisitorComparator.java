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

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;

public class ItemListVisitorComparator extends ItemsListVisitorAdapter {

	private final ItemsList otherItemsList;
	private final MatchResult result;

	public ItemListVisitorComparator(ItemsList otherItemsList, MatchResult result) {
		this.otherItemsList = otherItemsList;
		this.result = result;
	}

	@Override
	public void visit(ExpressionList expressionList1) {

		boolean sameClass = true;
		ExpressionList expressionList2 = null;
		try {
			expressionList2 = (ExpressionList) otherItemsList;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			processSameClassExpressions(expressionList1, expressionList2);
		} else {
			result.setResult(false);
		}
	}

	private void processSameClassExpressions(ExpressionList expressionList1, ExpressionList expressionList2) {
		final List<Expression> expressions1 = expressionList1.getExpressions();
		final List<Expression> expressions2 = expressionList2.getExpressions();
		final Iterator<Expression> it1 = expressions1.iterator();
		final Iterator<Expression> it2 = expressions2.iterator();

		if (expressions2.size() == 1 && !expressions1.isEmpty()) {
			final Expression expression2 = it2.next();
			if (UserVariable.class.equals(expression2.getClass())) {
				final UserVariable userVariable = (UserVariable) expression2;
				result.addVariable(userVariable.getName(), expressions1.toString(), VariableData.Type.STRING);
				result.setResult(true);
			} else {
				final Expression expression1 = it1.next();
				expression1.accept(new ExpressionVisitorComparator(expression2, result));
			}
		} else {
			while (it1.hasNext() && it2.hasNext()) {
				final Expression expression1 = it1.next();
				final Expression expression2 = it2.next();
				expression1.accept(new ExpressionVisitorComparator(expression2, result));
				if (!result.isMatch()) {
					break;
				}
			}

			if (it1.hasNext() || it2.hasNext()) {
				result.setResult(false);
			}
		}

	}

	@Override
	public void visit(MultiExpressionList multiExprList1) {
		boolean sameClass = true;
		MultiExpressionList multiExprList2 = null;
		try {
			multiExprList2 = (MultiExpressionList) otherItemsList;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			processSameClassExpressionLists(multiExprList1, multiExprList2);
		} else {
			result.setResult(false);
		}
	}

	private void processSameClassExpressionLists(MultiExpressionList multiExprList1,
			MultiExpressionList multiExprList2) {
		final List<ExpressionList> exprList1 = multiExprList1.getExprList();
		final List<ExpressionList> exprList2 = multiExprList2.getExprList();
		final Iterator<ExpressionList> it1 = exprList1.iterator();
		final Iterator<ExpressionList> it2 = exprList2.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			final ExpressionList expressionList1 = it1.next();
			final ExpressionList expressionList2 = it2.next();

			final List<Expression> expressions1 = expressionList1.getExpressions();
			final List<Expression> expressions2 = expressionList2.getExpressions();
			final Iterator<Expression> itExp1 = expressions1.iterator();
			final Iterator<Expression> itExp2 = expressions2.iterator();

			while (itExp1.hasNext() && itExp2.hasNext()) {
				final Expression expression1 = itExp1.next();
				final Expression expression2 = itExp2.next();
				expression1.accept(new ExpressionVisitorComparator(expression2, result));
				if (!result.isMatch()) {
					break;
				}
			}

			if (itExp1.hasNext() || itExp2.hasNext()) {
				result.setResult(false);
			}

			if (!result.isMatch()) {
				break;
			}
		}

		if (it1.hasNext() || it2.hasNext()) {
			result.setResult(false);
		}
	}
}
