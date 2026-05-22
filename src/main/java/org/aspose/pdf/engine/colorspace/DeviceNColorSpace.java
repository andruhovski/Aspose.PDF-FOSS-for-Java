package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.function.PdfFunction;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * DeviceN color space (ISO 32000-1:2008, §8.6.6.5).
 * N named colorants mapped to an alternate color space via a tint transform.
 * Generalizes Separation to multiple components.
 *
 * <p>Format: {@code [/DeviceN [names] alternateCS tintTransform {attributes}]}</p>
 */
public final class DeviceNColorSpace extends ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(DeviceNColorSpace.class.getName());

    private final String[] colorantNames;
    private final ColorSpaceBase alternateCS;
    private final PdfFunction tintTransform;

    /**
     * Creates a DeviceN color space.
     *
     * @param colorantNames the colorant names
     * @param alternateCS   the alternate (fallback) color space
     * @param tintTransform the tint transform function
     */
    public DeviceNColorSpace(String[] colorantNames, ColorSpaceBase alternateCS,
                              PdfFunction tintTransform) {
        this.colorantNames = colorantNames;
        this.alternateCS = alternateCS != null ? alternateCS : DeviceRGB.INSTANCE;
        this.tintTransform = tintTransform;
    }

    /**
     * Parses a DeviceN color space from a COS array.
     *
     * @param arr       the array {@code [/DeviceN [names] alternateCS tintTransform]}
     * @param resources the page resources
     * @param parser    the PDF parser
     * @return the parsed color space
     * @throws IOException if parsing fails
     */
    public static DeviceNColorSpace fromArray(COSArray arr, Resources resources,
                                               PDFParser parser) throws IOException {
        String[] names = new String[0];
        if (arr.size() > 1) {
            COSBase namesObj = resolveRef(arr.get(1));
            if (namesObj instanceof COSArray) {
                COSArray namesArr = (COSArray) namesObj;
                names = new String[namesArr.size()];
                for (int i = 0; i < namesArr.size(); i++) {
                    COSBase n = resolveRef(namesArr.get(i));
                    names[i] = (n instanceof COSName) ? ((COSName) n).getName() : "Unknown";
                }
            }
        }
        ColorSpaceBase altCS = DeviceRGB.INSTANCE;
        if (arr.size() > 2) {
            altCS = ColorSpaceBase.resolve(arr.get(2), resources, parser);
        }
        PdfFunction func = null;
        if (arr.size() > 3) {
            func = PdfFunction.parse(arr.get(3), parser);
        }
        return new DeviceNColorSpace(names, altCS, func);
    }

    /**
     * Converts N tint values to alternate color space components.
     *
     * @param tints the tint values (one per colorant, each 0..1)
     * @return the alternate color space component values
     */
    public double[] tintsToAlternate(double[] tints) {
        if (tintTransform == null) return tints;
        return tintTransform.evaluate(tints);
    }

    @Override
    public String getName() { return "DeviceN"; }

    @Override
    public int getNumberOfComponents() { return colorantNames.length; }

    /** Returns the colorant names. */
    public String[] getColorantNames() { return colorantNames.clone(); }

    /** Returns the alternate (fallback) color space. */
    public ColorSpaceBase getAlternateCS() { return alternateCS; }

    /** Returns the tint transform function. */
    public PdfFunction getTintTransform() { return tintTransform; }
}
