package org.aspose.pdf;

/// Describes the placement of an image on a PDF page.
///
/// Contains the image reference, the current transformation matrix (CTM)
/// at the point the image was rendered, and the computed bounding rectangle
/// and resolution.
///
public class ImagePlacement {

    private final XImage image;
    private final Matrix matrix;
    private final Rectangle rectangle;
    private final Page page;

    /// Creates an ImagePlacement.
    ///
    /// @param image  the image XObject
    /// @param matrix the CTM at the Do operator
    /// @param page   the page containing this placement
    public ImagePlacement(XImage image, Matrix matrix, Page page) {
        this.image = image;
        this.matrix = matrix != null ? matrix : Matrix.IDENTITY;
        this.page = page;
        this.rectangle = computeRectangle();
    }

    /// Returns the placed image.
    ///
    /// @return the XImage
    public XImage getImage() {
        return image;
    }

    /// Returns the CTM at the point of rendering.
    ///
    /// @return the transformation matrix
    public Matrix getMatrix() {
        return matrix;
    }

    /// Returns the bounding rectangle of the image on the page (in page coordinates).
    ///
    /// @return the rectangle
    public Rectangle getRectangle() {
        return rectangle;
    }

    /// Returns the page containing this image placement.
    ///
    /// @return the page
    public Page getPage() {
        return page;
    }

    /// Returns the horizontal resolution in DPI.
    ///
    /// @return the resolution (dots per inch)
    public double getResolution() {
        double widthPts = rectangle.getWidth();
        int widthPx = image.getWidth();
        return widthPx > 0 && widthPts > 0 ? widthPx / widthPts * 72.0 : 72.0;
    }

    private Rectangle computeRectangle() {
        // Image occupies unit square [0,0]-[1,1], transformed by CTM
        double[] ll = matrix.transformPoint(0, 0);
        double[] lr = matrix.transformPoint(1, 0);
        double[] ul = matrix.transformPoint(0, 1);
        double[] ur = matrix.transformPoint(1, 1);

        double minX = Math.min(Math.min(ll[0], lr[0]), Math.min(ul[0], ur[0]));
        double minY = Math.min(Math.min(ll[1], lr[1]), Math.min(ul[1], ur[1]));
        double maxX = Math.max(Math.max(ll[0], lr[0]), Math.max(ul[0], ur[0]));
        double maxY = Math.max(Math.max(ll[1], lr[1]), Math.max(ul[1], ur[1]));

        return new Rectangle(minX, minY, maxX, maxY);
    }
}
