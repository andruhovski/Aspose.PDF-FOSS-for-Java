package org.aspose.pdf.text;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Extracts paragraph structures from PDF pages by analyzing
 * text fragment positions and line spacing.
 * <p>
 * Groups text fragments into paragraphs based on vertical proximity
 * and reading order (left-to-right, top-to-bottom). Lines with small
 * vertical gaps are grouped into the same paragraph; larger gaps indicate
 * paragraph breaks, and even larger gaps indicate section breaks.
 * </p>
 */
public class ParagraphAbsorber {

    private static final Logger LOG = Logger.getLogger(ParagraphAbsorber.class.getName());

    /** Tolerance for grouping fragments into the same line (points). */
    private static final double LINE_TOLERANCE = 2.0;

    /** Gap threshold for paragraph break — lines separated by more than this are in different paragraphs. */
    private static final double PARAGRAPH_GAP = 14.0;

    /** Gap threshold for section break — gaps larger than this separate sections. */
    private static final double SECTION_GAP = 28.0;

    private final List<PageMarkup> pageMarkups = new ArrayList<>();

    /**
     * Visits a single page and extracts paragraph structures.
     *
     * @param page the page to analyze
     * @throws IOException if text extraction fails
     */
    public void visit(Page page) throws IOException {
        // 1. Extract all text fragments
        TextFragmentAbsorber tfa = new TextFragmentAbsorber();
        tfa.visit(page);
        TextFragmentCollection fragments = tfa.getTextFragments();

        PageMarkup markup = new PageMarkup(page);

        if (fragments == null || fragments.size() == 0) {
            pageMarkups.add(markup);
            return;
        }

        // 2. Collect fragments with positions
        List<TextFragment> positioned = new ArrayList<>();
        for (TextFragment f : fragments) {
            if (f.getPosition() != null) {
                positioned.add(f);
            }
        }

        if (positioned.isEmpty()) {
            // Add all fragments as a single paragraph if no positions
            MarkupSection section = new MarkupSection();
            MarkupParagraph para = new MarkupParagraph();
            for (TextFragment f : fragments) {
                para.addFragment(f);
            }
            section.addParagraph(para);
            markup.addSection(section);
            pageMarkups.add(markup);
            return;
        }

        // 3. Sort by Y descending (top to bottom), then X ascending (left to right)
        positioned.sort(Comparator
                .comparingDouble((TextFragment f) -> -f.getPosition().getYIndent())
                .thenComparingDouble(f -> f.getPosition().getXIndent()));

        // 4. Group into lines by Y coordinate
        List<List<TextFragment>> lines = groupByY(positioned);

        // 5. Group lines into paragraphs, paragraphs into sections
        MarkupSection currentSection = new MarkupSection();
        MarkupParagraph currentPara = new MarkupParagraph();

        for (int i = 0; i < lines.size(); i++) {
            List<TextFragment> line = lines.get(i);

            // Add all fragments from the line to current paragraph
            for (TextFragment f : line) {
                currentPara.addFragment(f);
            }

            // Check gap to next line
            if (i + 1 < lines.size()) {
                double currentY = averageY(line);
                double nextY = averageY(lines.get(i + 1));
                double gap = Math.abs(currentY - nextY);

                if (gap > SECTION_GAP) {
                    // Section break
                    currentSection.addParagraph(currentPara);
                    markup.addSection(currentSection);
                    currentSection = new MarkupSection();
                    currentPara = new MarkupParagraph();
                } else if (gap > PARAGRAPH_GAP) {
                    // Paragraph break
                    currentSection.addParagraph(currentPara);
                    currentPara = new MarkupParagraph();
                }
            }
        }

        // Finalize remaining content
        if (currentPara.getFragments().size() > 0) {
            currentSection.addParagraph(currentPara);
        }
        if (currentSection.getParagraphs().size() > 0) {
            markup.addSection(currentSection);
        }

        pageMarkups.add(markup);
        LOG.fine(() -> "ParagraphAbsorber: page analyzed, " + markup.getSections().size() + " section(s)");
    }

    /**
     * Visits all pages of a document and extracts paragraph structures.
     *
     * @param document the document to analyze
     * @throws IOException if text extraction fails
     */
    public void visit(Document document) throws IOException {
        for (int i = 1; i <= document.getPages().getCount(); i++) {
            visit(document.getPages().get(i));
        }
    }

    /**
     * Returns the page markup results for all visited pages.
     *
     * @return unmodifiable list of page markups
     */
    public List<PageMarkup> getPageMarkups() {
        return Collections.unmodifiableList(pageMarkups);
    }

    /**
     * Groups text fragments into lines by Y-coordinate proximity.
     */
    private List<List<TextFragment>> groupByY(List<TextFragment> fragments) {
        List<List<TextFragment>> lines = new ArrayList<>();
        List<TextFragment> currentLine = new ArrayList<>();
        double currentY = Double.NaN;

        for (TextFragment f : fragments) {
            double y = f.getPosition().getYIndent();
            if (Double.isNaN(currentY) || Math.abs(y - currentY) <= LINE_TOLERANCE) {
                currentLine.add(f);
                if (Double.isNaN(currentY)) {
                    currentY = y;
                }
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.sort(Comparator.comparingDouble(
                            fr -> fr.getPosition().getXIndent()));
                    lines.add(currentLine);
                }
                currentLine = new ArrayList<>();
                currentLine.add(f);
                currentY = y;
            }
        }
        if (!currentLine.isEmpty()) {
            currentLine.sort(Comparator.comparingDouble(
                    fr -> fr.getPosition().getXIndent()));
            lines.add(currentLine);
        }
        return lines;
    }

    /**
     * Returns the average Y coordinate of fragments in a line.
     */
    private double averageY(List<TextFragment> line) {
        double sum = 0;
        for (TextFragment f : line) {
            sum += f.getPosition().getYIndent();
        }
        return sum / line.size();
    }
}
