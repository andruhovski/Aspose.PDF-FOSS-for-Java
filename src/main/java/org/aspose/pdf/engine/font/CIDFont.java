package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CID font (ISO 32000-1:2008, §9.7.4).
 * <p>
 * Handles /Subtype /CIDFontType0 (CFF-based) and /CIDFontType2 (TrueType-based).
 * CID fonts are always used as descendant fonts of Type 0 (composite) fonts.
 * Parses the /W (width) array and /DW (default width) entries.
 * </p>
 */
public class CIDFont extends PdfFont {

    private static final Logger LOG = Logger.getLogger(CIDFont.class.getName());

    private double defaultWidth = 1000;
    private final Map<Integer, Double> cidWidths = new HashMap<>();

    /**
     * Creates a CIDFont from a font dictionary.
     *
     * @param fontDict the CIDFont dictionary
     * @param parser   the PDF parser (may be null)
     * @throws IOException if reading font data fails
     */
    public CIDFont(COSDictionary fontDict, PDFParser parser) throws IOException {
        super(fontDict, parser);

        // /DW default width
        COSBase dwVal = fontDict.get("DW");
        if (dwVal != null) {
            defaultWidth = getNumber(dwVal);
        }

        // /W width array
        parseWidthArray();

        LOG.fine(() -> "CIDFont created: " + baseFont + ", " + cidWidths.size() + " width entries");
    }

    @Override
    public double getWidth(int cid) {
        Double w = cidWidths.get(cid);
        return w != null ? w : defaultWidth;
    }

    /**
     * Returns the default width (/DW).
     *
     * @return the default width
     */
    public double getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * Parses the /W (widths) array.
     * <p>
     * Format: [cidFirst [w1 w2 ...]] or [cidFirst cidLast w]
     * </p>
     */
    private void parseWidthArray() {
        COSBase wVal = resolve(fontDict.get("W"));
        if (!(wVal instanceof COSArray)) return;
        COSArray wArray = (COSArray) wVal;

        int i = 0;
        while (i < wArray.size()) {
            COSBase first = wArray.get(i);
            if (!(first instanceof COSInteger)) { i++; continue; }
            int cidFirst = ((COSInteger) first).intValue();

            if (i + 1 >= wArray.size()) break;
            COSBase second = wArray.get(i + 1);

            if (second instanceof COSArray) {
                // Format: cidFirst [w1 w2 w3 ...]
                COSArray widths = (COSArray) second;
                for (int j = 0; j < widths.size(); j++) {
                    cidWidths.put(cidFirst + j, getNumber(widths.get(j)));
                }
                i += 2;
            } else if (second instanceof COSInteger) {
                // Format: cidFirst cidLast w
                if (i + 2 >= wArray.size()) break;
                int cidLast = ((COSInteger) second).intValue();
                double width = getNumber(wArray.get(i + 2));
                for (int cid = cidFirst; cid <= cidLast; cid++) {
                    cidWidths.put(cid, width);
                }
                i += 3;
            } else {
                i++;
            }
        }
    }
}
