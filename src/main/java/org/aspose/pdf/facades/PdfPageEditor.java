package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.PageSize;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.operators.ConcatenateMatrix;
import org.aspose.pdf.operators.GRestore;
import org.aspose.pdf.operators.GSave;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for editing individual page properties such as size, rotation,
 * and retrieving page information.
 */
public class PdfPageEditor implements java.io.Closeable {

    private static final Logger LOG = Logger.getLogger(PdfPageEditor.class.getName());

    private Document document;
    private int[] processPages;

    private float zoom = 1.0f;
    private int moveDx = 0;
    private int moveDy = 0;
    private boolean hasPendingTransform = false;

    /**
     * Creates a new {@code PdfPageEditor} instance.
     */
    public PdfPageEditor() {
    }

    /**
     * Binds a PDF file to this editor.
     *
     * @param inputFile path to the PDF file
     * @return {@code true} on success
     */
    public boolean bindPdf(String inputFile) {
        try {
            this.document = new Document(inputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /**
     * Binds a PDF from an input stream.
     *
     * @param inputStream the input stream containing PDF data
     * @return {@code true} on success
     */
    public boolean bindPdf(InputStream inputStream) {
        try {
            this.document = new Document(inputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /**
     * Binds an existing {@link Document} to this editor.
     *
     * @param document the document to bind
     * @return {@code true} on success
     */
    public boolean bindPdf(Document document) {
        if (document == null) {
            LOG.warning("Cannot bind null document");
            return false;
        }
        this.document = document;
        return true;
    }

    /**
     * Returns the size of the specified page as a float array {@code [width, height]}.
     *
     * @param pageNumber 1-based page number
     * @return float array with width at index 0 and height at index 1,
     *         or {@code null} if the page is invalid
     */
    public float[] getPageSize(int pageNumber) {
        try {
            Page page = document.getPages().get(pageNumber);
            Rectangle mediaBox = page.getMediaBox();
            return new float[]{
                    (float) mediaBox.getWidth(),
                    (float) mediaBox.getHeight()
            };
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get page size for page " + pageNumber, e);
            return null;
        }
    }

    /**
     * Sets the size of the specified page.
     *
     * @param pageNumber 1-based page number
     * @param size       the new page size
     */
    public void setPageSize(int pageNumber, PageSize size) {
        try {
            Page page = document.getPages().get(pageNumber);
            page.setPageSize(size.getWidth(), size.getHeight());
            LOG.fine("Set page " + pageNumber + " size to " + size);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to set page size for page " + pageNumber, e);
        }
    }

    /**
     * Returns the rotation of the specified page in degrees.
     *
     * @param pageNumber 1-based page number
     * @return the rotation in degrees (0, 90, 180, 270), or 0 on error
     */
    public int getPageRotation(int pageNumber) {
        try {
            Page page = document.getPages().get(pageNumber);
            return page.getRotate();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get page rotation for page " + pageNumber, e);
            return 0;
        }
    }

    /**
     * Sets the rotation of the specified page.
     *
     * @param pageNumber 1-based page number
     * @param rotation   the rotation in degrees (must be 0, 90, 180, or 270)
     */
    public void setPageRotation(int pageNumber, int rotation) {
        try {
            Page page = document.getPages().get(pageNumber);
            page.setRotation(rotation);
            LOG.fine("Set page " + pageNumber + " rotation to " + rotation);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to set page rotation for page " + pageNumber, e);
        }
    }

    /**
     * Bulk-applies rotations from a {@code pageNumber → degrees} map. Mirrors
     * the C# property {@code PdfPageEditor.PageRotations}: Aspose not only
     * writes /Rotate, it also rewrites every annotation's /Rect into the new
     * page coordinate frame so the widget stays visually anchored to the same
     * spot. We replicate both halves of that contract.
     *
     * @param rotations map of 1-based page number to rotation in degrees
     *                  (each value must be 0, 90, 180, or 270); ignored if null
     */
    public void setPageRotations(java.util.Map<Integer, Integer> rotations) {
        if (rotations == null || document == null) return;
        for (java.util.Map.Entry<Integer, Integer> e : rotations.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            int pageNumber = e.getKey();
            int newRot = ((e.getValue() % 360) + 360) % 360;
            try {
                Page page = document.getPages().get(pageNumber);
                int oldRot = ((page.getRotate() % 360) + 360) % 360;
                int delta  = ((newRot - oldRot) % 360 + 360) % 360;
                if (delta != 0) {
                    Rectangle media = page.getMediaBox();
                    if (media != null) {
                        rotateAnnotationRects(page, delta, media);
                    }
                }
                page.setRotation(newRot);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to set rotation for page " + pageNumber, ex);
            }
        }
    }

    /**
     * Transforms every annotation's /Rect on {@code page} by {@code delta}
     * degrees clockwise so the annotation stays in the same visual location
     * after the page itself is rotated. {@code delta} must be 0, 90, 180, or
     * 270; other values are no-ops.
     */
    private static void rotateAnnotationRects(Page page, int delta, Rectangle media) {
        if (delta == 0) return;
        double pageW = media.getURX() - media.getLLX();
        double pageH = media.getURY() - media.getLLY();
        double mx = media.getLLX();
        double my = media.getLLY();
        try {
            for (org.aspose.pdf.annotations.Annotation ann : page.getAnnotations()) {
                Rectangle r = ann.getRect();
                if (r == null) continue;
                // Translate to media-origin coords for the math.
                double llx = r.getLLX() - mx;
                double lly = r.getLLY() - my;
                double urx = r.getURX() - mx;
                double ury = r.getURY() - my;
                Rectangle out;
                switch (delta) {
                    case 90:
                        // (x,y) → (y, pageW - x), bbox formed from rotated corners.
                        out = new Rectangle(
                                lly + mx,
                                (pageW - urx) + my,
                                ury + mx,
                                (pageW - llx) + my);
                        break;
                    case 180:
                        out = new Rectangle(
                                (pageW - urx) + mx,
                                (pageH - ury) + my,
                                (pageW - llx) + mx,
                                (pageH - lly) + my);
                        break;
                    case 270:
                        // (x,y) → (pageH - y, x)
                        out = new Rectangle(
                                (pageH - ury) + mx,
                                llx + my,
                                (pageH - lly) + mx,
                                urx + my);
                        break;
                    default:
                        continue;
                }
                ann.setRect(out);
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to rotate annotations on page", e);
        }
    }

    /**
     * Returns the rotations currently applied to every page. Companion getter
     * for {@link #setPageRotations(java.util.Map)}.
     *
     * @return map of 1-based page number to rotation degrees
     */
    public java.util.Map<Integer, Integer> getPageRotations() {
        java.util.LinkedHashMap<Integer, Integer> out = new java.util.LinkedHashMap<>();
        if (document == null) return out;
        try {
            int n = document.getPages().getCount();
            for (int i = 1; i <= n; i++) {
                out.put(i, getPageRotation(i));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to enumerate page rotations", e);
        }
        return out;
    }

    /**
     * Returns the array of page numbers that operations should affect.
     *
     * @return the array of page numbers, or {@code null} if all pages are targeted
     */
    public int[] getProcessPages() {
        return processPages;
    }

    /**
     * Sets the array of page numbers that operations should affect.
     *
     * @param processPages 1-based page numbers to process, or {@code null} for all pages
     */
    public void setProcessPages(int[] processPages) {
        this.processPages = processPages;
    }

    /**
     * Returns the total number of pages in the bound document.
     *
     * @return the page count, or 0 on error
     */
    public int getPageCount() {
        try {
            return document.getPages().getCount();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get page count", e);
            return 0;
        }
    }

    /**
     * Returns the current zoom factor.
     *
     * @return the zoom factor (default 1.0)
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets a zoom factor applied to all (or processed) pages when
     * {@link #save(String)} or {@link #save(OutputStream)} is called. Default
     * is {@code 1.0} (no scaling); {@code 0.5} scales contents to 50%,
     * {@code 2.0} to 200%. Non-positive values are ignored.
     *
     * @param zoom the zoom factor (must be &gt; 0)
     */
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            LOG.warning("Zoom must be positive, got " + zoom + " — ignored");
            return;
        }
        this.zoom = zoom;
        this.hasPendingTransform = true;
    }

    /**
     * Translates page contents by {@code (dx, dy)} units when the document is
     * saved. Positive {@code dx} moves content right; positive {@code dy}
     * moves it up (PDF coordinates have origin at the lower-left).
     *
     * @param dx horizontal offset in points
     * @param dy vertical offset in points
     */
    public void movePosition(int dx, int dy) {
        this.moveDx = dx;
        this.moveDy = dy;
        this.hasPendingTransform = true;
    }

    /**
     * Wraps each target page's content stream in {@code q ... cm ... Q} using
     * the pending zoom/move values, then resets the pending state. Idempotent
     * when there is no pending transform.
     */
    private void applyPendingTransform() throws IOException {
        if (!hasPendingTransform) return;
        if (zoom == 1.0f && moveDx == 0 && moveDy == 0) {
            hasPendingTransform = false;
            return;
        }
        int[] pages = (processPages != null) ? processPages : allPageNumbers();
        // Combined affine matrix: scale-then-translate, fused into a single
        // PDF cm — [scaleX 0 0 scaleY (dx*scaleX) (dy*scaleY)].
        double a = zoom;
        double d = zoom;
        double e = moveDx * (double) zoom;
        double f = moveDy * (double) zoom;
        for (int pageNum : pages) {
            try {
                Page page = document.getPages().get(pageNum);
                wrapPageContent(page, a, 0, 0, d, e, f);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to apply transform on page " + pageNum, ex);
            }
        }
        // Reset so a subsequent save() does not re-apply the same transform.
        zoom = 1.0f;
        moveDx = 0;
        moveDy = 0;
        hasPendingTransform = false;
    }

    /**
     * Wraps {@code page}'s content stream in {@code q  a b c d e f cm  ... Q}.
     * Preserves the existing /Contents indirect object identity via the
     * Page.markContentsDirty path (see Page.flushContentsIfDirty).
     */
    static void wrapPageContent(Page page, double a, double b, double c,
                                double d, double e, double f) throws IOException {
        OperatorCollection ops = page.getContents();
        // Append Q first so existing indexes remain valid while we insert at 0.
        ops.add(new GRestore());
        ops.addAt(0, new ConcatenateMatrix(a, b, c, d, e, f));
        ops.addAt(0, new GSave());
        page.markContentsDirty();
    }

    private int[] allPageNumbers() throws IOException {
        int n = document.getPages().getCount();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i + 1;
        return arr;
    }

    /**
     * Saves the bound document to a file.
     *
     * @param outputFile path to the output file
     * @return {@code true} on success
     */
    public boolean save(String outputFile) {
        try {
            applyPendingTransform();
            document.requestFullRewrite();
            document.save(outputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to file: " + outputFile, e);
            return false;
        }
    }

    /**
     * Saves the bound document to an output stream.
     *
     * @param outputStream the output stream
     * @return {@code true} on success
     */
    public boolean save(OutputStream outputStream) {
        try {
            applyPendingTransform();
            document.requestFullRewrite();
            document.save(outputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to stream", e);
            return false;
        }
    }

    /**
     * Closes the editor and releases the bound document.
     */
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
}
