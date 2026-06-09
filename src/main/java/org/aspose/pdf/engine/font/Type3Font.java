package org.aspose.pdf.engine.font;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Type 3 font (/Subtype /Type3) — ISO 32000-1:2008, §9.6.5.
 * <p>
 * Type 3 fonts define each glyph by a content stream in /CharProcs.
 * For text extraction, the glyph shapes are irrelevant — we only need
 * the character-to-Unicode mapping via /ToUnicode or /Encoding, which
 * the base class {@link PdfFont} already handles. For rendering, the
 * renderer executes the glyph's content stream with the CTM set to
 * {@code FontMatrix × textState × Tm × CTM} (see §9.6.5 and §9.4.4);
 * this class exposes {@link #getFontMatrix()}, {@link #getCharProc(int)}
 * and {@link #getFontResources()} for that purpose.
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
    public Type3Font(PdfDictionary fontDict, PDFParser parser) throws IOException {
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
        PdfBase encObj = resolve(fontDict.get("Encoding"));
        if (encObj instanceof PdfDictionary) {
            // Use FontEncoding.fromDictionary which handles BaseEncoding + Differences
            this.encoding = FontEncoding.fromDictionary((PdfDictionary) encObj);
        } else if (encObj instanceof PdfName) {
            this.encoding = FontEncoding.getInstance(((PdfName) encObj).getName());
        }
    }

    /**
     * Initializes the widths array from /Widths, /FirstChar, /LastChar.
     */
    private void initWidths() {
        this.firstChar = fontDict.getInt("FirstChar", 0);
        PdfBase widthsObj = resolve(fontDict.get("Widths"));
        if (widthsObj instanceof PdfArray) {
            PdfArray wa = (PdfArray) widthsObj;
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

    /**
     * Returns the /FontMatrix mapping glyph space to text space (§9.6.5).
     * Unlike all other font types (fixed 1/1000 scale), Type3 declares its
     * own matrix — glyph coordinates AND /Widths values are in glyph space
     * and must be transformed by this matrix.
     *
     * @return the font matrix; identity-scaled {@code [0.001 0 0 0.001 0 0]}
     *         when the entry is missing (the implicit default of other types)
     */
    public Matrix getFontMatrix() {
        PdfBase fm = resolve(fontDict.get("FontMatrix"));
        if (fm instanceof PdfArray && ((PdfArray) fm).size() >= 6) {
            PdfArray a = (PdfArray) fm;
            return new Matrix(getNumber(a.get(0)), getNumber(a.get(1)),
                              getNumber(a.get(2)), getNumber(a.get(3)),
                              getNumber(a.get(4)), getNumber(a.get(5)));
        }
        return new Matrix(0.001, 0, 0, 0.001, 0, 0);
    }

    /**
     * Returns the glyph-description content stream for a character code:
     * /Encoding(/Differences) maps the code to a glyph name, which keys
     * into the /CharProcs dictionary (§9.6.5).
     *
     * @param charCode the character code from the content stream
     * @return the glyph's content stream, or null if unmapped/missing
     */
    public PdfStream getCharProc(int charCode) {
        if (encoding == null) return null;
        String glyphName = encoding.getGlyphName(charCode);
        if (glyphName == null) return null;
        PdfBase procs = resolve(fontDict.get("CharProcs"));
        if (!(procs instanceof PdfDictionary)) return null;
        PdfBase proc = resolve(((PdfDictionary) procs).get(glyphName));
        return (proc instanceof PdfStream) ? (PdfStream) proc : null;
    }

    /**
     * Returns the font's own /Resources dictionary for its glyph streams,
     * or null — in which case the spec (§9.6.5, Note 2) says to fall back
     * to the resources of the content stream that painted the text.
     */
    public PdfDictionary getFontResources() {
        PdfBase res = resolve(fontDict.get("Resources"));
        return (res instanceof PdfDictionary) ? (PdfDictionary) res : null;
    }
}
