package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.logging.Logger;

/// Abstract base for PDF patterns (ISO 32000-1:2008, §8.7).
/// PatternType 1 = Tiling, PatternType 2 = Shading.
public abstract class PdfPattern {

    private static final Logger LOG = Logger.getLogger(PdfPattern.class.getName());

    /// The underlying pattern dictionary (or stream for tiling patterns).
    protected final PdfDictionary dict;

    protected PdfPattern(PdfDictionary dict) {
        this.dict = dict;
    }

    /// Returns the pattern type: 1 for tiling, 2 for shading.
    ///
    /// @return the pattern type
    public int getPatternType() { return dict.getInt("PatternType", 0); }

    /// Returns the pattern matrix that maps pattern space to user space.
    ///
    /// @return the matrix, or [Matrix#IDENTITY] if not specified
    public Matrix getMatrix() {
        PdfBase m = resolveRef(dict.get("Matrix"));
        if (m instanceof PdfArray && ((PdfArray) m).size() == 6) {
            return Matrix.fromPdfArray((PdfArray) m);
        }
        return Matrix.IDENTITY;
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the pattern dictionary
    public PdfDictionary getPdfDictionary() { return dict; }

    /// Factory: parses a pattern from a PDF object.
    ///
    /// @param obj    the pattern object (dictionary or stream)
    /// @param parser the PDF parser for resolving references
    /// @return the parsed pattern, or `null` if unparseable
    /// @throws IOException if parsing fails
    public static PdfPattern parse(PdfBase obj, PDFParser parser) throws IOException {
        obj = resolveRef(obj);
        if (obj instanceof PdfStream) {
            PdfStream stream = (PdfStream) obj;
            int type = stream.getInt("PatternType", 0);
            if (type == 1) return new TilingPattern(stream, parser);
            if (type == 2) return new ShadingPattern(stream, parser);
        } else if (obj instanceof PdfDictionary) {
            PdfDictionary dict = (PdfDictionary) obj;
            int type = dict.getInt("PatternType", 0);
            if (type == 1) return new TilingPattern(dict, parser);
            if (type == 2) return new ShadingPattern(dict, parser);
        }
        return null;
    }

    /// Resolves indirect object references.
    protected static PdfBase resolveRef(PdfBase obj) {
        if (obj instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }

    /// Extracts a numeric array from a dictionary entry.
    protected static double[] getNumberArray(PdfDictionary dict, String key) {
        PdfBase val = dict.get(key);
        if (val instanceof PdfArray) {
            PdfArray arr = (PdfArray) val;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0f);
            }
            return result;
        }
        return null;
    }

    /// Extracts a boolean array from a dictionary entry.
    protected static boolean[] getBooleanArray(PdfDictionary dict, String key) {
        PdfBase val = dict.get(key);
        if (val instanceof PdfArray) {
            PdfArray arr = (PdfArray) val;
            boolean[] result = new boolean[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                PdfBase item = arr.get(i);
                if (item instanceof org.aspose.pdf.engine.pdfobjects.PdfBoolean) {
                    result[i] = ((org.aspose.pdf.engine.pdfobjects.PdfBoolean) item).getValue();
                }
            }
            return result;
        }
        return null;
    }
}
