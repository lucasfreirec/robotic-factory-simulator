package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tp.inf112.projects.robotsim.model.Factory;

@Configuration
public class SimulationServiceConfig {

    @Bean
    public ProducerFactory<String, Factory> producerFactory(ObjectMapper objectMapper) {
        final Map<String, Object> config = new HashMap<>();
        
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                   SimulationServiceUtils.BOOTSTRAP_SERVERS); 

        final JsonSerializer<Factory> factorySerializer = new JsonSerializer<>(objectMapper);

        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                factorySerializer);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Factory> kafkaTemplate(ProducerFactory<String, Factory> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }
}