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
package com.minsait.onesait.platform.metrics.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.icu.text.SimpleDateFormat;
import com.minsait.onesait.platform.commons.model.MetricsApiDto;
import com.minsait.onesait.platform.commons.model.MetricsControlPanelDto;
import com.minsait.onesait.platform.commons.model.MetricsOntologyDto;
import com.minsait.onesait.platform.commons.model.MetricsOperationDto;
import com.minsait.onesait.platform.commons.model.MetricsPlatformDto;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.MongoDBTimeSeriesProcessor;

import lombok.extern.slf4j.Slf4j;

@RestController
@EnableAutoConfiguration
@RequestMapping("metrics-collector")
@Slf4j
public class MetricAggregatorController {

	private static final String FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static final String METRIC_ONTOLOGY = "MetricsOntology";
	private static final String METRIC_ONTOLOGY_TEMPLATE = "{\"TimeSerie\":{ \"timestamp\":{\"$date\": \"%s\"},\"source\":\"%s\",\"result\":\"%s\",\"user\":\"%s\",\"operationType\":\"%s\",\"ontology\":\"%s\",\"value\":%d}}";

	private static final String METRIC_OPERATION = "MetricsOperation";
	private static final String METRIC_OPERATION_TEMPLATE = "{\"TimeSerie\":{ \"timestamp\":{\"$date\": \"%s\"},\"source\":\"%s\",\"result\":\"%s\",\"user\":\"%s\",\"operationType\":\"%s\",\"value\":%d}}";

	private static final String METRIC_API = "MetricsApi";
	private static final String METRIC_API_TEMPLATE = "{\"TimeSerie\":{ \"timestamp\":{\"$date\": \"%s\"},\"result\":\"%s\",\"user\":\"%s\",\"operationType\":\"%s\",\"api\":\"%s\",\"value\":%d}}";

	private static final String METRIC_CONTROLPANEL = "MetricsControlPanel";
	private static final String METRIC_CONTROLPANEL_TEMPLATE = "{\"TimeSerie\":{ \"timestamp\":{\"$date\": \"%s\"},\"result\":\"%s\",\"user\":\"%s\",\"operationType\":\"%s\",\"value\":%d}}";

	private static final String METRIC_QUERIES_CONTROLPANEL = "MetricsQueriesControlPanel";
	private static final String METRIC_QUERIES_CONTROLPANEL_TEMPLATE = "{\"TimeSerie\":{ \"timestamp\":{\"$date\": \"%s\"},\"result\":\"%s\",\"user\":\"%s\",\"ontology\":\"%s\",\"value\":%d}}";

	@Autowired
	private MongoDBTimeSeriesProcessor timeSeriesProcessor;

	@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	public ResponseEntity<String> processMetrics(HttpServletRequest request, HttpServletResponse response,
			@RequestBody MetricsPlatformDto requestBody) {

		try {
			final SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_SECONDS);
			final String formattedDate = sdf.format(new Date(requestBody.getTimestamp()));

			final List<MetricsOntologyDto> metricsOntologies = requestBody.getLMetricsOntology();
			final List<MetricsOperationDto> metricsOperations = requestBody.getLMetricsOperations();
			final List<MetricsApiDto> metricsApies = requestBody.getLMetricsApi();
			final List<MetricsControlPanelDto> metricsControlPanel = requestBody.getLMetricsControlPanel();
			final List<MetricsOntologyDto> metricsQueriesControlPanel = requestBody.getLMetricsQueryControlPanel();

			for (MetricsOntologyDto dto : metricsOntologies) {
				synchronized (this) { // To avoid multidocuments creation -- Performance is not relevant in this
										// feature
					timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), METRIC_ONTOLOGY, buildOntologyInstance(formattedDate, dto));
				}
			}

			for (MetricsOperationDto dto : metricsOperations) {
				synchronized (this) { // To avoid multidocuments creation -- Performance is not relevant in this
										// feature
					timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), METRIC_OPERATION, buildOperationInstance(formattedDate, dto));
				}
			}

			for (MetricsApiDto dto : metricsApies) {
				synchronized (this) { // To avoid multidocuments creation -- Performance is not relevant in this
										// feature
					timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), METRIC_API, buildApiInstance(formattedDate, dto));
				}
			}

			for (MetricsControlPanelDto dto : metricsControlPanel) {
				synchronized (this) { // To avoid multidocuments creation -- Performance is not relevant in this
										// feature
					timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), METRIC_CONTROLPANEL,
							buildControlPanelInstance(formattedDate, dto));
				}
			}

			for (MetricsOntologyDto dto : metricsQueriesControlPanel) {
				synchronized (this) { // To avoid multidocuments creation -- Performance is not relevant in this
										// feature
					timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), METRIC_QUERIES_CONTROLPANEL,
							buildQueriesControlPanelInstance(formattedDate, dto));
				}
			}

		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);

	}

	private String buildOntologyInstance(String date, MetricsOntologyDto dto) {
		return String.format(METRIC_ONTOLOGY_TEMPLATE, date, dto.getSource(), dto.getResult(),
				dto.getUserIdentification(), dto.getOperationType(), dto.getOntology(), dto.getValue());

	}

	private String buildOperationInstance(String date, MetricsOperationDto dto) {
		return String.format(METRIC_OPERATION_TEMPLATE, date, dto.getSource(), dto.getResult(),
				dto.getUserIdentification(), dto.getOperationType(), dto.getValue());

	}

	private String buildApiInstance(String date, MetricsApiDto dto) {
		return String.format(METRIC_API_TEMPLATE, date, dto.getResult(), dto.getUserIdentification(),
				dto.getOperationType(), dto.getApi(), dto.getValue());

	}

	private String buildControlPanelInstance(String date, MetricsControlPanelDto dto) {
		return String.format(METRIC_CONTROLPANEL_TEMPLATE, date, dto.getResult(), dto.getUserIdentification(),
				dto.getOperationType(), dto.getValue());

	}

	private String buildQueriesControlPanelInstance(String date, MetricsOntologyDto dto) {
		return String.format(METRIC_QUERIES_CONTROLPANEL_TEMPLATE, date, dto.getResult(), dto.getUserIdentification(),
				dto.getOntology(), dto.getValue());

	}

}
