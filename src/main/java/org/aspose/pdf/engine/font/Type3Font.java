package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Type 3 font (/Subtype /Type3) — ISO 32000-1:2008, §9.6.5.
 * <p>
 * Type 3 fonts define each glyph by a content stream in /CharProcs.
 * For text extraction, the glyph shapes are irrelevant — we only need
 * the character-to-Unicode mapping via /ToUnicode or /Encoding, which
 * the base class {@link PdfFont} already handles.
 * </p>
 */
public class Type3Font extends PdfFont {

    private static final Logger LOG = Logger.getLogger(Type3Font.class.getName());

    private double[] widths;
    private int firstChar;

    /**
     * Creates a Type3Font from a font dictionary.
     *
     * @param fontDict the font dictionary (/Type /Font, /Subtype /Type3)
     * @param parser   the PDF parser (may be null)
     * @throws IOException if reading the font data fails
     */
    public Type3Font(COSDictionary fontDict, PDFParser parser) throws IOException {
        super(fontDict, parser);

        // Read /Encoding
        initEncoding();

        // Read /Widths, /FirstChar, /LastChar
        initWidths();

        LOG.fine(() -> "Type3Font created: " + baseFont);
    }

    /**
     * Initializes the encoding from the /Encoding entry.
     * Type3 fonts may use a /Differences-based encoding.
     */
    private void initEncoding() {
        if (this.encoding != null) return; // already set by base class
        COSBase encObj = resolve(fontDict.get("Encoding"));
        if (encObj instanceof COSDictionary) {
            // Use FontEncoding.fromDictionary which handles BaseEncoding + Differences
            this.encoding = FontEncoding.fromDictionary((COSDictionary) encObj);
        } else if (encObj instanceof COSName) {
            this.encoding = FontEncoding.getInstance(((COSName) encObj).getName());
        }
    }

    /**
     * Initializes the widths array from /Widths, /FirstChar, /LastChar.
     */
    private void initWidths() {
        this.firstChar = fontDict.getInt("FirstChar", 0);
        COSBase widthsObj = resolve(fontDict.get("Widths"));
        if (widthsObj instanceof COSArray) {
            COSArray wa = (COSArray) widthsObj;
            this.widths = new double[wa.size()];
            for (int i = 0; i < wa.size(); i++) {
                this.widths[i] = getNumber(wa.get(i));
            }
        }
    }

    @Override
    public double getWidth(int charCode) {
        if (widths != null) {
            int idx = charCode - firstChar;
            if (idx >= 0 && idx < widths.length) {
                return widths[idx];
            }
        }
        return 0;
    }
}
