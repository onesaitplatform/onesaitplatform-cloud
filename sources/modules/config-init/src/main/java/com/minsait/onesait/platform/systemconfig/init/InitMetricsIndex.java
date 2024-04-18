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
package com.minsait.onesait.platform.systemconfig.init;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.metrics.indexes")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitMetricsIndex {
	
	private boolean started = false;
	
	private static final String METRICS_ONTOLOGY = "MetricsOntology";
	private static final String METRICS_OPERATION = "MetricsOperation";
	private static final String METRICS_API = "MetricsApi";
	private static final String METRICS_CONTROL_PANEL = "MetricsControlPanel";
	private static final String METRICS_QUERIES_CONTROL_PANEL = "MetricsQueriesControlPanel";
	
	@Autowired
	@Qualifier("MongoManageDBRepository")
	ManageDBRepository manageDb;
	
	

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;
			init_MetricsApi();
			init_MetricsControlPanel();
			init_MetricsOntology();
			init_MetricsOperation();
			init_MetricsQueriesControlPanel();

		
			log.info("initMongoDB correctly...");
		}
	}
	
	public void init_MetricsOntology() {
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
			try {
				log.info("No Collection AuditGeneral...");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.user");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.result");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.source");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.ontology");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.operationType");
				manageDb.createIndex(METRICS_ONTOLOGY, "TimeSerie.value");

			} catch (final Exception e) {
				log.error("Error init_MetricsOntology:" + e.getMessage());
				manageDb.removeTable4Ontology(METRICS_ONTOLOGY);
			}
		
	}
	
	public void init_MetricsOperation() {
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
			try {
				manageDb.createIndex(METRICS_OPERATION, "TimeSerie.user");
				manageDb.createIndex(METRICS_OPERATION, "TimeSerie.result");
				manageDb.createIndex(METRICS_OPERATION, "TimeSerie.source");
				manageDb.createIndex(METRICS_OPERATION, "TimeSerie.operationType");
				manageDb.createIndex(METRICS_OPERATION, "TimeSerie.value");

			} catch (final Exception e) {
				log.error("Error init_MetricsOperation:" + e.getMessage());
				manageDb.removeTable4Ontology(METRICS_OPERATION);
			}
		
	}

	
	public void init_MetricsApi() {
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
			try {
				manageDb.createIndex(METRICS_API, "TimeSerie.user");
				manageDb.createIndex(METRICS_API, "TimeSerie.result");
				manageDb.createIndex(METRICS_API, "TimeSerie.api");
				manageDb.createIndex(METRICS_API, "TimeSerie.operationType");
				manageDb.createIndex(METRICS_API, "TimeSerie.value");
				manageDb.createIndex(METRICS_API, "TimeSerie.timestamp");


			} catch (final Exception e) {
				log.error("Error init_MetricsApi:" + e.getMessage());
				manageDb.removeTable4Ontology(METRICS_API);
			}
		
	}
	
	public void init_MetricsControlPanel() {
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
			try {
				manageDb.createIndex(METRICS_CONTROL_PANEL, "TimeSerie.user");
				manageDb.createIndex(METRICS_CONTROL_PANEL, "TimeSerie.result");
				manageDb.createIndex(METRICS_CONTROL_PANEL, "TimeSerie.timestamp");
				manageDb.createIndex(METRICS_CONTROL_PANEL, "TimeSerie.operationType");
				manageDb.createIndex(METRICS_CONTROL_PANEL, "TimeSerie.value");

			} catch (final Exception e) {
				log.error("Error init_MetricsControlPanel:" + e.getMessage());
				manageDb.removeTable4Ontology(METRICS_CONTROL_PANEL);
			}
		
	}
	
	public void init_MetricsQueriesControlPanel() {
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
			try {
				manageDb.createIndex(METRICS_QUERIES_CONTROL_PANEL, "TimeSerie.user");
				manageDb.createIndex(METRICS_QUERIES_CONTROL_PANEL, "TimeSerie.result");
				manageDb.createIndex(METRICS_QUERIES_CONTROL_PANEL, "TimeSerie.timestamp");
				manageDb.createIndex(METRICS_QUERIES_CONTROL_PANEL, "TimeSerie.ontology");
				manageDb.createIndex(METRICS_QUERIES_CONTROL_PANEL, "TimeSerie.value");

			} catch (final Exception e) {
				log.error("Error init_MetricsControlPanel:" + e.getMessage());
				manageDb.removeTable4Ontology(METRICS_QUERIES_CONTROL_PANEL);
			}
		
	}
	
	

}
