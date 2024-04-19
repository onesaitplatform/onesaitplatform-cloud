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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("HiveImpalaHelperImpl")
public class HiveImpalaHelperImpl extends SQLHelperImpl implements SQLHelper {

	private static final String GET_CURRENT_DATABASE_QUERY = "SELECT current_database()";

	@Override
	public String getDatabaseStatement() {
		return GET_CURRENT_DATABASE_QUERY;
	}
}
