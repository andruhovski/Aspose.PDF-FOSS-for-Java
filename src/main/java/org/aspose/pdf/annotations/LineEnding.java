package org.aspose.pdf.annotations;

/**
 * Enumerates the line ending styles for line and polyline annotations.
 * <p>
 * Defined in ISO 32000-1:2008, Table 176.
 * </p>
 */
public enum LineEnding {
    /** No line ending. */
    None,
    /** A square filled with the annotation's interior color. */
    Square,
    /** A circle filled with the annotation's interior color. */
    Circle,
    /** A diamond shape filled with the annotation's interior color. */
    Diamond,
    /** Two short lines meeting at a point. */
    OpenArrow,
    /** Two short lines meeting at a point, closed by a third line. */
    ClosedArrow,
    /** A short line at the endpoint perpendicular to the line. */
    Butt,
    /** Two short lines meeting at a point in reverse direction. */
    ROpenArrow,
    /** Closed reverse arrow. */
    RClosedArrow,
    /** A short line at the endpoint that slashes across the line. */
    Slash
}
