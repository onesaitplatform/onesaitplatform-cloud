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
package com.minsait.onesait.platform.config.repository;

import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)

@Ignore("Pendiente resolver")
public class OntologyVirtualIntegrationTest {

	@Autowired
	private OntologyVirtualDatasourceRepository repositoryDatasource;

	@Autowired
	private OntologyVirtualRepository repositoryVirtual;

	@Autowired
	private OntologyRepository repositoryOntology;

	@Autowired
	private DataModelRepository dataModelRepository;

	@Autowired
	private UserRepository userCDBRepository;

	@Test
	@Transactional
	public void addDatasource() {
		OntologyVirtualDatasource datasource = new OntologyVirtualDatasource();
		datasource.setCreatedAt(new Date());
		datasource.setUpdatedAt(new Date());
		datasource.setUserId("sys as sysdba");
		datasource.setCredentials("indra2013");
		datasource.setPoolSize("10");
		datasource.setIdentification("oracle2");
		datasource.setSgdb(VirtualDatasourceType.ORACLE);
		datasource.setQueryLimit(100);
		datasource.setConnectionString("jdbc:oracle:thin:@10.0.0.6:1521:XE");

		Ontology ontology = new Ontology();
		ontology.setJsonSchema("");
		ontology.setDescription("Ontology created for Ticketing");
		ontology.setIdentification("TestVirtual");
		ontology.setActive(true);
		ontology.setRtdbClean(true);
		ontology.setRtdbToHdb(true);
		ontology.setPublic(true);
		// ontology.setDigitaltwin(false);
		ontology.setDataModel(this.dataModelRepository.findByIdentification("EmptyBase").get(0));
		ontology.setUser(getUserDeveloper());
		ontology.setAllowsCypherFields(false);

		OntologyVirtual ontologyVirtual = new OntologyVirtual();
		ontologyVirtual.setDatasourceId(datasource);
		ontologyVirtual.setCreatedAt(new Date());
		ontologyVirtual.setUpdatedAt(new Date());
		ontologyVirtual.setOntologyId(ontology);

		this.repositoryOntology.save(ontology);
		this.repositoryDatasource.save(datasource);
		this.repositoryVirtual.save(ontologyVirtual);

	}

	@Test
	@Transactional
	public void getDatasourceByOntology() {
		Ontology ontology = this.repositoryOntology.findByIdentification("TestVirtual");
		if (ontology != null) {
			OntologyVirtualDatasource datasource = this.repositoryVirtual
					.findOntologyVirtualDatasourceByOntologyIdentification(ontology.getIdentification());
			System.out.println(datasource.getConnectionString());
		}
	}

	@Test
	@Transactional
	public void removeOntology() {

		Ontology ontology = this.repositoryOntology.findByIdentification("TestBorrado1");

		// this.repositoryOntology.delete("c2529c44-ab4a-47c3-8db1-760af77b57b1");
		this.repositoryOntology.delete(ontology);

		// this.repositoryOntology.
		// OntologyVirtual ontologyVirtual = this.repositoryVirtual.findAll().get(0);
		// this.repositoryVirtual.delete(ontologyVirtual);

	}

	private User getUserDeveloper() {
		return this.userCDBRepository.findByUserId("developer");

	}

}
