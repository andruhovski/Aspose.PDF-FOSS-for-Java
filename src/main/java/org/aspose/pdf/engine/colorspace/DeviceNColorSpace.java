package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.function.PdfFunction;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.IOException;
import java.util.logging.Logger;

/// DeviceN color space (ISO 32000-1:2008, §8.6.6.5).
/// N named colorants mapped to an alternate color space via a tint transform.
/// Generalizes Separation to multiple components.
///
/// Format: `[/DeviceN [names] alternateCS tintTransform {attributes}]`
public final class DeviceNColorSpace extends ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(DeviceNColorSpace.class.getName());

    private final String[] colorantNames;
    private final ColorSpaceBase alternateCS;
    private final PdfFunction tintTransform;

    /// Creates a DeviceN color space.
    ///
    /// @param colorantNames the colorant names
    /// @param alternateCS   the alternate (fallback) color space
    /// @param tintTransform the tint transform function
    public DeviceNColorSpace(String[] colorantNames, ColorSpaceBase alternateCS,
                              PdfFunction tintTransform) {
        this.colorantNames = colorantNames;
        this.alternateCS = alternateCS != null ? alternateCS : DeviceRGB.INSTANCE;
        this.tintTransform = tintTransform;
    }

    /// Parses a DeviceN color space from a PDF array.
    ///
    /// @param arr       the array `[/DeviceN [names] alternateCS tintTransform]`
    /// @param resources the page resources
    /// @param parser    the PDF parser
    /// @return the parsed color space
    /// @throws IOException if parsing fails
    public static DeviceNColorSpace fromArray(PdfArray arr, Resources resources,
                                               PDFParser parser) throws IOException {
        String[] names = new String[0];
        if (arr.size() > 1) {
            PdfBase namesObj = resolveRef(arr.get(1));
            if (namesObj instanceof PdfArray) {
                PdfArray namesArr = (PdfArray) namesObj;
                names = new String[namesArr.size()];
                for (int i = 0; i < namesArr.size(); i++) {
                    PdfBase n = resolveRef(namesArr.get(i));
                    names[i] = (n instanceof PdfName) ? ((PdfName) n).getName() : "Unknown";
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

    /// Converts N tint values to alternate color space components.
    ///
    /// @param tints the tint values (one per colorant, each 0..1)
    /// @return the alternate color space component values
    public double[] tintsToAlternate(double[] tints) {
        if (tintTransform == null) return tints;
        return tintTransform.evaluate(tints);
    }

    @Override
    public String getName() { return "DeviceN"; }

    @Override
    public int getNumberOfComponents() { return colorantNames.length; }

    /// Tints → alternate components (via tint transform) → alternate's RGB.
    @Override
    public int toRGBInt(double[] comps) {
        if (comps == null || comps.length == 0) return 0xFF000000;
        return alternateCS.toRGBInt(tintsToAlternate(comps));
    }

    /// Returns the colorant names.
    public String[] getColorantNames() { return colorantNames.clone(); }

    /// Returns the alternate (fallback) color space.
    public ColorSpaceBase getAlternateCS() { return alternateCS; }

    /// Returns the tint transform function.
    public PdfFunction getTintTransform() { return tintTransform; }
}
