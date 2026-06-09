package org.aspose.pdf.engine.font;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Wraps a PDF /FontDescriptor dictionary (ISO 32000-1:2008, §9.8, Table 122).
 * <p>
 * Provides font-level metrics (ascent, descent, cap height, etc.) and flags
 * describing font characteristics (fixed-pitch, serif, symbolic, italic, etc.).
 * Also gives access to embedded font program streams (/FontFile, /FontFile2, /FontFile3).
 * </p>
 */
public class FontDescriptor {

    private static final Logger LOG = Logger.getLogger(FontDescriptor.class.getName());

    private final PdfDictionary dict;

    /**
     * Creates a FontDescriptor from a /FontDescriptor dictionary.
     *
     * @param dict the font descriptor dictionary
     * @throws IllegalArgumentException if dict is null
     */
    public FontDescriptor(PdfDictionary dict) {
        if (dict == null) {
            throw new IllegalArgumentException("FontDescriptor dictionary must not be null");
        }
        this.dict = dict;
    }

    /**
     * Returns the PostScript font name (/FontName).
     *
     * @return the font name, or null if absent
     */
    public String getFontName() {
        String name = dict.getNameAsString("FontName");
        if (name == null) {
            // /FontName may be an indirect reference to a name object
            // (rare but legal — any object may be indirect, §7.3.10).
            PdfBase fn = dict.get("FontName");
            if (fn instanceof PdfObjectReference) {
                try {
                    fn = ((PdfObjectReference) fn).dereference();
                } catch (IOException e) {
                    return null;
                }
            }
            if (fn instanceof PdfName) {
                name = ((PdfName) fn).getName();
            }
        }
        return name;
    }

    /**
     * Returns the font flags bitmask (/Flags, §9.8.2, Table 123).
     *
     * @return the flags value, or 0 if absent
     */
    public int getFlags() {
        return dict.getInt("Flags", 0);
    }

    /**
     * Returns the ascent (/Ascent) — maximum height above baseline.
     *
     * @return the ascent in glyph units
     */
    public double getAscent() {
        return getDouble("Ascent", 0);
    }

    /**
     * Returns the descent (/Descent) — maximum depth below baseline (usually negative).
     *
     * @return the descent in glyph units
     */
    public double getDescent() {
        return getDouble("Descent", 0);
    }

    /**
     * Returns the cap height (/CapHeight) — height of capital letters.
     *
     * @return the cap height in glyph units
     */
    public double getCapHeight() {
        return getDouble("CapHeight", 0);
    }

    /**
     * Returns the italic angle (/ItalicAngle) in degrees counter-clockwise from vertical.
     *
     * @return the italic angle, or 0 if absent
     */
    public double getItalicAngle() {
        return getDouble("ItalicAngle", 0);
    }

    /**
     * Returns the dominant stem width (/StemV) for vertical stems.
     *
     * @return the stem width
     */
    public double getStemV() {
        return getDouble("StemV", 0);
    }

    /**
     * Returns the font bounding box (/FontBBox [llx lly urx ury]).
     *
     * @return the bounding box rectangle, or null if absent
     */
    public Rectangle getFontBBox() {
        PdfBase val = dict.get("FontBBox");
        if (val instanceof PdfArray) {
            PdfArray arr = (PdfArray) val;
            if (arr.size() == 4) {
                return Rectangle.fromPdfArray(arr);
            }
        }
        return null;
    }

    /**
     * Returns the missing width (/MissingWidth) for characters not in the font's /Widths.
     *
     * @return the missing width, or 0 if absent
     */
    public double getMissingWidth() {
        return getDouble("MissingWidth", 0);
    }

    /**
     * Returns the /FontFile stream (Type 1 font program).
     *
     * @return the font file stream, or null
     */
    public PdfStream getFontFile() {
        return getStream("FontFile");
    }

    /**
     * Returns the /FontFile2 stream (TrueType font program).
     *
     * @return the font file stream, or null
     */
    public PdfStream getFontFile2() {
        return getStream("FontFile2");
    }

    /**
     * Returns the /FontFile3 stream (CFF/OpenType font program).
     *
     * @return the font file stream, or null
     */
    public PdfStream getFontFile3() {
        return getStream("FontFile3");
    }

    /**
     * Returns true if the font is fixed-pitch (flag bit 1).
     *
     * @return true if fixed-pitch
     */
    public boolean isFixedPitch() {
        return (getFlags() & 0x01) != 0;
    }

    /**
     * Returns true if the font is serif (flag bit 2).
     *
     * @return true if serif
     */
    public boolean isSerif() {
        return (getFlags() & 0x02) != 0;
    }

    /**
     * Returns true if the font is symbolic (flag bit 3).
     *
     * @return true if symbolic
     */
    public boolean isSymbolic() {
        return (getFlags() & 0x04) != 0;
    }

    /**
     * Returns true if the font is italic (flag bit 7).
     *
     * @return true if italic
     */
    public boolean isItalic() {
        return (getFlags() & 0x40) != 0;
    }

    /**
     * Returns the underlying PDF dictionary.
     *
     * @return the font descriptor dictionary
     */
    public PdfDictionary getPdfDictionary() {
        return dict;
    }

    private double getDouble(String key, double defaultValue) {
        PdfBase val = dict.get(key);
        if (val instanceof PdfInteger) {
            return ((PdfInteger) val).intValue();
        }
        if (val instanceof PdfFloat) {
            return ((PdfFloat) val).doubleValue();
        }
        return defaultValue;
    }

    private PdfStream getStream(String key) {
        PdfBase val = dict.get(key);
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                LOG.fine(() -> "Failed to dereference " + key + ": " + e.getMessage());
                return null;
            }
        }
        if (val instanceof PdfStream) {
            return (PdfStream) val;
        }
        return null;
    }
}
