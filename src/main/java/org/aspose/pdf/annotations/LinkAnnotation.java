package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;

/**
 * Link annotation (ISO 32000-1:2008, Section 12.5.6.5, /Subtype /Link).
 * <p>
 * A link annotation represents either a hypertext link to a destination elsewhere
 * in the document or an action to be performed. Link annotations do NOT extend
 * MarkupAnnotation per the ISO specification.
 * </p>
 */
public class LinkAnnotation extends Annotation {

    /**
     * Constructs a link annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public LinkAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new link annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public LinkAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Link"));
    }

    /**
     * Returns the action associated with this link annotation (/A entry).
     *
     * @return the action, or null if not set or if parsing fails
     * @throws IOException if parsing fails
     */
    public PdfAction getAction() throws IOException {
        PdfBase a = resolveRef(dict.get("A"));
        if (a instanceof PdfDictionary) {
            return PdfAction.fromDictionary((PdfDictionary) a, null);
        }
        return null;
    }

    /**
     * Sets the action associated with this link annotation (/A entry).
     *
     * @param action the action to set
     */
    public void setAction(PdfAction action) {
        if (action != null) {
            dict.set(PdfName.of("A"), action.getPdfDictionary());
        } else {
            dict.remove(PdfName.of("A"));
        }
    }

    /**
     * Returns the explicit destination associated with this link annotation (/Dest entry).
     *
     * @param doc the document for resolving page references
     * @return the destination, or null if not set or invalid
     * @throws IOException if resolution fails
     */
    public ExplicitDestination getDestination(Document doc) throws IOException {
        PdfBase dest = resolveRef(dict.get("Dest"));
        if (dest instanceof PdfArray) {
            return ExplicitDestination.fromPdfArray((PdfArray) dest, doc);
        }
        // Named destination (string or name) — resolve through document
        if (dest instanceof PdfString || dest instanceof PdfName) {
            String name = (dest instanceof PdfString)
                    ? ((PdfString) dest).getString()
                    : ((PdfName) dest).getName();
            if (doc != null) {
                NamedDestinations nd = doc.getNamedDestinations();
                if (nd != null) return nd.get(name);
            }
        }
        return null;
    }

    /**
     * Resolves indirect references to their underlying objects.
     *
     * @param val the value to resolve
     * @return the resolved value, or the original if not an indirect reference
     */
    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }
}
