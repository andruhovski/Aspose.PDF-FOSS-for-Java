package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

import java.io.IOException;

/**
 * Widget annotation (ISO 32000-1:2008, §12.5.6.19, /Subtype /Widget).
 * <p>
 * Widget annotations represent the visual appearance of interactive form fields.
 * In PDF, a form field and its widget annotation are often the same dictionary.
 * This class serves as the base for all form field classes in
 * {@code org.aspose.pdf.forms}.
 * </p>
 */
public class WidgetAnnotation extends Annotation {

    private org.aspose.pdf.forms.AppearanceCharacteristics cachedCharacteristics;

    /**
     * Constructs a widget annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary
     * @param page the page this annotation belongs to
     */
    public WidgetAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new widget annotation.
     *
     * @param page the page
     * @param rect the rectangle
     */
    public WidgetAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
    }

    /**
     * Returns the field type (/FT), e.g. "Tx", "Btn", "Ch", "Sig".
     *
     * @return the field type, or null
     */
    public String getFieldType() {
        return dict.getNameAsString("FT");
    }

    /**
     * Returns the raw field value (/V).
     *
     * @return the value COS object, or null
     */
    public COSBase getFieldValue() {
        return dict.get("V");
    }

    /**
     * Returns the partial field name (/T).
     *
     * @return the field name, or null
     */
    public String getFieldName() {
        COSBase t = dict.get("T");
        return (t instanceof COSString) ? ((COSString) t).getString() : null;
    }

    /**
     * Returns the highlight mode (/H). Default "I" (invert).
     *
     * @return the highlight mode
     */
    public String getHighlightMode() {
        String h = dict.getNameAsString("H");
        return h != null ? h : "I";
    }

    /**
     * Returns the appearance characteristics dictionary (/MK).
     *
     * @return the MK dictionary, or null
     */
    public COSDictionary getAppearanceCharacteristics() {
        COSBase mk = dict.get("MK");
        return (mk instanceof COSDictionary) ? (COSDictionary) mk : null;
    }

    /**
     * Returns typed access to this widget's appearance characteristics
     * (the {@code /MK} sub-dictionary). The wrapper reads and writes through
     * to the underlying COS dictionary and creates {@code /MK} on demand.
     *
     * @return the characteristics wrapper (never null)
     */
    public org.aspose.pdf.forms.AppearanceCharacteristics getCharacteristics() {
        if (cachedCharacteristics == null) {
            cachedCharacteristics = new org.aspose.pdf.forms.AppearanceCharacteristics(dict);
        }
        return cachedCharacteristics;
    }

    /**
     * Returns the action (/A) associated with this widget.
     *
     * @return the action, or null
     * @throws IOException if parsing fails
     */
    public PdfAction getWidgetAction() throws IOException {
        COSBase a = dict.get("A");
        if (a instanceof COSObjectReference) {
            try { a = ((COSObjectReference) a).dereference(); } catch (Exception e) { return null; }
        }
        return (a instanceof COSDictionary) ? PdfAction.fromDictionary((COSDictionary) a, null) : null;
    }
}
