package org.aspose.pdf;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Represents a background artifact — a convenience subclass for creating
 * artifacts that serve as page backgrounds (ISO 32000-1:2008, §14.8.2.2).
 * <p>
 * Background artifacts can include solid colors or images placed behind
 * the primary page content.
 * </p>
 */
public class BackgroundArtifact extends Artifact {

    private static final Logger LOG = Logger.getLogger(BackgroundArtifact.class.getName());

    private Color backgroundColor;
    private InputStream backgroundImage;

    /**
     * Creates a new background artifact with type Background and subtype Background.
     */
    public BackgroundArtifact() {
        super(ArtifactType.Background, ArtifactSubtype.Background);
        setBackground(true);
        LOG.fine("BackgroundArtifact created");
    }

    /**
     * Returns the background color.
     *
     * @return the background color, or {@code null} if not set
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param color the background color
     */
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    /**
     * Returns the background image input stream.
     *
     * @return the background image stream, or {@code null} if not set
     */
    public InputStream getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Sets the background image from an input stream.
     *
     * @param imageStream the image input stream
     */
    public void setBackgroundImage(InputStream imageStream) {
        this.backgroundImage = imageStream;
    }
}
