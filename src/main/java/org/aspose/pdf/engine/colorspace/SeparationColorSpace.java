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
 * Separation color space (ISO 32000-1:2008, §8.6.6.4).
 * Single tint component mapped to an alternate color space via a tint transform function.
 * Used for spot colors (e.g., "PANTONE 485 C", "Gold", "Varnish").
 *
 * <p>Format: {@code [/Separation name alternateCS tintTransform]}</p>
 */
public final class SeparationColorSpace extends ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(SeparationColorSpace.class.getName());

    private final String colorantName;
    private final ColorSpaceBase alternateCS;
    private final PdfFunction tintTransform;

    /**
     * Creates a Separation color space.
     *
     * @param colorantName  the colorant name (e.g., "Cyan", "PANTONE 485 C")
     * @param alternateCS   the alternate (fallback) color space
     * @param tintTransform the tint transform function (maps tint 0..1 → alternate components)
     */
    public SeparationColorSpace(String colorantName, ColorSpaceBase alternateCS,
                                 PdfFunction tintTransform) {
        this.colorantName = colorantName;
        this.alternateCS = alternateCS != null ? alternateCS : DeviceRGB.INSTANCE;
        this.tintTransform = tintTransform;
    }

    /**
     * Parses a Separation color space from a COS array.
     *
     * @param arr       the array {@code [/Separation name alternateCS tintTransform]}
     * @param resources the page resources
     * @param parser    the PDF parser
     * @return the parsed color space
     * @throws IOException if parsing fails
     */
    public static SeparationColorSpace fromArray(COSArray arr, Resources resources,
                                                  PDFParser parser) throws IOException {
        String name = "Unknown";
        if (arr.size() > 1) {
            COSBase nameObj = resolveRef(arr.get(1));
            if (nameObj instanceof COSName) name = ((COSName) nameObj).getName();
        }
        ColorSpaceBase altCS = DeviceRGB.INSTANCE;
        if (arr.size() > 2) {
            altCS = ColorSpaceBase.resolve(arr.get(2), resources, parser);
        }
        PdfFunction func = null;
        if (arr.size() > 3) {
            func = PdfFunction.parse(arr.get(3), parser);
        }
        return new SeparationColorSpace(name, altCS, func);
    }

    /**
     * Converts a tint value (0..1) to alternate color space components.
     *
     * @param tint the tint value (0.0 = no ink, 1.0 = full ink)
     * @return the alternate color space component values
     */
    public double[] tintToAlternate(double tint) {
        if ("None".equals(colorantName)) {
            return new double[alternateCS.getNumberOfComponents()];
        }
        if (tintTransform == null) {
            return new double[]{clamp01(tint)};
        }
        return tintTransform.evaluate(new double[]{clamp01(tint)});
    }

    @Override
    public String getName() { return "Separation"; }

    @Override
    public int getNumberOfComponents() { return 1; }

    /** Returns the colorant name. */
    public String getColorantName() { return colorantName; }

    /** Returns the alternate (fallback) color space. */
    public ColorSpaceBase getAlternateCS() { return alternateCS; }

    /** Returns the tint transform function. */
    public PdfFunction getTintTransform() { return tintTransform; }
}
