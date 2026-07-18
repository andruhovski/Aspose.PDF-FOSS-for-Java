package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;

/// Object reference — links a structure element to a PDF object such as an annotation
/// (ISO 32000-1:2008, §14.7.4.3).
///
/// Corresponds to a /Type /OBJR dictionary entry in the /K array.
public class ObjectReference {

    private final PdfDictionary referencedObject;
    private final PdfDictionary page;

    /// Creates an object reference.
    ///
    /// @param referencedObject the referenced object dictionary
    /// @param page             the page dictionary (may be null)
    public ObjectReference(PdfDictionary referencedObject, PdfDictionary page) {
        this.referencedObject = referencedObject;
        this.page = page;
    }

    /// Returns the referenced object dictionary.
    public PdfDictionary getReferencedObject() { return referencedObject; }

    /// Returns the page dictionary, or `null`.
    public PdfDictionary getPage() { return page; }

    /// Parses an OBJR from a PDF dictionary.
    ///
    /// @param dict the OBJR dictionary
    /// @return the object reference
    public static ObjectReference fromDictionary(PdfDictionary dict) {
        PdfBase obj = resolve(dict.get("Obj"));
        PdfBase pg = resolve(dict.get("Pg"));
        return new ObjectReference(
                (obj instanceof PdfDictionary) ? (PdfDictionary) obj : null,
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
