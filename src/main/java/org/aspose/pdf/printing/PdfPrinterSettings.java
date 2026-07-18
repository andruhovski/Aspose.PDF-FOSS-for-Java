package org.aspose.pdf.printing;

import java.util.ArrayList;
import java.util.List;

/// Specifies information about how a document is printed, including the printer to use.
public class PdfPrinterSettings {

    private String printerName;
    private boolean printToFile;
    private String printFileName;
    private short copies = 1;
    private int fromPage = 0;
    private int toPage = 0;
    private PdfPrintRange printRange = PdfPrintRange.AllPages;
    private boolean collate;
    private DuplexKind duplex = DuplexKind.Default;
    private PrintPageSettings defaultPageSettings;

    /// Creates default printer settings.
    public PdfPrinterSettings() {
        this.defaultPageSettings = new PrintPageSettings(this);
    }

    /// Returns the target printer name.
    public String getPrinterName() { return printerName; }
    /// Sets the target printer name.
    public void setPrinterName(String name) { this.printerName = name; }

    /// Returns whether to print to a file instead of a printer.
    public boolean isPrintToFile() { return printToFile; }
    /// Sets whether to print to a file.
    public void setPrintToFile(boolean v) { this.printToFile = v; }

    /// Returns the output file name when printing to file.
    public String getPrintFileName() { return printFileName; }
    /// Sets the output file name.
    public void setPrintFileName(String f) { this.printFileName = f; }

    /// Returns the number of copies to print.
    public short getCopies() { return copies; }
    /// Sets the number of copies.
    public void setCopies(short c) { this.copies = c; }

    /// Returns the 1-based start page number (0 = not set).
    public int getFromPage() { return fromPage; }
    /// Sets the 1-based start page number.
    public void setFromPage(int p) { this.fromPage = p; }

    /// Returns the 1-based end page number (0 = not set).
    public int getToPage() { return toPage; }
    /// Sets the 1-based end page number.
    public void setToPage(int p) { this.toPage = p; }

    /// Returns the print range setting.
    public PdfPrintRange getPrintRange() { return printRange; }
    /// Sets the print range setting.
    public void setPrintRange(PdfPrintRange r) { this.printRange = r; }

    /// Returns whether copies are collated.
    public boolean isCollate() { return collate; }
    /// Sets whether copies are collated.
    public void setCollate(boolean c) { this.collate = c; }

    /// Returns the duplex setting.
    public DuplexKind getDuplex() { return duplex; }
    /// Sets the duplex setting.
    public void setDuplex(DuplexKind d) { this.duplex = d; }

    /// Returns the default page settings.
    public PrintPageSettings getDefaultPageSettings() { return defaultPageSettings; }

    /// Returns the names of all installed printers via javax.print.
    ///
    /// @return list of printer names
    public static List<String> getInstalledPrinters() {
        List<String> result = new ArrayList<>();
        try {
            for (javax.print.PrintService ps : javax.print.PrintServiceLookup.lookupPrintServices(null, null)) {
                result.add(ps.getName());
            }
        } catch (Exception ignored) {
            // javax.print may not be available in all environments
        }
        return result;
    }
}
