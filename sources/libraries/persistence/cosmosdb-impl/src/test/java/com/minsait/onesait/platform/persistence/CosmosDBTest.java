/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.microsoft.azure.documentdb.bulkexecutor.UpdateItem;
import com.microsoft.azure.documentdb.bulkexecutor.UpdateOperationBase;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.bean.CosmosDBBulkManager;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.sql.CosmosDBSQLUtils;

@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class CosmosDBTest {

	@Autowired
	CosmosDBQueryAsTextDBRepository textRepo;
	@Autowired
	CosmosDBBasicOpsDBRepository basicops;
	@Autowired
	private CosmosDBBulkManager bulkManager;

	@Test
	@Ignore
	public void test() {
		final String queryLimit = "SELECT * FROM RESTAURANTS c LIMIT 2";
		final String queryNoLimit = "SELECT * FROM RESTAURANTS c";
		textRepo.querySQLAsJson("Restaurants", queryLimit, 0);
		textRepo.querySQLAsJson("Restaurants", queryNoLimit, 0);
	}

	@Autowired
	private CosmosDBSQLUtils sqlUtils;

	@Test
	@Ignore
	public void update() {
		final String update = "UPDATE Restaurants c SET c.Restaurant.restaurant_id=\"56789\"";
		final List<UpdateOperationBase> base = sqlUtils.getUpdateOperationBase(update);
		try {
			bulkManager.get("Restaurants").updateAll(
					Arrays.asList(new UpdateItem("a4a203e7-c282-4377-a203-e7c282637719", "Manhattan", base)), null);
		} catch (final Exception e) {

			e.printStackTrace();
		}
	}
}
