package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
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
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

public class RemoteSimulatorController extends SimulatorController {

    private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
    
    private static final String SERVICE_URL = "http://localhost:8181/simulation";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private volatile boolean isRunning = false;

    public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
        
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = createConfiguredObjectMapper();
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

    @Override
    public void startAnimation() {
        try {
        	Factory factory = getFactory();
            String factoryId = getFactory().getId();
            LOGGER.info("Auto-saving factory before starting simulation...");
            try {
                getPersistenceManager().persist(factory);
            } catch (Exception e) {
                LOGGER.severe("Auto-Save failed: " + e.getMessage());
            }
            
            String encodedId = Base64.getUrlEncoder().encodeToString(factoryId.getBytes());
            
            LOGGER.info("Starting remote simulation for: " + factoryId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SERVICE_URL + "/start/" + encodedId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                isRunning = true;
                new Thread(() -> updateViewerLoop()).start();
            } else {
                LOGGER.severe("Failed to start remote simulation. Code: " + response.statusCode());
            }

        } catch (Exception e) {
            LOGGER.severe("Error starting animation: " + e.getMessage());
        }
    }

    @Override
    public void stopAnimation() {
        isRunning = false;
        try {
            String factoryId = getFactory().getId();
            String encodedId = Base64.getUrlEncoder().encodeToString(factoryId.getBytes());
            
            LOGGER.info("Stopping remote simulation for: " + factoryId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SERVICE_URL + "/stop/" + encodedId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            LOGGER.severe("Error stopping animation: " + e.getMessage());
        }
    }

    private void updateViewerLoop() {
        while (isRunning) { 
            try {
                String factoryId = getFactory().getId();
                String encodedId = java.util.Base64.getUrlEncoder().encodeToString(factoryId.getBytes());
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(SERVICE_URL + "/" + encodedId))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                
                if (response.statusCode() == 200) {
                    Factory remoteFactory = objectMapper.readValue(response.body(), Factory.class);
                   
                    if (!remoteFactory.isSimulationStarted()) { 
                        isRunning = false;
                        break;
                    }
                    
                    SwingUtilities.invokeLater(() -> setCanvas(remoteFactory));
                }

                Thread.sleep(100);

            } catch (Exception e) {
                LOGGER.severe("Erro no loop: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        Factory currentFactory = getFactory();
        
        if (currentFactory == null) {
            super.setCanvas(canvasModel);
            return;
        }

        final List<Observer> observers = currentFactory.getObservers();
        
        super.setCanvas(canvasModel); 
        
        for (final Observer observer : observers) {
            getFactory().addObserver(observer);
        }
        
        getFactory().notifyObservers();
    }
    
    private Factory getFactory() {
        return (Factory) getCanvas();
    }
}