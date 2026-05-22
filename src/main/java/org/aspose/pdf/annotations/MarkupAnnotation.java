package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base for markup annotations (ISO 32000-1:2008, §12.5.6.2).
 * Adds author, subject, opacity, popup, rich text, creation date, replies.
 */
public abstract class MarkupAnnotation extends Annotation {

    /**
     * Constructs a markup annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    protected MarkupAnnotation(COSDictionary dict, Page page) { super(dict, page); }

    /**
     * Constructs a new markup annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    protected MarkupAnnotation(Page page, Rectangle rect) { super(page, rect); }

    /**
     * Returns the author or title of the annotation (/T entry).
     *
     * @return the title string, or null if not set
     */
    public String getTitle() {
        COSBase t = dict.get("T");
        return (t instanceof COSString) ? ((COSString) t).getString() : null;
    }

    /**
     * Sets the author or title of the annotation (/T entry).
     *
     * @param title the title string, or null to remove
     */
    public void setTitle(String title) {
        if (title != null) dict.set(COSName.of("T"), new COSString(title.getBytes(StandardCharsets.UTF_8)));
        else dict.remove(COSName.of("T"));
    }

    /**
     * Returns the opacity of the annotation (/CA entry, 0.0-1.0, default 1.0).
     *
     * @return the opacity value
     */
    public double getOpacity() {
        COSBase ca = dict.get("CA");
        if (ca instanceof COSFloat) return ((COSFloat) ca).doubleValue();
        if (ca instanceof COSInteger) return (double) ((COSInteger) ca).intValue();
        return 1.0;
    }

    /**
     * Sets the opacity of the annotation (/CA entry, 0.0-1.0).
     *
     * @param opacity the opacity value
     */
    public void setOpacity(double opacity) { dict.set(COSName.of("CA"), new COSFloat(opacity)); }

    /**
     * Returns the subject of the annotation (/Subj entry).
     *
     * @return the subject string, or null if not set
     */
    public String getSubject() {
        COSBase s = dict.get("Subj");
        return (s instanceof COSString) ? ((COSString) s).getString() : null;
    }

    /**
     * Sets the subject of the annotation (/Subj entry).
     *
     * @param subject the subject string, or null to remove
     */
    public void setSubject(String subject) {
        if (subject != null) dict.set(COSName.of("Subj"), new COSString(subject.getBytes(StandardCharsets.UTF_8)));
        else dict.remove(COSName.of("Subj"));
    }

    /**
     * Returns the rich text content of the annotation (/RC entry, XHTML format).
     *
     * @return the rich text string, or null if not set
     */
    public String getRichText() {
        COSBase rc = dict.get("RC");
        return (rc instanceof COSString) ? ((COSString) rc).getString() : null;
    }

    /**
     * Sets the rich text content of the annotation (/RC entry, XHTML format).
     *
     * @param richText the rich text string
     */
    public void setRichText(String richText) {
        if (richText != null) dict.set(COSName.of("RC"), new COSString(richText.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Returns the creation date of the annotation (/CreationDate entry).
     *
     * @return the creation date string, or null if not set
     */
    public String getCreationDate() {
        COSBase d = dict.get("CreationDate");
        return (d instanceof COSString) ? ((COSString) d).getString() : null;
    }

    /**
     * Sets the creation date of the annotation (/CreationDate entry).
     *
     * @param date the creation date string in PDF date format, or null to remove
     */
    public void setCreationDate(String date) {
        if (date != null) dict.set(COSName.of("CreationDate"), new COSString(date.getBytes(StandardCharsets.UTF_8)));
        else dict.remove(COSName.of("CreationDate"));
    }

    /**
     * Returns the reply type of this annotation (/RT entry).
     * Values: "R" (reply) or "Group" (grouped).
     *
     * @return the reply type name, or null if not set
     */
    public String getReplyType() {
        COSBase rt = dict.get("RT");
        if (rt instanceof COSName) return ((COSName) rt).getName();
        return null;
    }

    /**
     * Sets the reply type of this annotation (/RT entry).
     *
     * @param replyType "R" for reply, "Group" for grouped, or null to remove
     */
    public void setReplyType(String replyType) {
        if (replyType != null) dict.set(COSName.of("RT"), COSName.of(replyType));
        else dict.remove(COSName.of("RT"));
    }

    /**
     * Returns the associated popup annotation (/Popup entry).
     *
     * @return the popup annotation, or null if not set
     */
    public PopupAnnotation getPopup() {
        COSBase p = resolveRef(dict.get("Popup"));
        if (p instanceof COSDictionary) return new PopupAnnotation((COSDictionary) p, page);
        return null;
    }

    /**
     * Sets the associated popup annotation (/Popup entry).
     *
     * @param popup the popup annotation to associate
     */
    public void setPopup(PopupAnnotation popup) {
        if (popup != null) dict.set(COSName.of("Popup"), popup.getCOSDictionary());
    }

    /**
     * Returns the annotation this one is in reply to (/IRT entry).
     *
     * @return the referenced annotation, or null if not set
     */
    public Annotation getInReplyTo() {
        COSBase irt = resolveRef(dict.get("IRT"));
        if (irt instanceof COSDictionary) return Annotation.fromDictionary((COSDictionary) irt, page);
        return null;
    }

    /**
     * Sets the annotation this one is in reply to (/IRT entry).
     *
     * @param annotation the annotation being replied to
     */
    public void setInReplyTo(Annotation annotation) {
        if (annotation != null) dict.set(COSName.of("IRT"), annotation.getCOSDictionary());
    }

    private COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try { return ((COSObjectReference) val).dereference(); }
            catch (Exception e) { return null; }
        }
        return val;
    }
}
