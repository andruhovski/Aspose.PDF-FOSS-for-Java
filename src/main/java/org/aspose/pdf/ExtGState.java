package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.util.logging.Logger;

/// Extended graphics state parameter dictionary (ISO 32000-1:2008, §8.4.5, Table 58).
///
/// Wraps a /ExtGState dictionary referenced via the "gs" operator.
/// Provides access to transparency (alpha), blend mode, soft mask,
/// line properties, and other graphics state parameters.
///
public class ExtGState {

    private static final Logger LOG = Logger.getLogger(ExtGState.class.getName());

    private final PdfDictionary dict;

    /// Creates an ExtGState from a graphics state parameter dictionary.
    ///
    /// @param dict the /ExtGState dictionary
    /// @throws IllegalArgumentException if dict is null
    public ExtGState(PdfDictionary dict) {
        if (dict == null) {
            throw new IllegalArgumentException("ExtGState dictionary must not be null");
        }
        this.dict = dict;
    }

    /// Returns the stroking alpha constant (/CA). Default: 1.0 (fully opaque).
    ///
    /// @return the stroking alpha (0..1)
    public double getStrokingAlpha() {
        return dict.getFloat("CA", 1.0f);
    }

    /// Returns the non-stroking alpha constant (/ca). Default: 1.0 (fully opaque).
    ///
    /// @return the non-stroking alpha (0..1)
    public double getNonStrokingAlpha() {
        return dict.getFloat("ca", 1.0f);
    }

    /// Returns the blend mode (/BM). Default: "Normal".
    ///
    /// @return the blend mode name
    public String getBlendMode() {
        String bm = dict.getNameAsString("BM");
        return bm != null ? bm : "Normal";
    }

    /// Returns the soft mask (/SMask) value — may be PdfName("None") or PdfDictionary.
    ///
    /// @return the soft mask object, or null
    public PdfBase getSoftMask() {
        return dict.get("SMask");
    }

    /// Returns the line width (/LW), or -1 if not set.
    ///
    /// @return the line width, or -1
    public double getLineWidth() {
        return dict.getFloat("LW", -1f);
    }

    /// Returns the line cap style (/LC), or -1 if not set.
    ///
    /// @return the line cap (0=butt, 1=round, 2=square), or -1
    public int getLineCap() {
        return dict.getInt("LC", -1);
    }

    /// Returns the line join style (/LJ), or -1 if not set.
    ///
    /// @return the line join (0=miter, 1=round, 2=bevel), or -1
    public int getLineJoin() {
        return dict.getInt("LJ", -1);
    }

    /// Returns the miter limit (/ML), or -1 if not set.
    ///
    /// @return the miter limit, or -1
    public double getMiterLimit() {
        return dict.getFloat("ML", -1f);
    }

    /// Returns the font entry (/Font) — [font size] array, or null.
    ///
    /// @return the font array, or null
    public PdfArray getFont() {
        PdfBase val = dict.get("Font");
        return val instanceof PdfArray ? (PdfArray) val : null;
    }

    /// Returns the overprint flag (/OP). Default: false.
    ///
    /// @return true if overprint is enabled
    public boolean getOverprint() {
        return dict.getBoolean("OP", false);
    }

    /// Returns the non-stroking overprint flag (/op). Default: false.
    ///
    /// @return true if non-stroking overprint is enabled
    public boolean getNonStrokingOverprint() {
        return dict.getBoolean("op", false);
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the raw dictionary
    public PdfDictionary getPdfDictionary() {
        return dict;
    }
}
