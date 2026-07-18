package org.aspose.pdf.engine.font;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/// Caches and resolves PDF fonts from resource dictionaries.
///
/// Maintains a per-instance cache to avoid re-parsing the same font dictionary
/// multiple times during text extraction.
///
public final class FontRepository {

    private static final Logger LOG = Logger.getLogger(FontRepository.class.getName());

    private final Map<String, PdfFont> cache = new HashMap<>();

    /// Returns the PdfFont for the given font name from the fonts dictionary.
    ///
    /// Caches fonts by name to avoid repeated parsing.
    ///
    /// @param fontsDict the /Font sub-dictionary from page resources
    /// @param fontName  the font resource name (e.g., "F1", "TT0")
    /// @param parser    the PDF parser for resolving indirect references
    /// @return the resolved PdfFont
    /// @throws IOException if font creation fails
    public PdfFont getFont(PdfDictionary fontsDict, String fontName, PDFParser parser)
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
        PdfBase fontVal = fontsDict.get(fontName);
        if (fontVal instanceof PdfObjectReference) {
            try {
                fontVal = ((PdfObjectReference) fontVal).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference font " + fontName + ": " + e.getMessage());
                return null;
            }
        }

        if (!(fontVal instanceof PdfDictionary)) {
            LOG.warning(() -> "Font " + fontName + " is not a dictionary");
            return null;
        }

        PdfFont font = PdfFont.fromDictionary((PdfDictionary) fontVal, parser);
        cache.put(fontName, font);
        return font;
    }

    /// Convenience method: resolves a font from page Resources.
    ///
    /// @param resources the page resources
    /// @param fontName  the font resource name (e.g., "F1")
    /// @param parser    the PDF parser
    /// @return the resolved PdfFont, or null
    /// @throws IOException if font creation fails
    public static PdfFont fromResources(Resources resources, String fontName, PDFParser parser)
            throws IOException {
        if (resources == null) return null;
        PdfDictionary fonts = resources.getFonts();
        if (fonts == null) return null;
        FontRepository repo = new FontRepository();
        return repo.getFont(fonts, fontName, parser);
    }

    /// Clears the font cache.
    public void clear() {
        cache.clear();
    }
}
