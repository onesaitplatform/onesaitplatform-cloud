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
package com.minsait.onesait.platform.config.services.templates;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public class ExpressionVisitorComparator extends ExpressionVisitorAdapter {

	private final Expression otherExpression;
	private final MatchResult result;

	public ExpressionVisitorComparator(Expression otherExpression, MatchResult result) {
		this.otherExpression = otherExpression;
		this.result = result;
	}

	@Override
	public void visit(CaseExpression caseExpression1) {
		boolean sameClass = true;
		CaseExpression caseExpression2 = null;
		try {
			caseExpression2 = (CaseExpression) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			processSameClassStatement(caseExpression1, caseExpression2);
		} else {
			result.setResult(false);
		}
	}

	private void processSameClassStatement(CaseExpression caseExpression1, CaseExpression caseExpression2) {
		caseExpression1.getSwitchExpression()
				.accept(new ExpressionVisitorComparator(caseExpression2.getSwitchExpression(), result));
		if (result.isMatch()) {
			final List<WhenClause> whenList1 = caseExpression1.getWhenClauses();
			final List<WhenClause> whenList2 = caseExpression2.getWhenClauses();

			final Iterator<WhenClause> it1 = whenList1.iterator();
			final Iterator<WhenClause> it2 = whenList2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				final WhenClause when1 = it1.next();
				final WhenClause when2 = it2.next();
				when1.getWhenExpression().accept(new ExpressionVisitorComparator(when2.getWhenExpression(), result));
				if (result.isMatch()) {
					when1.getThenExpression()
							.accept(new ExpressionVisitorComparator(when2.getThenExpression(), result));

				}
				if (!result.isMatch()) {
					break;
				}
			}

			if (result.isMatch()) {
				caseExpression1.getElseExpression()
						.accept(new ExpressionVisitorComparator(caseExpression2.getElseExpression(), result));
			}
		}
	}

	@Override
	public void visit(Column tableColumn1) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			final UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), tableColumn1.getColumnName(), VariableData.Type.STRING);
			result.setResult(true);
		} else {
			boolean sameClass = true;
			Column tableColumn2 = null;
			try {
				tableColumn2 = (Column) otherExpression;
			} catch (final ClassCastException e) {
				sameClass = false;
			}

			if (sameClass) {
				SqlComparator.matchColumn(tableColumn1, tableColumn2, result);
			} else {
				result.setResult(false);
			}
		}
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo1) {
		boolean sameClass = true;
		NotEqualsTo notEqualsTo2 = null;
		try {
			notEqualsTo2 = (NotEqualsTo) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			notEqualsTo1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(notEqualsTo2.getLeftExpression(), result));
			if (result.isMatch()) {
				notEqualsTo1.getRightExpression()
						.accept(new ExpressionVisitorComparator(notEqualsTo2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(MinorThanEquals minorThanEquals1) {
		boolean sameClass = true;
		MinorThanEquals minorThanEquals2 = null;
		try {
			minorThanEquals2 = (MinorThanEquals) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			minorThanEquals1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(minorThanEquals2.getLeftExpression(), result));
			if (result.isMatch()) {
				minorThanEquals1.getRightExpression()
						.accept(new ExpressionVisitorComparator(minorThanEquals2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(MinorThan minorThan1) {
		boolean sameClass = true;
		MinorThan minorThan2 = null;
		try {
			minorThan2 = (MinorThan) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			minorThan1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(minorThan2.getLeftExpression(), result));
			if (result.isMatch()) {
				minorThan1.getRightExpression()
						.accept(new ExpressionVisitorComparator(minorThan2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(LikeExpression likeExpression1) {
		boolean sameClass = true;
		LikeExpression likeExpression2 = null;
		try {
			likeExpression2 = (LikeExpression) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			likeExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(likeExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				likeExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(likeExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(InExpression inExpression1) {
		boolean sameClass = true;
		InExpression inExpression2 = null;
		try {
			inExpression2 = (InExpression) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			inExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(inExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				inExpression1.getRightItemsList()
						.accept(new ItemListVisitorComparator(inExpression2.getRightItemsList(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals1) {
		boolean sameClass = true;
		GreaterThanEquals greaterThanEquals2 = null;
		try {
			greaterThanEquals2 = (GreaterThanEquals) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			greaterThanEquals1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(greaterThanEquals2.getLeftExpression(), result));
			if (result.isMatch()) {
				greaterThanEquals1.getRightExpression()
						.accept(new ExpressionVisitorComparator(greaterThanEquals2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(GreaterThan greaterThan1) {
		boolean sameClass = true;
		GreaterThan greaterThan2 = null;
		try {
			greaterThan2 = (GreaterThan) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			greaterThan1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(greaterThan2.getLeftExpression(), result));
			if (result.isMatch()) {
				greaterThan1.getRightExpression()
						.accept(new ExpressionVisitorComparator(greaterThan2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(EqualsTo equalsTo1) {
		boolean sameClass = true;
		EqualsTo equalsTo2 = null;
		try {
			equalsTo2 = (EqualsTo) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			equalsTo1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(equalsTo2.getLeftExpression(), result));
			if (result.isMatch()) {
				equalsTo1.getRightExpression()
						.accept(new ExpressionVisitorComparator(equalsTo2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Between between1) {
		boolean sameClass = true;
		Between between2 = null;
		try {
			between2 = (Between) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			between1.getLeftExpression().accept(new ExpressionVisitorComparator(between2.getLeftExpression(), result));
			if (result.isMatch()) {
				between1.getBetweenExpressionStart()
						.accept(new ExpressionVisitorComparator(between2.getBetweenExpressionStart(), result));
				if (result.isMatch()) {
					between1.getBetweenExpressionEnd()
							.accept(new ExpressionVisitorComparator(between2.getBetweenExpressionEnd(), result));
				}
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(OrExpression orExpression1) {
		boolean sameClass = true;
		OrExpression orExpression2 = null;
		try {
			orExpression2 = (OrExpression) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			orExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(orExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				orExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(orExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(AndExpression andExpression1) {
		boolean sameClass = true;
		AndExpression andExpression2 = null;
		try {
			andExpression2 = (AndExpression) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			andExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(andExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				andExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(andExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Subtraction subtraction1) {
		boolean sameClass = true;
		Subtraction subtraction2 = null;
		try {
			subtraction2 = (Subtraction) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			subtraction1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(subtraction2.getLeftExpression(), result));
			if (result.isMatch()) {
				subtraction1.getRightExpression()
						.accept(new ExpressionVisitorComparator(subtraction2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(StringValue stringValue1) {
		if (StringValue.class.equals(otherExpression.getClass())) {
			final StringValue stringValue2 = (StringValue) otherExpression;
			SqlComparator.matchStringValue(stringValue1, stringValue2, result);
		} else if (UserVariable.class.equals(otherExpression.getClass())) {
			final UserVariable userVariable = (UserVariable) otherExpression;
			SqlComparator.matchStringValueWithVariable(stringValue1, userVariable, result);
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Parenthesis parenthesis1) {
		boolean sameClass = true;
		Parenthesis parenthesis2 = null;
		try {
			parenthesis2 = (Parenthesis) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			parenthesis1.getExpression().accept(new ExpressionVisitorComparator(parenthesis2.getExpression(), result));
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(LongValue longValue1) {
		if (LongValue.class.equals(otherExpression.getClass())) {
			final LongValue longValue2 = (LongValue) otherExpression;
			SqlComparator.matchLongValueValue(longValue1, longValue2, result);
		} else if (UserVariable.class.equals(otherExpression.getClass())) {
			final UserVariable userVariable = (UserVariable) otherExpression;
			SqlComparator.matchLongValueValueWithVariable(longValue1, userVariable, result);
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter1) {
		boolean sameClass = true;
		JdbcNamedParameter jdbcNamedParameter2 = null;
		try {
			jdbcNamedParameter2 = (JdbcNamedParameter) otherExpression;
		} catch (final ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			SqlComparator.matchJdbcNamedParameter(jdbcNamedParameter1, jdbcNamedParameter2, result);

		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(SignedExpression signedExpression) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			final UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), signedExpression.toString(), VariableData.Type.STRING);
			result.setResult(true);
		}
	}

	@Override
	public void visit(Function function1) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			final UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), function1.toString(), VariableData.Type.STRING);
			result.setResult(true);
		} else if (otherExpression.getClass().equals(Function.class)) {
			final Function function2 = (Function) otherExpression;
			if (function1.getParameters() != null && function2.getParameters() != null) {
				function1.getParameters().accept(new ItemListVisitorComparator(function2.getParameters(), result));
			}
			if (function1.getKeep() != null) {
				function1.getKeep().accept(this);
			}
		}
	}

}
