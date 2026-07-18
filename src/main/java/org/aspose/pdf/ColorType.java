package org.aspose.pdf;

/// Classification of the dominant colour content of a [Page] — used by
/// [Page#getColorType()] to answer "is this page colour, grayscale, or
/// black-and-white?".
///
/// The constant order matches Aspose.Pdf's
/// `Aspose.Pdf.ColorType` enum (RGB=0, Grayscale=1, BlackAndWhite=2,
/// Undefined=3) so `ColorType.values()[i]` works the same way for ports
/// that index into the enum.
public enum ColorType {
    /// Page contains color (RGB or CMYK) content.
    Rgb,
    /// Page contains shades of gray but no chromatic colour.
    Grayscale,
    /// Page contains only black and white pixels (no gray midtones).
    BlackAndWhite,
    /// Page is empty or its content could not be classified.
    Undefined
}
