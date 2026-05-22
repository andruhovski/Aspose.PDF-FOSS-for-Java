package org.aspose.pdf.engine.font;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Caches and resolves PDF fonts from resource dictionaries.
 * <p>
 * Maintains a per-instance cache to avoid re-parsing the same font dictionary
 * multiple times during text extraction.
 * </p>
 */
public final class FontRepository {

    private static final Logger LOG = Logger.getLogger(FontRepository.class.getName());

    private final Map<String, PdfFont> cache = new HashMap<>();

    /**
     * Returns the PdfFont for the given font name from the fonts dictionary.
     * <p>
     * Caches fonts by name to avoid repeated parsing.
     * </p>
     *
     * @param fontsDict the /Font sub-dictionary from page resources
     * @param fontName  the font resource name (e.g., "F1", "TT0")
     * @param parser    the PDF parser for resolving indirect references
     * @return the resolved PdfFont
     * @throws IOException if font creation fails
     */
    public PdfFont getFont(COSDictionary fontsDict, String fontName, PDFParser parser)
            throws IOException {
        if (fontsDict == null || fontName == null) {
            return null;
        }

        // Check cache
        PdfFont cached = cache.get(fontName);
        if (cached != null) {
            return cached;
        }

        // Resolve font dictionary
        COSBase fontVal = fontsDict.get(fontName);
        if (fontVal instanceof COSObjectReference) {
            try {
                fontVal = ((COSObjectReference) fontVal).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference font " + fontName + ": " + e.getMessage());
                return null;
            }
        }

        if (!(fontVal instanceof COSDictionary)) {
            LOG.warning(() -> "Font " + fontName + " is not a dictionary");
            return null;
        }

        PdfFont font = PdfFont.fromDictionary((COSDictionary) fontVal, parser);
        cache.put(fontName, font);
        return font;
    }

    /**
     * Convenience method: resolves a font from page Resources.
     *
     * @param resources the page resources
     * @param fontName  the font resource name (e.g., "F1")
     * @param parser    the PDF parser
     * @return the resolved PdfFont, or null
     * @throws IOException if font creation fails
     */
    public static PdfFont fromResources(Resources resources, String fontName, PDFParser parser)
            throws IOException {
        if (resources == null) return null;
        COSDictionary fonts = resources.getFonts();
        if (fonts == null) return null;
        FontRepository repo = new FontRepository();
        return repo.getFont(fonts, fontName, parser);
    }

    /**
     * Clears the font cache.
     */
    public void clear() {
        cache.clear();
    }
}
