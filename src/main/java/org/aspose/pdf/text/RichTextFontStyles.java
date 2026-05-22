package org.aspose.pdf.text;

/**
 * Defines bitmask constants for rich text font styles used in PDF annotations and form fields.
 * <p>
 * These constants can be combined using bitwise OR to specify multiple styles.
 * They correspond to styling declarations commonly used in rich text strings
 * within PDF annotations (ISO 32000-1:2008, Section 12.7.3.4).
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * int style = RichTextFontStyles.Bold | RichTextFontStyles.Italic;
 * freeTextAnnotation.setTextStyle(0, 10, style);
 * }</pre>
 * </p>
 */
public final class RichTextFontStyles {

    private RichTextFontStyles() {
        // Utility class — no instantiation
    }

    /** No style (normal). */
    public static final int Normal = 0;

    /** Bold font weight. */
    public static final int Bold = 1;

    /** Italic font style. */
    public static final int Italic = 2;

    /** Underline text decoration. */
    public static final int Underline = 4;

    /** Clear all existing styles before applying new ones. */
    public static final int ClearExisting = 8;
}
