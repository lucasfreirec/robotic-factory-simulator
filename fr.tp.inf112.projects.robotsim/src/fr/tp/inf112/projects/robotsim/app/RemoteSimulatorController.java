package fr.tp.inf112.projects.robotsim.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.LocalFactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

public class RemoteSimulatorController extends SimulatorController {

    private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
    private static final String SERVICE_URL = "http://localhost:8181/simulation";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LocalFactoryModelChangedNotifier controllerNotifier;
    
    private FactorySimulationEventConsumer kafkaConsumer; 
    private volatile boolean simulationRunning = false;

    public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = createConfiguredObjectMapper();
        this.controllerNotifier = new LocalFactoryModelChangedNotifier();
    }

    private ObjectMapper createConfiguredObjectMapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(java.util.ArrayList.class.getName())
                .allowIfSubType(java.util.LinkedHashSet.class.getName())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
        
        return mapper;
    }

    public String getFactoryId() {
        return getFactory().getId();
    }

    @Override
    public boolean isAnimationRunning() {
        return this.simulationRunning;
    }

    @Override
    public boolean addObserver(Observer observer) {
        return controllerNotifier.addObserver(observer);
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return controllerNotifier.removeObserver(observer);
    }

    @Override
    public void startAnimation() {
        try {
            Factory factory = getFactory();            
            String factoryId = factory.getId();
            
            if (factoryId == null || factoryId.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                        "You need to save the factory before starting a simulation", 
                        "Warning", 
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                LOGGER.warning("You need to save the factory before starting a simulation .");
                return;
            }
            
            String encodedId = Base64.getUrlEncoder().withoutPadding().encodeToString(factoryId.getBytes());
            
            LOGGER.info("Starting remote simulation via HTTP Trigger: " + factoryId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SERVICE_URL + "/start/" + encodedId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                this.simulationRunning = true;
                LOGGER.info("Server started. Connecting to Kafka...");
                
                new Thread(() -> {
                    kafkaConsumer = new FactorySimulationEventConsumer(this, objectMapper);
                    kafkaConsumer.consumeMessages();
                }).start();
                
            } else {
                LOGGER.severe("Failed to start remote simulation: " + response.statusCode());
            }

        } catch (Exception e) {
            LOGGER.severe("Error starting animation: " + e.getMessage());
        }
    }

    @Override
    public void stopAnimation() {
        this.simulationRunning = false;
        try {
            String factoryId = getFactory().getId();
            String encodedId = Base64.getUrlEncoder().encodeToString(factoryId.getBytes());
            
            LOGGER.info("Stopping remote simulation...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SERVICE_URL + "/stop/" + encodedId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            LOGGER.severe("Error stopping animation: " + e.getMessage());
        }
    }

    public void updateModelFromKafka(Factory remoteFactory) {
        SwingUtilities.invokeLater(() -> {
            try {
                setCanvas(remoteFactory);
            } catch (Exception e) {
                LOGGER.severe("Erro UI: " + e.getMessage());
            }
        });
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        super.setCanvas(canvasModel);
        controllerNotifier.notifyObservers();
    }
    
    private Factory getFactory() {
        return (Factory) getCanvas();
    }
}