package org.aspose.pdf.text;

/**
 * Represents a font used in PDF documents.
 * <p>
 * Provides access to font name and basic font properties.
 * </p>
 */
public class Font {

    private String name;
    private boolean isEmbedded;
    private boolean isSubset;
    private byte[] fontData;
    private String fontFilePath;

    /**
     * Creates a font with default (empty) name.
     */
    public Font() {
        this.name = "";
    }

    /**
     * Creates a font with the specified name.
     *
     * @param name the font name
     */
    public Font(String name) {
        this.name = name != null ? name : "";
    }

    /**
     * Gets the font name.
     *
     * @return the font name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the font name.
     *
     * @param name the font name
     */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    /**
     * Returns whether this font is embedded in the PDF.
     *
     * @return {@code true} if embedded
     */
    public boolean isEmbedded() {
        return isEmbedded;
    }

    /**
     * Sets whether this font is embedded in the PDF.
     *
     * @param embedded {@code true} to mark as embedded
     */
    public void setEmbedded(boolean embedded) {
        this.isEmbedded = embedded;
    }

    /**
     * Returns whether this font is a subset.
     *
     * @return {@code true} if subset
     */
    public boolean isSubset() {
        return isSubset;
    }

    /**
     * Sets whether this font is a subset.
     *
     * @param subset {@code true} to mark as subset
     */
    public void setSubset(boolean subset) {
        this.isSubset = subset;
    }

    /**
     * Gets the raw font data bytes.
     *
     * @return the font data, or {@code null} if not set
     */
    public byte[] getFontData() {
        return fontData;
    }

    /**
     * Sets the raw font data bytes.
     *
     * @param data the font data
     */
    public void setFontData(byte[] data) {
        this.fontData = data;
    }

    /**
     * Gets the file path from which this font was loaded.
     *
     * @return the font file path, or {@code null} if not set
     */
    public String getFontFilePath() {
        return fontFilePath;
    }

    /**
     * Sets the file path from which this font was loaded.
     *
     * @param path the font file path
     */
    public void setFontFilePath(String path) {
        this.fontFilePath = path;
    }

    @Override
    public String toString() {
        return name;
    }
}
