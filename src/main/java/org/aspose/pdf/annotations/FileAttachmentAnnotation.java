package org.aspose.pdf.annotations;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// File attachment annotation (ISO 32000-1:2008, Section 12.5.6.15, /Subtype /FileAttachment).
///
/// A file attachment annotation contains a reference to a file, which typically
/// is embedded in the PDF document. The annotation displays an icon (such as a
/// pushpin or paperclip) indicating the presence of the attached file.
///
public class FileAttachmentAnnotation extends MarkupAnnotation {

    /// Constructs a file attachment annotation from an existing PDF dictionary.
    ///
    /// @param dict the PDF dictionary backing this annotation
    /// @param page the page this annotation belongs to
    public FileAttachmentAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /// Constructs a new file attachment annotation with the given rectangle on the specified page.
    ///
    /// @param page the page this annotation belongs to
    /// @param rect the annotation rectangle
    public FileAttachmentAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("FileAttachment"));
    }

    /// Returns the icon name for this file attachment annotation (/Name entry).
    ///
    /// @return the icon name (e.g. "PushPin", "Paperclip", "Graph", "Tag"), default "PushPin"
    public String getIcon() {
        String name = dict.getNameAsString("Name");
        return name != null ? name : "PushPin";
    }

    /// Sets the icon name for this file attachment annotation
    /// (ISO 32000-1:2008 §12.5.6.15, Table 184 — `/Name` entry).
    ///
    /// The standard predefined values are `"Graph"`, `"PushPin"`,
    /// `"Paperclip"` and `"Tag"`. Any other name is preserved
    /// verbatim; viewers that don't recognise it fall back to their default
    /// icon.
    ///
    /// @param icon the icon name to set; pass `null` to remove the
    ///             `/Name` entry entirely (viewer will use its default).
    public void setIcon(String icon) {
        if (icon == null) {
            dict.set(PdfName.of("Name"), (PdfBase) null);
        } else {
            dict.set(PdfName.of("Name"), PdfName.of(icon));
        }
    }
}
