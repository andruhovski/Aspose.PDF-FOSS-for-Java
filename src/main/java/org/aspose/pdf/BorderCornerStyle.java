package org.aspose.pdf;

/**
 * Specifies the corner style for table borders.
 * <p>
 * Controls whether table border corners are rendered as sharp right angles
 * or as rounded curves.
 * </p>
 */
public enum BorderCornerStyle {

    /**
     * Sharp (right-angle) corners (default).
     */
    None,

    /**
     * Rounded corners, using the radius specified by
     * {@link BorderInfo#getRoundedBorderRadius()}.
     */
    Round
}
