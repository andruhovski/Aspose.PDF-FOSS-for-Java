package org.aspose.pdf.facades;

import org.aspose.pdf.Rectangle;

/// Lightweight information about a stamp placed on a page.
public class StampInfo {

    private final int stampId;
    private final String stampType;
    private final String text;
    private final Rectangle rectangle;

    /// Creates a stamp info record.
    ///
    /// @param stampId the stamp identifier
    /// @param stampType the stamp type
    /// @param rectangle the stamp rectangle, may be `null`
    public StampInfo(int stampId, String stampType, String text, Rectangle rectangle) {
        this.stampId = stampId;
        this.stampType = stampType;
        this.text = text;
        this.rectangle = rectangle;
    }

    /// Returns the stamp identifier.
    ///
    /// @return the stamp identifier
    public int getStampId() {
        return stampId;
    }

    /// Returns the stamp type.
    ///
    /// @return the stamp type
    public String getStampType() {
        return stampType;
    }

    /// Returns the stamp text when available.
    ///
    /// @return stamp text or `null`
    public String getText() {
        return text;
    }

    /// Returns the stamp rectangle.
    ///
    /// @return the stamp rectangle
    public Rectangle getRectangle() {
        return rectangle;
    }
}
