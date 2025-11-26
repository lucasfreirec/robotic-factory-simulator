package fr.tp.inf112.projects.robotsim.model;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.app.SimulatorServer;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
	
    private static final Logger LOGGER = Logger.getLogger(SimulatorServer.class.getName());
    final private String serverHost = "localhost";
    final private int serverPort = 8000;

    public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser) {
        super(canvasChooser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas read(final String canvasId)
            throws IOException {
        try (
                Socket socket = new Socket(serverHost, serverPort);
        ) {
            final OutputStream bufOutputStream = new BufferedOutputStream(socket.getOutputStream());
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufOutputStream);
            objectOutputStream.writeObject(canvasId);
            objectOutputStream.flush();

            final InputStream bufInputStream = new BufferedInputStream(socket.getInputStream());
            final ObjectInputStream objectInputStream = new ObjectInputStream(bufInputStream);

            return (Canvas) objectInputStream.readObject();
        }
        catch (ClassNotFoundException | IOException ex) {
            throw new IOException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Canvas canvasModel)
            throws IOException {
        try (
                Socket socket = new Socket(serverHost, serverPort);
        ) {
            LOGGER.info("Connected on persist");
            final OutputStream bufOutputStream = new BufferedOutputStream(socket.getOutputStream());
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufOutputStream);
            objectOutputStream.writeObject(canvasModel);
            objectOutputStream.flush();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(final Canvas canvasModel)
            throws IOException {
            return false;
    }
}
