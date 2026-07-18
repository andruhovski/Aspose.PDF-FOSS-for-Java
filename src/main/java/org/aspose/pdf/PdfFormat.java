package org.aspose.pdf;

import java.util.logging.Logger;

/// Enumerates PDF format standards and versions used for validation and conversion.
///
/// Each constant carries metadata about its ISO standard number, part, conformance level,
/// and the underlying PDF version it targets.
///
public enum PdfFormat {

    /// PDF/A-1a (ISO 19005-1, Level A) based on PDF 1.4.
    PDF_A_1A("19005", 1, "A", "1.4"),

    /// PDF/A-1b (ISO 19005-1, Level B) based on PDF 1.4.
    PDF_A_1B("19005", 1, "B", "1.4"),

    /// PDF/A-2a (ISO 19005-2, Level A) based on PDF 1.7.
    PDF_A_2A("19005", 2, "A", "1.7"),

    /// PDF/A-2b (ISO 19005-2, Level B) based on PDF 1.7.
    PDF_A_2B("19005", 2, "B", "1.7"),

    /// PDF/A-2u (ISO 19005-2, Level U) based on PDF 1.7.
    PDF_A_2U("19005", 2, "U", "1.7"),

    /// PDF/A-3a (ISO 19005-3, Level A) based on PDF 1.7.
    PDF_A_3A("19005", 3, "A", "1.7"),

    /// PDF/A-3b (ISO 19005-3, Level B) based on PDF 1.7.
    PDF_A_3B("19005", 3, "B", "1.7"),

    /// PDF/A-3u (ISO 19005-3, Level U) based on PDF 1.7.
    PDF_A_3U("19005", 3, "U", "1.7"),

    /// PDF/A-4 (ISO 19005-4) based on PDF 2.0.
    PDF_A_4("19005", 4, null, "2.0"),

    /// PDF/A-4e (ISO 19005-4, engineering) based on PDF 2.0.
    PDF_A_4E("19005", 4, "E", "2.0"),

    /// PDF/A-4f (ISO 19005-4, file attachments) based on PDF 2.0.
    PDF_A_4F("19005", 4, "F", "2.0"),

    /// PDF/UA-1 (ISO 14289-1) based on PDF 1.7.
    PDF_UA_1("14289", 1, null, "1.7"),

    /// PDF/X-1a (ISO 15930-1) based on PDF 1.3.
    PDF_X_1A("15930", 1, null, "1.3"),

    /// PDF/X-1a:2001 (ISO 15930-1) based on PDF 1.3.
    PDF_X_1A_2001("15930", 1, null, "1.3"),

    /// PDF/X-3 (ISO 15930-3) based on PDF 1.3.
    PDF_X_3("15930", 3, null, "1.3"),

    /// Plain PDF version 1.0.
    v_1_0(null, 0, null, "1.0"),

    /// Plain PDF version 1.1.
    v_1_1(null, 0, null, "1.1"),

    /// Plain PDF version 1.2.
    v_1_2(null, 0, null, "1.2"),

    /// Plain PDF version 1.3.
    v_1_3(null, 0, null, "1.3"),

    /// Plain PDF version 1.4.
    v_1_4(null, 0, null, "1.4"),

    /// Plain PDF version 1.5.
    v_1_5(null, 0, null, "1.5"),

    /// Plain PDF version 1.6.
    v_1_6(null, 0, null, "1.6"),

    /// Plain PDF version 1.7.
    v_1_7(null, 0, null, "1.7"),

    /// Plain PDF version 2.0.
    v_2_0(null, 0, null, "2.0");

    private static final Logger LOG = Logger.getLogger(PdfFormat.class.getName());

    private final String isoNumber;
    private final int part;
    private final String conformance;
    private final String pdfVersion;

    PdfFormat(String isoNumber, int part, String conformance, String pdfVersion) {
        this.isoNumber = isoNumber;
        this.part = part;
        this.conformance = conformance;
        this.pdfVersion = pdfVersion;
    }

    /// Returns the ISO standard number (e.g. "19005" for PDF/A), or `null`
    /// for plain version targets.
    ///
    /// @return the ISO number, or null
    public String getIsoNumber() {
        return isoNumber;
    }

    /// Returns the part number within the ISO standard (e.g. 1, 2, 3 for PDF/A).
    ///
    /// @return the part number, or 0 for plain version targets
    public int getPart() {
        return part;
    }

    /// Returns the conformance level (e.g. "A", "B", "U"), or `null`
    /// if not applicable.
    ///
    /// @return the conformance level, or null
    public String getConformance() {
        return conformance;
    }

    /// Returns the underlying PDF version string (e.g. "1.4", "1.7").
    ///
    /// @return the PDF version
    public String getPdfVersion() {
        return pdfVersion;
    }

    /// Returns `true` if this format is any PDF/A variant (ISO 19005).
    ///
    /// @return true if PDF/A
    public boolean isPdfA() {
        return "19005".equals(isoNumber);
    }

    /// Returns `true` if this is a PDF/A Level A conformance.
    ///
    /// @return true if Level A
    public boolean isLevelA() {
        return isPdfA() && "A".equals(conformance);
    }

    /// Returns `true` if this is a PDF/A Level U conformance.
    ///
    /// @return true if Level U
    public boolean isLevelU() {
        return isPdfA() && "U".equals(conformance);
    }

    /// Returns `true` if this is a PDF/A-1 variant (part 1).
    ///
    /// @return true if PDF/A-1
    public boolean isPdfA1() {
        return isPdfA() && part == 1;
    }

    /// Returns `true` if this is PDF/A-2 or later (part >= 2).
    ///
    /// @return true if PDF/A-2 or later
    public boolean isPdfA2OrLater() {
        return isPdfA() && part >= 2;
    }

    /// Returns `true` if this is a PDF/A-3 variant (part 3).
    ///
    /// @return true if PDF/A-3
    public boolean isPdfA3() {
        return isPdfA() && part == 3;
    }

    /// Returns `true` if this is a PDF/A-4 variant (part 4).
    ///
    /// @return true if PDF/A-4
    public boolean isPdfA4() {
        return isPdfA() && part == 4;
    }

    /// Returns `true` if this format is any PDF/X variant (ISO 15930).
    ///
    /// @return true if PDF/X
    public boolean isPdfX() {
        return "15930".equals(isoNumber);
    }

    /// Returns `true` if this is PDF/X-1a specifically.
    ///
    /// @return true if PDF/X-1a
    public boolean isPdfX1a() {
        return isPdfX() && (this == PDF_X_1A || this == PDF_X_1A_2001);
    }

    /// Returns `true` if this is PDF/X-3.
    ///
    /// @return true if PDF/X-3
    public boolean isPdfX3() {
        return this == PDF_X_3;
    }

    /// Returns `true` if this is a plain PDF version target (not a standard).
    ///
    /// @return true if this is a version target without an ISO standard
    public boolean isVersionTarget() {
        return isoNumber == null;
    }
}
