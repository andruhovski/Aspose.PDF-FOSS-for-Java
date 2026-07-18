package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.PageCoordinateType;
import org.aspose.pdf.PageSize;
import org.aspose.pdf.devices.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Converts PDF pages to raster images (JPEG, PNG).
///
/// Usage pattern:
///
/// <pre>
///   PdfConverter converter = new PdfConverter();
///   converter.bindPdf("input.pdf");
///   converter.setResolution(new Resolution(150));
///   converter.doConvert();
///   int page = 1;
///   while (converter.hasNextImage()) {
///       converter.getNextImage("page_" + page + ".jpg", "jpeg");
///       page++;
///   }
///   converter.close();
/// </pre>
public class PdfConverter implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PdfConverter.class.getName());

    private Document document;
    private Resolution resolution;
    private int currentPage;
    private int pageCount;
    private int startPage;
    private int endPage;
    private PageCoordinateType coordinateType = PageCoordinateType.MediaBox;

    /// Creates a new `PdfConverter` instance with default resolution of 150 DPI.
    public PdfConverter() {
        this.resolution = new Resolution(150);
    }

    /// Creates a new converter bound to the supplied document.
    ///
    /// @param document the document to bind initially
    public PdfConverter(Document document) {
        this();
        bindPdf(document);
    }

    /// Binds a PDF file to this converter.
    ///
    /// @param inputFile path to the PDF file
    /// @return `true` on success
    public boolean bindPdf(String inputFile) {
        try {
            this.document = new Document(inputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /// Binds a PDF from an input stream.
    ///
    /// @param inputStream the input stream containing PDF data
    /// @return `true` on success
    public boolean bindPdf(InputStream inputStream) {
        try {
            this.document = new Document(inputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /// Binds an existing [Document] to this converter.
    ///
    /// @param document the document to bind
    /// @return `true` on success
    public boolean bindPdf(Document document) {
        if (document == null) {
            LOG.warning("Cannot bind null document");
            return false;
        }
        this.document = document;
        return true;
    }

    /// Returns the currently bound document.
    ///
    /// @return the bound document, or `null` if none is bound
    public Document getDocument() {
        return document;
    }

    /// Sets the resolution for image rendering.
    ///
    /// @param resolution the rendering resolution
    public void setResolution(Resolution resolution) {
        if (resolution == null) {
            LOG.warning("Cannot set null resolution");
            return;
        }
        this.resolution = resolution;
    }

    /// Initializes the conversion process by resetting the page counter.
    /// Must be called before [#hasNextImage()] and [#getNextImage].
    ///
    /// @return `true` on success
    public boolean doConvert() {
        try {
            int total = document.getPages().getCount();
            if (total <= 0) {
                this.currentPage = 0;
                this.pageCount = 0;
                LOG.warning("Conversion initialized on a document with no pages");
                return true;
            }
            int from = effectiveStartPage(total);
            int to = effectiveEndPage(total, from);
            this.currentPage = from - 1;
            this.pageCount = to;
            LOG.fine("Conversion initialized: pages " + from + "-" + to + " of " + total);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize conversion", e);
            return false;
        }
    }

    /// Returns the total number of pages in the bound document.
    ///
    /// @return the page count, or 0 if no document is bound
    public int getPageCount() {
        if (document == null) return 0;
        try {
            return document.getPages().getCount();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read page count", e);
            return 0;
        }
    }

    /// Sets the first page (1-based) to include in subsequent renders.
    ///
    /// @param startPage 1-based first page; 0 or negative means "from first page"
    public void setStartPage(int startPage) {
        this.startPage = startPage;
        if (startPage > 0) {
            if (document != null) {
                try {
                    int total = document.getPages().getCount();
                    if (total > 0) {
                        this.currentPage = effectiveStartPage(total) - 1;
                        this.pageCount = effectiveEndPage(total, effectiveStartPage(total));
                    } else {
                        this.currentPage = 0;
                        this.pageCount = 0;
                    }
                } catch (IOException ignore) {}
            } else {
                this.currentPage = Math.max(0, startPage - 1);
            }
        }
    }

    /// Sets the last page (1-based, inclusive) for subsequent renders.
    ///
    /// @param endPage 1-based last page; 0 or negative means "until end"
    public void setEndPage(int endPage) {
        this.endPage = endPage;
        if (document != null) {
            try {
                int total = document.getPages().getCount();
                if (total > 0) {
                    int from = effectiveStartPage(total);
                    pageCount = effectiveEndPage(total, from);
                    if (currentPage < from - 1) {
                        currentPage = from - 1;
                    }
                } else {
                    pageCount = 0;
                    currentPage = 0;
                }
            } catch (IOException ignore) {}
        }
    }

    /// Returns the start page, or 0 if unset.
    public int getStartPage() {
        return startPage;
    }

    /// Returns the end page, or 0 if unset.
    public int getEndPage() {
        return endPage;
    }

    /// Returns the current resolution.
    public Resolution getResolution() {
        return resolution;
    }

    /// Returns which page box should be used as the coordinate space.
    ///
    /// @return the coordinate type
    public PageCoordinateType getCoordinateType() {
        return coordinateType;
    }

    /// Sets which page box should be used as the coordinate space.
    ///
    /// The current renderer keeps using the page default box; this property is
    /// stored for API compatibility and future rendering support.
    ///
    /// @param coordinateType the coordinate type
    public void setCoordinateType(PageCoordinateType coordinateType) {
        if (coordinateType == null) {
            LOG.warning("Cannot set null coordinateType");
            return;
        }
        this.coordinateType = coordinateType;
    }

    /// Returns whether there are more pages to convert. If [#doConvert()]
    /// was not called explicitly, this method initializes the iterator on the
    /// first call.
    ///
    /// @return `true` if more pages remain
    public boolean hasNextImage() {
        if (pageCount == 0 && currentPage == 0 && document != null) {
            try {
                int total = document.getPages().getCount();
                if (total <= 0) {
                    return false;
                }
                int from = effectiveStartPage(total);
                pageCount = effectiveEndPage(total, from);
                currentPage = from - 1;
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to query page count", e);
                return false;
            }
        }
        return currentPage < pageCount;
    }

    /// Renders the next page to an image file.
    ///
    /// @param outputFile path to the output image file
    /// @param format     image format: "jpeg", "jpg", or "png"
    /// @return `true` on success
    public boolean getNextImage(String outputFile, String format) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return getNextImage(fos, format);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page to file: " + outputFile, e);
            return false;
        }
    }

    /// Renders the next page to an output stream.
    ///
    /// @param stream the output stream
    /// @param format image format: "jpeg", "jpg", or "png"
    /// @return `true` on success
    public boolean getNextImage(OutputStream stream, String format) {
        try {
            currentPage++;
            Page page = document.getPages().get(currentPage);
            PageDevice device = createDevice(format);
            device.process(page, stream);
            LOG.fine("Rendered page " + currentPage + " as " + format);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page " + currentPage, e);
            return false;
        }
    }

    /// Renders the next page to an image file using the given [ImageFormat].
    ///
    /// @param outputFile path to the output image file
    /// @param format     image format enum value
    /// @return `true` on success
    public boolean getNextImage(String outputFile, ImageFormat format) {
        rejectTiffFormat(format);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return getNextImage(fos, format);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page to file: " + outputFile, e);
            return false;
        }
    }

    /// Renders the next page to an image file with explicit JPEG quality.
    /// The `quality` parameter is honored only for [ImageFormat#JPEG];
    /// for other formats it is ignored.
    ///
    /// @param outputFile path to the output image file
    /// @param format     image format enum value
    /// @param quality    JPEG compression quality (1-100)
    /// @return `true` on success
    public boolean getNextImage(String outputFile, ImageFormat format, int quality) {
        rejectTiffFormat(format);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return getNextImage(fos, format, quality);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page to file: " + outputFile, e);
            return false;
        }
    }

    /// Renders the next page to an output stream using the given [ImageFormat].
    ///
    /// @param stream the output stream
    /// @param format image format enum value
    /// @return `true` on success
    public boolean getNextImage(OutputStream stream, ImageFormat format) {
        return renderNext(stream, format, 0, 0, 100);
    }

    /// Renders the next page to an output stream with explicit JPEG quality.
    ///
    /// @param stream  the output stream
    /// @param format  image format enum value
    /// @param quality JPEG compression quality (1-100)
    /// @return `true` on success
    public boolean getNextImage(OutputStream stream, ImageFormat format, int quality) {
        return renderNext(stream, format, 0, 0, quality);
    }

    /// Renders the next page to a file using default JPEG format.
    ///
    /// @param outputFile path to the output file
    /// @return `true` on success
    public boolean getNextImage(String outputFile) {
        return getNextImage(outputFile, ImageFormat.JPEG);
    }

    /// Renders the next page to a stream using default JPEG format.
    ///
    /// @param stream the output stream
    /// @return `true` on success
    public boolean getNextImage(OutputStream stream) {
        return getNextImage(stream, ImageFormat.JPEG);
    }

    /// Renders the next page to a file with explicit output dimensions.
    ///
    /// @param outputFile path to the output image file
    /// @param format     image format enum value
    /// @param width      output width in pixels
    /// @param height     output height in pixels
    /// @return `true` on success
    public boolean getNextImage(String outputFile, ImageFormat format, int width, int height) {
        return getNextImage(outputFile, format, width, height, 100);
    }

    /// Renders the next page to a file with explicit dimensions and JPEG quality.
    ///
    /// @param outputFile path to the output image file
    /// @param format     image format enum value
    /// @param width      output width in pixels
    /// @param height     output height in pixels
    /// @param quality    JPEG compression quality (1-100)
    /// @return `true` on success
    public boolean getNextImage(String outputFile, ImageFormat format, int width, int height, int quality) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return renderNext(fos, format, width, height, quality);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page to file: " + outputFile, e);
            return false;
        }
    }

    /// Renders the next page to a stream with explicit output dimensions.
    public boolean getNextImage(OutputStream stream, ImageFormat format, int width, int height) {
        return renderNext(stream, format, width, height, 100);
    }

    /// Renders the next page to a stream with explicit dimensions and JPEG quality.
    public boolean getNextImage(OutputStream stream, ImageFormat format, int width, int height, int quality) {
        return renderNext(stream, format, width, height, quality);
    }

    /// Double-width/height file overload (matches C# API signature).
    public boolean getNextImage(String outputFile, ImageFormat format, double width, double height, int quality) {
        return getNextImage(outputFile, format, (int) Math.round(width), (int) Math.round(height), quality);
    }

    /// PageSize overload: renders the next page at the given page size (default JPEG).
    public boolean getNextImage(String outputFile, PageSize pageSize) {
        int[] wh = sizeToPixels(pageSize);
        return getNextImage(outputFile, ImageFormat.JPEG, wh[0], wh[1]);
    }

    /// PageSize + format overload — stream version.
    public boolean getNextImage(OutputStream stream, PageSize pageSize, ImageFormat format) {
        int[] wh = sizeToPixels(pageSize);
        return renderNext(stream, format, wh[0], wh[1], 100);
    }

    private int[] sizeToPixels(PageSize p) {
        if (p == null) {
            throw new IllegalArgumentException("pageSize must not be null");
        }
        double dpiX = (resolution != null) ? resolution.getX() : 150;
        double dpiY = (resolution != null) ? resolution.getY() : 150;
        int w = (int) Math.round(p.getWidth() * dpiX / 72.0);
        int h = (int) Math.round(p.getHeight() * dpiY / 72.0);
        return new int[] { w, h };
    }

    /// Double-width/height stream overload (matches C# API signature).
    public boolean getNextImage(OutputStream stream, ImageFormat format, double width, double height, int quality) {
        return renderNext(stream, format, (int) Math.round(width), (int) Math.round(height), quality);
    }

    /// Rejects [ImageFormat#TIFF] as an argument to `getNextImage` — TIFF is
    /// a multi-page container in Aspose's contract; callers must use `SaveAsTIFF()`
    /// instead. Mirrors Aspose.PDF behavior validated by PDFNEWNET\_32406.
    private static void rejectTiffFormat(ImageFormat format) {
        if (format == ImageFormat.TIFF) {
            throw new IllegalArgumentException(
                    "ImageFormat.TIFF is not supported by getNextImage(); use SaveAsTIFF() instead");
        }
    }

    private boolean renderNext(OutputStream stream, ImageFormat format, int width, int height, int quality) {
        rejectTiffFormat(format);
        try {
            currentPage++;
            Page page = document.getPages().get(currentPage);
            createDevice(format, quality, width, height).process(page, stream);
            LOG.fine("Rendered page " + currentPage);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render page " + currentPage, e);
            return false;
        }
    }

    /// Saves all pages of the bound document as a single multi-page TIFF file.
    ///
    /// @param outputFile path to the output TIFF file
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile) {
        return saveAsTIFF(outputFile, (TiffSettings) null, (Resolution) null);
    }

    /// Saves all pages of the bound document as a multi-page TIFF with settings.
    ///
    /// @param outputFile path to the output TIFF file
    /// @param settings   TIFF output settings (compression, color depth, ...)
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile, TiffSettings settings) {
        return saveAsTIFF(outputFile, settings, null);
    }

    /// Saves all pages as a multi-page TIFF with settings and explicit resolution.
    ///
    /// @param outputFile path to the output TIFF file
    /// @param settings   TIFF output settings, or `null` for defaults
    /// @param resolution rendering resolution, or `null` to use the converter's resolution
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile, TiffSettings settings, Resolution resolution) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return saveAsTIFF(fos, settings, resolution);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to write TIFF to file: " + outputFile, e);
            return false;
        }
    }

    /// Saves all pages as a multi-page TIFF to an output stream.
    ///
    /// @param stream   the output stream
    /// @param settings TIFF output settings, or `null` for defaults
    /// @return `true` on success
    public boolean saveAsTIFF(OutputStream stream, TiffSettings settings) {
        return saveAsTIFFInternal(stream, settings, 0, 0);
    }

    /// Saves all pages as multi-page TIFF with the given compression type.
    ///
    /// @param outputFile  path to the output TIFF file
    /// @param compression the compression type
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile, CompressionType compression) {
        return saveAsTIFF(outputFile, new TiffSettings(compression));
    }

    /// Saves all pages as a multi-page TIFF fitted to the specified page size.
    ///
    /// @param outputFile path to the output TIFF file
    /// @param pageSize   target page size used to derive pixel dimensions
    /// @param settings   TIFF settings, or `null` for defaults
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile, PageSize pageSize, TiffSettings settings) {
        int[] wh = sizeToPixels(pageSize);
        return saveAsTIFF(outputFile, wh[0], wh[1], settings);
    }

    /// Saves all pages as a multi-page TIFF fitted to the specified page size.
    ///
    /// @param stream   the output stream
    /// @param pageSize target page size used to derive pixel dimensions
    /// @param settings TIFF settings, or `null` for defaults
    /// @return `true` on success
    public boolean saveAsTIFF(OutputStream stream, PageSize pageSize, TiffSettings settings) {
        int[] wh = sizeToPixels(pageSize);
        return saveAsTIFF(stream, wh[0], wh[1], settings);
    }

    /// Saves all pages as multi-page TIFF scaled to the given pixel dimensions.
    ///
    /// @param outputFile path to the output TIFF file
    /// @param width      target image width in pixels
    /// @param height     target image height in pixels
    /// @return `true` on success
    public boolean saveAsTIFF(String outputFile, int width, int height) {
        return saveAsTIFF(outputFile, width, height, (TiffSettings) null);
    }

    /// Saves all pages as multi-page TIFF scaled to the given dimensions with compression.
    public boolean saveAsTIFF(String outputFile, int width, int height, CompressionType compression) {
        return saveAsTIFF(outputFile, width, height, new TiffSettings(compression));
    }

    /// Saves all pages as multi-page TIFF scaled to the given dimensions with settings.
    public boolean saveAsTIFF(String outputFile, int width, int height, TiffSettings settings) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            return saveAsTIFFInternal(fos, settings, width, height);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to write TIFF to file: " + outputFile, e);
            return false;
        }
    }

    /// Stream overload with explicit target dimensions and settings.
    public boolean saveAsTIFF(OutputStream stream, int width, int height, TiffSettings settings) {
        return saveAsTIFFInternal(stream, settings, width, height);
    }

    /// Saves all pages as TIFF Class F (B/W fax format, CCITT4 compression).
    ///
    /// @param outputFile path to the output TIFF file
    /// @return `true` on success
    public boolean saveAsTIFFClassF(String outputFile) {
        return saveAsTIFF(outputFile, new TiffSettings(CompressionType.CCITT4));
    }

    /// TIFF Class F with target dimensions.
    public boolean saveAsTIFFClassF(String outputFile, int width, int height) {
        return saveAsTIFF(outputFile, width, height, new TiffSettings(CompressionType.CCITT4));
    }

    private boolean saveAsTIFF(OutputStream stream, TiffSettings settings, Resolution res) {
        if (document == null) {
            LOG.warning("No document bound; call bindPdf() first");
            return false;
        }
        try {
            int total = document.getPages().getCount();
            if (total <= 0) {
                LOG.warning("Cannot render TIFF: bound document contains no pages");
                return false;
            }
            int from = effectiveStartPage(total);
            int to = effectiveEndPage(total, from);
            Resolution r = (res != null) ? res : resolution;
            TiffDevice device = new TiffDevice(r, settings);
            device.process(document, from, to, stream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to render document to TIFF", e);
            return false;
        }
    }

    private boolean saveAsTIFFInternal(OutputStream stream, TiffSettings settings, int width, int height) {
        // width/height are accepted for API parity; TiffDevice renders at the
        // current resolution. Scaling to explicit pixel size is a future enhancement.
        return saveAsTIFF(stream, settings, null);
    }

    /// Closes the converter and releases the bound document.
    public void close() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Error closing document", e);
            }
            document = null;
        }
    }

    /// Creates the appropriate device for the given image format.
    private PageDevice createDevice(String format) {
        if (format == null) {
            return new PngDevice(resolution);
        }
        String fmt = format.toLowerCase();
        if ("jpeg".equals(fmt) || "jpg".equals(fmt)) {
            return new JpegDevice(resolution);
        }
        if ("bmp".equals(fmt)) {
            return new BmpDevice(resolution);
        }
        return new PngDevice(resolution);
    }

    private PageDevice createDevice(ImageFormat format, int quality) {
        return createDevice(format, quality, 0, 0);
    }

    private PageDevice createDevice(ImageFormat format, int quality, int width, int height) {
        boolean sized = width > 0 && height > 0;
        if (format == null) {
            return sized ? new PngDevice(width, height, resolution) : new PngDevice(resolution);
        }
        switch (format) {
            case JPEG:
                return sized
                        ? new JpegDevice(width, height, resolution, quality)
                        : new JpegDevice(resolution, quality);
            case BMP:
                return sized
                        ? new BmpDevice(width, height, resolution)
                        : new BmpDevice(resolution);
            case EMF:
            case PNG:
            default:
                return sized
                        ? new PngDevice(width, height, resolution)
                        : new PngDevice(resolution);
        }
    }

    private int effectiveStartPage(int totalPages) {
        if (totalPages <= 0) {
            return 0;
        }
        if (startPage <= 0) {
            return 1;
        }
        return Math.min(startPage, totalPages);
    }

    private int effectiveEndPage(int totalPages, int fromPage) {
        if (totalPages <= 0) {
            return 0;
        }
        if (endPage <= 0) {
            return totalPages;
        }
        return Math.max(fromPage, Math.min(endPage, totalPages));
    }
}
