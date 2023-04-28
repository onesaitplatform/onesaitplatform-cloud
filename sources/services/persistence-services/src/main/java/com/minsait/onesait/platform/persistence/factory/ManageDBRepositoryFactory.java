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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.control.NoPersistenceManageDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBManageDBRepository;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchManageDBRepository;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualRelationalOntologyManageDBRepository;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mindsdb.MindsDBManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoNativeManageDBRepository;
import com.minsait.onesait.platform.persistence.nebula.NebulaGraphDBManageDBRepository;
import com.minsait.onesait.platform.persistence.presto.PrestoManageDBRepository;
import com.minsait.onesait.platform.persistence.timescaledb.TimescaleDBManageDBRepository;

@Component
public class ManageDBRepositoryFactory {

	@Autowired
	private MongoNativeManageDBRepository mongoManage;

	@Autowired(required = false)
	private ElasticSearchManageDBRepository elasticManage;

	@Autowired
	private CosmosDBManageDBRepository cosmosDB;

	@Autowired
	private NoPersistenceManageDBRepository noPersistenceManageDBRepository;

	@Autowired
	private VirtualRelationalOntologyManageDBRepository relationalManager;

	@Autowired
	private TimescaleDBManageDBRepository timescaleManager;

	@Autowired(required = false)
	@Qualifier(NameBeanConst.KUDU_MANAGE_DB_REPO_BEAN_NAME)
	private ManageDBRepository kuduManageDBRepository;

	@Autowired
	private PrestoManageDBRepository prestoManageDBRepository;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private MindsDBManageDBRepository mindsDBManageDBRepository;

	@Autowired
	private NebulaGraphDBManageDBRepository nebulaGraphDBManageDBRepository;

	public ManageDBRepository getInstance(String ontologyId) {
		final Ontology ds = ontologyRepository.findByIdentification(ontologyId);
		final RtdbDatasource dataSource = ds.getRtdbDatasource();
		return getInstance(dataSource);
	}

	public ManageDBRepository getInstance(RtdbDatasource dataSource) {
		if (dataSource.equals(RtdbDatasource.MONGO)) {
			return mongoManage;
		} else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH)) {
			return elasticManage;
		} else if (dataSource.equals(RtdbDatasource.KUDU)) {
			return kuduManageDBRepository;
		} else if (dataSource.equals(RtdbDatasource.VIRTUAL)) {
			return relationalManager;
		} else if (RtdbDatasource.COSMOS_DB.equals(dataSource)) {
			return cosmosDB;
		} else if (RtdbDatasource.NO_PERSISTENCE.equals(dataSource)) {
			return noPersistenceManageDBRepository;
		} else if (RtdbDatasource.TIMESCALE.equals(dataSource)) {
			return timescaleManager;
		} else if (RtdbDatasource.PRESTO.equals(dataSource)) {
			return prestoManageDBRepository;
		}else if (RtdbDatasource.AI_MINDS_DB.equals(dataSource)) {
			return mindsDBManageDBRepository;
		}else if (RtdbDatasource.NEBULA_GRAPH.equals(dataSource)) {
			return nebulaGraphDBManageDBRepository;
		} else {
			return mongoManage;
		}
	}

}
