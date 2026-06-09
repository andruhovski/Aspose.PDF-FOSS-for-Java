package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;

/**
 * Popup annotation (ISO 32000-1:2008, Section 12.5.6.14, /Subtype /Popup).
 * <p>
 * A popup annotation displays text in a pop-up window for entry and editing.
 * It is not used alone but is associated with a markup annotation, its parent
 * annotation, and is used for editing the parent's text.
 * Popup annotations do NOT extend MarkupAnnotation per the ISO specification.
 * </p>
 */
public class PopupAnnotation extends Annotation {

    /**
     * Constructs a popup annotation from an existing PDF dictionary.
     *
     * @param dict the PDF dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public PopupAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new popup annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public PopupAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Popup"));
    }

    /**
     * Returns whether the popup annotation should initially be displayed open.
     *
     * @return true if the popup is open, false otherwise (default false)
     */
    public boolean getOpen() {
        return dict.getBoolean("Open", false);
    }

    /**
     * Sets whether the popup annotation should initially be displayed open.
     *
     * @param open true to display the popup open
     */
    public void setOpen(boolean open) {
        dict.set(PdfName.of("Open"), PdfBoolean.valueOf(open));
    }

    /**
     * Returns the parent annotation with which this popup is associated (/Parent entry).
     *
     * @return the parent annotation, or null if not set or unresolvable
     */
    public Annotation getParent() {
        PdfBase parent = resolveRef(dict.get("Parent"));
        if (parent instanceof PdfDictionary) {
            return Annotation.fromDictionary((PdfDictionary) parent, page);
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
