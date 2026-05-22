package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * File attachment annotation (ISO 32000-1:2008, Section 12.5.6.15, /Subtype /FileAttachment).
 * <p>
 * A file attachment annotation contains a reference to a file, which typically
 * is embedded in the PDF document. The annotation displays an icon (such as a
 * pushpin or paperclip) indicating the presence of the attached file.
 * </p>
 */
public class FileAttachmentAnnotation extends MarkupAnnotation {

    /**
     * Constructs a file attachment annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public FileAttachmentAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new file attachment annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public FileAttachmentAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("FileAttachment"));
    }

    /**
     * Returns the icon name for this file attachment annotation (/Name entry).
     *
     * @return the icon name (e.g. "PushPin", "Paperclip", "Graph", "Tag"), default "PushPin"
     */
    public String getIcon() {
        String name = dict.getNameAsString("Name");
        return name != null ? name : "PushPin";
    }
}
