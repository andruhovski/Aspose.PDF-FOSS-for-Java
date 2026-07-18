package org.aspose.pdf.engine.layout;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/// Builds the /Resources dictionary for a PDF page during layout.
///
/// Tracks registered fonts and image XObjects, assigning resource names
/// (e.g. "F1", "Im1") and creating the corresponding PDF dictionary entries.
/// Each font is registered as a Type 1 standard font with WinAnsiEncoding
/// (ISO 32000-1:2008, Section 9.6.2.2).
///
public class ResourceBuilder {

    private static final Logger LOG = Logger.getLogger(ResourceBuilder.class.getName());

    /// Maps resource name (e.g. "F1") to its font PDF dictionary.
    private final Map<String, PdfDictionary> fonts = new LinkedHashMap<>();

    /// Maps resource name (e.g. "Im1") to its image PDF stream.
    private final Map<String, PdfStream> images = new LinkedHashMap<>();

    /// Maps base font name to resource name for deduplication.
    private final Map<String, String> fontNameToResource = new LinkedHashMap<>();

    private int fontCounter = 0;
    private int imageCounter = 0;

    /// Resource-name → TrueTypeReader for fonts registered via
    /// [#addEmbeddedFont]. Layout code retrieves the reader so the
    /// content-stream builder can encode text as 2-byte CIDs that the
    /// embedded Type0 dictionary understands.
    private final Map<String, org.aspose.pdf.engine.font.ttf.TrueTypeReader> type0Readers
            = new LinkedHashMap<>();

    /// Creates an empty ResourceBuilder.
    public ResourceBuilder() {
        LOG.fine("ResourceBuilder created");
    }

    /// Registers a TrueType font as a `/Type0`/`/Identity-H`
    /// embedded font and returns its resource name. The font's raw bytes
    /// (from [org.aspose.pdf.text.Font#getFontData()]) are
    /// embedded into a `/FontFile2` stream and wired through a
    /// `CIDFontType2` descendant plus a `/ToUnicode` CMap.
    ///
    /// Returns `null` (and registers nothing) when `font` has
    /// no font data — callers should fall back to [#addFont(String)]
    /// in that case.
    ///
    /// @param font the [org.aspose.pdf.text.Font] carrying TTF
    ///             bytes; the [org.aspose.pdf.text.Font#isEmbedded()]
    ///             flag is assumed already checked by the caller
    /// @return the resource name (e.g. `"F2"`), or `null`
    public String addEmbeddedFont(org.aspose.pdf.text.Font font) {
        if (font == null || font.getFontData() == null || font.getFontData().length == 0) {
            return null;
        }
        String key = "EMBED:" + (font.getName() != null ? font.getName() : "anon")
                + "@" + System.identityHashCode(font);
        String existing = fontNameToResource.get(key);
        if (existing != null) return existing;
        try {
            byte[] ttf = font.getFontData();
            // If for some reason a TTC slipped through (the FontRepository
            // path normally unpacks it), pull the first face out now.
            if (org.aspose.pdf.engine.font.ttf.FontDiskLookup.isTTC(ttf)) {
                byte[] unpacked = org.aspose.pdf.engine.font.ttf.FontDiskLookup
                        .extractFirstFaceFromTTC(ttf);
                if (unpacked != null) ttf = unpacked;
            }
            org.aspose.pdf.engine.font.ttf.Type0FontBuilder.Result built =
                    org.aspose.pdf.engine.font.ttf.Type0FontBuilder.build(
                            font.getName() != null ? font.getName() : "EmbeddedFont", ttf);
            fontCounter++;
            String resourceName = "F" + fontCounter;
            fonts.put(resourceName, built.type0Font);
            fontNameToResource.put(key, resourceName);
            type0Readers.put(resourceName, built.reader);
            LOG.fine(() -> "Added embedded TrueType font " + font.getName()
                    + " as " + resourceName);
            return resourceName;
        } catch (Exception e) {
            LOG.warning(() -> "Failed to build Type0 dict for " + font.getName()
                    + ": " + e.getMessage());
            return null;
        }
    }

