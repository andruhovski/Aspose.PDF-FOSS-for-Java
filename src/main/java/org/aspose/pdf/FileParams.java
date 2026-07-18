package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfString;

/// Embedded file parameters (ISO 32000-1:2008, §7.11.4, Table 46).
public class FileParams {

    private final PdfDictionary dict;

    /// Wraps an existing params dictionary.
    public FileParams(PdfDictionary dict) {
        this.dict = dict != null ? dict : new PdfDictionary();
    }

    /// Creates new empty params.
    public FileParams() { this.dict = new PdfDictionary(); }

    /// /Size — uncompressed file size in bytes.
    public long getSize() { return dict.getLong("Size", 0); }

    /// /CreationDate.
    public String getCreationDate() {
        PdfBase d = dict.get("CreationDate");
        return (d instanceof PdfString) ? ((PdfString) d).getString() : null;
    }

    /// /ModDate.
    public String getModDate() {
        PdfBase d = dict.get("ModDate");
        return (d instanceof PdfString) ? ((PdfString) d).getString() : null;
    }

    /// /CheckSum — MD5 digest.
    public String getCheckSum() {
        PdfBase cs = dict.get("CheckSum");
        return (cs instanceof PdfString) ? ((PdfString) cs).getString() : null;
    }

    /// Returns the underlying dictionary.
    public PdfDictionary getPdfDictionary() { return dict; }
}
