package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

public class KafkaFactoryModelChangeNotifier implements FactoryModelChangedNotifier {

    private final Factory factoryModel;
    private final KafkaTemplate<String, Factory> simulationEventTemplate;

    public KafkaFactoryModelChangeNotifier(Factory factoryModel, 
                                           KafkaTemplate<String, Factory> simulationEventTemplate) {
        this.factoryModel = factoryModel;
        this.simulationEventTemplate = simulationEventTemplate;
        
        TopicBuilder.name(SimulationServiceUtils.getTopicName(factoryModel)).build();
    }

    @Override
    public void notifyObservers() {
    	String topicName = SimulationServiceUtils.getTopicName(factoryModel);
    	
        final Message<Factory> factoryMessage = MessageBuilder
                .withPayload(factoryModel)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .build();

        final CompletableFuture<SendResult<String, Factory>> sendResult = 
                simulationEventTemplate.send(factoryMessage);

        sendResult.whenComplete((result, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public boolean addObserver(Observer observer) {
        return false;
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return false;
    }

    @Override
    public List<Observer> getObservers() {
        return Collections.emptyList();
    }
}