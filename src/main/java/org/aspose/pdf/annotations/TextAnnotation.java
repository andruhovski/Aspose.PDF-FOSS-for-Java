package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Text (sticky note) annotation (ISO 32000-1:2008, Section 12.5.6.4, /Subtype /Text).
 * <p>
 * A text annotation represents a sticky note attached to a point in the PDF document.
 * When closed, the annotation appears as an icon; when open, it displays a pop-up
 * window containing the text of the note.
 * </p>
 */
public class TextAnnotation extends MarkupAnnotation {

    /**
     * Constructs a text annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public TextAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new text annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public TextAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Text"));
    }

    /**
     * Returns whether the annotation should initially be displayed open.
     *
     * @return true if the annotation is open, false otherwise (default false)
     */
    public boolean getOpen() {
        return dict.getBoolean("Open", false);
    }

    /**
     * Sets whether the annotation should initially be displayed open.
     *
     * @param open true to display the annotation open
     */
    public void setOpen(boolean open) {
        dict.set(COSName.of("Open"), COSBoolean.valueOf(open));
    }

    /**
     * Returns the icon name for this text annotation (/Name entry).
     *
     * @return the icon name (e.g. "Note", "Comment", "Key"), default "Note"
     */
    public String getIcon() {
        String name = dict.getNameAsString("Name");
        return name != null ? name : "Note";
    }

    /**
     * Sets the icon name for this text annotation (/Name entry).
     *
     * @param icon the icon name (e.g. "Note", "Comment", "Key", "Help", "Insert", "NewParagraph", "Paragraph")
     */
    public void setIcon(String icon) {
        if (icon != null) {
            dict.set(COSName.of("Name"), COSName.of(icon));
        }
    }

    /**
     * Returns the state of the annotation (/State entry).
     * Used for review/marked state models (e.g. "Accepted", "Rejected", "Marked", "Unmarked").
     *
     * @return the state string, or null if not set
     */
    public String getState() {
        COSBase s = dict.get("State");
        if (s instanceof COSString) return ((COSString) s).getString();
        if (s instanceof COSName) return ((COSName) s).getName();
        return null;
    }

    /**
     * Sets the state of the annotation (/State entry).
     *
     * @param state the state string (e.g. "Accepted", "Rejected", "Marked", "Unmarked")
     */
    public void setState(String state) {
        if (state != null) dict.set(COSName.of("State"), COSName.of(state));
        else dict.remove(COSName.of("State"));
    }

    /**
     * Returns the state model of the annotation (/StateModel entry).
     *
     * @return the state model string ("Review" or "Marked"), or null if not set
     */
    public String getStateModel() {
        COSBase sm = dict.get("StateModel");
        if (sm instanceof COSString) return ((COSString) sm).getString();
        if (sm instanceof COSName) return ((COSName) sm).getName();
        return null;
    }

    /**
     * Sets the state model of the annotation (/StateModel entry).
     *
     * @param stateModel the state model string ("Review" or "Marked")
     */
    public void setStateModel(String stateModel) {
        if (stateModel != null) dict.set(COSName.of("StateModel"), COSName.of(stateModel));
        else dict.remove(COSName.of("StateModel"));
    }
}
