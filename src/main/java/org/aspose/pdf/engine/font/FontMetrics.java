package org.aspose.pdf.engine.font;

import java.util.logging.Logger;

/// Holds computed font metrics derived from a [FontDescriptor] and font-specific data.
///
/// Provides resolved values for ascent, descent, and glyph widths used by the
/// text extraction engine to calculate text positions and bounding boxes.
///
public class FontMetrics {

    private static final Logger LOG = Logger.getLogger(FontMetrics.class.getName());

    private double ascent;
    private double descent;
    private double capHeight;
    private double missingWidth;
    private double[] widths;

    /// Creates FontMetrics from a FontDescriptor.
    ///
    /// @param descriptor the font descriptor (may be null)
    public FontMetrics(FontDescriptor descriptor) {
        if (descriptor != null) {
            this.ascent = descriptor.getAscent();
            this.descent = descriptor.getDescent();
            this.capHeight = descriptor.getCapHeight();
            this.missingWidth = descriptor.getMissingWidth();
        } else {
            // Reasonable defaults
            this.ascent = 800;
            this.descent = -200;
            this.capHeight = 700;
            this.missingWidth = 0;
        }
    }

    /// Creates FontMetrics with explicit values.
    ///
    /// @param ascent       the font ascent
    /// @param descent      the font descent (usually negative)
    /// @param missingWidth the default width for unmapped characters
    public FontMetrics(double ascent, double descent, double missingWidth) {
        this.ascent = ascent;
        this.descent = descent;
        this.missingWidth = missingWidth;
    }

    /// Returns the ascent (maximum height above baseline) in glyph units (typically 1/1000 em).
    ///
    /// @return the ascent
    public double getAscent() {
        return ascent;
    }

    /// Returns the descent (maximum depth below baseline, usually negative).
    ///
    /// @return the descent
    public double getDescent() {
        return descent;
    }

    /// Returns the cap height (height of uppercase letters).
    ///
    /// @return the cap height
    public double getCapHeight() {
        return capHeight;
    }

    /// Returns the missing width — default width for characters not in the font's width table.
    ///
    /// @return the missing width in glyph units
    public double getMissingWidth() {
        return missingWidth;
    }

    /// Returns the custom width table if set.
    ///
    /// @return the width array, or null
    public double[] getWidths() {
        return widths;
    }

    /// Sets the custom width table.
    ///
    /// @param widths the width array
    public void setWidths(double[] widths) {
        this.widths = widths;
    }

    /// Sets the ascent.
    ///
    /// @param ascent the ascent value
    public void setAscent(double ascent) {
        this.ascent = ascent;
    }

    /// Sets the descent.
    ///
    /// @param descent the descent value
    public void setDescent(double descent) {
        this.descent = descent;
    }
}
