package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.io.IOException;

/// Shading pattern — PatternType 2 (ISO 32000-1:2008, §8.7.4).
/// Wraps a shading dictionary and provides a pattern-level matrix and ExtGState.
public final class ShadingPattern extends PdfPattern {

    private final Shading shading;

    /// Creates a ShadingPattern from its dictionary.
    ///
    /// @param dict   the pattern dictionary
    /// @param parser the PDF parser
    /// @throws IOException if the shading cannot be parsed
    public ShadingPattern(PdfDictionary dict, PDFParser parser) throws IOException {
        super(dict);
        PdfBase shadingObj = resolveRef(dict.get("Shading"));
        this.shading = (shadingObj != null) ? Shading.parse(shadingObj, parser) : null;
    }

    /// Returns the shading object.
    ///
    /// @return the shading, or `null`
    public Shading getShading() { return shading; }

    /// Returns the ExtGState associated with this pattern (if any).
    ///
    /// @return the graphics state dictionary, or `null`
    public PdfDictionary getExtGState() {
        PdfBase gs = resolveRef(dict.get("ExtGState"));
        return (gs instanceof PdfDictionary) ? (PdfDictionary) gs : null;
    }
}
