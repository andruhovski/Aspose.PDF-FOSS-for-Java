package org.aspose.pdf.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A section of a page containing paragraphs detected during markup analysis.
///
/// Sections are separated by larger vertical gaps than paragraph breaks.
///
public class MarkupSection {

    private final List<MarkupParagraph> paragraphs = new ArrayList<>();

    /// Returns the paragraphs in this section.
    ///
    /// @return unmodifiable list of paragraphs
    public List<MarkupParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    /// Adds a paragraph to this section.
    ///
    /// @param paragraph the paragraph to add
    public void addParagraph(MarkupParagraph paragraph) {
        if (paragraph != null) {
            paragraphs.add(paragraph);
        }
    }
}
