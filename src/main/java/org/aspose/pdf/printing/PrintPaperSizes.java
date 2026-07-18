package org.aspose.pdf.printing;

/// Standard paper sizes as pre-defined constants.
/// Dimensions are in hundredths of an inch.
public final class PrintPaperSizes {

    /// A4 paper (210 x 297 mm = 827 x 1169 hundredths of inch).
    public static final PrintPaperSize A4 = new PrintPaperSize("A4", 827, 1169);
    /// US Letter paper (8.5 x 11 in = 850 x 1100).
    public static final PrintPaperSize Letter = new PrintPaperSize("Letter", 850, 1100);
    /// US Legal paper (8.5 x 14 in = 850 x 1400).
    public static final PrintPaperSize Legal = new PrintPaperSize("Legal", 850, 1400);
    /// A3 paper (297 x 420 mm = 1169 x 1654).
    public static final PrintPaperSize A3 = new PrintPaperSize("A3", 1169, 1654);
    /// A5 paper (148 x 210 mm = 583 x 827).
    public static final PrintPaperSize A5 = new PrintPaperSize("A5", 583, 827);
    /// Tabloid paper (11 x 17 in = 1100 x 1700).
    public static final PrintPaperSize Tabloid = new PrintPaperSize("Tabloid", 1100, 1700);

    private PrintPaperSizes() {
    }
}
