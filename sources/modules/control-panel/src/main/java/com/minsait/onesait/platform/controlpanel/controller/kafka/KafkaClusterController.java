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
package com.minsait.onesait.platform.controlpanel.controller.kafka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.kafka.integration.KafkaIntegrationService;
import com.minsait.onesait.platform.commons.kafka.integration.KafkaIntegrationService.OffsetSeekType;
import com.minsait.onesait.platform.commons.kafka.integration.KafkaIntegrationServiceException;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.KafkaClusterInstance;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.kafka.KafkaClusterServiceImpl;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.cluster.status.KafkaClusterStatusDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.connections.KafkaClusterConnectionFIQL;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.connections.KafkaProperty;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.connections.KakfaClusterConnectionDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consume.ConsumeRecordDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consume.ConsumeRecordFIQL;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consume.ConsumerRequest;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consumergroup.ConsumerGroupDescriptionDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consumergroup.ConsumerGroupOffsetDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consumergroup.ConsumerGroupOffsetRequest;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.procude.ProducerRequest;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.topic.PartitionDTO;
import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.topic.TopicDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/kafka/cluster")
@Slf4j
public class KafkaClusterController {

	private enum KafkaPropertyType {
		CONNECTION("AdminClient"), TOPIC("topics");

		@Getter
		private final String label;

		private KafkaPropertyType(String label) {
			this.label = label;
		}
	}

	private static final String REDIRECT_CONN_CREATE = "redirect:/kafka/cluster/create";
	private static final String REDIRECT_CONN_LIST = "redirect:/kafka/cluster/list";
	private static final String REDIRECT_CONN_UPDATE = "redirect:/kafka/cluster/update/";
	private static final String REDIRECT_CONN_SHOW = "redirect:/kafka/cluster/show/";

	@Autowired
	private KafkaClusterServiceImpl kafkaClusterService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ConfigurationService configurationService;

