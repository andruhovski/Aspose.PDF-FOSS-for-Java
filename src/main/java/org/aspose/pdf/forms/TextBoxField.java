package org.aspose.pdf.forms;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Text input field (/FT /Tx) (ISO 32000-1:2008, §12.7.4.3).
 * <p>
 * Represents a single-line or multi-line text input field in an interactive form.
 * </p>
 */
public class TextBoxField extends Field {

    private static final int COMB_FLAG = 1 << 24;

    /**
     * Constructs a text box field from an existing COS dictionary.
     *
     * @param dict     the COS dictionary backing this field
     * @param page     the page this field belongs to (may be null)
     * @param fullName the fully-qualified dotted name
     */
    public TextBoxField(COSDictionary dict, Page page, String fullName) {
        super(dict, page, fullName);
    }

    /**
     * Constructs a new text box field on the given page with the specified rectangle.
     *
     * @param page the page
     * @param rect the field rectangle
     */
    public TextBoxField(Page page, Rectangle rect) {
        super(new COSDictionary(), page, "");
        dict.set(COSName.of("Type"), COSName.of("Annot"));
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        dict.set(COSName.of("FT"), COSName.of("Tx"));
        setRect(rect);
    }

    /**
     * Constructs a new text box field associated with the first page of the document.
     * Useful for Aspose-compatible code paths that create a field from a document first.
     *
     * @param document the owning document
     * @param rect the field rectangle
     */
    public TextBoxField(Document document, Rectangle rect) {
        this(document != null ? firstPage(document) : null, rect);
    }

    /**
     * Constructs a new text box field on the given page with multiple rectangles.
     * <p>
     * This creates a single field with multiple widget annotations (one per rectangle),
     * stored as /Kids entries. This is useful when the same field needs to appear
     * at multiple locations on a page or across pages.
     * </p>
     *
     * @param page  the page
     * @param rects the array of rectangles for the field's widgets
     */
    public TextBoxField(Page page, Rectangle[] rects) {
        super(new COSDictionary(), page, "");
        dict.set(COSName.of("Type"), COSName.of("Annot"));
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        dict.set(COSName.of("FT"), COSName.of("Tx"));
        if (rects != null && rects.length > 0) {
            if (rects.length == 1) {
                setRect(rects[0]);
            } else {
                // Multiple rects: create /Kids array with one widget per rect
                COSArray kids = new COSArray();
                for (Rectangle rect : rects) {
                    COSDictionary kid = new COSDictionary();
                    kid.set(COSName.of("Type"), COSName.of("Annot"));
                    kid.set(COSName.of("Subtype"), COSName.of("Widget"));
                    if (rect != null) {
                        kid.set(COSName.of("Rect"), rect.toCOSArray());
                    }
                    kids.add(kid);
                }
                dict.set(COSName.of("Kids"), kids);
                // Set main Rect to the first rectangle
                setRect(rects[0]);
            }
        }
    }

    /**
     * Returns whether this text field is multiline (/Ff bit 13).
     *
     * @return true if multiline
     */
    public boolean isMultiline() {
        return (getFieldFlags() & (1 << 12)) != 0;
    }

    /**
     * Sets whether this text field is multiline (/Ff bit 13).
     *
     * @param ml true to enable multiline
     */
    public void setMultiline(boolean ml) {
        int ff = getFieldFlags();
        setFieldFlags(ml ? (ff | (1 << 12)) : (ff & ~(1 << 12)));
    }

    /**
     * Returns whether this text field is a password field (/Ff bit 14).
     *
     * @return true if password
     */
    public boolean isPassword() {
        return (getFieldFlags() & (1 << 13)) != 0;
    }

    /**
     * Returns the maximum length of text (/MaxLen entry).
     *
     * @return the max length, or 0 if not set
     */
    public int getMaxLen() {
        return dict.getInt("MaxLen", 0);
    }

    /**
     * Sets the maximum length of text (/MaxLen entry).
     *
     * @param maxLen the maximum length
     */
    public void setMaxLen(int maxLen) {
        dict.set(COSName.of("MaxLen"), COSInteger.valueOf(maxLen));
    }

    /**
     * Returns whether comb formatting is enabled (/Ff bit 25).
     *
     * @return true if comb formatting is enabled
     */
    public boolean isForceCombs() {
        return (getFieldFlags() & COMB_FLAG) != 0;
    }

    /**
     * Sets whether comb formatting is enabled (/Ff bit 25).
     *
     * @param value true to enable comb formatting
     */
    public void setForceCombs(boolean value) {
        int flags = getFieldFlags();
        setFieldFlags(value ? (flags | COMB_FLAG) : (flags & ~COMB_FLAG));
    }

    private static Page firstPage(Document document) {
        try {
            if (document.getPages().getCount() == 0) {
                return document.getPages().add();
            }
            return document.getPages().get(1);
        } catch (Exception e) {
            return null;
        }
    }
}
