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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;

import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.DocumentResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESDeleteService {

	@Autowired
	ESBaseApi connector;

	public boolean deleteById(String index, String type, String id) {
		try {
			DocumentResult d = connector.getHttpClient()
					.execute(new Delete.Builder(id).index(index).type(type).build());

			log.info("Document has been deleted..." + id + " " + d.isSucceeded());

			return d.isSucceeded();

		} catch (Exception ex) {
			log.error("Exception occurred while delete Document : " + ex, ex);
		}
		return false;
	}

	public boolean deleteAll(String index, String type) {
		MultiDocumentOperationResult result = deleteByQuery(index, type, ESBaseApi.QUERY_ALL, false);
		return (result.getCount() != -1);
	}

	public MultiDocumentOperationResult deleteByQuery(String index, String type, String jsonQueryString,
			boolean includeIds) {
		DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(jsonQueryString).addIndex(index).addType(type).build();
		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		try {
			JestResult resultOp = connector.getHttpClient().execute(deleteByQuery);

			if (resultOp.isSucceeded()) {
				result.setCount(resultOp.getJsonObject().getAsJsonObject("_indices").getAsJsonObject(index)
						.get("deleted").getAsLong());
			} else {
				result.setCount(-1);
			}

		} catch (IOException e) {
			log.error("Exception occurred while delete Document : " + e, e);
			result.setCount(-1);
		}
		return result;
	}
}