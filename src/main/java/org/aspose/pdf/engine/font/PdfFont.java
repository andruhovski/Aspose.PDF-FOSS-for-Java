package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.font.cmap.CMapParser;
import org.aspose.pdf.engine.font.cmap.ToUnicodeCMap;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base class for all PDF font types (ISO 32000-1:2008, §9.5).
 * <p>
 * Handles common font operations including ToUnicode CMap parsing, encoding setup,
 * and the decode pipeline (ToUnicode → Encoding → identity fallback).
 * Concrete subclasses: {@code Type1Font}, {@code TrueTypeFont}, {@code Type0Font},
 * {@code CIDFont}.
 * </p>
 */
public abstract class PdfFont {

    private static final Logger LOG = Logger.getLogger(PdfFont.class.getName());

    /** The underlying font dictionary. */
    protected final COSDictionary fontDict;
    /** The PDF parser for resolving indirect references. */
    protected final PDFParser parser;
    /** Font name from /BaseFont. */
    protected String baseFont;
    /** The font encoding (char code → glyph name → Unicode). */
    protected FontEncoding encoding;
    /** The ToUnicode CMap (highest priority for decode). */
    protected ToUnicodeCMap toUnicode;
    /** Font descriptor. */
    protected FontDescriptor fontDescriptor;
    /** Font metrics. */
    protected FontMetrics fontMetrics;

    /**
     * Creates a PdfFont from a font dictionary.
     *
     * @param fontDict the font dictionary
     * @param parser   the PDF parser for resolving indirect references (may be null)
     */
    protected PdfFont(COSDictionary fontDict, PDFParser parser) {
        this.fontDict = fontDict != null ? fontDict : new COSDictionary();
        this.parser = parser;
        this.baseFont = this.fontDict.getNameAsString("BaseFont");
        initFontDescriptor();
        initToUnicode();
    }

    /**
     * Decodes raw character code bytes to a Unicode string.
     * <p>
     * The default implementation uses the three-level pipeline:
     * 1. ToUnicode CMap (highest priority)
     * 2. Encoding (glyph name → AdobeGlyphList)
     * 3. Identity fallback (charCode as-is)
     * </p>
     *
     * @param charCodes the raw bytes from the PDF content stream
     * @return the decoded Unicode string
     * @throws IOException if decoding fails
     */
    public String decode(byte[] charCodes) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte b : charCodes) {
            int code = b & 0xFF;
            // 1. Try ToUnicode CMap first
            if (toUnicode != null) {
                String mapped = toUnicode.lookup(code);
                if (mapped != null) {
                    sb.append(mapped);
                    continue;
                }
            }
            // 2. Try encoding
            if (encoding != null) {
                int unicode = encoding.getUnicode(code);
                if (unicode > 0) {
                    sb.append((char) unicode);
                    continue;
                }
            }
            // 3. Fallback: char code as-is
            sb.append((char) code);
        }
        return sb.toString();
    }

    /**
     * Returns the glyph width for the given character code, in units of 1/1000 text space.
     *
     * @param charCode the character code
     * @return the width
     */
    public abstract double getWidth(int charCode);

    /**
     * Returns the base font name (/BaseFont).
     *
     * @return the font name, or null
     */
    public String getBaseFont() {
        return baseFont;
    }

    /**
     * Returns the font encoding.
     *
     * @return the encoding, or null
     */
    public FontEncoding getEncoding() {
        return encoding;
    }

    /**
     * Returns the ToUnicode CMap.
     *
     * @return the ToUnicode CMap, or null
     */
    public ToUnicodeCMap getToUnicode() {
        return toUnicode;
    }

    /**
     * Returns the font descriptor.
     *
     * @return the font descriptor, or null
     */
    public FontDescriptor getFontDescriptor() {
        return fontDescriptor;
    }

    /**
     * Returns {@code true} when this is a Type0 composite font whose
     * content-stream encoding uses multi-byte character codes (e.g.
     * Identity-H = 2 bytes per CID). Used by the renderer to decide
     * whether to iterate {@code Tj} raw bytes one-by-one or in 2-byte
     * chunks. Simple fonts (Type1, TrueType) return {@code false}.
     */
    public boolean isComposite() {
        return false;
    }

    /**
     * Returns the font metrics.
     *
     * @return the font metrics
     */
    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    /**
     * Returns the underlying font dictionary.
     *
     * @return the font dictionary
     */
    public COSDictionary getFontDictionary() {
        return fontDict;
    }

    /**
     * Creates the appropriate PdfFont subclass from a font dictionary.
     *
     * @param fontDict the font dictionary with /Type /Font
     * @param parser   the PDF parser
     * @return the appropriate PdfFont instance
     * @throws IOException if font creation fails
     */
    public static PdfFont fromDictionary(COSDictionary fontDict, PDFParser parser) throws IOException {
        if (fontDict == null) {
            throw new IllegalArgumentException("Font dictionary must not be null");
        }
        String subtype = fontDict.getNameAsString("Subtype");
        if (subtype == null) {
            LOG.warning("Font dictionary missing /Subtype, defaulting to Type1");
            subtype = "Type1";
        }
        switch (subtype) {
            case "Type1":
            case "MMType1":
                return new Type1Font(fontDict, parser);
            case "TrueType":
                return new TrueTypeFont(fontDict, parser);
            case "Type0":
                return new Type0Font(fontDict, parser);
            case "CIDFontType0":
            case "CIDFontType2":
                return new CIDFont(fontDict, parser);
            case "Type3":
                return new Type3Font(fontDict, parser);
            default:
                String st = subtype;
                LOG.warning(() -> "Unknown font subtype: " + st + ", using Type1 fallback");
                return new Type1Font(fontDict, parser);
        }
    }

    /**
     * Resolves a potentially indirect COS object reference.
     *
     * @param obj the COS object
     * @return the resolved object
     */
    protected COSBase resolve(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) obj).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference: " + e.getMessage());
                return null;
            }
        }
        return obj;
    }

    /**
     * Extracts a numeric value from a COS object.
     */
    protected static double getNumber(COSBase val) {
        if (val instanceof org.aspose.pdf.engine.cos.COSInteger) {
            return ((org.aspose.pdf.engine.cos.COSInteger) val).intValue();
        }
        if (val instanceof org.aspose.pdf.engine.cos.COSFloat) {
            return ((org.aspose.pdf.engine.cos.COSFloat) val).doubleValue();
        }
        return 0;
    }

    private void initFontDescriptor() {
        COSBase fdVal = resolve(fontDict.get("FontDescriptor"));
        if (fdVal instanceof COSDictionary) {
            this.fontDescriptor = new FontDescriptor((COSDictionary) fdVal);
            this.fontMetrics = new FontMetrics(this.fontDescriptor);
        } else {
            this.fontMetrics = new FontMetrics(null);
        }
    }

    private void initToUnicode() {
        COSBase tuVal = resolve(fontDict.get("ToUnicode"));
        if (tuVal instanceof COSStream) {
            try {
                this.toUnicode = CMapParser.parseToUnicode((COSStream) tuVal);
            } catch (IOException e) {
                LOG.warning(() -> "Failed to parse ToUnicode CMap: " + e.getMessage());
            }
        }
    }
}
