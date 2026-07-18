package org.aspose.pdf.text;

import org.aspose.pdf.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Markup analysis result for a single page.
///
/// Contains the sections (groups of paragraphs) detected on the page.
///
public class PageMarkup {

    private final Page page;
    private final List<MarkupSection> sections = new ArrayList<>();

    /// Creates a PageMarkup for the given page.
    ///
    /// @param page the source page
    public PageMarkup(Page page) {
        this.page = page;
    }

    /// Returns the source page.
    ///
    /// @return the page
    public Page getPage() {
        return page;
    }

    /// Returns the sections detected on this page.
    ///
    /// @return unmodifiable list of sections
    public List<MarkupSection> getSections() {
        return Collections.unmodifiableList(sections);
    }

    /// Adds a section to this page markup.
    ///
    /// @param section the section to add
    public void addSection(MarkupSection section) {
        if (section != null) {
            sections.add(section);
        }
    }
}
