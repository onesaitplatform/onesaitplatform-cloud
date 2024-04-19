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
package com.minsait.onesait.platform.metrics.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.commons.metrics.Source;
import com.minsait.onesait.platform.commons.model.MetricsApiDto;
import com.minsait.onesait.platform.commons.model.MetricsControlPanelDto;
import com.minsait.onesait.platform.commons.model.MetricsOntologyDto;
import com.minsait.onesait.platform.commons.model.MetricsOperationDto;
import com.minsait.onesait.platform.commons.model.MetricsPlatformDto;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.Getter;

@Component
public class MetricsManagerImpl implements MetricsManager {

	@Value("${onesaitplatform.metrics.enabled:true}")
	@Getter
	private boolean metricsEnabled;

	private static final String METRICS_USAGE_ONTOLOGY_KEY = "onesaitplatform.ontology.usage";
	private static final String METRICS_OPERATION_KEY = "onesaitplatform.operation";
	private static final String METRICS_USAGE_API_KEY = "onesaitplatform.api.usage";
	private static final String METRICS_CONTROL_PANEL_LOGIN = "onesaitplatform.controlpanel.login";
	private static final String METRICS_CONTROL_PANEL_ONTOLOGIES = "onesaitplatform.controlpanel.ontologies";
	private static final String METRICS_CONTROL_PANEL_USERS = "onesaitplatform.controlpanel.users";
	private static final String METRICS_CONTROL_PANEL_APIS = "onesaitplatform.controlpanel.apis";
	private static final String METRICS_CONTROL_PANEL_DASHBOARDS = "onesaitplatform.controlpanel.dashboards";
	private static final String METRICS_CONTROL_PANEL_CLIENTS_PLATFORM = "onesaitplatform.controlpanel.clientsplatform";
	private static final String METRICS_CONTROL_PANEL_NOTEBOOKS = "onesaitplatform.controlpanel.notebooks";
	private static final String METRICS_CONTROL_PANEL_DATAFLOWS = "onesaitplatform.controlpanel.dataflows";
	private static final String METRICS_CONTROL_PANEL_PROJECTS = "onesaitplatform.controlpanel.projects";
	private static final String METRICS_CONTROL_PANEL_FLOWS = "onesaitplatform.controlpanel.flows";
	private static final String METRICS_CONTROL_PANEL_GIS_VIEWERS = "onesaitplatform.controlpanel.gisviewers";
	private static final String METRICS_CONTROL_PANEL_QUERIES = "onesaitplatform.controlpanel.queries";

	private static final String KEY_SEPARATOR = ".^.";
	private static final String KEY_SEPARATOR_REGEX = "\\.\\^\\.";

	private static final String ONTOLOGY_STR = "ontology";
	private static final String RESULT_STR = "result";
	private static final String OPERATION_TYPE_STR = "operationType";
	private static final String SOURCE_STR = "source";

	@Autowired
	private MeterRegistry metricsRegistry;

	private Map<String, AtomicInteger> ontologyMetrics;
	private Map<String, AtomicInteger> operationMetrics;
	private Map<String, AtomicInteger> apiMetrics;

	private Map<String, AtomicInteger> controlPanelLoginMetrics;
	private Map<String, AtomicInteger> controlPanelOntologyCreationMetrics;
	private Map<String, AtomicInteger> controlPanelUserCreationMetrics;
	private Map<String, AtomicInteger> controlPanelApisCreationMetrics;
	private Map<String, AtomicInteger> controlPanelDashboardsCreationMetrics;
	private Map<String, AtomicInteger> controlPanelClientsPlatformCreationMetrics;
	private Map<String, AtomicInteger> controlPanelNotebooksCreationMetrics;
	private Map<String, AtomicInteger> controlPanelDataflowsCreationMetrics;
	private Map<String, AtomicInteger> controlPanelProjectsCreationMetrics;
	private Map<String, AtomicInteger> controlPanelFlowsCreationMetrics;
	private Map<String, AtomicInteger> controlPanelGisViewersCreationMetrics;
	private Map<String, AtomicInteger> controlPanelQueriesMetrics;

	@PostConstruct
	public void init() {

		this.ontologyMetrics = new ConcurrentHashMap<>();
		this.operationMetrics = new ConcurrentHashMap<>();
		this.apiMetrics = new ConcurrentHashMap<>();
		this.controlPanelLoginMetrics = new ConcurrentHashMap<>();
		this.controlPanelOntologyCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelUserCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelApisCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelDashboardsCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelClientsPlatformCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelNotebooksCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelDataflowsCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelProjectsCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelFlowsCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelGisViewersCreationMetrics = new ConcurrentHashMap<>();
		this.controlPanelQueriesMetrics = new ConcurrentHashMap<>();

	}

