package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.render.PdfPageRenderer;
import org.aspose.pdf.printing.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade for viewing and printing PDF documents.
 * <p>
 * Uses {@code java.awt.print.PrinterJob} for physical printing and
 * {@link PdfPageRenderer} for rendering pages to images.
 * </p>
 */
public class PdfViewer implements Printable, AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PdfViewer.class.getName());

    private Document document;
    private boolean autoResize = true;
    private boolean autoRotate = true;
    private boolean printPageDialog = true;
    private boolean printAsGrayscale = false;
    private int resolution = 150;
    private boolean useIntermidiateImage = false;

    // Print range for Printable callback
    private int printFromPage = 1;
    private int printToPage = Integer.MAX_VALUE;

    /**
     * Creates a new PdfViewer instance.
     */
    public PdfViewer() {
    }

    /**
     * Binds a PDF file to this viewer.
     *
     * @param inputFile path to the PDF file
     * @throws IOException if the file cannot be opened
     */
    public void bindPdf(String inputFile) throws IOException {
        this.document = new Document(inputFile);
    }

    /**
     * Binds a PDF from an input stream.
     *
     * @param stream the input stream
     * @throws IOException if the stream cannot be read
     */
    public void bindPdf(InputStream stream) throws IOException {
        this.document = new Document(stream);
    }

    /**
     * Binds an existing Document to this viewer.
     *
     * @param doc the document
     */
    public void bindPdf(Document doc) {
        this.document = doc;
    }

    /**
     * Opens a PDF file (alias for bindPdf).
     *
     * @param inputFile path to the PDF file
     * @throws IOException if the file cannot be opened
     */
    public void openPdfFile(String inputFile) throws IOException {
        bindPdf(inputFile);
    }

    /**
     * Prints the document with specified page and printer settings.
     *
     * @param pageSettings    page settings (paper size, margins, orientation)
     * @param printerSettings printer settings (printer name, copies, page range)
     * @throws IOException      if document processing fails
     * @throws PrinterException if printing fails
     */
    public void printDocumentWithSettings(PrintPageSettings pageSettings,
                                           PdfPrinterSettings printerSettings)
            throws IOException, PrinterException {
        if (document == null) throw new IllegalStateException("No document bound");

        if (printerSettings.isPrintToFile()) {
            printToFile(printerSettings, pageSettings);
        } else {
            printToDevice(printerSettings, pageSettings);
        }
    }

    /**
     * Prints the document with the specified printer settings using default page settings.
     *
     * @param printerSettings the printer settings
     * @throws IOException      if document processing fails
     * @throws PrinterException if printing fails
     */
    public void printDocumentWithSettings(PdfPrinterSettings printerSettings)
            throws IOException, PrinterException {
        printDocumentWithSettings(printerSettings.getDefaultPageSettings(), printerSettings);
    }

    /**
     * Prints the document using default settings.
     *
     * @throws IOException      if document processing fails
     * @throws PrinterException if printing fails
     */
    public void printDocument() throws IOException, PrinterException {
        printDocumentWithSettings(new PdfPrinterSettings());
    }

    /**
     * Prints a large PDF file (binds and prints).
     *
     * @param inputFile path to the PDF file
     * @throws IOException      if document processing fails
     * @throws PrinterException if printing fails
     */
    public void printLargePdf(String inputFile) throws IOException, PrinterException {
        bindPdf(inputFile);
        printDocument();
    }

    /**
     * Renders a single page to a BufferedImage.
     *
     * @param pageNum 1-based page number
     * @return the rendered image
     * @throws IOException if rendering fails
     */
    public BufferedImage decodePage(int pageNum) throws IOException {
        if (document == null) throw new IllegalStateException("No document bound");
        PdfPageRenderer renderer = new PdfPageRenderer();
        return renderer.renderPage(document.getPages().get(pageNum), resolution, resolution);
    }

    /**
     * Renders all pages to BufferedImage array.
     *
     * @return array of rendered images
     * @throws IOException if rendering fails
     */
    public BufferedImage[] decodeAllPages() throws IOException {
        if (document == null) throw new IllegalStateException("No document bound");
        int n = document.getPages().getCount();
        BufferedImage[] result = new BufferedImage[n];
        PdfPageRenderer renderer = new PdfPageRenderer();
        for (int i = 0; i < n; i++) {
            result[i] = renderer.renderPage(document.getPages().get(i + 1), resolution, resolution);
        }
        return result;
    }

    // ===================== Printable interface =====================

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        int actualPage = pageIndex + printFromPage;
        try {
            if (actualPage > printToPage || actualPage > document.getPages().getCount()) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g2d = (Graphics2D) g;
            PdfPageRenderer renderer = new PdfPageRenderer();
            BufferedImage img = renderer.renderPage(document.getPages().get(actualPage), resolution, resolution);
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            if (autoResize) {
                double scale = Math.min(
                        pf.getImageableWidth() / img.getWidth(),
                        pf.getImageableHeight() / img.getHeight());
                g2d.scale(scale, scale);
            }
            g2d.drawImage(img, 0, 0, null);
            return PAGE_EXISTS;
        } catch (IOException e) {
            throw new PrinterException("Failed to render page " + actualPage + ": " + e.getMessage());
        }
    }

    // ===================== Print helpers =====================

    /**
     * Print-to-file: renders pages as images.
     */
    private void printToFile(PdfPrinterSettings settings, PrintPageSettings pageSettings) throws IOException {
        String outputFile = settings.getPrintFileName();
        if (outputFile == null) throw new IOException("PrintFileName not set");

        int start = settings.getFromPage() > 0 ? settings.getFromPage() : 1;
        int end = settings.getToPage() > 0
                ? Math.min(settings.getToPage(), document.getPages().getCount())
                : document.getPages().getCount();

        PdfPageRenderer renderer = new PdfPageRenderer();
        for (int i = start; i <= end; i++) {
            BufferedImage img = renderer.renderPage(document.getPages().get(i), resolution, resolution);
            if (autoResize && pageSettings.getPaperSize() != null) {
                int tw = (int) (pageSettings.getPaperSize().getWidth() * resolution / 100.0);
                int th = (int) (pageSettings.getPaperSize().getHeight() * resolution / 100.0);
                img = scaleImage(img, tw, th);
            }
            String path = (end > start) ? insertPageNum(outputFile, i) : outputFile;
            ImageIO.write(img, guessFormat(outputFile), new File(path));
        }
    }

    /**
     * Prints to a physical/virtual printer via java.awt.print.PrinterJob.
     */
    private void printToDevice(PdfPrinterSettings settings, PrintPageSettings pageSettings)
            throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();

        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        if (pageSettings.getPaperSize() != null) {
            double w = pageSettings.getPaperSize().getWidth() * 72.0 / 100.0;
            double h = pageSettings.getPaperSize().getHeight() * 72.0 / 100.0;
            paper.setSize(w, h);
            PrinterMargins m = pageSettings.getMargins();
            if (m != null) {
                double ml = m.getLeft() * 72.0 / 100.0;
                double mt = m.getTop() * 72.0 / 100.0;
                double mr = m.getRight() * 72.0 / 100.0;
                double mb = m.getBottom() * 72.0 / 100.0;
                paper.setImageableArea(ml, mt, w - ml - mr, h - mt - mb);
            }
        }
        pf.setPaper(paper);
        if (pageSettings.isLandscape()) {
            pf.setOrientation(PageFormat.LANDSCAPE);
        }

        try {
            printFromPage = settings.getFromPage() > 0 ? settings.getFromPage() : 1;
            printToPage = settings.getToPage() > 0 ? settings.getToPage() : document.getPages().getCount();
        } catch (IOException e) {
            throw new PrinterException("Failed to get page count: " + e.getMessage());
        }

        job.setPrintable(this, pf);
        job.setCopies(settings.getCopies());

        // Find printer by name
        if (settings.getPrinterName() != null) {
            for (PrintService ps : PrintServiceLookup.lookupPrintServices(null, null)) {
                if (ps.getName().equalsIgnoreCase(settings.getPrinterName())) {
                    job.setPrintService(ps);
                    break;
                }
            }
        }

        if (printPageDialog) {
            if (job.printDialog()) {
                job.print();
            }
        } else {
            job.print();
        }
    }

    // ===================== Properties =====================

    /** Returns whether pages are auto-resized to fit the paper. */
    public boolean isAutoResize() { return autoResize; }
    /** Sets whether pages are auto-resized to fit the paper. */
    public void setAutoResize(boolean v) { this.autoResize = v; }

    /** Returns whether pages are auto-rotated to best fit. */
    public boolean isAutoRotate() { return autoRotate; }
    /** Sets whether pages are auto-rotated. */
    public void setAutoRotate(boolean v) { this.autoRotate = v; }

    /** Returns whether a print dialog is shown before printing. */
    public boolean isPrintPageDialog() { return printPageDialog; }
    /** Sets whether a print dialog is shown. */
    public void setPrintPageDialog(boolean v) { this.printPageDialog = v; }

    /** Returns whether to print in grayscale. */
    public boolean isPrintAsGrayscale() { return printAsGrayscale; }
    /** Sets whether to print in grayscale. */
    public void setPrintAsGrayscale(boolean v) { this.printAsGrayscale = v; }

    /** Returns the rendering resolution in DPI. */
    public int getResolution() { return resolution; }
    /** Sets the rendering resolution in DPI. */
    public void setResolution(int v) { this.resolution = v; }

    /** Sets whether to use an intermediate image for printing. */
    public void setUseIntermidiateImage(boolean v) { this.useIntermidiateImage = v; }
    /** Returns whether intermediate image is used. */
    public boolean isUseIntermidiateImage() { return useIntermidiateImage; }

    /**
     * Closes the PDF file (alias for close).
     *
     * @throws IOException if closing fails
     */
    public void closePdfFile() throws IOException {
        close();
    }

    @Override
    public void close() throws IOException {
        if (document != null) {
            document.close();
            document = null;
        }
    }

    // ===================== Utilities =====================

    private static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        if (width <= 0 || height <= 0) return source;
        BufferedImage dest = new BufferedImage(width, height, source.getType() != 0 ? source.getType() : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return dest;
    }

    private static String guessFormat(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "JPEG";
        if (lower.endsWith(".bmp")) return "BMP";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "TIFF";
        return "PNG";
    }

    private static String insertPageNum(String path, int pageNum) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) return path + "_" + pageNum;
        return path.substring(0, dot) + "_" + pageNum + path.substring(dot);
    }
}
