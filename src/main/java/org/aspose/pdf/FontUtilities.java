package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.text.Font;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/// Provides utility methods for working with fonts in a PDF document.
///
/// Accessed via [Document#getFontUtilities()]. Allows enumerating all fonts
/// used across all pages of the document and marking fonts for subsetting.
///
public class FontUtilities {

    private static final Logger LOG = Logger.getLogger(FontUtilities.class.getName());

    private final Document document;

    /// Creates a FontUtilities instance for the given document.
    ///
    /// @param document the PDF document
    /// @throws IllegalArgumentException if document is null
    public FontUtilities(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null");
        }
        this.document = document;
    }

    /// Returns all fonts used across all pages of the document.
    ///
    /// Iterates each page's /Resources /Font dictionary and collects unique fonts
    /// by their base font name. Font embedding and subset status are determined
    /// from the font dictionary entries (/FontDescriptor, /FontFile, etc.).
    ///
    /// @return an array of all unique fonts found in the document
    public Font[] getAllFonts() {
        Map<String, Font> fontMap = new LinkedHashMap<>();
        try {
            PageCollection pages = document.getPages();
            for (int i = 1; i <= pages.getCount(); i++) {
                Page page = pages.get(i);
                Resources resources = page.getResources();
                if (resources == null) continue;
                PdfDictionary fontsDict = resources.getFonts();
                if (fontsDict == null) continue;
                for (PdfName key : fontsDict.keySet()) {
                    PdfBase fontObj = fontsDict.get(key);
                    fontObj = resolveRef(fontObj);
                    if (fontObj instanceof PdfDictionary) {
                        PdfDictionary fontDict = (PdfDictionary) fontObj;
                        String baseFontName = fontDict.getNameAsString("BaseFont");
                        if (baseFontName == null) {
                            baseFontName = key.getName();
                        }
                        if (!fontMap.containsKey(baseFontName)) {
                            Font font = new Font(baseFontName);
                            font.setEmbedded(isFontEmbedded(fontDict));
                            font.setSubset(isFontSubset(baseFontName));
                            fontMap.put(baseFontName, font);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOG.warning(() -> "Error collecting fonts: " + e.getMessage());
        }
        return fontMap.values().toArray(new Font[0]);
    }

    /// Subsets the document's embedded fonts in place: strips glyph outlines
    /// that no content stream uses from the embedded TrueType programs
    /// (Identity-encoded CID fonts; see the resource optimizer for the safe
    /// shapes handled). The change takes effect on the next save.
    public void subsetFonts() {
        if (document.getParser() == null) {
            LOG.fine("subsetFonts: new document — fonts are embedded per use already");
            return;
        }
        org.aspose.pdf.optimization.OptimizationOptions options =
                new org.aspose.pdf.optimization.OptimizationOptions();
        options.setSubsetFonts(true);
        org.aspose.pdf.engine.optimization.ResourceOptimizer.optimize(
                document.getParser(), options);
    }

    /// Checks whether a font dictionary indicates the font is embedded.
    ///
    /// @param fontDict the font dictionary
    /// @return true if the font has an embedded program
    private boolean isFontEmbedded(PdfDictionary fontDict) {
        PdfBase descriptor = resolveRef(fontDict.get("FontDescriptor"));
        if (descriptor instanceof PdfDictionary) {
            PdfDictionary desc = (PdfDictionary) descriptor;
            if (desc.get("FontFile") != null || desc.get("FontFile2") != null
                    || desc.get("FontFile3") != null) {
                return true;
            }
        }
        return false;
    }

    /// Checks whether a font name indicates it is a subset (has a 6-letter prefix + '+').
    ///
    /// @param baseFontName the base font name
    /// @return true if the name matches the subset naming convention
    private boolean isFontSubset(String baseFontName) {
        if (baseFontName == null || baseFontName.length() < 8) return false;
        // Subset fonts have the pattern ABCDEF+FontName
        if (baseFontName.charAt(6) != '+') return false;
        for (int i = 0; i < 6; i++) {
            char c = baseFontName.charAt(i);
            if (c < 'A' || c > 'Z') return false;
        }
        return true;
    }

    /// Resolves an indirect object reference.
    ///
    /// @param val the PDF value to resolve
    /// @return the resolved value, or null
    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) val).dereference();
            } catch (Exception e) {
                return null;
            }
        }
        return val;
    }
}
