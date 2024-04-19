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
package com.minsait.onesait.platform.persistence.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.control.NoPersistenceBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.external.api.rest.ExternalApiRestOpsDBRepository;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyOpsDBRepository;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.timescaledb.TimescaleDBBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.presto.PrestoOntologyBasicOpsDBRepository;

@Component
public class BasicOpsDBRepositoryFactory {

	@Autowired(required = false)
	private ElasticSearchBasicOpsDBRepository elasticBasicOps;

	@Autowired
	private MongoBasicOpsDBRepository mongoBasicOps;

	@Autowired
	private ExternalApiRestOpsDBRepository externalApiRest;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	@Lazy
	private VirtualOntologyOpsDBRepository virtualRepository;

	@Autowired
	private CosmosDBBasicOpsDBRepository cosmosBasicOps;

	@Autowired
	private NoPersistenceBasicOpsDBRepository noPersistenceBasicOpsDBRepository;

	@Autowired
	private TimescaleDBBasicOpsDBRepository timescaleDBBasicOpsDBRepository;

	@Autowired(required = false)
	@Qualifier(NameBeanConst.KUDU_BASIC_OPS_BEAN_NAME)
	private BasicOpsDBRepository kuduBasicOpsDBRepository;

	@Autowired
	private PrestoOntologyBasicOpsDBRepository prestoBasicOpsDBRepository;
	
	public BasicOpsDBRepository getInstance(String ontologyId) {
		final Ontology ds = ontologyRepository.findByIdentification(ontologyId);
		final RtdbDatasource dataSource = ds.getRtdbDatasource();
		return getInstance(dataSource);
	}

	public BasicOpsDBRepository getInstance(RtdbDatasource dataSource) {
		if (RtdbDatasource.MONGO.equals(dataSource)) {
			return mongoBasicOps;
		} else if (RtdbDatasource.ELASTIC_SEARCH.equals(dataSource)) {
			return elasticBasicOps;
		} else if (RtdbDatasource.KUDU.equals(dataSource)) {
			return kuduBasicOpsDBRepository;
		} else if (RtdbDatasource.API_REST.equals(dataSource)) {
			return externalApiRest;
		} else if (RtdbDatasource.VIRTUAL.equals(dataSource)) {
			return virtualRepository;
		} else if (RtdbDatasource.COSMOS_DB.equals(dataSource)) {
			return cosmosBasicOps;
		} else if (RtdbDatasource.NO_PERSISTENCE.equals(dataSource)) {
			return noPersistenceBasicOpsDBRepository;
		} else if (RtdbDatasource.TIMESCALE.equals(dataSource)) {
			return timescaleDBBasicOpsDBRepository;
		} else if (RtdbDatasource.PRESTO.equals(dataSource)) {
			return prestoBasicOpsDBRepository;
		} else {
			return mongoBasicOps;
		}
	}

}
