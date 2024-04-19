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

import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class StatementVisitorComparator implements StatementVisitor {

	private final MatchResult result;
	private final Statement otherStatement;

	public StatementVisitorComparator(Statement otherStatement, MatchResult result) {
		this.result = result;
		this.otherStatement = otherStatement;
	}

	@Override
	public void visit(Comment comment) {

	}

	@Override
	public void visit(Commit commit) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Delete delete) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Update update) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Insert insert) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Replace replace) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Drop drop) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Truncate truncate) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(CreateIndex createIndex) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(CreateTable createTable) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(CreateView createView) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(AlterView alterView) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Alter alter) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Statements stmts) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Execute execute) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(SetStatement set) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(ShowColumnsStatement set) {

	}

	@Override
	public void visit(Merge merge) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Upsert upsert) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(UseStatement use) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Block block) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(ValuesStatement values) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(DescribeStatement describe) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(ExplainStatement aThis) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(ShowStatement aThis) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(DeclareStatement aThis) {
		// Do nothing because not necessary
	}

	@Override
	public void visit(Select select1) {
		boolean sameClass = true;
		Select select2 = null;
		try {
			select2 = (Select) otherStatement;
		} catch (final ClassCastException e) {
			sameClass = false;
		}
		if (sameClass) {
			SqlComparator.matchSelect(select1, select2, result);
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Analyze analyze) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SavepointStatement savepointStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RollbackStatement rollbackStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CreateSchema aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ResetStatement reset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ShowIndexStatement showIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ShowTablesStatement showTables) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Grant grant) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CreateSequence createSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AlterSequence alterSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CreateFunctionalStatement createFunctionalStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CreateSynonym createSynonym) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AlterSession alterSession) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IfElseStatement aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RenameTableStatement renameTableStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(PurgeStatement purgeStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AlterSystemStatement alterSystemStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(UnsupportedStatement unsupportedStatement) {
		// TODO Auto-generated method stub

	}

}
