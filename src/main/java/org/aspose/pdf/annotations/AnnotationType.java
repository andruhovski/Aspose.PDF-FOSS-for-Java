package org.aspose.pdf.annotations;

/**
 * Enumerates the standard PDF annotation types (ISO 32000-1:2008, §12.5.6).
 * <p>
 * Each value corresponds to a /Subtype name in the annotation dictionary.
 * </p>
 */
public enum AnnotationType {

    /** Text (sticky note) annotation. */
    Text("Text"),
    /** Link annotation. */
    Link("Link"),
    /** Free text annotation. */
    FreeText("FreeText"),
    /** Line annotation. */
    Line("Line"),
    /** Square annotation. */
    Square("Square"),
    /** Circle annotation. */
    Circle("Circle"),
    /** Polygon annotation. */
    Polygon("Polygon"),
    /** Polyline annotation. */
    PolyLine("PolyLine"),
    /** Highlight annotation. */
    Highlight("Highlight"),
    /** Underline annotation. */
    Underline("Underline"),
    /** Squiggly underline annotation. */
    Squiggly("Squiggly"),
    /** Strikeout annotation. */
    StrikeOut("StrikeOut"),
    /** Stamp annotation. */
    Stamp("Stamp"),
    /** Caret annotation. */
    Caret("Caret"),
    /** Ink (freehand) annotation. */
    Ink("Ink"),
    /** Popup annotation. */
    Popup("Popup"),
    /** File attachment annotation. */
    FileAttachment("FileAttachment"),
    /** Widget (form field) annotation. */
    Widget("Widget"),
    /** Redaction annotation. */
    Redact("Redact"),
    /** Watermark annotation. */
    Watermark("Watermark"),
    /** Screen annotation. */
    Screen("Screen");

    private final String subtype;

    AnnotationType(String subtype) {
        this.subtype = subtype;
    }

    /**
     * Returns the PDF /Subtype name for this annotation type.
     *
     * @return the subtype string
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Looks up an AnnotationType by its PDF /Subtype name.
     *
     * @param subtype the subtype string
     * @return the matching AnnotationType, or null if not found
     */
    public static AnnotationType fromSubtype(String subtype) {
        for (AnnotationType at : values()) {
            if (at.subtype.equals(subtype)) return at;
        }
        return null;
    }
}
