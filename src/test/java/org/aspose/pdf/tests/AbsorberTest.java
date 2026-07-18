package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.text.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for TableAbsorber and ParagraphAbsorber.
public class AbsorberTest {

    @Test
    public void testTableAbsorberBasic() throws IOException {
        // Create a document with table-like text (multiple fragments per row, multiple rows)
        Document doc = new Document();
        Page page = doc.getPages().add();

        // Simulate table by adding text fragments with positions in a grid pattern
        // Since visit() uses TextFragmentAbsorber internally which parses content streams,
        // we test the absorber API without actual content (should find no tables)
        TableAbsorber absorber = new TableAbsorber();
        absorber.visit(page);

        // Empty page should yield no tables
        List<AbsorbedTable> tables = absorber.getTableList();
        assertNotNull(tables);
        assertEquals(0, tables.size());
    }

    @Test
    public void testTableAbsorberReturnType() {
        TableAbsorber absorber = new TableAbsorber();
        List<AbsorbedTable> tables = absorber.getTableList();
        assertNotNull(tables);
        assertTrue(tables.isEmpty());
    }

    @Test
    public void testAbsorbedCellText() {
        AbsorbedCell cell = new AbsorbedCell();
        cell.addTextFragment(new TextFragment("Hello"));
        cell.addTextFragment(new TextFragment("World"));
        assertEquals("Hello World", cell.getText());
        assertEquals(2, cell.getTextFragments().size());
    }

    @Test
    public void testAbsorbedCellRectangle() {
        AbsorbedCell cell = new AbsorbedCell();
        assertNull(cell.getRectangle());
        Rectangle rect = new Rectangle(0, 0, 100, 50);
        cell.setRectangle(rect);
        assertEquals(rect, cell.getRectangle());
    }

    @Test
    public void testAbsorbedRowCells() {
        AbsorbedRow row = new AbsorbedRow();
        assertTrue(row.getCellList().isEmpty());
        row.addCell(new AbsorbedCell());
        row.addCell(new AbsorbedCell());
        assertEquals(2, row.getCellList().size());
    }

    @Test
    public void testAbsorbedTableRows() {
        AbsorbedTable table = new AbsorbedTable();
        assertTrue(table.getRowList().isEmpty());
        table.addRow(new AbsorbedRow());
        assertEquals(1, table.getRowList().size());
        assertNull(table.getRectangle());
        table.setRectangle(new Rectangle(0, 0, 200, 300));
        assertNotNull(table.getRectangle());
    }

    @Test
    public void testParagraphAbsorberBasic() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();

        ParagraphAbsorber absorber = new ParagraphAbsorber();
        absorber.visit(page);

        List<PageMarkup> markups = absorber.getPageMarkups();
        assertNotNull(markups);
        assertEquals(1, markups.size());
        assertEquals(page, markups.get(0).getPage());
    }

    @Test
    public void testParagraphAbsorberVisitDocument() throws IOException {
        Document doc = new Document();
        doc.getPages().add();
        doc.getPages().add();

        ParagraphAbsorber absorber = new ParagraphAbsorber();
        absorber.visit(doc);

        assertEquals(2, absorber.getPageMarkups().size());
    }

    @Test
    public void testMarkupParagraphText() {
        MarkupParagraph para = new MarkupParagraph();
        para.addFragment(new TextFragment("Line one"));
        para.addFragment(new TextFragment("Line two"));
        assertEquals("Line one Line two", para.getText());
        assertEquals(2, para.getFragments().size());
    }

    @Test
    public void testMarkupSectionParagraphs() {
        MarkupSection section = new MarkupSection();
        assertTrue(section.getParagraphs().isEmpty());
        section.addParagraph(new MarkupParagraph());
        assertEquals(1, section.getParagraphs().size());
    }

    @Test
    public void testPageMarkupSections() throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();
        PageMarkup markup = new PageMarkup(page);
        assertEquals(page, markup.getPage());
        assertTrue(markup.getSections().isEmpty());
        markup.addSection(new MarkupSection());
        assertEquals(1, markup.getSections().size());
    }

    @Test
    public void testNullSafety() {
        AbsorbedCell cell = new AbsorbedCell();
        cell.addTextFragment(null); // should be ignored
        assertEquals(0, cell.getTextFragments().size());

        AbsorbedRow row = new AbsorbedRow();
        row.addCell(null); // should be ignored
        assertEquals(0, row.getCellList().size());

        AbsorbedTable table = new AbsorbedTable();
        table.addRow(null); // should be ignored
        assertEquals(0, table.getRowList().size());

        MarkupParagraph para = new MarkupParagraph();
        para.addFragment(null); // should be ignored
        assertEquals(0, para.getFragments().size());

        MarkupSection section = new MarkupSection();
        section.addParagraph(null); // should be ignored
        assertEquals(0, section.getParagraphs().size());
    }
}
