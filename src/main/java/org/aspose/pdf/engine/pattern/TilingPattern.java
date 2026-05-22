package org.aspose.pdf.engine.pattern;

import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Tiling pattern (ISO 32000-1:2008, §8.7.3).
 * A content stream replicated at fixed intervals to fill an area.
 *
 * <p>Key properties:</p>
 * <ul>
 *   <li>/PaintType: 1 = colored (pattern defines its own colors),
 *                    2 = uncolored (uses current non-pattern color)</li>
 *   <li>/TilingType: 1 = constant spacing, 2 = no distortion, 3 = faster</li>
 *   <li>/BBox: pattern cell bounding box</li>
 *   <li>/XStep, /YStep: horizontal and vertical tile spacing</li>
 *   <li>/Resources: pattern's own resources</li>
 * </ul>
 */
public final class TilingPattern extends PdfPattern {

    private static final Logger LOG = Logger.getLogger(TilingPattern.class.getName());

    /** Colored pattern: defines its own colors. */
    public static final int PAINT_COLORED = 1;
    /** Uncolored pattern: uses current non-pattern color. */
    public static final int PAINT_UNCOLORED = 2;

    /** Constant spacing: tiles are evenly spaced. */
    public static final int TILING_CONSTANT_SPACING = 1;
    /** No distortion: tile spacing may be altered to maintain appearance. */
    public static final int TILING_NO_DISTORTION = 2;
    /** Faster tiling: distortion and uneven spacing allowed. */
    public static final int TILING_FASTER = 3;

    private final PDFParser parser;

    /**
     * Creates a TilingPattern from its dictionary/stream.
     *
     * @param dict   the pattern dictionary (usually a COSStream)
     * @param parser the PDF parser
     */
    public TilingPattern(COSDictionary dict, PDFParser parser) {
        super(dict);
        this.parser = parser;
    }

    /** Returns the paint type: 1 = colored, 2 = uncolored. */
    public int getPaintType() { return dict.getInt("PaintType", 1); }

    /** Returns the tiling type: 1, 2, or 3. */
    public int getTilingType() { return dict.getInt("TilingType", 1); }

    /**
     * Returns the pattern cell bounding box.
     *
     * @return the bounding box
     */
    public Rectangle getBBox() {
        COSBase bbox = resolveRef(dict.get("BBox"));
        if (bbox instanceof COSArray && ((COSArray) bbox).size() == 4) {
            return Rectangle.fromCOSArray((COSArray) bbox);
        }
        return new Rectangle(0, 0, 1, 1);
    }

    /** Returns the horizontal tile spacing. */
    public double getXStep() { return dict.getFloat("XStep", 1.0f); }

    /** Returns the vertical tile spacing. */
    public double getYStep() { return dict.getFloat("YStep", 1.0f); }

    /**
     * Returns the pattern's own resources dictionary.
     *
     * @return the resources, or {@code null}
     */
    public Resources getResources() {
        COSBase res = resolveRef(dict.get("Resources"));
        if (res instanceof COSDictionary) {
            return new Resources((COSDictionary) res, parser);
        }
        return null;
    }

    /**
     * Parses the tiling pattern's content stream into operators.
     *
     * @return the operator collection
     * @throws IOException if parsing fails
     */
    public OperatorCollection getContents() throws IOException {
        if (dict instanceof COSStream) {
            return ContentStreamParser.parseToCollection((COSStream) dict);
        }
        return new OperatorCollection(java.util.Collections.emptyList());
    }
}
