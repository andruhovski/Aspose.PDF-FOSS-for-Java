package org.aspose.pdf;

/**
 * Classification of the dominant colour content of a {@link Page} — used by
 * {@link Page#getColorType()} to answer "is this page colour, grayscale, or
 * black-and-white?".
 *
 * <p>The constant order matches Aspose.Pdf's
 * {@code Aspose.Pdf.ColorType} enum (RGB=0, Grayscale=1, BlackAndWhite=2,
 * Undefined=3) so {@code ColorType.values()[i]} works the same way for ports
 * that index into the enum.</p>
 */
public enum ColorType {
    /** Page contains color (RGB or CMYK) content. */
    Rgb,
    /** Page contains shades of gray but no chromatic colour. */
    Grayscale,
    /** Page contains only black and white pixels (no gray midtones). */
    BlackAndWhite,
    /** Page is empty or its content could not be classified. */
    Undefined
}
