package org.aspose.pdf.printing;

/**
 * Represents the resolution of a printer.
 */
public class PdfPrinterResolution {

    private int x = 150;
    private int y = 150;
    private PdfPrinterResolutionKind kind = PdfPrinterResolutionKind.Custom;

    /**
     * Creates a default printer resolution (150 DPI).
     */
    public PdfPrinterResolution() {
    }

    /** Returns the horizontal resolution in DPI. */
    public int getX() { return x; }
    /** Sets the horizontal resolution in DPI. */
    public void setX(int x) { this.x = x; }

    /** Returns the vertical resolution in DPI. */
    public int getY() { return y; }
    /** Sets the vertical resolution in DPI. */
    public void setY(int y) { this.y = y; }

    /** Returns the resolution kind. */
    public PdfPrinterResolutionKind getKind() { return kind; }
    /** Sets the resolution kind. */
    public void setKind(PdfPrinterResolutionKind kind) { this.kind = kind; }
}
