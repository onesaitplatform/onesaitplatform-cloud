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
package com.minsait.onesait.platform.persistence.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.control.NoPersistenceQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.external.api.rest.QueryAsTextRestDBImpl;
import com.minsait.onesait.platform.persistence.external.virtual.QueryAsTextVirtualDBImpl;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.mindsdb.MindsDBQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.services.QueryAsTextMongoDBImpl;
import com.minsait.onesait.platform.persistence.nebula.NebulaGraphDBQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.opensearch.OpenSearchQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.presto.QueryAsTextPrestoDBImpl;
import com.minsait.onesait.platform.persistence.timescaledb.TimescaleDBQueryAsTextDBRepository;

@Component
public class QueryAsTextDBRepositoryFactory {

	@Autowired
	private QueryAsTextMongoDBImpl queryMongo;

	@Autowired(required = false)
	private ElasticSearchQueryAsTextDBRepository queryElasticSearch;

	@Autowired(required = false)
	private OpenSearchQueryAsTextDBRepository queryOpenSearch;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private QueryAsTextVirtualDBImpl queryVirtual;

	@Autowired
	private QueryAsTextRestDBImpl queryApiRest;

	@Autowired
	private NoPersistenceQueryAsTextDBRepository noPersistenceQueryAsTextDBRepository;

	@Autowired
	private CosmosDBQueryAsTextDBRepository comosDBQuery;

	@Autowired
	private TimescaleDBQueryAsTextDBRepository timescaleDBQueryRepository;

	@Autowired
	private MindsDBQueryAsTextDBRepository mindsDBQueryAsTextDBRepository;

	@Autowired
	private NebulaGraphDBQueryAsTextDBRepository nebulaGraphDBQueryAsTextDBRepository;

	@Autowired
	private QueryAsTextPrestoDBImpl prestoDBQuery;

	public QueryAsTextDBRepository getInstance(String ontologyId, String sessionUserId) {
		final Ontology ds = ontologyService.getOntologyByIdentification(ontologyId, sessionUserId);
		final RtdbDatasource dataSource = ds.getRtdbDatasource();
		return getInstance(dataSource);
	}

	public QueryAsTextDBRepository getInstanceClientPlatform(String ontologyId, String clientP) {
		final ClientPlatform cp = clientPlatformService.getByIdentification(clientP);

		final List<Ontology> ds = ontologyService.getOntologiesByClientPlatform(cp);

		final Ontology result1 = ds.stream().filter(x -> ontologyId.equals(x.getIdentification())).findAny()
				.orElse(null);

		if (result1 != null) {
			final RtdbDatasource dataSource = result1.getRtdbDatasource();
			return getInstance(dataSource);
		} else {
			return queryMongo;
		}
	}

	public QueryAsTextDBRepository getInstance(RtdbDatasource dataSource) {
		if (dataSource.equals(RtdbDatasource.MONGO)) {
			return queryMongo;
		} else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH)) {
			return queryElasticSearch;
		} else if (dataSource.equals(RtdbDatasource.OPEN_SEARCH)) {
			return queryOpenSearch;
		} else if (dataSource.equals(RtdbDatasource.VIRTUAL)) {
			return queryVirtual;
		} else if (dataSource.equals(RtdbDatasource.API_REST)) {
			return queryApiRest;
		} else if (dataSource.equals(RtdbDatasource.COSMOS_DB)) {
			return comosDBQuery;
		} else if (RtdbDatasource.NO_PERSISTENCE.equals(dataSource)) {
			return noPersistenceQueryAsTextDBRepository;
		} else if (RtdbDatasource.TIMESCALE.equals(dataSource)) {
			return timescaleDBQueryRepository;
		} else if (RtdbDatasource.PRESTO.equals(dataSource)) {
			return prestoDBQuery;
		} else if (RtdbDatasource.AI_MINDS_DB.equals(dataSource)) {
			return mindsDBQueryAsTextDBRepository;
		} else if (RtdbDatasource.NEBULA_GRAPH.equals(dataSource)) {
			return nebulaGraphDBQueryAsTextDBRepository;
		} else {
			return queryMongo;
		}
	}
}
