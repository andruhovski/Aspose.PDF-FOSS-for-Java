package org.aspose.pdf;

import java.util.logging.Logger;

/**
 * Represents a watermark artifact — a convenience subclass for creating
 * pagination artifacts with the Watermark subtype (ISO 32000-1:2008, §14.8.2.2).
 * <p>
 * Watermarks are typically semi-transparent text or images placed over or behind
 * page content to indicate status (e.g., "DRAFT", "CONFIDENTIAL") or ownership.
 * </p>
 */
public class WatermarkArtifact extends Artifact {

    private static final Logger LOG = Logger.getLogger(WatermarkArtifact.class.getName());

    /**
     * Creates a new watermark artifact with type Pagination and subtype Watermark.
     */
    public WatermarkArtifact() {
        super(ArtifactType.Pagination, ArtifactSubtype.Watermark);
        LOG.fine("WatermarkArtifact created");
    }

    /**
     * Sets the watermark text content.
     *
     * @param text the watermark text
     */
    public void setWatermarkText(String text) {
        setText(text);
    }

    /**
     * Returns the watermark text content.
     *
     * @return the watermark text
     */
    public String getWatermarkText() {
        return getText();
    }

    /**
     * Sets the watermark opacity (0.0 = fully transparent, 1.0 = fully opaque).
     *
     * @param opacity the opacity value
     * @throws IllegalArgumentException if opacity is not between 0.0 and 1.0
     */
    public void setWatermarkOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0, got: " + opacity);
        }
        setOpacity(opacity);
    }

    /**
     * Sets the watermark rotation angle in degrees.
     *
     * @param degrees the rotation angle
     */
    public void setWatermarkRotation(double degrees) {
        setRotation(degrees);
    }
}
