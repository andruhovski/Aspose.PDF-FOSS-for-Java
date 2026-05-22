package org.aspose.pdf.text;

/**
 * Represents a folder containing font files as a font source.
 * <p>
 * When added to {@link FontRepository}, the specified folder is scanned
 * for font files (TrueType, OpenType, etc.) that can be used for PDF rendering.
 * </p>
 */
public class FolderFontSource extends FontSource {

    private final String folderPath;

    /**
     * Creates a new folder font source.
     *
     * @param folderPath the path to the folder containing font files
     */
    public FolderFontSource(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Gets the path to the folder containing font files.
     *
     * @return the folder path
     */
    public String getFolderPath() {
        return folderPath;
    }
}
