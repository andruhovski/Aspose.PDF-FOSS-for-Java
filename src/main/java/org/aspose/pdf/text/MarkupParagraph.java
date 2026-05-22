package org.aspose.pdf.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A paragraph detected during markup analysis of a PDF page.
 * <p>
 * Groups text fragments that belong to the same logical paragraph based
 * on vertical proximity and reading order.
 * </p>
 */
public class MarkupParagraph {

    private final List<TextFragment> fragments = new ArrayList<>();

    /**
     * Returns the text fragments in this paragraph.
     *
     * @return unmodifiable list of fragments
     */
    public List<TextFragment> getFragments() {
        return Collections.unmodifiableList(fragments);
    }

    /**
     * Adds a text fragment to this paragraph.
     *
     * @param fragment the fragment to add
     */
    public void addFragment(TextFragment fragment) {
        if (fragment != null) {
            fragments.add(fragment);
        }
    }

    /**
     * Returns the concatenated text of all fragments, joined with spaces.
     *
     * @return the paragraph text
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragments.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(fragments.get(i).getText());
        }
        return sb.toString();
    }
}
