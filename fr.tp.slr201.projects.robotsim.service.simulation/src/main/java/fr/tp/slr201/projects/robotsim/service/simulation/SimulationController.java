package fr.tp.slr201.projects.robotsim.service.simulation;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

    private static final Logger LOGGER = Logger.getLogger(SimulationController.class.getName());
    private final Map<String, Factory> activeSimulations = new ConcurrentHashMap<>();

    private String decodeId(String encodedId) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedId);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Failed to decode ID, using original: " + encodedId);
            return encodedId;
        }
    }

    @PostMapping("/start/{id}")
    public boolean startSimulation(@PathVariable("id") String encodedId) {
        String id = decodeId(encodedId);
        
        LOGGER.info("Request to start factory simulation for file: " + id);

        if (activeSimulations.containsKey(id)) {
            return true;
        }

        try {
            RemoteFactoryPersistenceManager persistenceManager = new RemoteFactoryPersistenceManager(null);
            
            Factory factory = (Factory) persistenceManager.read(id);

            if (factory == null) {
                LOGGER.severe("Factory not found via Persistence Manager.");
                return false;
            }

            factory.setId(id);
            
            fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder pathFinder = 
                new fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder(factory, 5);
            
            pathFinder.buildGraph(); 
            
            for (fr.tp.inf112.projects.robotsim.model.Component c : factory.getComponents()) {
                if (c instanceof fr.tp.inf112.projects.robotsim.model.Robot) {
                    ((fr.tp.inf112.projects.robotsim.model.Robot) c).setPathFinder(pathFinder);
                }
            }

            factory.startSimulation(); 
            
            activeSimulations.put(id, factory);
            
            LOGGER.info("Simulation started successfully.");
            return true;

        } catch (IOException e) {
            LOGGER.severe("Error communicating with persistence server: " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/{id}")
    public Factory getSimulation(@PathVariable("id") String encodedId) {
        String id = decodeId(encodedId);
        return activeSimulations.get(id);
    }

    @PostMapping("/stop/{id}")
    public boolean stopSimulation(@PathVariable("id") String encodedId) {
        String id = decodeId(encodedId);
        LOGGER.info("Request to stop factory simulation: " + id);

        Factory factory = activeSimulations.remove(id);
        
        if (factory == null) {
            return false;
        }

        factory.stopSimulation();          
        return true;
    }
}