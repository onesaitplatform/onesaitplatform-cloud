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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;

public class IntoTableVisitorComparator implements IntoTableVisitor {

    private Table table2;
    private MatchResult result;

    public IntoTableVisitorComparator(Table table2, MatchResult result) {
        this.table2 = table2;
        this.result = result;
    }
    
    @Override
    public void visit(Table table1) {
        table1.accept(new FromItemVisitorComparator(table2, result));
        if (result.isMatch()) {
            //TODO this a simple approach, more detailed comparison could be necessary
            boolean match = table1.toString().equals(table2.toString());
            result.setResult(match);
        }
    }

}
