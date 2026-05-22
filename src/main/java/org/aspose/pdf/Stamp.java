package org.aspose.pdf;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base class for all stamp types that can be overlaid on PDF pages.
 * <p>
 * A stamp represents a visual element (text, image, or page content) placed
 * at a specified position on one or more pages. This base class provides
 * common properties shared by all stamp types: position, dimensions, rotation,
 * opacity, alignment, margins, and z-order (foreground/background).
 * </p>
 * <p>
 * Subclasses must implement {@link #put(Page)} to perform the actual rendering.
 * </p>
 *
 * @see TextStamp
 * @see ImageStamp
 * @see PdfPageStamp
 * @see PageNumberStamp
 */
public abstract class Stamp {

    private static final Logger LOG = Logger.getLogger(Stamp.class.getName());

    private double xIndent;
    private double yIndent;
    private double width;
    private double height;
    private boolean background;
    private Rotation rotate = Rotation.None;
    private double rotateAngle;
    private double opacity = 1.0;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.None;
    private VerticalAlignment verticalAlignment = VerticalAlignment.None;
    private double leftMargin;
    private double rightMargin;
    private double topMargin;
    private double bottomMargin;
    private int stampId;
    private double zoom = 1.0;
    private double zoomX;
    private double zoomY;

    /**
     * Applies this stamp to the given page.
     * <p>
     * Subclasses implement this method to generate the appropriate content
     * stream operators and merge them into the page.
     * </p>
     *
     * @param page the page to stamp; must not be {@code null}
     * @throws IOException if content stream generation or merging fails
     */
    public abstract void put(Page page) throws IOException;

    /**
     * Returns the horizontal position (X indent) of the stamp in points.
     *
     * @return the X indent
     */
    public double getXIndent() {
        return xIndent;
    }

    /**
     * Sets the horizontal position (X indent) of the stamp in points.
     *
     * @param xIndent the X indent
     */
    public void setXIndent(double xIndent) {
        this.xIndent = xIndent;
    }

    /**
     * Returns the vertical position (Y indent) of the stamp in points.
     *
     * @return the Y indent
     */
    public double getYIndent() {
        return yIndent;
    }

    /**
     * Sets the vertical position (Y indent) of the stamp in points.
     *
     * @param yIndent the Y indent
     */
    public void setYIndent(double yIndent) {
        this.yIndent = yIndent;
    }

    /**
     * Returns the width of the stamp area in points.
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of the stamp area in points.
     *
     * @param width the width
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Returns the height of the stamp area in points.
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of the stamp area in points.
     *
     * @param height the height
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Returns whether this stamp is rendered behind the page content.
     *
     * @return {@code true} if the stamp is a background element
     */
    public boolean isBackground() {
        return background;
    }

    /**
     * Sets whether this stamp is rendered behind the page content.
     *
     * @param background {@code true} to render the stamp behind content
     */
    public void setBackground(boolean background) {
        this.background = background;
    }

    /**
     * Returns the rotation enum for this stamp.
     *
     * @return the rotation enum value
     */
    public Rotation getRotate() {
        return rotate;
    }

    /**
     * Sets the rotation using the {@link Rotation} enum.
     * Also updates the rotation angle in degrees.
     *
     * @param rotate the rotation enum value
     */
    public void setRotate(Rotation rotate) {
        if (rotate == null) rotate = Rotation.None;
        this.rotate = rotate;
        this.rotateAngle = rotate.getDegrees();
    }

    /**
     * Returns the rotation angle of the stamp in degrees.
     *
     * @return the rotation angle
     */
    public double getRotateAngle() {
        return rotateAngle;
    }

    /**
     * Sets the rotation angle of the stamp in degrees.
     * Also updates the {@link Rotation} enum property if the angle matches
     * a standard rotation (0, 90, 180, 270).
     *
     * @param rotateAngle the rotation angle
     */
    public void setRotateAngle(double rotateAngle) {
        this.rotateAngle = rotateAngle;
        this.rotate = Rotation.fromDegrees((int) rotateAngle);
    }

    /**
     * Returns the opacity (transparency) of the stamp.
     * <p>
     * Values range from 0.0 (fully transparent) to 1.0 (fully opaque).
     * </p>
     *
     * @return the opacity value
     */
    public double getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity (transparency) of the stamp.
     * <p>
     * Values range from 0.0 (fully transparent) to 1.0 (fully opaque).
     * Values outside this range are clamped.
     * </p>
     *
     * @param opacity the opacity value
     */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    /**
     * Returns the horizontal alignment of the stamp on the page.
     *
     * @return the horizontal alignment
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the stamp on the page.
     *
     * @param horizontalAlignment the horizontal alignment
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Returns the vertical alignment of the stamp on the page.
     *
     * @return the vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the stamp on the page.
     *
     * @param verticalAlignment the vertical alignment
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Returns the left margin of the stamp in points.
     *
     * @return the left margin
     */
    public double getLeftMargin() {
        return leftMargin;
    }

    /**
     * Sets the left margin of the stamp in points.
     *
     * @param leftMargin the left margin
     */
    public void setLeftMargin(double leftMargin) {
        this.leftMargin = leftMargin;
    }

    /**
     * Returns the right margin of the stamp in points.
     *
     * @return the right margin
     */
    public double getRightMargin() {
        return rightMargin;
    }

    /**
     * Sets the right margin of the stamp in points.
     *
     * @param rightMargin the right margin
     */
    public void setRightMargin(double rightMargin) {
        this.rightMargin = rightMargin;
    }

    /**
     * Returns the top margin of the stamp in points.
     *
     * @return the top margin
     */
    public double getTopMargin() {
        return topMargin;
    }

    /**
     * Sets the top margin of the stamp in points.
     *
     * @param topMargin the top margin
     */
    public void setTopMargin(double topMargin) {
        this.topMargin = topMargin;
    }

    /**
     * Returns the bottom margin of the stamp in points.
     *
     * @return the bottom margin
     */
    public double getBottomMargin() {
        return bottomMargin;
    }

    /**
     * Sets the bottom margin of the stamp in points.
     *
     * @param bottomMargin the bottom margin
     */
    public void setBottomMargin(double bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    /**
     * Returns the stamp identifier.
     *
     * @return the stamp ID
     */
    public int getStampId() {
        return stampId;
    }

    /**
     * Sets the stamp identifier.
     *
     * @param stampId the stamp ID
     */
    public void setStampId(int stampId) {
        this.stampId = stampId;
    }

    /**
     * Returns the zoom (scale) factor for the stamp.
     *
     * @return the zoom factor; default is 1.0
     */
    public double getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom (scale) factor for the stamp.
     *
     * @param zoom the zoom factor
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    /**
     * Returns the horizontal zoom (scale) factor for the stamp.
     *
     * @return the horizontal zoom factor
     */
    public double getZoomX() {
        return zoomX;
    }

    /**
     * Sets the horizontal zoom (scale) factor for the stamp.
     *
     * @param zoomX the horizontal zoom factor
     */
    public void setZoomX(double zoomX) {
        this.zoomX = zoomX;
    }

    /**
     * Returns the vertical zoom (scale) factor for the stamp.
     *
     * @return the vertical zoom factor
     */
    public double getZoomY() {
        return zoomY;
    }

    /**
     * Sets the vertical zoom (scale) factor for the stamp.
     *
     * @param zoomY the vertical zoom factor
     */
    public void setZoomY(double zoomY) {
        this.zoomY = zoomY;
    }
}
