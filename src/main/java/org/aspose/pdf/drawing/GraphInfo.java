package org.aspose.pdf.drawing;

import org.aspose.pdf.Color;

import java.util.logging.Logger;

/**
 * Represents graphic styling properties for drawing shapes.
 * <p>
 * Holds stroke color, fill color, line width, dash pattern, rotation,
 * scaling, and skew parameters that control how a {@link Shape} is rendered
 * in the PDF content stream.
 * </p>
 */
public class GraphInfo {

    private static final Logger LOG = Logger.getLogger(GraphInfo.class.getName());

    private Color color;
    private Color fillColor;
    private float lineWidth = 1.0f;
    private float[] dashArray;
    private float dashPhase;
    private float rotationAngle;
    private boolean isDoubled;
    private float scalingRateX = 1.0f;
    private float scalingRateY = 1.0f;
    private float skewAngleX;
    private float skewAngleY;

    /**
     * Creates a new {@code GraphInfo} with default styling (line width 1.0, no dash, no fill).
     */
    public GraphInfo() {
        // defaults applied by field initializers
    }

    /**
     * Gets the stroke (outline) color.
     *
     * @return the stroke color, or {@code null} if not set
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the stroke (outline) color.
     *
     * @param color the stroke color to use
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the fill color for closed shapes.
     *
     * @return the fill color, or {@code null} if not set
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets the fill color for closed shapes.
     *
     * @param fillColor the fill color to use
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Gets the line width in user-space units.
     *
     * @return the line width; defaults to 1.0
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the line width in user-space units.
     *
     * @param lineWidth the line width to use
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Gets the dash pattern array.
     * <p>
     * The array specifies alternating dash and gap lengths as described in
     * ISO 32000-1:2008 section 8.4.3.6.
     * </p>
     *
     * @return the dash array, or {@code null} for a solid line
     */
    public float[] getDashArray() {
        return dashArray;
    }

    /**
     * Sets the dash pattern array.
     *
     * @param dashArray alternating dash and gap lengths, or {@code null} for solid
     */
    public void setDashArray(float[] dashArray) {
        this.dashArray = dashArray;
    }

    /**
     * Gets the dash phase offset.
     *
     * @return the dash phase in user-space units
     */
    public float getDashPhase() {
        return dashPhase;
    }

    /**
     * Sets the dash phase offset.
     *
     * @param dashPhase the dash phase in user-space units
     */
    public void setDashPhase(float dashPhase) {
        this.dashPhase = dashPhase;
    }

    /**
     * Gets the rotation angle in degrees.
     *
     * @return the rotation angle
     */
    public float getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Sets the rotation angle in degrees.
     *
     * @param rotationAngle the rotation angle to apply
     */
    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    /**
     * Returns whether the line is rendered doubled.
     *
     * @return {@code true} if the line should be drawn with a doubled style
     */
    public boolean isDoubled() {
        return isDoubled;
    }

    /**
     * Sets whether the line should be rendered doubled.
     *
     * @param doubled {@code true} to draw a doubled line
     */
    public void setDoubled(boolean doubled) {
        this.isDoubled = doubled;
    }

    /**
     * Gets the horizontal scaling rate.
     *
     * @return the X scaling rate; defaults to 1.0
     */
    public float getScalingRateX() {
        return scalingRateX;
    }

    /**
     * Sets the horizontal scaling rate.
     *
     * @param scalingRateX the X scaling rate
     */
    public void setScalingRateX(float scalingRateX) {
        this.scalingRateX = scalingRateX;
    }

    /**
     * Gets the vertical scaling rate.
     *
     * @return the Y scaling rate; defaults to 1.0
     */
    public float getScalingRateY() {
        return scalingRateY;
    }

    /**
     * Sets the vertical scaling rate.
     *
     * @param scalingRateY the Y scaling rate
     */
    public void setScalingRateY(float scalingRateY) {
        this.scalingRateY = scalingRateY;
    }

    /**
     * Gets the horizontal skew angle in degrees.
     *
     * @return the X skew angle
     */
    public float getSkewAngleX() {
        return skewAngleX;
    }

    /**
     * Sets the horizontal skew angle in degrees.
     *
     * @param skewAngleX the X skew angle
     */
    public void setSkewAngleX(float skewAngleX) {
        this.skewAngleX = skewAngleX;
    }

    /**
     * Gets the vertical skew angle in degrees.
     *
     * @return the Y skew angle
     */
    public float getSkewAngleY() {
        return skewAngleY;
    }

    /**
     * Sets the vertical skew angle in degrees.
     *
     * @param skewAngleY the Y skew angle
     */
    public void setSkewAngleY(float skewAngleY) {
        this.skewAngleY = skewAngleY;
    }
}
