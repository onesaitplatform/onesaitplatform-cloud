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
package com.minsait.onesait.platform.commons.kafka.integration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.DeleteRecordsResult;
import org.apache.kafka.clients.admin.DeletedRecords;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsOptions;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.minsait.onesait.platform.commons.kafka.integration.KafkaIntegrationServiceException.KafkaIntegrationServiceExceptionElement;
import com.minsait.onesait.platform.commons.kafka.integration.KafkaIntegrationServiceException.KafkaIntegrationServiceExceptionType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class KafkaIntegrationService {

	public enum OffsetSeekType {
		BEGINNING, END, TIMESTAMP;
	}

	private static AdminClient connect(Properties kafkaConfig) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient;
		try {
			kafkaAdminClient = AdminClient.create(kafkaConfig);
			return kafkaAdminClient;
		} catch (Exception e) {
			log.error("Error while connection to cluster. Cause={}, Message={}", e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while connecting to Kafka cluster.", e,
					KafkaIntegrationServiceExceptionElement.CLUSTER, KafkaIntegrationServiceExceptionType.CONNECTION);
		}

	}

	public static Collection<Node> getClusterInfo(Properties kafkaConfig) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			return kafkaAdminClient.describeCluster().nodes().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while accessing cluster info. Cause={}, Message={}", e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while accessing cluster info.", e,
					KafkaIntegrationServiceExceptionElement.CLUSTER, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static Set<String> listTopics(Properties kafkaConfig) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		ListTopicsResult result = kafkaAdminClient.listTopics();
		try {
			return result.names().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving topic list. Cause={}, Message={}", e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving topic list.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.LIST);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static Collection<ConfigEntry> getTopicConfig(Properties kafkaConfig, String topic)
			throws KafkaIntegrationServiceException {
		// GET topic configs
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		Collection<ConfigEntry> resourceConfig = new ArrayList<>();
		try {
			ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
			DescribeConfigsResult resConf = kafkaAdminClient.describeConfigs(Collections.singleton(resource));
			// only one resource is being asked for
			for (Entry<ConfigResource, Config> ent : resConf.all().get().entrySet()) {
				resourceConfig = ent.getValue().entries();
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while accessing Topic configuration info. Topic={}, Cause={}, Message={}", topic,
					e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while accessing Topic configuration info.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
		return resourceConfig;
	}

	public static TopicDescription getTopicPartitionInfo(Properties kafkaConfig, String topic)
			throws KafkaIntegrationServiceException {
		// Get topic partitios and repicas
		AdminClient kafkaAdminClient = connect(kafkaConfig);

		try {
			return getTopicPartitionInfo(kafkaAdminClient, topic);
		} finally {
			kafkaAdminClient.close();
		}

	}

	private static TopicDescription getTopicPartitionInfo(AdminClient kafkaAdminClient, String topic)
			throws KafkaIntegrationServiceException {
		List<String> topics = new ArrayList<String>();
		topics.add(topic);
		return getTopicsPartitionInfo(kafkaAdminClient, topics).get(topic);
	}

	private static Map<String, TopicDescription> getTopicsPartitionInfo(AdminClient kafkaAdminClient,
			List<String> topics) throws KafkaIntegrationServiceException {
		DescribeTopicsOptions opts = new DescribeTopicsOptions();
		opts.includeAuthorizedOperations(true);
		DescribeTopicsResult res = kafkaAdminClient.describeTopics(topics, opts);
		Map<String, TopicDescription> desc;
		try {
			desc = res.all().get();
			return desc;
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while accessing Topic list partition info. Cause={}, Message={}", e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while accessing Topic partition info.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.GETINFO);
		}

	}

	public static Map<TopicPartition, ListOffsetsResultInfo> getTopicOffsets(Properties kafkaConfig, String topic)
			throws KafkaIntegrationServiceException {
		// Get topic partitions offsets
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			ListOffsetsResult result = getOffsetForTopic(kafkaAdminClient, topic);
			return result.all().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving Topic offsets info. Topic={}, Cause={}, Message={}", topic, e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving Topic offsets info.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static Map<TopicPartition, ListOffsetsResultInfo> getTopicsOffsets(Properties kafkaConfig,
			List<String> topics) throws KafkaIntegrationServiceException {
		// Get topic partitions offsets
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			Map<TopicPartition, ListOffsetsResultInfo> result = new HashMap<>();
			for (String topic : topics) {
				result.putAll(getOffsetForTopic(kafkaAdminClient, topic).all().get());
			}
			return result;
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving Topic List offsets info. Cause={}, Message={}", e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving Topic offsets info.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
	}

	private static ListOffsetsResult getOffsetForTopic(AdminClient kafkaAdminClient, String topic)
			throws KafkaIntegrationServiceException {
		// Get topic partitions
		TopicDescription topicDescription = getTopicPartitionInfo(kafkaAdminClient, topic);
		Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
		// Set offset target to latest
		for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
			topicPartitionOffsets.put(new TopicPartition(topic, partitionInfo.partition()), OffsetSpec.latest());
		}
		return kafkaAdminClient.listOffsets(topicPartitionOffsets);
	}

	public static void purgeTopicData(Properties kafkaConfig, String topic) throws KafkaIntegrationServiceException {
		// Delete Records
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			Map<TopicPartition, RecordsToDelete> recordsToDelete = new HashMap<>();
			ListOffsetsResult result = getOffsetForTopic(kafkaAdminClient, topic);
			for (Entry<TopicPartition, ListOffsetsResultInfo> ent : result.all().get().entrySet()) {
				recordsToDelete.put(ent.getKey(), RecordsToDelete.beforeOffset(ent.getValue().offset()));
			}
			DeleteRecordsResult deletionResult = kafkaAdminClient.deleteRecords(recordsToDelete);
			Map<TopicPartition, KafkaFuture<DeletedRecords>> lowWatermarks = deletionResult.lowWatermarks();
			for (Map.Entry<TopicPartition, KafkaFuture<DeletedRecords>> entry : lowWatermarks.entrySet()) {
				if (log.isDebugEnabled()) {
					log.debug("Topic:{}, Partition:{}, lowerWatermark:{}", entry.getKey().topic(),
						entry.getKey().partition(), entry.getValue().get().lowWatermark());
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while deleting data from Topic. Topic={}, Cause={}, Message={}", topic, e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while deleting data from Topic.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.PURGE);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static Collection<ConsumerGroupListing> listConsumerGroups(Properties kafkaConfig)
			throws KafkaIntegrationServiceException {
		// get all consumer groups
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			return kafkaAdminClient.listConsumerGroups().all().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving consumer groups. Cause={}, Message={}", e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving consumer groups.", e,
					KafkaIntegrationServiceExceptionElement.GROUP, KafkaIntegrationServiceExceptionType.LIST);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static ConsumerGroupDescription getConsumerGroupState(Properties kafkaConfig, String consumerGroup)
			throws KafkaIntegrationServiceException {
		// See if consumerGroup is active and consumers from the group. State refletcs
		// if active (stable) or not running (empty)
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		List<String> groups = new ArrayList<>();
		groups.add(consumerGroup);
		try {
			return kafkaAdminClient.describeConsumerGroups(groups).all().get().get(consumerGroup);
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving consumer group state. ConsumerGroup={}, Cause={}, Message={}",
					consumerGroup, e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving consumer group state.", e,
					KafkaIntegrationServiceExceptionElement.GROUP, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static Map<TopicPartition, OffsetAndMetadata> getConsumerGroupOffsets(Properties kafkaConfig,
			String consumerGroup) throws KafkaIntegrationServiceException {
		// gets per partition consumer offset
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			return kafkaAdminClient.listConsumerGroupOffsets(consumerGroup).partitionsToOffsetAndMetadata().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while retrieving consumer group offsets. ConsumerGroup={}, Cause={}, Message={}",
					consumerGroup, e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while retrieving consumer group offsets.", e,
					KafkaIntegrationServiceExceptionElement.GROUP, KafkaIntegrationServiceExceptionType.GETINFO);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static void createTopic(Properties kafkaConfig, String topicName, int partitions, short replicas,
			Map<String, String> properties) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			NewTopic topic = new NewTopic(topicName, partitions, replicas);
			topic.configs(properties);
			kafkaAdminClient.createTopics(Collections.singleton(topic));
		} catch (Exception e) {
			log.error("Error while Creating topic. Topic={}, Cause={}, Message={}", topicName, e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while creating topic.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.CREATE);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static void updateTopic(Properties kafkaConfig, String topicName, int partitions, short replicas,
			Map<String, String> properties) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

			Collection<AlterConfigOp> updateConfig = new ArrayList<>();
			for (Entry<String, String> entry : properties.entrySet()) {
				ConfigEntry singleConfigEntry = new ConfigEntry(entry.getKey(), entry.getValue());

				AlterConfigOp alterConfOp = new AlterConfigOp(singleConfigEntry, AlterConfigOp.OpType.SET);
				updateConfig.add(alterConfOp);
			}
			// search for other manual set properties that are not in the incoming alter
			// config. If there are any, use DELETE to unset them
			DescribeConfigsResult resConf = kafkaAdminClient.describeConfigs(Collections.singleton(resource));
			for (Entry<ConfigResource, Config> ent : resConf.all().get().entrySet()) {
				for (ConfigEntry c : ent.getValue().entries()) {
					if (!c.isDefault() && properties.get(c.name()) == null) {
						AlterConfigOp alterConfOp = new AlterConfigOp(c, AlterConfigOp.OpType.DELETE);
						updateConfig.add(alterConfOp);
					}
				}
			}

			Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>(1);
			configs.put(resource, updateConfig);
			// SET new properties
			kafkaAdminClient.incrementalAlterConfigs(configs);
			// change Partitions
			Map<String, NewPartitions> counts = new HashMap<>();
			counts.put(topicName, NewPartitions.increaseTo(partitions));
			kafkaAdminClient.createPartitions(counts);

		} catch (Exception e) {
			log.error("Error while Updating topic. Topic={}, Cause={}, Message={}", topicName, e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while updating topic.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.UPDATE);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static void deleteTopic(Properties kafkaConfig, String topicName) throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			kafkaAdminClient.deleteTopics(Collections.singletonList(topicName));
		} catch (Exception e) {
			log.error("Error while deleting topic. Topic={}, Cause={}, Message={}", topicName, e.getCause(),
					e.getMessage());
			throw new KafkaIntegrationServiceException("Error while deleting topic.", e,
					KafkaIntegrationServiceExceptionElement.TOPIC, KafkaIntegrationServiceExceptionType.DELETE);
		} finally {
			kafkaAdminClient.close();
		}
	}

	public static void setConsumerGroupOffsets(Properties kafkaConfig, String topicName, String consumerGroupName,
			OffsetSeekType offsetSeekType, Long timestampMillis) throws KafkaIntegrationServiceException {
		// get partitions for that topic
		Map<TopicPartition, OffsetSpec> requestOffsets = new HashMap<>();
		Map<TopicPartition, OffsetAndMetadata> currentOffsets = getConsumerGroupOffsets(kafkaConfig, consumerGroupName);
		for (TopicPartition p : currentOffsets.keySet()) {
			if (p.topic().equals(topicName)) {

				switch (offsetSeekType) {
				case BEGINNING:
					requestOffsets.put(p, OffsetSpec.earliest());
					break;
				case TIMESTAMP:
					requestOffsets.put(p, OffsetSpec.forTimestamp(timestampMillis));
					break;
				case END:
				default:
					requestOffsets.put(p, OffsetSpec.latest());
					break;
				}
			}
		}
		AdminClient kafkaAdminClient = connect(kafkaConfig);

		try {
			Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> newOffsets = kafkaAdminClient
					.listOffsets(requestOffsets).all().get();
			Map<TopicPartition, OffsetAndMetadata> resetOffsets = new HashMap<>();
			for (Entry<TopicPartition, ListOffsetsResultInfo> entry : newOffsets.entrySet()) {
				if (log.isDebugEnabled()) {
						log.debug("Will reset topic-partition {} to offset {}", entry.getKey(), entry.getValue().offset());
				}
				long offset = entry.getValue().offset() + 1;

				resetOffsets.put(entry.getKey(), new OffsetAndMetadata(offset));
			}
			kafkaAdminClient.alterConsumerGroupOffsets(consumerGroupName, resetOffsets).all().get();
		} catch (ExecutionException | InterruptedException e) {
			log.error(
					"Error while reseting consumer group offsets to topic. Group = {}, Topic={}, Cause={}, Message={}",
					consumerGroupName, topicName, e.getCause(), e.getMessage());
			throw new KafkaIntegrationServiceException("Error while reseting consumer group offsets to topic.", e,
					KafkaIntegrationServiceExceptionElement.GROUP, KafkaIntegrationServiceExceptionType.UPDATE);
		} finally {
			kafkaAdminClient.close();
		}
	}

	/*
	 * KAFKA CONSUMER
	 */

	private static Consumer<String, String> createConsumer(Properties kafkaConfig, String topic,
			OffsetSeekType offsetSeekType, Long timestampMillis) {
		// Create the consumer using props.
		final Consumer<String, String> consumer = new KafkaConsumer<>(kafkaConfig);
		// Subscribe to the topic.
		consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceListener() {

			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
			}

			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
				switch (offsetSeekType) {
				case BEGINNING:
					consumer.seekToBeginning(partitions);
					break;
				case TIMESTAMP:
					Map<TopicPartition, Long> partitionsTimestamp = new HashMap<>();
					for (TopicPartition p : partitions) {
						partitionsTimestamp.put(p, timestampMillis);
					}
					Map<TopicPartition, OffsetAndTimestamp> newOffsets = consumer.offsetsForTimes(partitionsTimestamp);
					for (Entry<TopicPartition, OffsetAndTimestamp> entry : newOffsets.entrySet()) {
						if (entry.getValue() != null) {
							consumer.seek(entry.getKey(), entry.getValue().offset());
						}
					}
					break;
				default:
				case END:
					consumer.seekToEnd(partitions);
					break;
				}
			}
		});
		return consumer;
	}

	public static List<ConsumerRecord<String, String>> readRecordsFromTopic(Properties kafkaConfig, String topic,
			int numRecords, OffsetSeekType offsetSeekType, Long timestampMillis, Long timeout)
			throws KafkaIntegrationServiceException {
		kafkaConfig.put("enable.auto.commit", "true");
		kafkaConfig.put("auto.commit.interval.ms", "1000");
		kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "controlPanelConsumer");
		kafkaConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
		kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		Consumer<String, String> consumer = createConsumer(kafkaConfig, topic, offsetSeekType, timestampMillis);
		long pollTimeout = 200;
		try {
			Collection<TopicPartition> partitions = new ArrayList<>();

			TopicDescription topicDesc = getTopicPartitionInfo(kafkaConfig, topic);
			for (TopicPartitionInfo partitionInfo : topicDesc.partitions()) {
				TopicPartition p = new TopicPartition(topic, partitionInfo.partition());
				partitions.add(p);
			}
			// Dummy poll to force lazy partition assignment
			List<ConsumerRecord<String, String>> result = new ArrayList<>();

			int retries = 0;
			// Loop until data is found or timeout has elapsed
			// This is required as rebalance is async and there is no way to know when it
			// ends
			while (result.size() < numRecords && pollTimeout * retries < timeout) {
				retries++;
				ConsumerRecords<String, String> newRecords = consumer.poll(Duration.ofMillis(pollTimeout));

				if (!newRecords.isEmpty()) {
					// Add new read records to the total
					for (TopicPartition p : newRecords.partitions()) {
						for (ConsumerRecord<String, String> r : newRecords.records(p)) {
							result.add(r);
							if (result.size() >= numRecords) {
								break;
							}
						}
						if (result.size() >= numRecords) {
							break;
						}
					}
				}

			}
			return result;
		} catch (Exception e) {
			throw new KafkaIntegrationServiceException("Error while reading data from Kafka", e,
					KafkaIntegrationServiceExceptionElement.CONSUMER, KafkaIntegrationServiceExceptionType.CREATE);
		} finally {
			consumer.close();
		}
	}

	/*
	 * KAFKA PRODUCER
	 */
	private static Producer<String, String> createProducer(Properties kafkaConfig) {
		kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");
		Producer<String, String> producer = new KafkaProducer<>(kafkaConfig);
		return producer;
	}

	public static void insertDataInTopic(Properties kafkaConfig, String topic, String key, String value)
			throws KafkaIntegrationServiceException {

		Producer<String, String> producer = createProducer(kafkaConfig);

		try {
			ProducerRecord<String, String> record;
			if (key == null) {
				record = new ProducerRecord<>(topic, value);
			} else {
				record = new ProducerRecord<>(topic, key, value);
			}

			producer.send(record);
		} catch (Exception e) {
			throw new KafkaIntegrationServiceException("Error while reading data from Kafka", e,
					KafkaIntegrationServiceExceptionElement.PRODUCER, KafkaIntegrationServiceExceptionType.CREATE);
		} finally {
			producer.close();
		}
	}

	public static void deleteConsumerGroup(Properties kafkaConfig, String group)
			throws KafkaIntegrationServiceException {
		AdminClient kafkaAdminClient = connect(kafkaConfig);
		try {
			KafkaFuture<Void> resultFuture = kafkaAdminClient.deleteConsumerGroups(Collections.singleton(group)).all();
			resultFuture.get();
		} catch (Exception e) {
			throw new KafkaIntegrationServiceException(e.getMessage(), e, KafkaIntegrationServiceExceptionElement.GROUP,
					KafkaIntegrationServiceExceptionType.DELETE);
		} finally {
			kafkaAdminClient.close();
		}
	}
}
