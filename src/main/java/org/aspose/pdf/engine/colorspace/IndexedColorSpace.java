package org.aspose.pdf.engine.colorspace;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Indexed color space (ISO 32000-1:2008, §8.6.6.3).
 * <p>
 * Maps a single integer index (0..hival) to a color in the base color space
 * via a lookup table. The table has (hival+1) entries, each consisting of
 * as many bytes as the base color space has components.
 * </p>
 */
public class IndexedColorSpace extends ColorSpaceBase {

    private static final Logger LOG = Logger.getLogger(IndexedColorSpace.class.getName());

    private final ColorSpaceBase base;
    private final int hival;
    private final byte[] lookupTable;

    /**
     * Creates an IndexedColorSpace.
     *
     * @param base        the base color space
     * @param hival       the maximum valid index (0..hival)
     * @param lookupTable the palette: (hival+1) * base.components bytes
     */
    public IndexedColorSpace(ColorSpaceBase base, int hival, byte[] lookupTable) {
        this.base = base != null ? base : DeviceRGB.INSTANCE;
        this.hival = Math.max(0, Math.min(hival, 255));
        this.lookupTable = lookupTable != null ? lookupTable : new byte[0];
    }

    /**
     * Creates an IndexedColorSpace from a COSArray: [/Indexed base hival lookup].
     *
     * @param arr       the COSArray definition
     * @param resources the page resources (may be null)
     * @param parser    the PDF parser (may be null)
     * @return the IndexedColorSpace
     * @throws IOException if parsing fails
     */
    public static IndexedColorSpace fromArray(COSArray arr, Resources resources,
                                               PDFParser parser) throws IOException {
        if (arr.size() < 4) {
            return new IndexedColorSpace(DeviceRGB.INSTANCE, 0, new byte[0]);
        }

        // arr[1] = base color space
        COSBase baseObj = resolveRef(arr.get(1));
        ColorSpaceBase base = ColorSpaceBase.resolve(baseObj, resources, parser);

        // arr[2] = hival
        int hival = 255;
        COSBase hivalObj = arr.get(2);
        if (hivalObj instanceof COSInteger) {
            hival = ((COSInteger) hivalObj).intValue();
        }

        // arr[3] = lookup table (string or stream)
        byte[] lookup;
        COSBase lookupObj = resolveRef(arr.get(3));
        if (lookupObj instanceof COSString) {
            lookup = ((COSString) lookupObj).getBytes();
        } else if (lookupObj instanceof COSStream) {
            lookup = ((COSStream) lookupObj).getDecodedData();
        } else {
            lookup = new byte[0];
        }

        return new IndexedColorSpace(base, hival, lookup);
    }

    @Override
    public String getName() { return "Indexed"; }

    @Override
    public int getNumberOfComponents() { return 1; }

    /**
     * Returns the base color space.
     *
     * @return the base color space
     */
    public ColorSpaceBase getBase() { return base; }

    /**
     * Returns the maximum valid index.
     *
     * @return the hival
     */
    public int getHival() { return hival; }

    /**
     * Looks up a palette entry, returning the base color space components.
     *
     * @param index the palette index (0..hival)
     * @return the color components in the base color space, each in [0, 1]
     */
    public double[] lookupColor(int index) {
        int nc = base.getNumberOfComponents();
        int safeIndex = Math.max(0, Math.min(index, hival));
        int offset = safeIndex * nc;
        double[] components = new double[nc];
        for (int i = 0; i < nc && offset + i < lookupTable.length; i++) {
            components[i] = (lookupTable[offset + i] & 0xFF) / 255.0;
        }
        return components;
    }
}
