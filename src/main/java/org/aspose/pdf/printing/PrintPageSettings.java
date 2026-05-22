package org.aspose.pdf.printing;

/**
 * Specifies settings that apply to a single printed page.
 */
public class PrintPageSettings {

    private PrintPaperSize paperSize = PrintPaperSizes.A4;
    private PrinterMargins margins = new PrinterMargins(0, 0, 0, 0);
    private boolean landscape;
    private boolean color = true;
    private PdfPrinterSettings printerSettings;

    /**
     * Creates default page settings.
     */
    public PrintPageSettings() {
    }

    /**
     * Creates page settings associated with the given printer settings.
     *
     * @param printerSettings the printer settings
     */
    public PrintPageSettings(PdfPrinterSettings printerSettings) {
        this.printerSettings = printerSettings;
    }

    /** Returns the paper size. */
    public PrintPaperSize getPaperSize() { return paperSize; }
    /** Sets the paper size. */
    public void setPaperSize(PrintPaperSize paperSize) { this.paperSize = paperSize; }

    /** Returns the page margins. */
    public PrinterMargins getMargins() { return margins; }
    /** Sets the page margins. */
    public void setMargins(PrinterMargins margins) { this.margins = margins; }

    /** Returns whether the page is landscape. */
    public boolean isLandscape() { return landscape; }
    /** Sets whether the page is landscape. */
    public void setLandscape(boolean landscape) { this.landscape = landscape; }

    /** Returns whether color printing is enabled. */
    public boolean isColor() { return color; }
    /** Sets whether color printing is enabled. */
    public void setColor(boolean color) { this.color = color; }

    /** Returns the associated printer settings. */
    public PdfPrinterSettings getPrinterSettings() { return printerSettings; }
    /** Sets the associated printer settings. */
    public void setPrinterSettings(PdfPrinterSettings printerSettings) { this.printerSettings = printerSettings; }
}
