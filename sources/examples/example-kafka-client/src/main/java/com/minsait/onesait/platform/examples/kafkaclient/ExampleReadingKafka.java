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
package com.minsait.onesait.platform.examples.kafkaclient;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ExampleReadingKafka {

    private final static String TOPIC = "ONTOLOGY_EXAMPLE_KAFKA";
    private final static String BOOTSTRAP_SERVERS = "localhost:9095";
    
    //different from the consumer group of the platform to avoid conflicts.
    private final static String KAFKA_CONSUMER_GROUP = "myExampleConsumerGroup";
    
    private final static String TOKEN = "02148604a7ed4a4986c973513d35cca3";
    private final static String DEVICETEMPLATE = "KafkaInserts";
    
    private static Consumer<String, String> createConsumer() {
        final Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_CONSUMER_GROUP);
        config.put("security.protocol", "SASL_PLAINTEXT");
        config.put("sasl.mechanism", "PLAIN");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + DEVICETEMPLATE + "\" password=\"" + TOKEN + "\";");config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(config);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }
    
    static void runConsumer() throws InterruptedException {
        final Consumer<String, String> consumer = createConsumer();

        final int giveUp = 350;   
        int noRecordsCount = 0;
        
        

        while (true) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
            
            for (ConsumerRecord<String, String> record: consumerRecords) {                
                System.out.printf("Consumer Record:(%d, %d, %s, %s)\n",
                        record.partition(), record.offset(),
                        record.key(), record.value() );
                noRecordsCount++;                
            }            

            consumer.commitAsync();
            if (noRecordsCount >= giveUp) {
                break;
            }
        }
        
    }
    
    public static void main(String[] args) throws InterruptedException {
        runConsumer();
    }
    
}
