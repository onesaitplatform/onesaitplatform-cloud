/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.mongodb.metrics;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;

public interface MetricQueryResolver {

	public final static String ONTOLOGY_NAME = "ONTOLOGY_NAME";
	public final static String STATEMENT = "STATEMENT";
	public final static String STATEMENT_CEROS_COMPLETION = "STATEMENT_CEROS_COMPLETION";
	public final static String SELECT_ITEMS = "SELECT_ITEMS";

	public Map<String, String> buildMongoDBQueryStatement(String query) throws Exception;

	public String buildUnifiedResponse(List<String> data, List<String> cerosInInterval, String selectItem)
			throws Exception;

	public void loadMetricsBase(MongoBasicOpsDBRepository mongodbRepository);

}
