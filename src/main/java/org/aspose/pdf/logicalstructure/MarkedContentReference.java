package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;

/// Marked content reference — links a structure element to marked content
/// in a page's content stream (ISO 32000-1:2008, §14.7.4.2).
///
/// Corresponds to a /Type /MCR dictionary entry in the /K array,
/// or a bare integer MCID.
public class MarkedContentReference {

    private final int mcid;
    private final PdfDictionary page;

    /// Creates a marked content reference.
    ///
    /// @param mcid the marked content identifier
    /// @param page the page dictionary (may be null if inherited)
    public MarkedContentReference(int mcid, PdfDictionary page) {
        this.mcid = mcid;
        this.page = page;
    }

    /// Returns the marked content identifier (MCID).
    public int getMCID() { return mcid; }

    /// Returns the page dictionary, or `null` if inherited from parent.
    public PdfDictionary getPage() { return page; }

    /// Parses a MCR from a PDF dictionary.
    ///
    /// @param dict the MCR dictionary
    /// @return the marked content reference
    public static MarkedContentReference fromDictionary(PdfDictionary dict) {
        int mcid = dict.getInt("MCID", -1);
        PdfBase pg = resolve(dict.get("Pg"));
        return new MarkedContentReference(mcid,
                (pg instanceof PdfDictionary) ? (PdfDictionary) pg : null);
    }

    private static PdfBase resolve(PdfBase obj) {
        if (obj instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
