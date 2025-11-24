package fr.tp.inf112.projects.robotsim.model.shapes;

import fr.tp.inf112.projects.canvas.model.OvalShape;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CircularShape extends PositionedShape implements OvalShape {
	
	private static final long serialVersionUID = -1912941556210518344L;

	private int radius;
	
	public CircularShape(final int xCoordinate,
						 final int yCoordinate,
						 final int radius) {
		super( xCoordinate, yCoordinate );
		
		this.radius = radius;
	}
	
	public CircularShape() {
		super();
		this.radius = 0;
    }
	
    @JsonIgnore
	@Override
	public int getWidth() {
		return 2 * radius;
	}

    @JsonIgnore
	@Override
	public int getHeight() {
		return getWidth();
	}

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

	@Override
	public String toString() {
		return super.toString() + " [radius=" + radius + "]";
	}
}