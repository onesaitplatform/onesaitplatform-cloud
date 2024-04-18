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
/**

< * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
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
package com.minsait.onesait.platform.config.components;

import lombok.Data;

@Data
public class Urls {

	private Iotbroker iotbroker;

	private ScriptingEngine scriptingEngine;

	private FlowEngine flowEngine;

	private RouterStandAlone routerStandAlone;

	private ApiManager apiManager;

	private Notebook notebook;

	private Controlpanel controlpanel;

	private DigitalTwinBroker digitalTwinBroker;

	private DashboardEngine dashboardEngine;

	private Domain domain;

	private Gravitee gravitee;

	private MonitoringUI monitoringUI;

	private RulesEngine rulesEngine;

	private BPMEngine bpmEngine;

	private GISViewer gisViewer;

	private ReportEngine reportEngine;

	private OpenData openData;

	private DataCleanerUI dataCleanerUI;

	private LogCentralizer logCentralizer;

	private KeycloakManager keycloakManager;

	private Edge edge;

	private Prometheus prometheus;

	private MinIO minio;

	private Presto presto;

	private Serverless serverless;

	private Modeljsonld modeljsonld;

	private Datalabeling datalabeling;

	private Nebula nebula;

	private Spark spark;

	private TracingUI tracingUI;

}
