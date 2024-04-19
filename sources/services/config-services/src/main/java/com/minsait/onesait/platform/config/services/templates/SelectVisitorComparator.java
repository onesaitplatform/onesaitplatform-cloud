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

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class SelectVisitorComparator implements SelectVisitor {

    private SelectBody otherSelectBody;
    private MatchResult result;
    
    public SelectVisitorComparator(SelectBody otherSelectBody, MatchResult result) {
        this.otherSelectBody = otherSelectBody;
        this.result = result;
    }
        
    @Override
    public void visit(PlainSelect plainSelect1) {
        boolean sameClass = true;
        PlainSelect plainSelect2 = null;
        try {
            plainSelect2 = (PlainSelect) otherSelectBody;
        } catch (ClassCastException e) {
            sameClass = false;
        }
        if (sameClass) {
            SqlComparator.matchPlainSelect(plainSelect1, plainSelect2, result);
        } else {
            result.setResult(false);
        }
    }

    @Override
    public void visit(SetOperationList setOpList1) {
    	// Do nothing because not necessary
    }

    @Override
    public void visit(ValuesStatement aThis) {

    }

    @Override
    public void visit(WithItem withItem1) {
        boolean sameClass = true;
        WithItem withItem2 = null;
        try {
            withItem2 = (WithItem) otherSelectBody;
        } catch (ClassCastException e) {
            sameClass = false;
        }
        if (sameClass) {
            SqlComparator.matchWithItem(withItem1, withItem2, result);
        } else {
            result.setResult(false);
        }

    }

}
