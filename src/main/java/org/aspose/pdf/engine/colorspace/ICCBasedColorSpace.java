package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * ICCBased color space (ISO 32000-1:2008, §8.6.5.5).
 * <p>
 * Wraps an ICC profile stream. Since we do not parse ICC profiles
 * (would require java.awt.color.ICC_Profile), we use the /Alternate
 * color space for actual color conversion. The /N entry provides the
 * number of components.
 * </p>
 */
public class ICCBasedColorSpace extends ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(ICCBasedColorSpace.class.getName());

    private final int numComponents;
    private final ColorSpaceBase alternate;

    /**
     * Creates an ICCBasedColorSpace from a profile stream.
     *
     * @param iccStream the ICC profile stream (must have /N)
     * @param parser    the PDF parser (may be null)
     * @throws IOException if reading the stream dict fails
     */
    public ICCBasedColorSpace(COSStream iccStream, PDFParser parser) throws IOException {
        this.numComponents = iccStream.getInt("N", 3);

        // Try /Alternate
        COSBase alt = iccStream.get("Alternate");
        if (alt != null) {
            alt = resolveRef(alt);
            ColorSpaceBase resolved = ColorSpaceBase.resolve(alt, null, parser);
            if (resolved != null) {
                this.alternate = resolved;
                return;
            }
        }

        // Default based on N
        switch (numComponents) {
            case 1:
                this.alternate = DeviceGray.INSTANCE;
                break;
            case 4:
                this.alternate = DeviceCMYK.INSTANCE;
                break;
            default:
                this.alternate = DeviceRGB.INSTANCE;
                break;
        }
    }

    @Override
    public String getName() { return "ICCBased"; }

    @Override
    public int getNumberOfComponents() { return numComponents; }

    /**
     * Returns the alternate (fallback) color space used for conversions.
     *
     * @return the alternate color space
     */
    public ColorSpaceBase getAlternate() { return alternate; }
}
