package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents a floating box container that can hold paragraph elements at a specific
/// position on the page.
///
/// A floating box has explicit width and height, and can be positioned using
/// [#setLeft(double)] and [#setTop(double)]. Its content is a
/// [Paragraphs] collection of [BaseParagraph] elements.
///
public class FloatingBox extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(FloatingBox.class.getName());

    private double width;
    private double height;
    private Paragraphs paragraphs;
    private double left;
    private double top;
    private Color backgroundColor;
    private BorderInfo border;
    private MarginInfo padding;

    /// Creates a new FloatingBox with zero dimensions.
    public FloatingBox() {
        // defaults
    }

    /// Creates a new FloatingBox with the specified dimensions.
    ///
    /// @param width  the width in points
    /// @param height the height in points
    public FloatingBox(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /// Returns the width of this floating box in points.
    ///
    /// @return the width
    public double getWidth() {
        return width;
    }

    /// Sets the width of this floating box in points.
    ///
    /// @param width the width
    public void setWidth(double width) {
        this.width = width;
    }

    /// Returns the height of this floating box in points.
    ///
    /// @return the height
    public double getHeight() {
        return height;
    }

    /// Sets the height of this floating box in points.
    ///
    /// @param height the height
    public void setHeight(double height) {
        this.height = height;
    }

    /// Returns the paragraphs collection for this floating box, creating it lazily if needed.
    ///
    /// @return the paragraphs collection; never `null`
    public Paragraphs getParagraphs() {
        if (paragraphs == null) {
            paragraphs = new Paragraphs();
        }
        return paragraphs;
    }

    /// Sets the paragraphs collection for this floating box.
    ///
    /// @param paragraphs the paragraphs collection
    public void setParagraphs(Paragraphs paragraphs) {
        this.paragraphs = paragraphs;
    }

    /// Returns the left position offset in points.
    ///
    /// @return the left offset
    public double getLeft() {
        return left;
    }

    /// Sets the left position offset in points.
    ///
    /// @param left the left offset
    public void setLeft(double left) {
        this.left = left;
    }

    /// Returns the top position offset in points.
    ///
    /// @return the top offset
    public double getTop() {
        return top;
    }

    /// Sets the top position offset in points.
    ///
    /// @param top the top offset
    public void setTop(double top) {
        this.top = top;
    }

    /// Returns the background color of this floating box.
    ///
    /// @return the background color, or `null`
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /// Sets the background color of this floating box.
    ///
    /// @param backgroundColor the background color
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /// Returns the border styling for this floating box.
    ///
    /// @return the border info, or `null`
    public BorderInfo getBorder() {
        return border;
    }

    /// Sets the border styling for this floating box.
    ///
    /// @param border the border info
    public void setBorder(BorderInfo border) {
        this.border = border;
    }

    /// Returns the padding (internal margin) for this floating box.
    ///
    /// @return the padding info, or `null`
    public MarginInfo getPadding() {
        return padding;
    }

    /// Sets the padding (internal margin) for this floating box.
    ///
    /// @param padding the padding info
    public void setPadding(MarginInfo padding) {
        this.padding = padding;
    }
}
