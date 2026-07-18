package org.aspose.pdf;

import java.io.InputStream;
import java.util.logging.Logger;

/// Represents an image element that can be added to a PDF page's paragraph collection.
///
/// This class is used for laying out images within the document content flow (e.g.,
/// adding an image to a page's paragraphs or a table cell). It is distinct from
/// [ImagePlacement], which represents an already-placed image extracted from
/// an existing PDF.
///
public class Image extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(Image.class.getName());

    private String file;
    private InputStream imageStream;
    private double fixWidth;
    private double fixHeight;
    private String imageType;
    private String title;
    private int selectedFrame = -1;

    /// Creates a new Image with default settings.
    public Image() {
        // defaults
    }

    /// Returns the file path of the image.
    ///
    /// @return the file path, or `null` if the image is provided via stream
    public String getFile() {
        return file;
    }

    /// Sets the file path of the image.
    ///
    /// @param file the file path
    public void setFile(String file) {
        this.file = file;
    }

    /// Returns the input stream providing the image data.
    ///
    /// @return the image stream, or `null` if the image is provided via file path
    public InputStream getImageStream() {
        return imageStream;
    }

    /// Sets the input stream providing the image data.
    ///
    /// @param imageStream the image stream
    public void setImageStream(InputStream imageStream) {
        this.imageStream = imageStream;
    }

    /// Returns the fixed display width of the image in points.
    ///
    /// @return the fixed width; 0 means use the image's natural width
    public double getFixWidth() {
        return fixWidth;
    }

    /// Sets the fixed display width of the image in points.
    ///
    /// @param fixWidth the fixed width; 0 for natural width
    public void setFixWidth(double fixWidth) {
        this.fixWidth = fixWidth;
    }

    /// Returns the fixed display height of the image in points.
    ///
    /// @return the fixed height; 0 means use the image's natural height
    public double getFixHeight() {
        return fixHeight;
    }

    /// Sets the fixed display height of the image in points.
    ///
    /// @param fixHeight the fixed height; 0 for natural height
    public void setFixHeight(double fixHeight) {
        this.fixHeight = fixHeight;
    }

    /// Returns the image type identifier (e.g., "JPEG", "PNG").
    ///
    /// @return the image type, or `null` if auto-detected
    public String getImageType() {
        return imageType;
    }

    /// Sets the image type identifier.
    ///
    /// @param imageType the image type (e.g., "JPEG", "PNG")
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /// Returns the title (alt text) for this image.
    ///
    /// @return the title, or `null`
    public String getTitle() {
        return title;
    }

    /// Sets the title (alt text) for this image.
    ///
    /// @param title the title
    public void setTitle(String title) {
        this.title = title;
    }

    /// Returns the 0-based frame index to render when the source is a
    /// multi-frame image (e.g. a multi-page TIFF).
    ///
    /// @return the selected frame, or `-1` when unset — the document
    ///         expands a multi-frame TIFF into one page per frame on save
    public int getSelectedFrame() {
        return selectedFrame;
    }

    /// Selects a single frame of a multi-frame source image (e.g. a
    /// multi-page TIFF) to render. When left at the default `-1`,
    /// saving a new document expands the multi-frame image into one page
    /// per decodable frame, matching Aspose.PDF behaviour.
    ///
    /// @param selectedFrame the 0-based frame index, or `-1` for all
    public void setSelectedFrame(int selectedFrame) {
        this.selectedFrame = selectedFrame;
    }
}
