package org.aspose.pdf.text;

import java.io.IOException;
import java.awt.GraphicsEnvironment;
import java.util.logging.Logger;

/**
 * Provides access to system fonts and font lookup.
 * <p>
 * The public API FontRepository (analogous to Aspose.PDF's FontRepository)
 * allows finding fonts by name for use in TextState and other text operations.
 * </p>
 */
public final class FontRepository {

    private static final Logger LOG = Logger.getLogger(FontRepository.class.getName());

    private FontRepository() {} // utility class

    /**
     * Finds a font by name from the system font collection.
     * <p>
     * Returns a {@link Font} object wrapping the font name.
     * The font name is validated against system fonts where possible,
     * but a Font object is always returned (even if the font is not installed)
     * to maintain compatibility.
     * </p>
     *
     * @param fontName the font name (e.g., "Helvetica", "Times New Roman")
     * @return a Font object for the given name
     */
    public static Font findFont(String fontName) {
        if (fontName == null || fontName.isEmpty()) {
            throw new IllegalArgumentException("Font name must not be null or empty");
        }
        LOG.fine(() -> "Finding font: " + fontName);
        Font font = new Font(fontName);
        // Try to resolve the file on disk so that a subsequent
        // setEmbedded(true) actually has bytes to inline. We don't fail
        // when the file is missing — Standard 14 fonts (Helvetica, Times
        // …) never live on disk anyway, and the legacy fallback path
        // continues to work by name alone.
        byte[] bytes = org.aspose.pdf.engine.font.ttf.FontDiskLookup.loadByName(fontName);
        if (bytes != null) {
            font.setFontData(bytes);
        }
        return font;
    }

    /**
     * Finds a font by name, optionally allowing an embedded font.
     *
     * @param fontName the font name
     * @param embedded whether the font should be embedded
     * @return a Font object
     */
    public static Font findFont(String fontName, boolean embedded) {
        Font font = findFont(fontName);
        font.setEmbedded(embedded);
        return font;
    }

    /**
     * Opens a font from a file path.
     *
     * @param fontPath the path to the font file
     * @return a Font loaded from the file
     * @throws IOException if the file cannot be read
     */
    public static Font openFont(String fontPath) throws IOException {
        return openFont(fontPath, null);
    }

    /**
     * Opens a font from a file path with the specified style.
     *
     * @param fontPath the path to the font file
     * @param style the font style, or {@code null} for default
     * @return a Font loaded from the file
     * @throws IOException if the file cannot be read
     */
    public static Font openFont(String fontPath, org.aspose.pdf.facades.FontStyle style) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(fontPath);
        byte[] fontData = java.nio.file.Files.readAllBytes(path);
        Font font = new Font();
        font.setFontFilePath(fontPath);
        font.setFontData(fontData);
        if (style != null) {
            // FontStyle maps to font style flags
            LOG.fine(() -> "Opening font with style: " + style);
        }
        return font;
    }

    /**
     * Opens a font from a byte array.
     *
     * @param fontData the raw font data
     * @return a Font loaded from the data
     */
    public static Font openFont(byte[] fontData) {
        Font font = new Font();
        font.setFontData(fontData);
        return font;
    }

    /**
     * Opens a font from an input stream with an explicit format hint.
     * Mirrors {@code Aspose.Pdf.Text.FontRepository.OpenFont(Stream, FontTypes)}.
     *
     * @param fontStream the font byte stream (fully read by this call)
     * @param type       the font format ({@link FontTypes#TTF}, {@code OTF}, {@code Type1})
     * @return a Font loaded from the stream
     * @throws java.io.IOException if reading the stream fails
     */
    public static Font openFont(java.io.InputStream fontStream, FontTypes type) throws java.io.IOException {
        if (fontStream == null) {
            throw new IllegalArgumentException("fontStream must not be null");
        }
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] tmp = new byte[8192];
        int n;
        while ((n = fontStream.read(tmp)) >= 0) buf.write(tmp, 0, n);
        Font font = new Font();
        font.setFontData(buf.toByteArray());
        // The format hint is informational for now — the engine inspects the
        // header bytes to determine actual format on first use.
        if (type != null) {
            LOG.fine(() -> "Opened font of declared type: " + type);
        }
        return font;
    }
}
