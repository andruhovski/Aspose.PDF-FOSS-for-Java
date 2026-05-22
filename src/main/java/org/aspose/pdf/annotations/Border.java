package org.aspose.pdf.annotations;

/**
 * Represents the border of an annotation or form field (ISO 32000-1:2008, §12.5.4).
 */
public class Border {
    private Annotation parent;
    private double width = 1;
    private BorderStyle style = BorderStyle.Solid;
    private int[] dash;

    /**
     * Constructs a border for the specified annotation.
     *
     * @param parent the parent annotation (may be null)
     */
    public Border(Annotation parent) {
        this.parent = parent;
    }

    /**
     * Returns the border width in points.
     *
     * @return the border width
     */
    public double getWidth() { return width; }

    /**
     * Sets the border width in points.
     *
     * @param width the border width
     */
    public void setWidth(double width) {
        this.width = width;
        syncParent();
    }

    /**
     * Returns the border style.
     *
     * @return the border style
     */
    public BorderStyle getStyle() { return style; }

    /**
     * Sets the border style.
     *
     * @param style the border style
     */
    public void setStyle(BorderStyle style) {
        this.style = style;
        syncParent();
    }

    /**
     * Returns the dash pattern array for dashed borders.
     *
     * @return the dash pattern, or null if not set
     */
    public int[] getDash() { return dash; }

    /**
     * Sets the dash pattern array for dashed borders.
     *
     * @param dash the dash pattern array
     */
    public void setDash(int[] dash) {
        this.dash = dash;
        syncParent();
    }

    private void syncParent() {
        if (parent != null) {
            parent.setBorder(this);
        }
    }
}
