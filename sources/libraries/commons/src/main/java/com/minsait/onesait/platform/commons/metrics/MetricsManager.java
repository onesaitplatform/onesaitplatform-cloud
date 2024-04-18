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
package com.minsait.onesait.platform.commons.metrics;

import com.minsait.onesait.platform.commons.model.MetricsPlatformDto;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;

public interface MetricsManager {

	public void logMetricDigitalBroker(String userIdentification, String ontologyIdentification,
			SSAPMessageTypes operationType, Source source, String result);

	public void logMetricApiManager(String userIdentification, String ontologyIdentification, String method,
			Source source, String result, String api);

	public void logControlPanelLogin(String userIdentification, String result);

	public MetricsPlatformDto computeMetrics(long date);

	public void logControlPanelOntologyCreation(String userIdentification, String result);

	public void logControlPanelUserCreation(String result);

	public void logControlPanelApiCreation(String userIdentification, String result);

	public void logControlPanelDashboardsCreation(String userIdentification, String result);

	void logControlPanelClientsPlatformCreation(String userIdentification, String result);

	void logControlPanelNotebooksCreation(String userIdentification, String result);

	void logControlPanelDataflowsCreation(String userIdentification, String result);

	void logControlPanelProjectsCreation(String userIdentification, String result);

	void logControlPanelFlowsCreation(String userIdentification, String result);

	void logControlPanelGisViewersCreation(String userIdentification, String result);

	void logControlPanelQueries(String userIdentification, String ontology, String result);
	
	public boolean isMetricsEnabled();

}
