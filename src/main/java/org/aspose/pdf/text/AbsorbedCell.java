package org.aspose.pdf.text;

import org.aspose.pdf.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A cell detected in a table during absorption.
 * <p>
 * Contains the text fragments found within the cell boundaries and the
 * bounding rectangle of the cell on the page.
 * </p>
 */
public class AbsorbedCell {

    private final List<TextFragment> textFragments = new ArrayList<>();
    private Rectangle rectangle;

    /**
     * Adds a text fragment to this cell.
     *
     * @param fragment the fragment to add
     */
    public void addTextFragment(TextFragment fragment) {
        if (fragment != null) {
            textFragments.add(fragment);
        }
    }

    /**
     * Returns the text fragments in this cell.
     *
     * @return unmodifiable list of text fragments
     */
    public List<TextFragment> getTextFragments() {
        return Collections.unmodifiableList(textFragments);
    }

    /**
     * Returns the bounding rectangle of this cell.
     *
     * @return the rectangle, or null if not set
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Sets the bounding rectangle of this cell.
     *
     * @param rectangle the bounding rectangle
     */
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    /**
     * Returns the concatenated text content of all fragments in this cell.
     *
     * @return the cell text
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < textFragments.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(textFragments.get(i).getText());
        }
        return sb.toString();
    }
}
