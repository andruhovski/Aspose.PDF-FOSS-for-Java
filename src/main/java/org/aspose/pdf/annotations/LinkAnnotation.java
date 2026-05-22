package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

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
     * Constructs a link annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public LinkAnnotation(COSDictionary dict, Page page) {
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
        dict.set(COSName.of("Subtype"), COSName.of("Link"));
    }

    /**
     * Returns the action associated with this link annotation (/A entry).
     *
     * @return the action, or null if not set or if parsing fails
     * @throws IOException if parsing fails
     */
    public PdfAction getAction() throws IOException {
        COSBase a = resolveRef(dict.get("A"));
        if (a instanceof COSDictionary) {
            return PdfAction.fromDictionary((COSDictionary) a, null);
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
            dict.set(COSName.of("A"), action.getCOSDictionary());
        } else {
            dict.remove(COSName.of("A"));
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
        COSBase dest = resolveRef(dict.get("Dest"));
        if (dest instanceof COSArray) {
            return ExplicitDestination.fromCOSArray((COSArray) dest, doc);
        }
        // Named destination (string or name) — resolve through document
        if (dest instanceof COSString || dest instanceof COSName) {
            String name = (dest instanceof COSString)
                    ? ((COSString) dest).getString()
                    : ((COSName) dest).getName();
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
    private COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }
}
