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
package com.minsait.onesait.platform.controlpanel.services.hadoop;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.persistence.hadoop.json.JsonGeneratorFromHive;
import com.minsait.onesait.platform.persistence.hadoop.json.JsonSchemaHive;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

@Service
public class HadoopService {

	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;

	@Autowired
	private JsonGeneratorFromHive jsonGenerator;

	public List<String> getHiveTables() {
		return manageDBPersistenceServiceFacade.getListOfTables(RtdbDatasource.KUDU);
	}

	public List<DescribeColumnData> describe(String name) {
		return manageDBPersistenceServiceFacade.describeTable(RtdbDatasource.KUDU, name);
	}

	public String generateSchemaFromHive(String tablename) {
		List<DescribeColumnData> columns = describe(tablename);
		JsonSchemaHive schema = jsonGenerator.parse(tablename, columns);
		return schema.build();
	}
}
