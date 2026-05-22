package org.aspose.pdf.facades;

/**
 * Raster image formats supported by {@link PdfConverter}.
 *
 * <p>Mirrors the subset of {@code System.Drawing.Imaging.ImageFormat} values
 * used by {@code Aspose.Pdf.Facades.PdfConverter}. {@code EMF} is accepted by
 * the API for source compatibility but rendered as PNG (vector EMF export is
 * not supported).</p>
 */
public enum ImageFormat {
    /** Portable Network Graphics, lossless, alpha-capable. */
    PNG,
    /** JPEG, lossy, configurable quality via the dedicated overloads. */
    JPEG,
    /** Bitmap (uncompressed RGB). */
    BMP,
    /** Tagged Image File Format (single page). */
    TIFF,
    /**
     * Enhanced Metafile. Accepted for API parity; rendered as PNG since vector
     * EMF output is out of scope for this library.
     */
    EMF
}
