package fr.tp.inf112.projects.robotsim.app;

import java.time.Duration;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tp.inf112.projects.robotsim.model.Factory;

public class FactorySimulationEventConsumer {

    private static final Logger LOGGER = Logger.getLogger(FactorySimulationEventConsumer.class.getName());
    
    private final KafkaConsumer<String, String> consumer;
    private final RemoteSimulatorController controller;
    private final ObjectMapper objectMapper;

    public FactorySimulationEventConsumer(RemoteSimulatorController controller, ObjectMapper objectMapper) {
        this.controller = controller;
        this.objectMapper = objectMapper;

        this.consumer = new KafkaConsumer<>(SimulationServiceUtils.getDefaultConsumerProperties());
    }

    public void consumeMessages() {
        try {
            String factoryId = controller.getFactoryId(); 
            String topicName = SimulationServiceUtils.getTopicName(factoryId);
            
            LOGGER.info("Starting Kafka consumer on topic: " + topicName);
            
            consumer.subscribe(Collections.singletonList(topicName));

            while (controller.isAnimationRunning()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        String json = record.value();
                        
                        Factory remoteFactory = objectMapper.readValue(json, Factory.class);
                        
                        controller.updateModelFromKafka(remoteFactory);
                        
                    } catch (Exception e) {
                        LOGGER.severe(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        } finally {
            consumer.close();
            LOGGER.info("Kafka Consumer ended.");
        }
    }
}