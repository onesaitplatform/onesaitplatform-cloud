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
package com.minsait.onesait.platform.config.services.jsql;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class JSQLParserTest {

	@Test
	public void testOffset() throws JSQLParserException {

		final String s = "SELECT * FROM Entity";
		final Select select = (Select) CCJSqlParserUtil.parse(s);
		final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		final LongValue lv = new LongValue(2);
		final Offset offset = new Offset();
		offset.setOffset(lv);
		plainSelect.setOffset(offset);
		System.out.println(plainSelect.toString());

	}
}
