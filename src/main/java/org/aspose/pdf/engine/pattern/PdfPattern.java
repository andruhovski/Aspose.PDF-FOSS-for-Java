package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.Matrix;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base for PDF patterns (ISO 32000-1:2008, §8.7).
 * PatternType 1 = Tiling, PatternType 2 = Shading.
 */
public abstract class PdfPattern {

    private static final Logger LOG = Logger.getLogger(PdfPattern.class.getName());

    /** The underlying pattern dictionary (or stream for tiling patterns). */
    protected final COSDictionary dict;

    protected PdfPattern(COSDictionary dict) {
        this.dict = dict;
    }

    /**
     * Returns the pattern type: 1 for tiling, 2 for shading.
     *
     * @return the pattern type
     */
    public int getPatternType() { return dict.getInt("PatternType", 0); }

    /**
     * Returns the pattern matrix that maps pattern space to user space.
     *
     * @return the matrix, or {@link Matrix#IDENTITY} if not specified
     */
    public Matrix getMatrix() {
        COSBase m = resolveRef(dict.get("Matrix"));
        if (m instanceof COSArray && ((COSArray) m).size() == 6) {
            return Matrix.fromCOSArray((COSArray) m);
        }
        return Matrix.IDENTITY;
    }

    /**
     * Returns the underlying COS dictionary.
     *
     * @return the pattern dictionary
     */
    public COSDictionary getCOSDictionary() { return dict; }

    /**
     * Factory: parses a pattern from a COS object.
     *
     * @param obj    the pattern object (dictionary or stream)
     * @param parser the PDF parser for resolving references
     * @return the parsed pattern, or {@code null} if unparseable
     * @throws IOException if parsing fails
     */
    public static PdfPattern parse(COSBase obj, PDFParser parser) throws IOException {
        obj = resolveRef(obj);
        if (obj instanceof COSStream) {
            COSStream stream = (COSStream) obj;
            int type = stream.getInt("PatternType", 0);
            if (type == 1) return new TilingPattern(stream, parser);
            if (type == 2) return new ShadingPattern(stream, parser);
        } else if (obj instanceof COSDictionary) {
            COSDictionary dict = (COSDictionary) obj;
            int type = dict.getInt("PatternType", 0);
            if (type == 1) return new TilingPattern(dict, parser);
            if (type == 2) return new ShadingPattern(dict, parser);
        }
        return null;
    }

    /**
     * Resolves indirect object references.
     */
    protected static COSBase resolveRef(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try { return ((COSObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }

    /**
     * Extracts a numeric array from a dictionary entry.
     */
    protected static double[] getNumberArray(COSDictionary dict, String key) {
        COSBase val = dict.get(key);
        if (val instanceof COSArray) {
            COSArray arr = (COSArray) val;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0f);
            }
            return result;
        }
        return null;
    }

    /**
     * Extracts a boolean array from a dictionary entry.
     */
    protected static boolean[] getBooleanArray(COSDictionary dict, String key) {
        COSBase val = dict.get(key);
        if (val instanceof COSArray) {
            COSArray arr = (COSArray) val;
            boolean[] result = new boolean[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = arr.get(i);
                if (item instanceof org.aspose.pdf.engine.cos.COSBoolean) {
                    result[i] = ((org.aspose.pdf.engine.cos.COSBoolean) item).getValue();
                }
            }
            return result;
        }
        return null;
    }
}
