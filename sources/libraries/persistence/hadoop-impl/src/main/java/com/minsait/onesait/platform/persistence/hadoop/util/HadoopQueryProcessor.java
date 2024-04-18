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
package com.minsait.onesait.platform.persistence.hadoop.util;

import org.springframework.stereotype.Service;

@Service
public class HadoopQueryProcessor {

	public String parse(String query) {

		String parsedQuery = query.trim();

		parsedQuery = parsedQuery.replaceAll("(?i).coordinates.0", HiveFieldType.LATITUDE_FIELD);
		parsedQuery = parsedQuery.replaceAll("(?i).coordinates.1", HiveFieldType.LONGITUDE_FIELD);

		return parsedQuery;
	}

}
