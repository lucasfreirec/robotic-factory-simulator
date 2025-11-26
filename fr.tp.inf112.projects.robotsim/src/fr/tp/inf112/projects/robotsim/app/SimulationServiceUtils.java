package fr.tp.inf112.projects.robotsim.app;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Base64;

public class SimulationServiceUtils {

    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "Factory-Simulation-Group";
    
    private static final String AUTO_OFFSET_RESET = "earliest";
    
    private static final String TOPIC_PREFIX = "simulation-topic-";

    public static String getTopicName(String factoryId) {
        String safeId = Base64.getUrlEncoder().encodeToString(factoryId.getBytes());
        return TOPIC_PREFIX + safeId;
    }

    public static Properties getDefaultConsumerProperties() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        return props;
    }
}