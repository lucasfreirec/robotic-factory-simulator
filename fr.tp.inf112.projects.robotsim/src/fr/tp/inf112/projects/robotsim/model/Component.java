package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Component implements Figure, Serializable, Runnable {
    
    private static final long serialVersionUID = -5960950869184030220L;

    private String id;

    @JsonBackReference
    private Factory factory;
    
    private PositionedShape positionedShape;
    
    private String name;

    private static final Logger LOGGER = Logger.getLogger(Component.class.getName());
    
    
    protected Component(final Factory factory,
                        final PositionedShape shape,
                        final String name) {
        this.factory = factory;
        this.positionedShape = shape;
        this.name = name;

        if (factory != null) {
            factory.addComponent(this);
        }
    }
    
    public Component() {
        this.factory = null;
        this.positionedShape = null;
        this.name = null;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PositionedShape getPositionedShape() {
        return positionedShape;
    }
    
    @JsonIgnore
    public Position getPosition() {
        return getPositionedShape() == null ? null : getPositionedShape().getPosition();
    }

    protected Factory getFactory() {
        return factory;
    }
    
    @Override
    public void run() {
        while (isSimulationStarted()) {
            behave();

            try {
                Thread.sleep(50);
            }
            catch (final InterruptedException ex) {
                LOGGER.info("Simulation was abruptely interrupted");
            }
        }
    }
    
    @JsonIgnore
    @Override
    public int getxCoordinate() {
        return getPositionedShape() == null ? 0 : getPositionedShape().getxCoordinate();
    }

    protected boolean setxCoordinate(int xCoordinate) {
        if (getPositionedShape() != null && getPositionedShape().setxCoordinate(xCoordinate)) {
            notifyObservers();
            return true;
        }
        return false;
    }
    
    @JsonIgnore
    @Override
    public int getyCoordinate() {
        return getPositionedShape() == null ? 0 : getPositionedShape().getyCoordinate();
    }

    protected boolean setyCoordinate(final int yCoordinate) {
        if (getPositionedShape() != null && getPositionedShape().setyCoordinate(yCoordinate) ) {
            notifyObservers();
            return true;
        }
        return false;
    }

    protected void notifyObservers() {
        if (getFactory() != null) {
            getFactory().notifyObservers();
        }
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
                + ", shape=" + getPositionedShape();
    }
    
    @JsonIgnore
    public int getWidth() {
        return getPositionedShape() == null ? 0 : getPositionedShape().getWidth();
    }
    
    @JsonIgnore
    public int getHeight() {
        return getPositionedShape() == null ? 0 : getPositionedShape().getHeight();
    }
    
    public boolean behave() {
        return false;
    }
    
    @JsonIgnore
    public boolean isMobile() {
        return false;
    }
    
    public boolean overlays(final Component component) {
        return overlays(component.getPositionedShape());
    }
    
    public boolean overlays(final PositionedShape shape) {
        return getPositionedShape() != null && getPositionedShape().overlays(shape);
    }
    
    public boolean canBeOverlayed(final PositionedShape shape) {
        return false;
    }
    
    @JsonIgnore
    @Override
    public Style getStyle() {
        return ComponentStyle.DEFAULT;
    }
    
    @JsonIgnore
    @Override
    public Shape getShape() {
        return getPositionedShape();
    }
    
    @JsonIgnore
    public boolean isSimulationStarted() {
        return getFactory() != null && getFactory().isSimulationStarted();
    }
    
    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public void setPositionedShape(PositionedShape shape) {
        this.positionedShape = shape;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}