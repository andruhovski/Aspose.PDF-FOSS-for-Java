package org.aspose.pdf;

import org.aspose.pdf.operators.*;

import java.io.IOException;
import java.util.logging.Logger;

/// Converts RGB color values in a page's content stream to DeviceGray equivalents.
///
/// Uses the standard luminance formula:
/// `gray = 0.299 * R + 0.587 * G + 0.114 * B`
///
/// The following operator replacements are performed:
///
///   - `rg` (SetRGBColor) is replaced with `g` (SetGray)
///   - `RG` (SetRGBColorStroke) is replaced with `G` (SetGrayStroke)
///   - `sc` with 3 components is replaced with `sc` with 1 component
///   - `SC` with 3 components is replaced with `SC` with 1 component
///   - `cs DeviceRGB` is replaced with `cs DeviceGray`
///   - `CS DeviceRGB` is replaced with `CS DeviceGray`
public class RgbToDeviceGrayConversionStrategy {

    private static final Logger LOG = Logger.getLogger(RgbToDeviceGrayConversionStrategy.class.getName());

    /// Creates a new RGB to DeviceGray conversion strategy.
    public RgbToDeviceGrayConversionStrategy() {
        // default constructor
    }

    /// Converts all RGB color operators in the given page's content stream to their
    /// DeviceGray equivalents.
    ///
    /// The page's content stream is parsed, RGB color operators are replaced with
    /// gray equivalents using the luminance formula, and the modified content stream
    /// is written back to the page.
    ///
    /// @param page the page to convert
    /// @throws IOException if reading or writing the content stream fails
    /// @throws IllegalArgumentException if page is null
    public void convert(Page page) throws IOException {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }

        OperatorCollection ops = page.getContents();
        if (ops == null || ops.size() == 0) {
            return;
        }

        boolean modified = false;

        for (int i = 0; i < ops.size(); i++) {
            Operator op = ops.getAt(i);

            if (op instanceof SetRGBColor) {
                // rg → g
                SetRGBColor rgb = (SetRGBColor) op;
                double gray = rgbToGray(rgb.getR(), rgb.getG(), rgb.getB());
                ops.set(i + 1, new SetGray(gray));
                modified = true;

            } else if (op instanceof SetRGBColorStroke) {
                // RG → G
                SetRGBColorStroke rgb = (SetRGBColorStroke) op;
                double gray = rgbToGray(rgb.getR(), rgb.getG(), rgb.getB());
                ops.set(i + 1, new SetGrayStroke(gray));
                modified = true;

            } else if (op instanceof SetColor) {
                // sc with 3 components (RGB) → sc with 1 component (gray)
                SetColor sc = (SetColor) op;
                double[] components = sc.getComponents();
                if (components.length == 3) {
                    double gray = rgbToGray(components[0], components[1], components[2]);
                    ops.set(i + 1, new SetColor(gray));
                    modified = true;
                }

            } else if (op instanceof SetColorStroke) {
                // SC with 3 components (RGB) → SC with 1 component (gray)
                SetColorStroke sc = (SetColorStroke) op;
                double[] components = sc.getComponents();
                if (components.length == 3) {
                    double gray = rgbToGray(components[0], components[1], components[2]);
                    ops.set(i + 1, new SetColorStroke(gray));
                    modified = true;
                }

            } else if (op instanceof SetColorSpace) {
                // cs DeviceRGB → cs DeviceGray
                SetColorSpace cs = (SetColorSpace) op;
                if ("DeviceRGB".equals(cs.getColorSpaceName())) {
                    ops.set(i + 1, new SetColorSpace("DeviceGray"));
                    modified = true;
                }

            } else if (op instanceof SetColorSpaceStroke) {
                // CS DeviceRGB → CS DeviceGray
                SetColorSpaceStroke cs = (SetColorSpaceStroke) op;
                if ("DeviceRGB".equals(cs.getColorSpaceName())) {
                    ops.set(i + 1, new SetColorSpaceStroke("DeviceGray"));
                    modified = true;
                }
            }
        }

        if (modified) {
            page.setContents(ops);
            LOG.fine(() -> "Converted RGB operators to DeviceGray on page " + page.getNumber());
        }
    }

    /// Converts RGB color components to a single gray value using the standard
    /// luminance formula (ITU-R BT.601).
    ///
    /// @param r the red component (0.0 to 1.0)
    /// @param g the green component (0.0 to 1.0)
    /// @param b the blue component (0.0 to 1.0)
    /// @return the gray value (0.0 to 1.0)
    private static double rgbToGray(double r, double g, double b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }
}
