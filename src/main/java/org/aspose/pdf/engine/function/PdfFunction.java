package org.aspose.pdf.engine.function;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base for PDF functions (ISO 32000-1:2008, §7.10).
 * Functions map m input values to n output values.
 *
 * <p>Four function types are defined:</p>
 * <ul>
 *   <li>Type 0 — Sampled (lookup table with interpolation)</li>
 *   <li>Type 2 — Exponential Interpolation</li>
 *   <li>Type 3 — Stitching (combines subfunctions)</li>
 *   <li>Type 4 — PostScript Calculator</li>
 * </ul>
 */
public abstract class PdfFunction {

    private static final Logger LOG = Logger.getLogger(PdfFunction.class.getName());

    /** Domain: [min0 max0 min1 max1 ...] — 2*m values for m inputs. */
    protected final double[] domain;
    /** Range: [min0 max0 min1 max1 ...] — 2*n values for n outputs. May be null. */
    protected final double[] range;

    protected PdfFunction(double[] domain, double[] range) {
        this.domain = domain != null ? domain : new double[]{0, 1};
        this.range = range;
    }

    /**
     * Evaluates the function for the given input values.
     *
     * @param input the input values (length = getInputDimension())
     * @return the output values (length = getOutputDimension())
     */
    public abstract double[] evaluate(double[] input);

    /** Returns the number of input values. */
    public int getInputDimension() { return domain.length / 2; }

    /** Returns the number of output values. */
    public int getOutputDimension() { return range != null ? range.length / 2 : 0; }

    /**
     * Clamps a value to the given interval.
     *
     * @param val the value
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    protected static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Factory: parses a function from a COS object (dictionary or stream).
     *
     * @param obj    the function object
     * @param parser the PDF parser for resolving references
     * @return the parsed function, or {@code null} if unparseable
     * @throws IOException if parsing fails
     */
    public static PdfFunction parse(COSBase obj, PDFParser parser) throws IOException {
        obj = resolveRef(obj, parser);
        if (!(obj instanceof COSDictionary)) return null;
        COSDictionary dict = (COSDictionary) obj;

        int type = dict.getInt("FunctionType", -1);
        double[] domain = getNumberArray(dict, "Domain");
        double[] range = getNumberArray(dict, "Range");

        switch (type) {
            case 0: return new SampledFunction(dict, domain, range);
            case 2: return new ExponentialFunction(dict, domain, range);
            case 3: return new StitchingFunction(dict, domain, range, parser);
            case 4: return new PostScriptFunction(dict, domain, range);
            default:
                LOG.fine(() -> "Unknown function type: " + type);
                return null;
        }
    }

    /**
     * Extracts a numeric array from a dictionary entry.
     *
     * @param dict the dictionary
     * @param key  the key
     * @return the array of doubles, or {@code null} if not present
     */
    protected static double[] getNumberArray(COSDictionary dict, String key) {
        COSBase val = dict.get(key);
        if (val instanceof COSArray) {
            return ((COSArray) val).toFloatArray() != null
                    ? toDoubleArray((COSArray) val) : null;
        }
        return null;
    }

    private static double[] toDoubleArray(COSArray arr) {
        double[] result = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            result[i] = arr.getFloat(i, 0f);
        }
        return result;
    }

    /**
     * Creates an array filled with a given value.
     */
    protected static double[] fillArray(int length, double value) {
        double[] arr = new double[length];
        java.util.Arrays.fill(arr, value);
        return arr;
    }

    private static COSBase resolveRef(COSBase obj, PDFParser parser) throws IOException {
        if (obj instanceof COSObjectReference) {
            return ((COSObjectReference) obj).dereference();
        }
        return obj;
    }
}
