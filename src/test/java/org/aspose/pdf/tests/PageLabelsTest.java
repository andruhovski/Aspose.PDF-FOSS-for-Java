package org.aspose.pdf.tests;

import org.aspose.pdf.PageLabels;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for page labels (§12.4.2).
 */
public class PageLabelsTest {

    @Test
    public void noPageLabelsReturnsNull() throws IOException {
        COSDictionary catalog = new COSDictionary();
        assertNull(PageLabels.parse(catalog));
    }

    @Test
    public void decimalStyleLabels() throws IOException {
        // Single range: D style starting at page 0
        PageLabels labels = createLabels(0, "D", null, 1);
        assertEquals("1", labels.getLabel(0));
        assertEquals("2", labels.getLabel(1));
        assertEquals("3", labels.getLabel(2));
        assertEquals("10", labels.getLabel(9));
    }

    @Test
    public void romanLowercaseLabels() throws IOException {
        PageLabels labels = createLabels(0, "r", null, 1);
        assertEquals("i", labels.getLabel(0));
        assertEquals("ii", labels.getLabel(1));
        assertEquals("iii", labels.getLabel(2));
        assertEquals("iv", labels.getLabel(3));
        assertEquals("v", labels.getLabel(4));
        assertEquals("ix", labels.getLabel(8));
        assertEquals("x", labels.getLabel(9));
    }

    @Test
    public void romanUppercaseLabels() throws IOException {
        PageLabels labels = createLabels(0, "R", null, 1);
        assertEquals("I", labels.getLabel(0));
        assertEquals("II", labels.getLabel(1));
        assertEquals("IV", labels.getLabel(3));
        assertEquals("XIV", labels.getLabel(13));
    }

    @Test
    public void alphaLowercaseLabels() throws IOException {
        PageLabels labels = createLabels(0, "a", null, 1);
        assertEquals("a", labels.getLabel(0));
        assertEquals("b", labels.getLabel(1));
        assertEquals("z", labels.getLabel(25));
        assertEquals("aa", labels.getLabel(26));
    }

    @Test
    public void alphaUppercaseLabels() throws IOException {
        PageLabels labels = createLabels(0, "A", null, 1);
        assertEquals("A", labels.getLabel(0));
        assertEquals("B", labels.getLabel(1));
        assertEquals("Z", labels.getLabel(25));
    }

    @Test
    public void prefixWithDecimal() throws IOException {
        PageLabels labels = createLabels(0, "D", "A-", 1);
        assertEquals("A-1", labels.getLabel(0));
        assertEquals("A-2", labels.getLabel(1));
        assertEquals("A-3", labels.getLabel(2));
    }

    @Test
    public void multipleRanges() throws IOException {
        // Pages 0-3: roman, pages 4+: decimal
        COSDictionary catalog = new COSDictionary();
        COSDictionary pageLabelsTree = new COSDictionary();
        COSArray nums = new COSArray();

        // Range 1: page 0, roman
        nums.add(COSInteger.valueOf(0));
        COSDictionary range1 = new COSDictionary();
        range1.set(COSName.of("S"), COSName.of("r"));
        nums.add(range1);

        // Range 2: page 4, decimal
        nums.add(COSInteger.valueOf(4));
        COSDictionary range2 = new COSDictionary();
        range2.set(COSName.of("S"), COSName.of("D"));
        nums.add(range2);

        pageLabelsTree.set(COSName.of("Nums"), nums);
        catalog.set(COSName.of("PageLabels"), pageLabelsTree);

        PageLabels labels = PageLabels.parse(catalog);
        assertNotNull(labels);
        assertEquals("i", labels.getLabel(0));
        assertEquals("ii", labels.getLabel(1));
        assertEquals("iv", labels.getLabel(3));
        assertEquals("1", labels.getLabel(4));
        assertEquals("2", labels.getLabel(5));
    }

    @Test
    public void startNumberOffset() throws IOException {
        // Start numbering at 5
        PageLabels labels = createLabels(0, "D", null, 5);
        assertEquals("5", labels.getLabel(0));
        assertEquals("6", labels.getLabel(1));
        assertEquals("14", labels.getLabel(9));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Roman numeral conversion
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void romanNumeralEdgeCases() throws IOException {
        // Test Roman conversions through getLabel()
        PageLabels labels = createLabels(0, "R", null, 1);
        assertEquals("IV", labels.getLabel(3));       // 4
        assertEquals("IX", labels.getLabel(8));       // 9
        assertEquals("XL", labels.getLabel(39));      // 40
        assertEquals("XC", labels.getLabel(89));      // 90
        assertEquals("CD", labels.getLabel(399));     // 400
        assertEquals("CM", labels.getLabel(899));     // 900
        assertEquals("MCMXCIX", labels.getLabel(1998)); // 1999
    }

    @Test
    public void alphaLabelEdgeCases() throws IOException {
        PageLabels labels = createLabels(0, "A", null, 1);
        assertEquals("A", labels.getLabel(0));
        assertEquals("Z", labels.getLabel(25));
        assertEquals("AA", labels.getLabel(26));
        assertEquals("AZ", labels.getLabel(51));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helper
    // ═══════════════════════════════════════════════════════════════

    private PageLabels createLabels(int startPage, String style, String prefix, int startNum)
            throws IOException {
        COSDictionary catalog = new COSDictionary();
        COSDictionary tree = new COSDictionary();
        COSArray nums = new COSArray();
        nums.add(COSInteger.valueOf(startPage));
        COSDictionary rangeDict = new COSDictionary();
        if (style != null) rangeDict.set(COSName.of("S"), COSName.of(style));
        if (prefix != null) rangeDict.set(COSName.of("P"), new COSString(prefix));
        rangeDict.set(COSName.of("St"), COSInteger.valueOf(startNum));
        nums.add(rangeDict);
        tree.set(COSName.of("Nums"), nums);
        catalog.set(COSName.of("PageLabels"), tree);
        return PageLabels.parse(catalog);
    }
}