	@Override
	@Async
	public void logMetricDigitalBroker(String userIdentification, String ontologyIdentification,
			SSAPMessageTypes operationType, Source source, String result) {

		if (metricsEnabled) {
			if (ontologyIdentification != null && ontologyIdentification.trim().length() > 0) {
				this.logMetricOntology(userIdentification, ontologyIdentification, operationType.name(), source,
						result);
			}

			this.logMetricOperation(userIdentification, operationType.name(), source, result);
		}

	}

	@Override
	@Async
	public void logMetricApiManager(String userIdentification, String ontologyIdentification, String method,
			Source source, String result, String api) {

		if (metricsEnabled) {
			if (ontologyIdentification != null && ontologyIdentification.trim().length() > 0) {
				this.logMetricOntology(userIdentification, ontologyIdentification, method, source, result);
			}

			this.logMetricOperation(userIdentification, method, source, result);
			this.logApiOperation(userIdentification, api, method, source, result);
		}

	}

	@Override
	@Async
	public void logControlPanelLogin(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelLoginOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelOntologyCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelOntologyCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelUserCreation(String result) {
		if (metricsEnabled) {
			this.logControlPanelUserCreationOperation(result);
		}
	}

	@Override
	@Async
	public void logControlPanelApiCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelApiCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelDashboardsCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelDashboardsCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelClientsPlatformCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelClientsPlatformCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelNotebooksCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelNotebooksCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelDataflowsCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelDataflowsCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelProjectsCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelProjectsCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelFlowsCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelFlowsCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelGisViewersCreation(String userIdentification, String result) {
		if (metricsEnabled) {
			this.logControlPanelGisViewersCreationOperation(userIdentification, result);
		}
	}

	@Override
	@Async
	public void logControlPanelQueries(String userIdentification, String ontology, String result) {
		if (metricsEnabled) {
			this.logControlPanelQueriesOperation(userIdentification, ontology, result);
		}
	}

	@Override
	public MetricsPlatformDto computeMetrics(long date) {
		MetricsPlatformDto dto = new MetricsPlatformDto();

		List<MetricsOntologyDto> lMetricsOntology = new ArrayList<>();
		List<MetricsOperationDto> lMetricsOperation = new ArrayList<>();
		List<MetricsApiDto> lMetricsApi = new ArrayList<>();
		List<MetricsControlPanelDto> lMetricsControlPanel = new ArrayList<>();
		List<MetricsOntologyDto> lMetricsQueryControlPanel = new ArrayList<>();

		this.ontologyMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsOntologyDto metricsOntology = new MetricsOntologyDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);
				metricsOntology.setOntology(keys[0]);
				metricsOntology.setUserIdentification(keys[1]);
				metricsOntology.setOperationType(keys[2]);
				metricsOntology.setSource(keys[3]);
				metricsOntology.setResult(keys[4]);
				metricsOntology.setValue(value.longValue());
				lMetricsOntology.add(metricsOntology);

				// Reset the value
				value.set(0);
			}
		});

		this.operationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsOperationDto metricsOperation = new MetricsOperationDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);
				metricsOperation.setUserIdentification(keys[0]);
				metricsOperation.setOperationType(keys[1]);
				metricsOperation.setSource(keys[2]);
				metricsOperation.setResult(keys[3]);
				metricsOperation.setValue(value.longValue());

				lMetricsOperation.add(metricsOperation);

				// Reset the value
				value.set(0);
			}
		});

		this.apiMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsApiDto metricsApi = new MetricsApiDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);
				metricsApi.setApi(keys[0]);
				metricsApi.setUserIdentification(keys[1]);
				metricsApi.setOperationType(keys[2]);
				metricsApi.setSource(keys[3]);
				metricsApi.setResult(keys[4]);
				metricsApi.setValue(value.longValue());
				lMetricsApi.add(metricsApi);

				// Reset the value
				value.set(0);
			}
		});

		this.controlPanelLoginMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue() / 2);
				metricsControlPanel.setOperationType("LOGIN");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelOntologyCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("ONTOLOGY_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelUserCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification("");
				metricsControlPanel.setResult(keys[0]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("USER_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelApisCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("API_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelDashboardsCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("DASHBOARD_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelClientsPlatformCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("CLIENT_PLATFORM_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelNotebooksCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("NOTEBOOK_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelDataflowsCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("DATAFLOW_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelProjectsCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("PROJECT_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelFlowsCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("FLOW_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelGisViewersCreationMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsControlPanelDto metricsControlPanel = new MetricsControlPanelDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsControlPanel.setUserIdentification(keys[0]);
				metricsControlPanel.setResult(keys[1]);
				metricsControlPanel.setValue(value.longValue());
				metricsControlPanel.setOperationType("GIS_VIEWERS_CREATION");
				lMetricsControlPanel.add(metricsControlPanel);

				// Reset the value
				value.set(0);
			}

		});

		this.controlPanelQueriesMetrics.forEach((key, value) -> {
			if (value.longValue() > 0) {
				MetricsOntologyDto metricsOntology = new MetricsOntologyDto();
				String[] keys = key.split(KEY_SEPARATOR_REGEX);

				metricsOntology.setUserIdentification(keys[0]);
				metricsOntology.setOntology(keys[1]);
				metricsOntology.setResult(keys[2]);
				metricsOntology.setValue(value.longValue());
				metricsOntology.setOperationType("TOOL_QUERY");

				lMetricsQueryControlPanel.add(metricsOntology);

				// Reset the value
				value.set(0);
			}

		});

		dto.setTimestamp(date);
		dto.setLMetricsOntology(lMetricsOntology);
		dto.setLMetricsOperations(lMetricsOperation);
		dto.setLMetricsApi(lMetricsApi);
		dto.setLMetricsControlPanel(lMetricsControlPanel);
		dto.setLMetricsQueryControlPanel(lMetricsQueryControlPanel);

		return dto;

	}

	private void logMetricOntology(String userIdentification, String ontologyIdentification, String operationType,
			Source source, String result) {

		Gauge search = metricsRegistry.find(METRICS_USAGE_ONTOLOGY_KEY).tag(ONTOLOGY_STR, ontologyIdentification)
				.tag("user", userIdentification).tag(OPERATION_TYPE_STR, operationType).tag(SOURCE_STR, source.name())
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_USAGE_ONTOLOGY_KEY,
					Arrays.asList(Tag.of(ONTOLOGY_STR, ontologyIdentification), Tag.of("user", userIdentification),
							Tag.of(OPERATION_TYPE_STR, operationType), Tag.of(SOURCE_STR, source.name()),
							Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.ontologyMetrics.put(ontologyIdentification + KEY_SEPARATOR + userIdentification + KEY_SEPARATOR
					+ operationType + KEY_SEPARATOR + source.name() + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.ontologyMetrics.get(ontologyIdentification + KEY_SEPARATOR + userIdentification
					+ KEY_SEPARATOR + operationType + KEY_SEPARATOR + source.name() + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_USAGE_ONTOLOGY_KEY,
						Arrays.asList(Tag.of(ONTOLOGY_STR, ontologyIdentification), Tag.of("user", userIdentification),
								Tag.of(OPERATION_TYPE_STR, operationType), Tag.of(SOURCE_STR, source.name()),
								Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.ontologyMetrics.put(ontologyIdentification + KEY_SEPARATOR + userIdentification + KEY_SEPARATOR
						+ operationType + KEY_SEPARATOR + source.name() + source.name() + KEY_SEPARATOR + result,
						counter);
			}
			counter.incrementAndGet();
		}

	}

	private void logMetricOperation(String userIdentification, String operationType, Source source, String result) {

		Gauge search = metricsRegistry.find(METRICS_OPERATION_KEY).tag("user", userIdentification)
				.tag(OPERATION_TYPE_STR, operationType).tag(SOURCE_STR, source.name()).tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry
					.gauge(METRICS_OPERATION_KEY,
							Arrays.asList(Tag.of("user", userIdentification), Tag.of(OPERATION_TYPE_STR, operationType),
									Tag.of(SOURCE_STR, source.name()), Tag.of(RESULT_STR, result)),
							new AtomicInteger(1));

			this.operationMetrics.put(userIdentification + KEY_SEPARATOR + operationType + KEY_SEPARATOR + source.name()
					+ KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.operationMetrics.get(userIdentification + KEY_SEPARATOR + operationType
					+ KEY_SEPARATOR + source.name() + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_OPERATION_KEY,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(OPERATION_TYPE_STR, operationType),
								Tag.of(SOURCE_STR, source.name()), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.operationMetrics.put(userIdentification + KEY_SEPARATOR + operationType + KEY_SEPARATOR
						+ source.name() + KEY_SEPARATOR + result, counter);

			}
			counter.incrementAndGet();
		}
	}

	private void logApiOperation(String userIdentification, String apiIdentification, String method, Source source,
			String result) {
		Gauge search = metricsRegistry.find(METRICS_USAGE_API_KEY).tag("api", apiIdentification)
				.tag("user", userIdentification).tag(OPERATION_TYPE_STR, method).tag(SOURCE_STR, source.name())
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_USAGE_API_KEY,
					Arrays.asList(Tag.of("api", apiIdentification), Tag.of("user", userIdentification),
							Tag.of(OPERATION_TYPE_STR, method), Tag.of(SOURCE_STR, source.name()),
							Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.apiMetrics.put(apiIdentification + KEY_SEPARATOR + userIdentification + KEY_SEPARATOR + method
					+ KEY_SEPARATOR + source.name() + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.apiMetrics.get(apiIdentification + KEY_SEPARATOR + userIdentification
					+ KEY_SEPARATOR + method + KEY_SEPARATOR + source.name() + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_USAGE_API_KEY,
						Arrays.asList(Tag.of("api", apiIdentification), Tag.of("user", userIdentification),
								Tag.of(OPERATION_TYPE_STR, method), Tag.of(SOURCE_STR, source.name()),
								Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.apiMetrics.put(apiIdentification + KEY_SEPARATOR + userIdentification + KEY_SEPARATOR + method
						+ KEY_SEPARATOR + source.name() + source.name() + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelLoginOperation(String userIdentification, String result) {
		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_LOGIN).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_LOGIN,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelLoginMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelLoginMetrics.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_LOGIN,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelLoginMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelOntologyCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_ONTOLOGIES).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_ONTOLOGIES,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelOntologyCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelOntologyCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_ONTOLOGIES,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelOntologyCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelUserCreationOperation(String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_USERS).tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_USERS,
					Arrays.asList(Tag.of(RESULT_STR, result)), new AtomicInteger(1));

			this.controlPanelUserCreationMetrics.put(result, counter);
		} else {

			AtomicInteger counter = this.controlPanelUserCreationMetrics.get(result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_USERS, Arrays.asList(Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelUserCreationMetrics.put(result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelApiCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_APIS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_APIS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelApisCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelApisCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_APIS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelApisCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelDashboardsCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_DASHBOARDS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_DASHBOARDS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelDashboardsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelDashboardsCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_DASHBOARDS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelDashboardsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelClientsPlatformCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_CLIENTS_PLATFORM).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_CLIENTS_PLATFORM,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelClientsPlatformCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelClientsPlatformCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_CLIENTS_PLATFORM,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelClientsPlatformCreationMetrics.put(userIdentification + KEY_SEPARATOR + result,
						counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelNotebooksCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_NOTEBOOKS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_NOTEBOOKS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelNotebooksCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelNotebooksCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_NOTEBOOKS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelNotebooksCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelDataflowsCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_DATAFLOWS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_DATAFLOWS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelDataflowsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelDataflowsCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_DATAFLOWS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelDataflowsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelProjectsCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_PROJECTS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_PROJECTS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelProjectsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelProjectsCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_PROJECTS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelProjectsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelFlowsCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_FLOWS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_FLOWS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelFlowsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelFlowsCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_FLOWS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelFlowsCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelGisViewersCreationOperation(String userIdentification, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_GIS_VIEWERS).tag("user", userIdentification)
				.tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_GIS_VIEWERS,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelGisViewersCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
		} else {

			AtomicInteger counter = this.controlPanelGisViewersCreationMetrics
					.get(userIdentification + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_GIS_VIEWERS,
						Arrays.asList(Tag.of("user", userIdentification), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelGisViewersCreationMetrics.put(userIdentification + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

	private void logControlPanelQueriesOperation(String userIdentification, String ontology, String result) {

		Gauge search = metricsRegistry.find(METRICS_CONTROL_PANEL_QUERIES).tag("user", userIdentification)
				.tag(ONTOLOGY_STR, ontology).tag(RESULT_STR, result).gauge();

		if (null == search) {
			AtomicInteger counter = metricsRegistry.gauge(METRICS_CONTROL_PANEL_QUERIES,
					Arrays.asList(Tag.of("user", userIdentification), Tag.of(ONTOLOGY_STR, ontology),
							Tag.of(RESULT_STR, result)),
					new AtomicInteger(1));

			this.controlPanelQueriesMetrics.put(userIdentification + KEY_SEPARATOR + ontology + KEY_SEPARATOR + result,
					counter);
		} else {

			AtomicInteger counter = this.controlPanelQueriesMetrics
					.get(userIdentification + KEY_SEPARATOR + ontology + KEY_SEPARATOR + result);

			if (null == counter) {
				counter = metricsRegistry.gauge(
						METRICS_CONTROL_PANEL_QUERIES, Arrays.asList(Tag.of("user", userIdentification),
								Tag.of(ONTOLOGY_STR, ontology), Tag.of(RESULT_STR, result)),
						new AtomicInteger((int) search.value()));

				this.controlPanelQueriesMetrics
						.put(userIdentification + KEY_SEPARATOR + ontology + KEY_SEPARATOR + result, counter);
			}
			counter.incrementAndGet();
		}
	}

}