    /// Returns the [org.aspose.pdf.engine.font.ttf.TrueTypeReader]
    /// paired with an embedded font resource, or `null` if the
    /// resource is a standard Type1 font (or not registered).
    ///
    /// @param resourceName the `/Fn` name returned by [#addEmbeddedFont]
    /// @return the reader, or `null`
    public org.aspose.pdf.engine.font.ttf.TrueTypeReader getType0Reader(String resourceName) {
        return type0Readers.get(resourceName);
    }

    /// Registers a standard PDF font and returns its resource name.
    ///
    /// Creates a font dictionary with:
    ///
    ///   - /Type /Font
    ///   - /Subtype /Type1
    ///   - /BaseFont /fontName
    ///   - /Encoding /WinAnsiEncoding
    ///
    /// If the same base font is registered again, the existing resource name is returned.
    ///
    /// @param baseFont the standard font name (e.g. "Helvetica", "Courier-Bold")
    /// @return the resource name (e.g. "F1")
    public String addFont(String baseFont) {
        String effectiveFont = baseFont != null ? baseFont : "Helvetica";

        // Check if already registered
        String existing = fontNameToResource.get(effectiveFont);
        if (existing != null) {
            return existing;
        }

        fontCounter++;
        String resourceName = "F" + fontCounter;

        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.TYPE, PdfName.FONT);
        fontDict.set(PdfName.SUBTYPE, PdfName.of("Type1"));
        fontDict.set(PdfName.BASE_FONT, PdfName.of(effectiveFont));
        fontDict.set(PdfName.ENCODING, PdfName.of("WinAnsiEncoding"));

        fonts.put(resourceName, fontDict);
        fontNameToResource.put(effectiveFont, resourceName);

        LOG.fine(() -> "Added font " + effectiveFont + " as " + resourceName);
        return resourceName;
    }

    /// Registers an image XObject and returns its resource name.
    ///
    /// The caller provides the image PdfStream which must already contain
    /// the appropriate /Subtype /Image, /Width, /Height, etc. entries.
    ///
    /// @param key         a unique key for deduplication
    /// @param imageStream the image XObject stream
    /// @return the resource name (e.g. "Im1")
    public String addImage(String key, PdfStream imageStream) {
        if (key == null) {
            throw new IllegalArgumentException("Image key must not be null");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Image stream must not be null");
        }

        // Check if already registered
        for (Map.Entry<String, PdfStream> entry : images.entrySet()) {
            if (entry.getValue() == imageStream) {
                return entry.getKey();
            }
        }

        imageCounter++;
        String resourceName = "Im" + imageCounter;
        images.put(resourceName, imageStream);

        LOG.fine(() -> "Added image " + key + " as " + resourceName);
        return resourceName;
    }

    /// Builds and returns the complete /Resources PDF dictionary.
    ///
    /// The dictionary contains /Font and /XObject sub-dictionaries mapping
    /// resource names to their objects.
    ///
    /// @return the /Resources PdfDictionary
    public PdfDictionary buildResourcesDictionary() {
        PdfDictionary resources = new PdfDictionary();

        // Build /Font sub-dictionary
        if (!fonts.isEmpty()) {
            PdfDictionary fontDict = new PdfDictionary();
            for (Map.Entry<String, PdfDictionary> entry : fonts.entrySet()) {
                fontDict.set(PdfName.of(entry.getKey()), entry.getValue());
            }
            resources.set(PdfName.FONT, fontDict);
        }

        // Build /XObject sub-dictionary
        if (!images.isEmpty()) {
            PdfDictionary xobjectDict = new PdfDictionary();
            for (Map.Entry<String, PdfStream> entry : images.entrySet()) {
                xobjectDict.set(PdfName.of(entry.getKey()), entry.getValue());
            }
            resources.set(PdfName.XOBJECT, xobjectDict);
        }

        LOG.fine(() -> "Built resources dictionary with " + fonts.size()
                + " font(s) and " + images.size() + " image(s)");
        return resources;
    }

    /// Returns the resource name for a previously registered font, or null.
    ///
    /// @param baseFont the base font name
    /// @return the resource name, or null if not registered
    public String getFontResourceName(String baseFont) {
        return fontNameToResource.get(baseFont);
    }
}
