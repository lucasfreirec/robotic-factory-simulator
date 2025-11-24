package fr.tp.inf112.projects.robotsim.app;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class SimulatorServer {
    private static final Logger LOGGER = Logger.getLogger(SimulatorServer.class.getName());

    public static void main(String args[]) {
        try (
                ServerSocket serverSocket = new ServerSocket(8000);
        ) {
            System.out.println("Server started ");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    LOGGER.info("Connection accepted");
                    InputStream buffInput = new BufferedInputStream(clientSocket.getInputStream());
                    OutputStream buffOutput = new BufferedOutputStream(clientSocket.getOutputStream());

                    ObjectInputStream objInStream = new ObjectInputStream(buffInput);
                    ObjectOutputStream objOutStream = new ObjectOutputStream(buffOutput);

                    Object unkObj = objInStream.readObject();

                    FactoryPersistenceManager persistenceManager = new FactoryPersistenceManager(null);
                    if (unkObj instanceof String) {
                        LOGGER.info("Read option choosed");
                        Canvas data = persistenceManager.read((String) unkObj);
                        objOutStream.writeObject(data);
                        objOutStream.flush();
                    } else if (unkObj instanceof Factory) {
                        LOGGER.info("Persist option choosed");
                        persistenceManager.persist((Factory) unkObj);
                    } else {
                        LOGGER.info("The type of object sent to the server is invalid.");
                    }

                    System.out.println("Closing connection.");
                }
                catch (IOException ex) {
                    LOGGER.severe("Server communication problem: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (ClassNotFoundException e) {
                    LOGGER.info("Deserialization Class not found.");
                }
            }
        }
        catch (IOException ex) {
            LOGGER.info("Server could not be started.");
        }
    }
}

