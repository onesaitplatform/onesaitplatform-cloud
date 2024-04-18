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
package com.minsait.onesait.platform.commons.kafka.burrow;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.kafka.burrow.response.cluster.BurrowClustersResponse;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.BurrowConsumerGroupsResponse;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.BurrowConsumerTopicInfoResponse;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.BurrowGroupStatusResponse;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.info.BurrowOffsetInstant;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.info.BurrowOffsetWindow;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.status.BurrowPartitionStatusDetail;
import com.minsait.onesait.platform.commons.kafka.monitoring.KafkaMonitoringTopicInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BurrowService {

	@Value("${onesaitplatform.kafka.burrow.monitoring.host.url:http://localhost:18400}")
	private String burrowHostUrl;
	@Value("${onesaitplatform.kafka.burrow.monitoring.request.timeout.ms:5000}")
	private int restRequestTimeout;

	private static final String V3_KAFKA = "/v3/kafka/";

	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private ObjectMapper mapper;

	@PostConstruct
	public void init() {
		httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectTimeout(restRequestTimeout);
		mapper = new ObjectMapper();
	}

	public BurrowGroupStatusResponse getClientGroupStatus(String cluster, String clientGroup) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String requestResponse = null;
		BurrowGroupStatusResponse state = null;
		try {
			requestResponse = restTemplate.getForObject(
					burrowHostUrl + V3_KAFKA + cluster + "/consumer/" + clientGroup + "/status", String.class);
		} catch (Exception e) {
			log.error("Unable to retrieve Client Group status. Cluster={}, Group={}, Cause={}, message={}", cluster,
					clientGroup, e.getCause(), e.getMessage());
		}

		try {
			state = mapper.readValue(requestResponse, BurrowGroupStatusResponse.class);
		} catch (IOException e) {
			log.error("Unable to process Client Group status. Cluster={}, Group={}, Cause={}, message={}", cluster,
					clientGroup, e.getCause(), e.getMessage());
			log.error(e.getMessage());
		}
		return state;
	}

	public List<BurrowGroupStatusResponse> getAllClientGroupStatus(String cluster) {
		List<BurrowGroupStatusResponse> response = new ArrayList<>();
		BurrowConsumerGroupsResponse clientGroups = getAllClientGroups(cluster);
		BurrowGroupStatusResponse state = null;
		for (String clientGroup : clientGroups.getConsumers()) {
			if (!clientGroup.isEmpty()) {
				try {
					state = getClientGroupStatus(cluster, clientGroup);
					response.add(state);
				} catch (Exception e) {
					log.error("Unable to process Client Group status. Cluster={}, Group={}, Cause={}, message={}",
							cluster, clientGroup, e.getCause(), e.getMessage());
					log.error(e.getMessage());
				}
			}
		}
		return response;
	}

	public BurrowConsumerGroupsResponse getAllClientGroups(String cluster) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String requestResponse = null;
		BurrowConsumerGroupsResponse response = null;
		try {
			requestResponse = restTemplate.getForObject(burrowHostUrl + V3_KAFKA + cluster + "/consumer", String.class);
		} catch (Exception e) {
			log.error("Unable to retrieve client groups list from cluster. Cluster={}, Cause={}, message={}", cluster,
					e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage(), e);
		}

		try {
			response = mapper.readValue(requestResponse, BurrowConsumerGroupsResponse.class);
		} catch (IOException e) {
			log.error("Unable process client group list from cluster. Cluster={}, Cause={}, message={}", cluster,
					e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage(), e);
		}
		return response;
	}

	public BurrowClustersResponse getAllClusters() {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		BurrowClustersResponse response = null;
		String requestResponse = null;
		try {
			requestResponse = restTemplate.getForObject(burrowHostUrl + V3_KAFKA, String.class);
		} catch (Exception e) {
			log.error("Unable to retrieve clusters list. Cause={}, message={}", e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage());
		}

		try {
			response = mapper.readValue(requestResponse, BurrowClustersResponse.class);
		} catch (IOException e) {
			log.error("Unable process clusters list. Cause={}, message={}", e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage(), e);
		}
		return response;
	}

	private BurrowConsumerTopicInfoResponse getBurrowConsumerTopicInfo(String cluster, String consumer) {
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		BurrowConsumerTopicInfoResponse response = null;
		String requestResponse = null;

		try {
			requestResponse = restTemplate.getForObject(burrowHostUrl + V3_KAFKA + cluster + "/consumer/" + consumer,
					String.class);
		} catch (Exception e) {
			log.error("Unable to retrieve consumers topics and offsets.Cluster={}, Consumer={}, Cause={}, message={}",
					cluster, consumer, e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage());
		}

		try {
			response = mapper.readValue(requestResponse, BurrowConsumerTopicInfoResponse.class);
		} catch (IOException e) {
			log.error("Unable process consumers topics and offsets.Cluster={}, Consumer={}, Cause={}, message={}",
					cluster, consumer, e.getCause(), e.getMessage());
			throw new BurrowClientRequestException(e.getMessage(), e);
		}

		return response;
	}

	public List<KafkaMonitoringTopicInfo> getConsumerTopicInfo(String cluster, String consumer) {
		List<KafkaMonitoringTopicInfo> result = new ArrayList<>();
		BurrowConsumerTopicInfoResponse rawTopicInfo = getBurrowConsumerTopicInfo(cluster, consumer);
		for (String topic : rawTopicInfo.getTopics().keySet()) {
			BurrowOffsetWindow[] partitions = rawTopicInfo.getTopics().get(topic);
			for (int partition = 0; partition < partitions.length; partition++) {
				BurrowOffsetInstant[] windows = partitions[partition].getOffsets();
				Long millis = windows[windows.length - 1].getTimestamp();

				Date d = new Date(millis);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
				String timestamp = sdf.format(d);

				Long offset = windows[windows.length - 1].getOffset();
				Long lag = windows[windows.length - 1].getLag();
				String status = "OK";
				BurrowGroupStatusResponse consumerStatus = getClientGroupStatus(cluster, consumer);
				for (BurrowPartitionStatusDetail detail : consumerStatus.getStatus().getPartitions()) {
					if (detail.getTopic().equals(topic) && detail.getPartition() == partition) {
						// this topic/partition its not OK
						status = detail.getStatus();
						lag = detail.getCurrentLag();
					}
				}

				KafkaMonitoringTopicInfo partitionInfo = KafkaMonitoringTopicInfo.builder().topic(topic)
						.partition(partition).status(status).timestamp(timestamp).offset(offset).lag(lag).build();
				result.add(partitionInfo);
			}
		}
		return result;
	}

	public List<KafkaMonitoringTopicInfo> getConsumerTopicPartitionEvolutionWindow(String cluster, String consumer,
			String topic, int partition) {
		List<BurrowOffsetInstant> rawResult = null;
		List<KafkaMonitoringTopicInfo> result = new ArrayList<>();
		BurrowConsumerTopicInfoResponse response = getBurrowConsumerTopicInfo(cluster, consumer);
		if (!response.getError()) {
			BurrowOffsetWindow[] windows = response.getTopics().get(topic);
			if (partition >= windows.length || partition < 0) {
				throw new BurrowClientRequestException("Especified partition does not exist.");
			}
			BurrowOffsetInstant[] offsetsEvolution = windows[partition].getOffsets();
			rawResult = Arrays.asList(offsetsEvolution);
			for (BurrowOffsetInstant instant : rawResult) {
				Date d = new Date(instant.getTimestamp());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
				String timestamp = sdf.format(d);
				result.add(KafkaMonitoringTopicInfo.builder().timestamp(timestamp).offset(instant.getOffset())
						.lag(instant.getLag()).build());
			}
		} else {
			throw new BurrowClientRequestException(response.getMessage());
		}
		return result;
	}
}
