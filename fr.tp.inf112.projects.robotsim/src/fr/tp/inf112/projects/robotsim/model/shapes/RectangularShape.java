package fr.tp.inf112.projects.robotsim.model.shapes;

import fr.tp.inf112.projects.canvas.model.RectangleShape;

public class RectangularShape extends PositionedShape implements RectangleShape {
	
	private static final long serialVersionUID = -6113167952556242089L;

	private int width;

	private int heigth;

	public RectangularShape(final int xCoordinate,
							final int yCoordinate,
							final int width,
							final int heigth) {
		super(xCoordinate, yCoordinate);
	
		this.width = width;
		this.heigth = heigth;
	}
	
    public RectangularShape() {
        super();
        this.width = 0;
        this.heigth = 0;
    }
    
	@Override
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
        this.width = width;
    }

	@Override
	public int getHeight() {
		return heigth;
	}
	
	public void setHeight(int heigth) {
        this.heigth = heigth;
    }

	@Override
	public String toString() {
		return super.toString() + " [width=" + width + ", heigth=" + heigth + "]";
	}
}