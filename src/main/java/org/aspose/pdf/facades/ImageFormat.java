package org.aspose.pdf.facades;

/// Raster image formats supported by [PdfConverter].
///
/// Mirrors the subset of `System.Drawing.Imaging.ImageFormat` values
/// used by `Aspose.Pdf.Facades.PdfConverter`. `EMF` is accepted by
/// the API for source compatibility but rendered as PNG (vector EMF export is
/// not supported).
public enum ImageFormat {
    /// Portable Network Graphics, lossless, alpha-capable.
    PNG,
    /// JPEG, lossy, configurable quality via the dedicated overloads.
    JPEG,
    /// Bitmap (uncompressed RGB).
    BMP,
    /// Tagged Image File Format (single page).
    TIFF,
    /// Enhanced Metafile. Accepted for API parity; rendered as PNG since vector
    /// EMF output is out of scope for this library.
    EMF
}
