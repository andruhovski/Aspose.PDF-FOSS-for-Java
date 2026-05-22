package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides utility methods for working with fonts in a PDF document.
 * <p>
 * Accessed via {@link Document#getFontUtilities()}. Allows enumerating all fonts
 * used across all pages of the document and marking fonts for subsetting.
 * </p>
 */
public class FontUtilities {

    private static final Logger LOG = Logger.getLogger(FontUtilities.class.getName());

    private final Document document;

    /**
     * Creates a FontUtilities instance for the given document.
     *
     * @param document the PDF document
     * @throws IllegalArgumentException if document is null
     */
    public FontUtilities(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null");
        }
        this.document = document;
    }

    /**
     * Returns all fonts used across all pages of the document.
     * <p>
     * Iterates each page's /Resources /Font dictionary and collects unique fonts
     * by their base font name. Font embedding and subset status are determined
     * from the font dictionary entries (/FontDescriptor, /FontFile, etc.).
     * </p>
     *
     * @return an array of all unique fonts found in the document
     */
    public Font[] getAllFonts() {
        Map<String, Font> fontMap = new LinkedHashMap<>();
        try {
            PageCollection pages = document.getPages();
            for (int i = 1; i <= pages.getCount(); i++) {
                Page page = pages.get(i);
                Resources resources = page.getResources();
                if (resources == null) continue;
                COSDictionary fontsDict = resources.getFonts();
                if (fontsDict == null) continue;
                for (COSName key : fontsDict.keySet()) {
                    COSBase fontObj = fontsDict.get(key);
                    fontObj = resolveRef(fontObj);
                    if (fontObj instanceof COSDictionary) {
                        COSDictionary fontDict = (COSDictionary) fontObj;
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

    /**
     * Marks all fonts in the document for subsetting on save.
     * <p>
     * This is a hint to the PDF writer that fonts should be subsetted
     * (only include glyphs actually used in the document) to reduce file size.
     * Currently a no-op placeholder for API compatibility.
     * </p>
     */
    public void subsetFonts() {
        // Mark fonts for subsetting — this is a flag for the writer
        LOG.fine("Font subsetting requested");
    }

    /**
     * Checks whether a font dictionary indicates the font is embedded.
     *
     * @param fontDict the font dictionary
     * @return true if the font has an embedded program
     */
    private boolean isFontEmbedded(COSDictionary fontDict) {
        COSBase descriptor = resolveRef(fontDict.get("FontDescriptor"));
        if (descriptor instanceof COSDictionary) {
            COSDictionary desc = (COSDictionary) descriptor;
            if (desc.get("FontFile") != null || desc.get("FontFile2") != null
                    || desc.get("FontFile3") != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a font name indicates it is a subset (has a 6-letter prefix + '+').
     *
     * @param baseFontName the base font name
     * @return true if the name matches the subset naming convention
     */
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

    /**
     * Resolves an indirect object reference.
     *
     * @param val the COS value to resolve
     * @return the resolved value, or null
     */
    private COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) val).dereference();
            } catch (Exception e) {
                return null;
            }
        }
        return val;
    }
}
