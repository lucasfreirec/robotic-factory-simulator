package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import fr.tp.inf112.projects.canvas.model.Color;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.Stroke;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComponentStyle implements Style, Stroke, Serializable {
    
    private static final long serialVersionUID = -782486828505388494L;

    public static final ComponentStyle DEFAULT = new ComponentStyle();

    public static final ComponentStyle DEFAULT_BLACK = new ComponentStyle(RGBColor.BLACK, null, (short) -1, null);

    public static final ComponentStyle DEFAULT_WHITE = new ComponentStyle(RGBColor.WHITE, null, (short) -1, null);

    public static final ComponentStyle DEFAULT_GREEN = new ComponentStyle(RGBColor.GREEN, null, (short) -1, null);

    public static final ComponentStyle DEFAULT_RED = new ComponentStyle(RGBColor.GREEN, null, (short) -1, null);

    public final static float[] DEFAULT_DASH_PATTERN = {4.0f};

    private Color backgroundColor;
    
    private Color lineColor;

    private float lineThickness;
    
    final float[] dashPattern;
    
    public ComponentStyle() {
        this(null);
    }

    public ComponentStyle(final float lineThickness) {
        this(null, null, lineThickness, null);
    }

    public ComponentStyle(final float[] dashPattern) {
        this(null, null, 1.0f, dashPattern);
    }

    public ComponentStyle(final Color backgroundColor,
                          final Color lineColor,
                          final float lineThickness,
                          final float[] dashPattern) {
        this.backgroundColor = backgroundColor;
        this.lineColor = lineColor;
        this.lineThickness = lineThickness;
        this.dashPattern = dashPattern == null ? null : dashPattern.clone();
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    @JsonIgnore 
    public Color getColor() {
        return lineColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    @Override
    @JsonIgnore
    public Stroke getStroke() {
        return this;
    }

    @Override
    @JsonIgnore
    public float getThickness() {
        return lineThickness;
    }
    
    public float getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }

    @Override
    public float[] getDashPattern() {
        return dashPattern;
    }
}