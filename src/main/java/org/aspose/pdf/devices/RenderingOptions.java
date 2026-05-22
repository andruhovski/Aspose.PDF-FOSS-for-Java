package org.aspose.pdf.devices;

/**
 * Options for rendering PDF pages to images.
 */
public class RenderingOptions {

    private boolean smoothImages = false;
    private String bgColor;
    private int dpiX = 0;
    private int dpiY = 0;

    /**
     * Creates default rendering options.
     */
    public RenderingOptions() {
    }

    /**
     * Returns whether image smoothing is enabled.
     *
     * @return true if smooth
     */
    public boolean isSmoothImages() {
        return smoothImages;
    }

    /**
     * Sets whether image smoothing is enabled.
     *
     * @param smooth true to enable smoothing
     */
    public void setSmoothImages(boolean smooth) {
        this.smoothImages = smooth;
    }

    /**
     * Returns the background color.
     *
     * @return the background color, or null
     */
    public String getBackgroundColor() {
        return bgColor;
    }

    /**
     * Sets the background color.
     *
     * @param color the background color
     */
    public void setBackgroundColor(String color) {
        this.bgColor = color;
    }

    /**
     * Returns the horizontal DPI.
     *
     * @return the X DPI
     */
    public int getDpiX() {
        return dpiX;
    }

    /**
     * Sets the horizontal DPI.
     *
     * @param dpi the X DPI
     */
    public void setDpiX(int dpi) {
        this.dpiX = dpi;
    }

    /**
     * Returns the vertical DPI.
     *
     * @return the Y DPI
     */
    public int getDpiY() {
        return dpiY;
    }

    /**
     * Sets the vertical DPI.
     *
     * @param dpi the Y DPI
     */
    public void setDpiY(int dpi) {
        this.dpiY = dpi;
    }
}
