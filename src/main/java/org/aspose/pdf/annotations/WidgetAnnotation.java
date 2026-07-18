package org.aspose.pdf.annotations;

import org.aspose.pdf.Page;
import org.aspose.pdf.PdfAction;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;

/// Widget annotation (ISO 32000-1:2008, §12.5.6.19, /Subtype /Widget).
///
/// Widget annotations represent the visual appearance of interactive form fields.
/// In PDF, a form field and its widget annotation are often the same dictionary.
/// This class serves as the base for all form field classes in
/// `org.aspose.pdf.forms`.
///
public class WidgetAnnotation extends Annotation {

    private org.aspose.pdf.forms.AppearanceCharacteristics cachedCharacteristics;

    /// Constructs a widget annotation from an existing PDF dictionary.
    ///
    /// @param dict the PDF dictionary
    /// @param page the page this annotation belongs to
    public WidgetAnnotation(PdfDictionary dict, Page page) {
        super(dict, page);
    }

    /// Constructs a new widget annotation.
    ///
    /// @param page the page
    /// @param rect the rectangle
    public WidgetAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(PdfName.of("Subtype"), PdfName.of("Widget"));
    }

    /// Returns the field type (/FT), e.g. "Tx", "Btn", "Ch", "Sig".
    ///
    /// @return the field type, or null
    public String getFieldType() {
        return dict.getNameAsString("FT");
    }

    /// Returns the raw field value (/V).
    ///
    /// @return the value PDF object, or null
    public PdfBase getFieldValue() {
        return dict.get("V");
    }

    /// Returns the partial field name (/T).
    ///
    /// @return the field name, or null
    public String getFieldName() {
        PdfBase t = dict.get("T");
        return (t instanceof PdfString) ? ((PdfString) t).getString() : null;
    }

    /// Returns the highlight mode (/H). Default "I" (invert).
    ///
    /// @return the highlight mode
    public String getHighlightMode() {
        String h = dict.getNameAsString("H");
        return h != null ? h : "I";
    }

    /// Returns the appearance characteristics dictionary (/MK).
    ///
    /// @return the MK dictionary, or null
    public PdfDictionary getAppearanceCharacteristics() {
        PdfBase mk = dict.get("MK");
        return (mk instanceof PdfDictionary) ? (PdfDictionary) mk : null;
    }

    /// Returns typed access to this widget's appearance characteristics
    /// (the `/MK` sub-dictionary). The wrapper reads and writes through
    /// to the underlying PDF dictionary and creates `/MK` on demand.
    ///
    /// @return the characteristics wrapper (never null)
    public org.aspose.pdf.forms.AppearanceCharacteristics getCharacteristics() {
        if (cachedCharacteristics == null) {
            cachedCharacteristics = new org.aspose.pdf.forms.AppearanceCharacteristics(dict);
        }
        return cachedCharacteristics;
    }

    /// Returns the action (/A) associated with this widget.
    ///
    /// @return the action, or null
    /// @throws IOException if parsing fails
    public PdfAction getWidgetAction() throws IOException {
        PdfBase a = dict.get("A");
        if (a instanceof PdfObjectReference) {
            try { a = ((PdfObjectReference) a).dereference(); } catch (Exception e) { return null; }
        }
        return (a instanceof PdfDictionary) ? PdfAction.fromDictionary((PdfDictionary) a, null) : null;
    }
}