	/*
	 * CLUSTER CONNECTIONS
	 */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {
		List<KafkaClusterInstance> kafkaClusterConnections = null;

		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}
		try {
			kafkaClusterConnections = kafkaClusterService.listClusterConnections(identification, description);
			model.addAttribute("kafkaClusterConnections",
					KafkaClusterConnectionFIQL.fromKafkaClusterInstanceList(kafkaClusterConnections));
		} catch (Exception e) {
			log.error("Unable to retrieve Kafka cluster connections. Cause = {}, Message = {}", e.getCause(),
					e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.list.cluster.error",
					"Error while accessing Kafka cluster connections."));
			model.addAttribute("messageAlertType", "WARNING");
		}

		return "kafkaclusterconnections/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		try {
			final KakfaClusterConnectionDTO deviceDTO = new KakfaClusterConnectionDTO();
			model.addAttribute("kafkaClusterConnection", deviceDTO);
			model.addAttribute("connectionProperties", fillKafkaProperties(KafkaPropertyType.CONNECTION));
			return "kafkaclusterconnections/create";
		} catch (Exception e) {
			log.error("Unable to Load Kafka client configuration properties. Cause = {}, Message = {}", e.getCause(),
					e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.load.properties.error",
					"Error while accessing Kafka cluster connection properties."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		}

	}

	private List<KafkaProperty> fillKafkaProperties(KafkaPropertyType type) throws IOException {
		List<KafkaProperty> properties = new ArrayList<>();
		Configuration config = configurationService.getConfiguration(Type.KAFKA_PROPERTIES, "Kafka Client Properties");
		String configValues = config.getYmlConfig();
		JsonNode jsonNodeconf = new ObjectMapper().readTree(configValues);
		ArrayNode arrayNode = (ArrayNode) jsonNodeconf.get(type.getLabel());
		for (JsonNode node : arrayNode) {
			properties.add(
					KafkaProperty.builder().id(node.get("id").asText()).description(node.get("description").asText())
							.defaultValue(node.get("defaultValue").asText()).build());
		}
		return properties;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = { "/create" })
	public String createKafkaClusterConnection(Model model, @Valid KakfaClusterConnectionDTO kafkaClusterConnectionDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {

		try {
			kafkaClusterService.createClusterConnection(
					KafkaClusterConnectionFIQL.toKafkaClusterInstance(kafkaClusterConnectionDTO));
		} catch (final JSONException e) {
			log.error("Cannot create Kafka cluster connection", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_CONN_CREATE;
		} catch (final Exception e) {
			log.error("Error creating Kafka cluster connection", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_CONN_CREATE;
		}
		return REDIRECT_CONN_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);

		if (kafkaClusterConnection.isPresent()) {

			try {
				model.addAttribute("kafkaClusterConnection",
						KafkaClusterConnectionFIQL.fromKafkaClusterInstance(kafkaClusterConnection.get()));
			} catch (Exception e) {
				log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
						e.getMessage());
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
			try {
				model.addAttribute("connectionProperties", fillKafkaProperties(KafkaPropertyType.CONNECTION));
			} catch (Exception e) {
				log.error("Unable to Load Kafka client configuration properties. Cause = {}, Message = {}",
						e.getCause(), e.getMessage());
				model.addAttribute("message", utils.getMessage("kafka.cluster.load.properties.error",
						"Error while accessing Kafka cluster connection properties."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}

			return "kafkaclusterconnections/create";
		} else {
			log.error("Kafka cluster connection not found. ID={}", id);
			return REDIRECT_CONN_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateKafkaClusterConnection(Model model, @PathVariable("id") String id,
			@Valid KakfaClusterConnectionDTO kafkaClusterConnectionDTO, BindingResult bindingResult,
			RedirectAttributes redirect) {

		try {
			kafkaClusterService.updateClusterConnection(
					KafkaClusterConnectionFIQL.toKafkaClusterInstance(kafkaClusterConnectionDTO));
		} catch (final Exception e) {
			log.error("Error Updating Kafka cluster connection", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_CONN_UPDATE + id;
		}
		return REDIRECT_CONN_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			if (!kafkaClusterConnection.isPresent()) {
				return new ResponseEntity<>(utils.getMessage("device.delete.error.forbidden", "forbidden"),
						HttpStatus.FORBIDDEN);
			}
			kafkaClusterService.deleteClusterConnection(id);

		} catch (final Exception e) {
			return new ResponseEntity<>(utils.getMessage("device.delete.error", "error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("/controlpanel/kafka/cluster/list", HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);

		if (kafkaClusterConnection.isPresent()) {

			try {
				KakfaClusterConnectionDTO connectionDTO = KafkaClusterConnectionFIQL
						.fromKafkaClusterInstance(kafkaClusterConnection.get());
				model.addAttribute("kafkaClusterConnection", connectionDTO);

				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				KafkaClusterStatusDTO connectionStatus = new KafkaClusterStatusDTO();
				connectionStatus.setConnection(connectionDTO);
				connectionStatus.setBrokerNodes(new ArrayList<>(KafkaIntegrationService.getClusterInfo(properties)));
				connectionStatus
						.setConsumerGroups(new ArrayList<>(KafkaIntegrationService.listConsumerGroups(properties)));
				connectionStatus.setTopics(new ArrayList<>(KafkaIntegrationService.listTopics(properties)));
				model.addAttribute("kafkaClusterStatus", connectionStatus);
			} catch (IOException e) {
				log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
						e.getMessage());
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");

				StringBuilder errorMsg = new StringBuilder().append(
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."))
						.append(e.getCause().getCause());
				utils.addRedirectMessage(errorMsg.toString(), redirect);
				return REDIRECT_CONN_LIST;
			} catch (KafkaIntegrationServiceException e) {
				log.error("Unable to connect to the kafka cluster. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
						e.getMessage());
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.connection.error", "Error while connecting to Kafka cluster."));
				model.addAttribute("messageAlertType", "WARNING");

				StringBuilder errorMsg = new StringBuilder().append(
						utils.getMessage("kafka.cluster.connection.error", "Error while connecting to Kafka cluster. "))
						.append(e.getCause().getCause());
				utils.addRedirectMessage(errorMsg.toString(), redirect);
				return REDIRECT_CONN_LIST;
			}

			return "kafkaclusterconnections/show";
		} else {
			return REDIRECT_CONN_LIST;
		}

	}

	private Properties fromConfigToProperties(KafkaClusterInstance instance)
			throws JsonParseException, JsonMappingException, IOException {
		Properties props = new Properties();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(instance.getKafkaConfig());
		ArrayNode array = ((ArrayNode) actualObj);
		for (Iterator<JsonNode> arrayIterator = array.iterator(); arrayIterator.hasNext();) {
			JsonNode property = arrayIterator.next();
			for (Iterator<Entry<String, JsonNode>> iterator = property.fields(); iterator.hasNext();) {
				Entry<String, JsonNode> item = iterator.next();
				props.put(item.getKey(), item.getValue().asText());
			}
		}

		return props;
	}

	/*
	 * TOPICS
	 */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/topic/show/{topic}", produces = "text/html")
	public String showTopic(Model model, HttpServletRequest request, @PathVariable("connectionId") String id,
			@PathVariable("topic") String topic) {

		TopicDTO topicDto = new TopicDTO();
		topicDto.setTopic(topic);
		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
			if (kafkaClusterConnection.isPresent()) {
				TopicDescription topicDescr = KafkaIntegrationService.getTopicPartitionInfo(properties, topic);
				Map<TopicPartition, ListOffsetsResultInfo> offsets = KafkaIntegrationService.getTopicOffsets(properties,
						topic);
				topicDto.setPartitions(new ArrayList<>(offsets.size()));
				for (TopicPartitionInfo p : topicDescr.partitions()) {
					topicDto.getPartitions().add(p.partition(), new PartitionDTO());
					topicDto.getPartitions().get(p.partition()).setId(p.partition());
					// fill topicDTO attributes -- this below are lists, put in better format
					topicDto.getPartitions().get(p.partition()).setIsr(p.isr().toString()
							.replaceAll(" rack:(.*)\\)", ")").replaceAll(",", ", ").replaceAll("[\\[\\]]", ""));
					topicDto.getPartitions().get(p.partition())
							.setLeader(p.leader().toString().replaceAll(" rack:(.*)\\)", ")"));
					topicDto.getPartitions().get(p.partition()).setReplicas(p.replicas().toString()
							.replaceAll(" rack:(.*)\\)", ")").replaceAll(",", ", ").replaceAll("[\\[\\]]", ""));
				}
				for (Entry<TopicPartition, ListOffsetsResultInfo> entry : offsets.entrySet()) {
					topicDto.getPartitions().get(entry.getKey().partition()).setOffset(entry.getValue().offset());
				}
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
				model.addAttribute("topicConfig", KafkaIntegrationService.getTopicConfig(properties, topic));
				model.addAttribute("topicPartitionInfo", topicDto);
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (IOException e) {
			log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		} catch (KafkaIntegrationServiceException e) {
			log.error("Unable to get topic partition info and offsets. ID = {}, Cause = {}, Message = {}", id,
					e.getCause(), e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.topic.show.error",
					"Error while loading Kafka topic's partition info and offsets."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		}

		return "kafkaclusterconnections/topic/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/topic/create")
	public String createTopic(Model model, @PathVariable("connectionId") String id) {
		try {

			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			if (kafkaClusterConnection.isPresent()) {
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
				TopicDTO topicDto = new TopicDTO();
				topicDto.setPartitionNum(1);
				topicDto.setReplicasNum(1);
				model.addAttribute("topic", topicDto);
				model.addAttribute("topicProperties", fillKafkaProperties(KafkaPropertyType.TOPIC));
				return "kafkaclusterconnections/topic/create";
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (IOException e) {
			log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = { "/{connectionId}/topic/create" })
	public String createKafkaTopic(Model model, @PathVariable("connectionId") String id, @Valid TopicDTO topicDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {

		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			if (kafkaClusterConnection.isPresent()) {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				Map<String, String> topicProperties = arrayOfPropertiesToMap(topicDTO.getConfig());
				KafkaIntegrationService.createTopic(properties, topicDTO.getTopic(), topicDTO.getPartitionNum(),
						new Integer(topicDTO.getReplicasNum()).shortValue(), topicProperties);
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_SHOW + id;
			}
		} catch (final JSONException e) {
			log.error("Cannot create Kafka Topic", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_CONN_SHOW + id;
		} catch (final Exception e) {
			log.error("Error creating Kafka Topic", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_CONN_SHOW + id;
		}
		return REDIRECT_CONN_SHOW + id;
	}

	private Map<String, String> arrayOfPropertiesToMap(String arrayJson) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(arrayJson);
		ArrayNode array = ((ArrayNode) actualObj);
		Map<String, String> topicProperties = new HashMap<>();
		for (int i = 0; i < array.size(); i++) {
			for (Iterator<Entry<String, JsonNode>> iterator = array.get(i).fields(); iterator.hasNext();) {
				Entry<String, JsonNode> item = iterator.next();
				topicProperties.put(item.getKey(), item.getValue().asText());
			}
		}
		return topicProperties;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/topic/update/{topic}", produces = "text/html")
	public String updateTopic(Model model, @PathVariable("connectionId") String id,
			@PathVariable("topic") String topic) {

		TopicDTO topicDto = new TopicDTO();
		topicDto.setTopic(topic);
		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			if (kafkaClusterConnection.isPresent()) {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				TopicDescription topicDescr = KafkaIntegrationService.getTopicPartitionInfo(properties, topic);
				Map<TopicPartition, ListOffsetsResultInfo> offsets = KafkaIntegrationService.getTopicOffsets(properties,
						topic);
				topicDto.setPartitions(new ArrayList<>(offsets.size()));
				for (TopicPartitionInfo p : topicDescr.partitions()) {
					topicDto.getPartitions().add(p.partition(), new PartitionDTO());
					topicDto.getPartitions().get(p.partition()).setId(p.partition());
					// fill topicDTO attributes -- this below are lists, put in better format
					topicDto.getPartitions().get(p.partition()).setIsr(p.isr().toString()
							.replaceAll(" rack:(.*)\\)", ")").replaceAll(",", ", ").replaceAll("[\\[\\]]", ""));
					topicDto.getPartitions().get(p.partition())
							.setLeader(p.leader().toString().replaceAll(" rack:(.*)\\)", ")"));
					topicDto.getPartitions().get(p.partition()).setReplicas(p.replicas().toString()
							.replaceAll(" rack:(.*)\\)", ")").replaceAll(",", ", ").replaceAll("[\\[\\]]", ""));
					topicDto.setReplicasNum(p.replicas().size());
				}
				for (Entry<TopicPartition, ListOffsetsResultInfo> entry : offsets.entrySet()) {
					topicDto.getPartitions().get(entry.getKey().partition()).setOffset(entry.getValue().offset());
				}
				topicDto.setPartitionNum(topicDto.getPartitions().size());

				Collection<ConfigEntry> configs = KafkaIntegrationService.getTopicConfig(properties, topic);
				ObjectMapper mapper = new ObjectMapper();
				ArrayNode configArray = mapper.createArrayNode();
				for (ConfigEntry c : configs) {
					if (!c.isDefault()) {
						configArray.add(mapper.createObjectNode().put(c.name(), c.value()));
					}
				}
				topicDto.setConfig(configArray.toString());

				model.addAttribute("topic", topicDto);
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
				model.addAttribute("topicProperties", fillKafkaProperties(KafkaPropertyType.TOPIC));
				model.addAttribute("topicConfig", configs);
				model.addAttribute("topicPartitionInfo", topicDto);
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (IOException e) {
			log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		} catch (KafkaIntegrationServiceException e) {
			log.error("Unable to get topic partition info and offsets. ID = {}, Cause = {}, Message = {}", id,
					e.getCause(), e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.topic.show.error",
					"Error while loading Kafka topic's partition info and offsets."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_SHOW + id;
		}

		return "kafkaclusterconnections/topic/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/{connectionId}/topic/update/{id}", produces = "text/html")
	public String updateKafkaTopic(Model model, @PathVariable("connectionId") String connectionId,
			@PathVariable("id") String id, @Valid TopicDTO topicDto, BindingResult bindingResult,
			RedirectAttributes redirect) {
		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(connectionId);
			if (kafkaClusterConnection.isPresent()) {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				Map<String, String> topicProperties = arrayOfPropertiesToMap(topicDto.getConfig());
				KafkaIntegrationService.updateTopic(properties, id, topicDto.getPartitionNum(),
						new Integer(topicDto.getReplicasNum()).shortValue(), topicProperties);
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (IOException e) {
			log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		} catch (KafkaIntegrationServiceException e) {
			log.error("Unable to update Kafka topic properties. Topic = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.topic.update.error",
					"Error while updating Kafka topic's partition info and offsets."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		}
		return REDIRECT_CONN_SHOW + connectionId;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/{connectionId}/topic/{id}")
	public ResponseEntity<String> deleteTopic(Model model, @PathVariable("connectionId") String connectionId,
			@PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(connectionId);
			if (!kafkaClusterConnection.isPresent()) {
				return new ResponseEntity<>(utils.getMessage("device.delete.error.forbidden", "forbidden"),
						HttpStatus.FORBIDDEN);
			}
			Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
			KafkaIntegrationService.deleteTopic(properties, id);

		} catch (final Exception e) {
			return new ResponseEntity<>(utils.getMessage("device.delete.error", "error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("/controlpanel/kafka/cluster/list", HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/topic/explore/{topic}", produces = "text/html")
	public String exploreTopic(Model model, HttpServletRequest request, @PathVariable("connectionId") String id,
			@PathVariable("topic") String topic) {
		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
			if (kafkaClusterConnection.isPresent()) {
				// Just check for connectivity
				KafkaIntegrationService.getClusterInfo(properties);
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (Exception e) {
			log.error(
					"Unable to connect to the kafka cluster while loading explore screen. ID = {}, Cause = {}, Message = {}",
					id, e.getCause(), e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.connection.error", "Error while connecting to Kafka cluster."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		}
		return "kafkaclusterconnections/topic/explore";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/{connectionId}/topic/explore/{topic}/consume")
	public @ResponseBody List<ConsumeRecordDTO> consumeFromTopic(@PathVariable("connectionId") String id,
			@PathVariable("topic") String topic, @RequestBody ConsumerRequest request) {

		List<ConsumerRecord<String, String>> records = new ArrayList<>();
		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			Properties properties;
			try {
				properties = fromConfigToProperties(kafkaClusterConnection.get());
				records = KafkaIntegrationService.readRecordsFromTopic(properties, topic, request.getNumReccords(),
						OffsetSeekType.valueOf(request.getType()), request.getTimestamp(), request.getTimeout());
			} catch (IOException e) {
				log.error(
						"Error processing Kafka properties while consuming data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error processing Kafka connection properties.");
			} catch (KafkaIntegrationServiceException e) {
				log.error("Error while consuming data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while consuming data.");
			}
		} else {
			log.error("Error while loading Kafka cluster connection. Connection not found.");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found");
		}
		return ConsumeRecordFIQL.fromConsumerRecordList(records);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/{connectionId}/topic/explore/{topic}/purge")
	public @ResponseBody void purgeDataFromTopic(@PathVariable("connectionId") String id,
			@PathVariable("topic") String topic) {
		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			try {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				KafkaIntegrationService.purgeTopicData(properties, topic);
			} catch (IOException e) {
				log.error(
						"Error processing Kafka properties while deleting topic data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error processing Kafka connection properties.");
			} catch (KafkaIntegrationServiceException e) {
				log.error("Error while deleting topic data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting topic data.");
			}
		} else {
			log.error("Error while loading Kafka cluster connection.");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found");
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/{connectionId}/topic/explore/{topic}/produce")
	public @ResponseBody void produceToTopic(@PathVariable("connectionId") String id,
			@PathVariable("topic") String topic, @RequestBody ProducerRequest request) {

		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			Properties properties;
			try {
				properties = fromConfigToProperties(kafkaClusterConnection.get());
				KafkaIntegrationService.insertDataInTopic(properties, topic, request.getKey(), request.getValue());
			} catch (IOException e) {
				log.error(
						"Error processing Kafka properties while producing data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error processing Kafka connection properties.");
			} catch (KafkaIntegrationServiceException e) {
				log.error("Error while producing data. Connection = {}, Topic = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), topic, e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while producing data.");
			}
		} else {
			log.error("Error while loading Kafka cluster connection.");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found");
		}
	}

	/*
	 * CONSUMER GROUPS
	 */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/consumer/group/show/{consumerGroup}", produces = "text/html")
	public String showConsumerGroup(Model model, HttpServletRequest request, @PathVariable("connectionId") String id,
			@PathVariable("consumerGroup") String consumerGroup) {

		try {
			final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
			if (kafkaClusterConnection.isPresent()) {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				ConsumerGroupDescription consumerGroupState = KafkaIntegrationService.getConsumerGroupState(properties,
						consumerGroup);
				Map<TopicPartition, OffsetAndMetadata> offsets = KafkaIntegrationService
						.getConsumerGroupOffsets(properties, consumerGroup);
				List<ConsumerGroupOffsetDTO> offsetsDTO = new ArrayList<>();
				Map<String, String> topics = new HashMap<>();

				for (TopicPartition part : offsets.keySet()) {
					topics.put(part.topic(), part.topic());
				}
				Map<TopicPartition, ListOffsetsResultInfo> topicOffsets = KafkaIntegrationService
						.getTopicsOffsets(properties, new ArrayList<>(topics.keySet()));
				// Create DTO array, one for each topic Partion
				for (Entry<TopicPartition, OffsetAndMetadata> offset : offsets.entrySet()) {
					long topicOffset = topicOffsets.get(offset.getKey()).offset();
					offsetsDTO.add(ConsumerGroupOffsetDTO.builder().topic(offset.getKey().topic())
							.partition(offset.getKey().partition()).topicOffset(topicOffset)
							.groupOffset(offset.getValue().offset()).build());
				}
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
				model.addAttribute("consumerGroupState", new ConsumerGroupDescriptionDTO(consumerGroupState));
				model.addAttribute("consumerGroupOffsets", offsetsDTO);
			} else {
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			}
		} catch (IOException e) {
			log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
					e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_LIST;
		} catch (KafkaIntegrationServiceException e) {
			log.error("Unable to retrieve consumer group information.ConsumerGroup = {}, Cause = {}, Message = {}",
					consumerGroup, e.getCause(), e.getMessage());
			model.addAttribute("message", utils.getMessage("kafka.cluster.group.info.error",
					"kafka.cluster.group.info.error=Error while retrieving consumer group information."));
			model.addAttribute("messageAlertType", "WARNING");
			return REDIRECT_CONN_SHOW + id;
		}

		return "kafkaclusterconnections/consumergroup/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/{connectionId}/consumer/group/edit/{consumerGroup}", produces = "text/html")
	public String editConsumerGroup(Model model, HttpServletRequest request, @PathVariable("connectionId") String id,
			@PathVariable("consumerGroup") String consumerGroup) {
		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			try {
				Properties properties = fromConfigToProperties(kafkaClusterConnection.get());
				ConsumerGroupDescription consumerGroupState = KafkaIntegrationService.getConsumerGroupState(properties,
						consumerGroup);
				Map<TopicPartition, OffsetAndMetadata> offsets = KafkaIntegrationService
						.getConsumerGroupOffsets(properties, consumerGroup);
				List<String> topics = new ArrayList<>();
				for (TopicPartition p : offsets.keySet()) {
					if (!topics.contains(p.topic())) {
						topics.add(p.topic());
					}
				}
				model.addAttribute("connectionIdentification", kafkaClusterConnection.get().getIdentification());
				model.addAttribute("consumerGroupState", new ConsumerGroupDescriptionDTO(consumerGroupState));
				model.addAttribute("topics", topics);
			} catch (IOException e) {
				log.error("Unable to Load Kafka client connection. ID = {}, Cause = {}, Message = {}", id, e.getCause(),
						e.getMessage());
				model.addAttribute("message",
						utils.getMessage("kafka.cluster.edit.error", "Error while loading Kafka cluster connection."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_LIST;
			} catch (KafkaIntegrationServiceException e) {
				log.error("Unable to retrieve consumer group information.ConsumerGroup = {}, Cause = {}, Message = {}",
						consumerGroup, e.getCause(), e.getMessage());
				model.addAttribute("message", utils.getMessage("kafka.cluster.group.info.error",
						"kafka.cluster.group.info.error=Error while retrieving consumer group information."));
				model.addAttribute("messageAlertType", "WARNING");
				return REDIRECT_CONN_SHOW + id;
			}
		} else {
			log.error("Error while loading Kafka cluster connection.");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found");
		}
		return "kafkaclusterconnections/consumergroup/edit";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/{connectionId}/consumer/group/{consumerGroup}/offsets")
	public @ResponseBody void setOffsetsForGroupAndTopic(@PathVariable("connectionId") String id,
			@PathVariable("consumerGroup") String consumerGroup, @RequestBody ConsumerGroupOffsetRequest request) {

		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			Properties properties;
			try {
				properties = fromConfigToProperties(kafkaClusterConnection.get());
				KafkaIntegrationService.setConsumerGroupOffsets(properties, request.getTopic(), consumerGroup,
						OffsetSeekType.valueOf(request.getType()), request.getTimestamp());

			} catch (IOException e) {
				log.error(
						"Error processing Kafka properties while setting new offsets to consumer group data. Connection = {}, Topic = {}, ConsumerGroup = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), request.getTopic(), consumerGroup,
						e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error processing Kafka connection properties.");
			} catch (KafkaIntegrationServiceException e) {
				log.error(
						"Error while changing consumer group's offset. Connection = {}, Topic = {}, ConsumerGroup = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), request.getTopic(), consumerGroup,
						e.getCause(), e.getMessage());
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error while changing consumer group's offset.");
			}
		} else {
			log.error("Error while loading Kafka cluster connection.");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found");
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping(value = "/{connectionId}/consumer/group/{consumerGroup}")
	public ResponseEntity<String> deleteConsumerGroup(@PathVariable("connectionId") String id,
			@PathVariable("consumerGroup") String consumerGroup) {
		final Optional<KafkaClusterInstance> kafkaClusterConnection = kafkaClusterService.getById(id);
		if (kafkaClusterConnection.isPresent()) {
			Properties properties;
			try {
				properties = fromConfigToProperties(kafkaClusterConnection.get());
				KafkaIntegrationService.deleteConsumerGroup(properties, consumerGroup);
				return ResponseEntity.status(HttpStatus.OK).body("OK");
			} catch (IOException e) {
				log.error(
						"Error processing Kafka properties while removing consumer group. Connection = {}, ConsumerGroup = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), consumerGroup, e.getCause(), e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Error processing Kafka connection properties.");
			} catch (KafkaIntegrationServiceException e) {
				log.error(
						"Error while removing consumer group. Connection = {}, ConsumerGroup = {}, Cause = {}, Message = {}",
						kafkaClusterConnection.get().getIdentification(), consumerGroup, e.getCause(), e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		} else {
			log.error("Error while loading Kafka cluster connection.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Connection not found");
		}
	}

}
