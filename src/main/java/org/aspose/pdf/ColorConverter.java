package org.aspose.pdf;

import org.aspose.pdf.operators.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Converts color operators in page content streams from one color space to another.
 * <p>
 * Handles the six device-color operators: rg/RG (RGB), k/K (CMYK), g/G (Gray).
 * Conversion formulas follow standard colorimetric transforms.
 * </p>
 */
public final class ColorConverter {

    private static final Logger LOG = Logger.getLogger(ColorConverter.class.getName());

    private ColorConverter() {
        // utility class
    }

    /**
     * Converts all color operators in every page of the document according to the given strategy.
     *
     * @param document the document to process
     * @param strategy the color conversion strategy
     * @throws IOException              if reading content streams fails
     * @throws IllegalArgumentException if document or strategy is null
     */
    public static void convert(Document document, ColorConversionStrategy strategy) throws IOException {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null");
        }
        if (strategy == ColorConversionStrategy.None) {
            return;
        }
        PageCollection pages = document.getPages();
        for (int i = 1; i <= pages.getCount(); i++) {
            convertPageColors(pages.get(i), strategy);
        }
    }

    /**
     * Converts color operators on a single page.
     */
    private static void convertPageColors(Page page, ColorConversionStrategy strategy) throws IOException {
        OperatorCollection contents = page.getContents();
        List<Operator> converted = new ArrayList<>(contents.size());
        boolean changed = false;

        for (Operator op : contents) {
            Operator replacement = convertOperator(op, strategy);
            if (replacement != op) {
                changed = true;
            }
            converted.add(replacement);
        }

        if (changed) {
            // Replace the page content stream with converted operators
            OperatorCollection newContents = new OperatorCollection(converted);
            // Write back to the page's content stream
            page.setContents(newContents);
            LOG.fine(() -> "Converted colors on page " + page.getNumber());
        }
    }

    /**
     * Converts a single operator if it is a color operator; otherwise returns it unchanged.
     */
    private static Operator convertOperator(Operator op, ColorConversionStrategy strategy) {
        // Non-stroking RGB (rg)
        if (op instanceof SetRGBColor) {
            SetRGBColor rgb = (SetRGBColor) op;
            return convertFromRgb(rgb.getR(), rgb.getG(), rgb.getB(), false, strategy);
        }
        // Stroking RGB (RG)
        if (op instanceof SetRGBColorStroke) {
            SetRGBColorStroke rgb = (SetRGBColorStroke) op;
            return convertFromRgb(rgb.getR(), rgb.getG(), rgb.getB(), true, strategy);
        }
        // Non-stroking CMYK (k)
        if (op instanceof SetCMYKColor) {
            SetCMYKColor cmyk = (SetCMYKColor) op;
            return convertFromCmyk(cmyk.getC(), cmyk.getM(), cmyk.getY(), cmyk.getK(), false, strategy);
        }
        // Stroking CMYK (K)
        if (op instanceof SetCMYKColorStroke) {
            SetCMYKColorStroke cmyk = (SetCMYKColorStroke) op;
            return convertFromCmyk(cmyk.getC(), cmyk.getM(), cmyk.getY(), cmyk.getK(), true, strategy);
        }
        // Non-stroking Gray (g)
        if (op instanceof SetGray) {
            SetGray gray = (SetGray) op;
            return convertFromGray(gray.getGray(), false, strategy);
        }
        // Stroking Gray (G)
        if (op instanceof SetGrayStroke) {
            SetGrayStroke gray = (SetGrayStroke) op;
            return convertFromGray(gray.getGray(), true, strategy);
        }
        return op;
    }

    // ── RGB source ──

    private static Operator convertFromRgb(double r, double g, double b,
                                           boolean stroking, ColorConversionStrategy strategy) {
        switch (strategy) {
            case ConvertToRgb:
                // already RGB, return as-is by creating same type
                return stroking ? new SetRGBColorStroke(r, g, b) : new SetRGBColor(r, g, b);
            case ConvertToGrayscale: {
                double gray = rgbToGray(r, g, b);
                return stroking ? new SetGrayStroke(gray) : new SetGray(gray);
            }
            case ConvertToCmyk: {
                double[] cmyk = rgbToCmyk(r, g, b);
                return stroking
                        ? new SetCMYKColorStroke(cmyk[0], cmyk[1], cmyk[2], cmyk[3])
                        : new SetCMYKColor(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
            }
            default:
                return stroking ? new SetRGBColorStroke(r, g, b) : new SetRGBColor(r, g, b);
        }
    }

    // ── CMYK source ──

    private static Operator convertFromCmyk(double c, double m, double y, double k,
                                            boolean stroking, ColorConversionStrategy strategy) {
        switch (strategy) {
            case ConvertToCmyk:
                return stroking
                        ? new SetCMYKColorStroke(c, m, y, k)
                        : new SetCMYKColor(c, m, y, k);
            case ConvertToRgb: {
                double[] rgb = cmykToRgb(c, m, y, k);
                return stroking
                        ? new SetRGBColorStroke(rgb[0], rgb[1], rgb[2])
                        : new SetRGBColor(rgb[0], rgb[1], rgb[2]);
            }
            case ConvertToGrayscale: {
                double[] rgb = cmykToRgb(c, m, y, k);
                double gray = rgbToGray(rgb[0], rgb[1], rgb[2]);
                return stroking ? new SetGrayStroke(gray) : new SetGray(gray);
            }
            default:
                return stroking
                        ? new SetCMYKColorStroke(c, m, y, k)
                        : new SetCMYKColor(c, m, y, k);
        }
    }

    // ── Gray source ──

    private static Operator convertFromGray(double gray, boolean stroking,
                                            ColorConversionStrategy strategy) {
        switch (strategy) {
            case ConvertToGrayscale:
                return stroking ? new SetGrayStroke(gray) : new SetGray(gray);
            case ConvertToRgb:
                return stroking
                        ? new SetRGBColorStroke(gray, gray, gray)
                        : new SetRGBColor(gray, gray, gray);
            case ConvertToCmyk:
                return stroking
                        ? new SetCMYKColorStroke(0, 0, 0, 1 - gray)
                        : new SetCMYKColor(0, 0, 0, 1 - gray);
            default:
                return stroking ? new SetGrayStroke(gray) : new SetGray(gray);
        }
    }

    // ── Conversion formulas ──

    /**
     * Converts RGB to grayscale using luminance weights.
     *
     * @param r red (0..1)
     * @param g green (0..1)
     * @param b blue (0..1)
     * @return gray level (0..1)
     */
    public static double rgbToGray(double r, double g, double b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    /**
     * Converts RGB to CMYK color values.
     *
     * @param r red (0..1)
     * @param g green (0..1)
     * @param b blue (0..1)
     * @return array of {c, m, y, k} each in range 0..1
     */
    public static double[] rgbToCmyk(double r, double g, double b) {
        double k = 1.0 - Math.max(r, Math.max(g, b));
        if (k >= 1.0) {
            return new double[]{0, 0, 0, 1};
        }
        double c = (1.0 - r - k) / (1.0 - k);
        double m = (1.0 - g - k) / (1.0 - k);
        double y = (1.0 - b - k) / (1.0 - k);
        return new double[]{c, m, y, k};
    }

    /**
     * Converts CMYK to RGB color values.
     *
     * @param c cyan (0..1)
     * @param m magenta (0..1)
     * @param y yellow (0..1)
     * @param k black/key (0..1)
     * @return array of {r, g, b} each in range 0..1
     */
    public static double[] cmykToRgb(double c, double m, double y, double k) {
        double r = (1.0 - c) * (1.0 - k);
        double g = (1.0 - m) * (1.0 - k);
        double b = (1.0 - y) * (1.0 - k);
        return new double[]{r, g, b};
    }
}
