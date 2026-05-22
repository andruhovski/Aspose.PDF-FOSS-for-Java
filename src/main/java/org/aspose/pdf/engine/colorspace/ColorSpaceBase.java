package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract base for all PDF color spaces (ISO 32000-1:2008, §8.6).
 * <p>
 * PDF defines three families of color spaces:
 * <ul>
 *   <li>Device: DeviceRGB, DeviceCMYK, DeviceGray</li>
 *   <li>CIE-based: CalRGB, CalGray, Lab, ICCBased</li>
 *   <li>Special: Indexed, Separation, DeviceN, Pattern</li>
 * </ul>
 * The {@link #resolve} factory method parses a color space specification
 * from a COSName or COSArray into the appropriate subclass.
 * </p>
 */
public abstract class ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(ColorSpaceBase.class.getName());

    /**
     * Returns the color space name (e.g., "DeviceRGB", "ICCBased", "Indexed").
     *
     * @return the color space name
     */
    public abstract String getName();

    /**
     * Returns the number of color components in this color space.
     *
     * @return 1 for gray, 3 for RGB, 4 for CMYK, etc.
     */
    public abstract int getNumberOfComponents();

    /**
     * Resolves a color space from a PDF object.
     * <p>
     * Handles COSName (e.g., /DeviceRGB) and COSArray (e.g., [/ICCBased stream]).
     * </p>
     *
     * @param csObj     the color space object (COSName or COSArray)
     * @param resources the page resources for named color space lookup (may be null)
     * @param parser    the PDF parser for resolving indirect refs (may be null)
     * @return the resolved color space, or DeviceRGB as default
     * @throws IOException if resolution fails
     */
    public static ColorSpaceBase resolve(COSBase csObj, Resources resources,
                                          PDFParser parser) throws IOException {
        if (csObj == null) {
            return DeviceRGB.INSTANCE;
        }
        csObj = resolveRef(csObj);

        if (csObj instanceof COSName) {
            return resolveByName(((COSName) csObj).getName(), resources, parser);
        }

        if (csObj instanceof COSArray) {
            return resolveFromArray((COSArray) csObj, resources, parser);
        }

        return DeviceRGB.INSTANCE;
    }

    private static ColorSpaceBase resolveByName(String name, Resources resources,
                                                 PDFParser parser) throws IOException {
        switch (name) {
            case "DeviceRGB":
            case "RGB":
                return DeviceRGB.INSTANCE;
            case "DeviceCMYK":
            case "CMYK":
                return DeviceCMYK.INSTANCE;
            case "DeviceGray":
            case "G":
                return DeviceGray.INSTANCE;
            case "Pattern":
                return PatternColorSpace.INSTANCE;
            default:
                // Look up in resources /ColorSpace dictionary
                if (resources != null) {
                    COSDictionary csDict = resources.getColorSpaces();
                    if (csDict != null) {
                        COSBase resolved = csDict.get(name);
                        if (resolved != null) {
                            resolved = resolveRef(resolved);
                            return resolve(resolved, null, parser);
                        }
                    }
                }
                LOG.fine(() -> "Unknown color space name: " + name + ", defaulting to DeviceRGB");
                return DeviceRGB.INSTANCE;
        }
    }

    private static ColorSpaceBase resolveFromArray(COSArray arr, Resources resources,
                                                    PDFParser parser) throws IOException {
        if (arr.size() == 0) return DeviceRGB.INSTANCE;

        COSBase firstElem = arr.get(0);
        if (!(firstElem instanceof COSName)) return DeviceRGB.INSTANCE;

        String csType = ((COSName) firstElem).getName();

        switch (csType) {
            case "ICCBased":
                if (arr.size() >= 2) {
                    COSBase streamObj = resolveRef(arr.get(1));
                    if (streamObj instanceof COSStream) {
                        return new ICCBasedColorSpace((COSStream) streamObj, parser);
                    }
                }
                return DeviceRGB.INSTANCE;

            case "Indexed":
                return IndexedColorSpace.fromArray(arr, resources, parser);

            case "Separation":
                return SeparationColorSpace.fromArray(arr, resources, parser);

            case "DeviceN":
                return DeviceNColorSpace.fromArray(arr, resources, parser);

            case "CalRGB":
                if (arr.size() >= 2) {
                    COSBase params = resolveRef(arr.get(1));
                    if (params instanceof COSDictionary) {
                        return new CalRGBColorSpace((COSDictionary) params);
                    }
                }
                return DeviceRGB.INSTANCE;

            case "CalGray":
                if (arr.size() >= 2) {
                    COSBase params = resolveRef(arr.get(1));
                    if (params instanceof COSDictionary) {
                        return new CalGrayColorSpace((COSDictionary) params);
                    }
                }
                return DeviceGray.INSTANCE;

            case "Lab":
                if (arr.size() >= 2) {
                    COSBase params = resolveRef(arr.get(1));
                    if (params instanceof COSDictionary) {
                        return new LabColorSpace((COSDictionary) params);
                    }
                }
                return DeviceRGB.INSTANCE;

            case "Pattern":
                return PatternColorSpace.INSTANCE;

            default:
                LOG.fine(() -> "Unknown color space array type: " + csType);
                return DeviceRGB.INSTANCE;
        }
    }

    /**
     * Resolves an indirect object reference.
     */
    protected static COSBase resolveRef(COSBase obj) throws IOException {
        if (obj instanceof COSObjectReference) {
            return ((COSObjectReference) obj).dereference();
        }
        return obj;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CIE color conversion utilities
    // ═══════════════════════════════════════════════════════════════

    /**
     * Converts CIE XYZ (D65 illuminant) to sRGB (0..1 per component).
     * Uses the standard sRGB matrix and gamma transfer function.
     *
     * @param x the X tristimulus value
     * @param y the Y tristimulus value
     * @param z the Z tristimulus value
     * @return array of [r, g, b] each in 0..1
     */
    protected static double[] xyzToSRGB(double x, double y, double z) {
        // XYZ → linear RGB (sRGB matrix, D65 illuminant)
        double rl =  3.2406 * x - 1.5372 * y - 0.4986 * z;
        double gl = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        double bl =  0.0557 * x - 0.2040 * y + 1.0570 * z;
        // Apply sRGB gamma
        return new double[]{srgbGamma(rl), srgbGamma(gl), srgbGamma(bl)};
    }

    /**
     * Applies the sRGB gamma transfer function to a linear component.
     */
    private static double srgbGamma(double linear) {
        linear = Math.max(0, Math.min(1, linear));
        if (linear <= 0.0031308) return 12.92 * linear;
        return 1.055 * Math.pow(linear, 1.0 / 2.4) - 0.055;
    }

    /**
     * Clamps a value to [0, 1].
     *
     * @param v the value
     * @return the clamped value
     */
    protected static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    /**
     * Extracts a triple (3-element double array) from a dictionary key.
     * Returns the default if not present or wrong size.
     *
     * @param dict       the dictionary
     * @param key        the key
     * @param defaultVal the default triple
     * @return the extracted triple
     */
    protected static double[] getTriple(COSDictionary dict, String key, double[] defaultVal) {
        double[] arr = getNumberArray(dict, key);
        return (arr != null && arr.length >= 3) ? arr : defaultVal;
    }

    /**
     * Extracts a numeric array from a dictionary entry.
     *
     * @param dict the dictionary
     * @param key  the key
     * @return array of doubles, or {@code null} if not present
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
}
