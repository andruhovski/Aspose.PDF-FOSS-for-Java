package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.colorspace.ColorSpaceBase;
import org.aspose.pdf.engine.colorspace.DeviceRGB;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base for shading dictionaries (ISO 32000-1:2008, §8.7.4.3).
 *
 * <p>Seven shading types are defined:</p>
 * <ol>
 *   <li>Function-based</li>
 *   <li>Axial (linear gradient)</li>
 *   <li>Radial (radial gradient)</li>
 *   <li>Free-form Gouraud-shaded triangle mesh</li>
 *   <li>Lattice-form Gouraud-shaded triangle mesh</li>
 *   <li>Coons patch mesh</li>
 *   <li>Tensor-product patch mesh</li>
 * </ol>
 */
public abstract class Shading {

    private static final Logger LOG = Logger.getLogger(Shading.class.getName());

    /** The underlying shading dictionary. */
    protected final COSDictionary dict;
    /** The color space used by this shading. */
    protected final ColorSpaceBase colorSpace;
    /** Optional background color (components in the shading's color space). */
    protected final double[] background;
    /** Optional bounding box. */
    protected final Rectangle bbox;
    /** Anti-aliasing flag. */
    protected final boolean antiAlias;

    protected Shading(COSDictionary dict, PDFParser parser) throws IOException {
        this.dict = dict;
        COSBase csObj = resolveRef(dict.get("ColorSpace"));
        this.colorSpace = (csObj != null)
                ? ColorSpaceBase.resolve(csObj, null, parser)
                : DeviceRGB.INSTANCE;
        this.background = getNumberArray(dict, "Background");
        COSBase bb = resolveRef(dict.get("BBox"));
        this.bbox = (bb instanceof COSArray && ((COSArray) bb).size() == 4)
                ? Rectangle.fromCOSArray((COSArray) bb) : null;
        this.antiAlias = dict.getBoolean("AntiAlias", false);
    }

    /**
     * Returns the shading type (1–7).
     *
     * @return the shading type
     */
    public abstract int getShadingType();

    /**
     * Evaluates the shading color at a point in shading coordinate space.
     * Returns color components in the shading's color space.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the color components
     */
    public abstract double[] getColorAt(double x, double y);

    /** Returns the color space. */
    public ColorSpaceBase getColorSpace() { return colorSpace; }

    /** Returns the optional background color, or {@code null}. */
    public double[] getBackground() { return background; }

    /** Returns the optional bounding box, or {@code null}. */
    public Rectangle getBBox() { return bbox; }

    /** Returns whether anti-aliasing is requested. */
    public boolean isAntiAlias() { return antiAlias; }

    /** Returns the underlying dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }

    /**
     * Factory: parses a shading from a COS object.
     *
     * @param obj    the shading object
     * @param parser the PDF parser
     * @return the parsed shading, or {@code null}
     * @throws IOException if parsing fails
     */
    public static Shading parse(COSBase obj, PDFParser parser) throws IOException {
        obj = resolveRef(obj);
        if (!(obj instanceof COSDictionary)) return null;
        COSDictionary dict = (COSDictionary) obj;
        int type = dict.getInt("ShadingType", 0);
        switch (type) {
            case 1: return new FunctionBasedShading(dict, parser);
            case 2: return new AxialShading(dict, parser);
            case 3: return new RadialShading(dict, parser);
            case 4: return new FreeFormGouraudShading(dict, parser);
            case 5: return new LatticeGouraudShading(dict, parser);
            case 6: return new CoonsPatchShading(dict, parser);
            case 7: return new TensorPatchShading(dict, parser);
            default:
                LOG.fine(() -> "Unknown shading type: " + type);
                return null;
        }
    }

    /** Resolves indirect references. */
    protected static COSBase resolveRef(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try { return ((COSObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }

    /** Extracts a numeric array from a dictionary. */
    protected static double[] getNumberArray(COSDictionary dict, String key) {
        COSBase val = dict.get(key);
        if (val instanceof COSArray) {
            COSArray arr = (COSArray) val;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) result[i] = arr.getFloat(i, 0f);
            return result;
        }
        return null;
    }

    /** Extracts a boolean array from a dictionary. */
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
