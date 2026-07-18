package org.aspose.pdf.drawing;

import java.util.logging.Logger;

/// Abstract base class for all drawing shapes in the PDF drawing API.
///
/// Each shape carries its own [GraphInfo] styling properties and an optional
/// text label. Concrete subclasses define specific geometry (rectangle, circle, line,
/// etc.) and implement bounds checking logic.
///
public abstract class Shape {

    private static final Logger LOG = Logger.getLogger(Shape.class.getName());

    private GraphInfo graphInfo = new GraphInfo();
    private String text;

    /// Creates a new shape with default graphic styling.
    protected Shape() {
        // defaults applied by field initializers
    }

    /// Gets the graphic styling properties for this shape.
    ///
    /// @return the graph info; never `null`
    public GraphInfo getGraphInfo() {
        return graphInfo;
    }

    /// Sets the graphic styling properties for this shape.
    ///
    /// @param graphInfo the graph info to apply
    public void setGraphInfo(GraphInfo graphInfo) {
        this.graphInfo = graphInfo;
    }

    /// Gets the optional text label associated with this shape.
    ///
    /// @return the text, or `null` if not set
    public String getText() {
        return text;
    }

    /// Sets the optional text label associated with this shape.
    ///
    /// @param text the text label
    public void setText(String text) {
        this.text = text;
    }

    /// Checks whether this shape fits within the specified container bounds.
    ///
    /// If the shape extends beyond the given width or height, a
    /// [BoundsOutOfRangeException] is thrown.
    ///
    /// @param width  the container width in user-space units
    /// @param height the container height in user-space units
    /// @throws BoundsOutOfRangeException if the shape does not fit
    public abstract void checkBounds(double width, double height);
}
