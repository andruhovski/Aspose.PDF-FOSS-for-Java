package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// L1.2 growable-height B oracle: the resolved height of a field is asserted against the
/// hand-computed `wrapped-lines × lineHeight` formula, with `minH`/`maxH`/`h`
/// clamping, all measured through the existing [TextLayoutHelper] primitive.
public class XfaGrowableHeightTest {

    private static final String TPL = org.aspose.pdf.engine.xfa.model.XfaNode.TEMPLATE_NS;
    private static final double EPS = 1e-6;

    /// Helvetica 10pt line height — the formula's unit; read from the same primitive under test.
    private static final double LH = TextLayoutHelper.getLineHeight("Helvetica", 10);

    @Test
    void oneLineVersusFiveLinesIsProportional() throws Exception {
        // No fixed h ⇒ growable. Explicit newlines give an exact line count independent of width.
        Element f = field("<field name='m' w='200pt'><ui><textEdit/></ui>"
                + "<font typeface='Arial' size='10pt'/><value><text/></value></field>");

        double one = XfaGrowableHeight.height(f, "one line", 200);
        double five = XfaGrowableHeight.height(f, "a\nb\nc\nd\ne", 200);

        assertEquals(LH, one, EPS, "1 line = 1 × lineHeight");
        assertEquals(5 * LH, five, EPS, "5 lines = 5 × lineHeight");
        assertTrue(XfaGrowableHeight.isGrowable(f), "no fixed h ⇒ growable");
    }

    @Test
    void emptyFieldOccupiesOneLine() throws Exception {
        Element f = field("<field name='m' w='100pt'><font size='10pt'/><value><text/></value></field>");
        assertEquals(LH, XfaGrowableHeight.height(f, null, 100), EPS, "empty ⇒ one line tall");
        assertEquals(LH, XfaGrowableHeight.height(f, "", 100), EPS, "empty string ⇒ one line tall");
    }

    @Test
    void fixedHeightIsNotGrowable() throws Exception {
        Element f = field("<field name='m' w='100pt' h='35pt'><font size='10pt'/><value><text/></value></field>");
        assertFalse(XfaGrowableHeight.isGrowable(f), "explicit h ⇒ fixed");
        assertEquals(35, XfaGrowableHeight.height(f, "anything\nlong\nvalue\nhere\nmore", 100), EPS,
                "fixed h ignores content");
    }

    @Test
    void minHeightClampsUp() throws Exception {
        // 1 line (12pt) is shorter than minH=50 ⇒ clamped up to 50.
        Element f = field("<field name='m' w='100pt' minH='50pt'><font size='10pt'/><value><text/></value></field>");
        assertEquals(50, XfaGrowableHeight.height(f, "short", 100), EPS, "clamped up to minH");
    }

    @Test
    void maxHeightClampsDown() throws Exception {
        // 5 lines (60pt) exceeds maxH=20 ⇒ clamped down to 20.
        Element f = field("<field name='m' w='100pt' maxH='20pt'><font size='10pt'/><value><text/></value></field>");
        assertEquals(20, XfaGrowableHeight.height(f, "a\nb\nc\nd\ne", 100), EPS, "clamped down to maxH");
    }

    @Test
    void marginInsetsAddToContentHeight() throws Exception {
        // top+bottom insets add directly to the line stack.
        Element f = field("<field name='m' w='100pt'><margin topInset='4pt' bottomInset='6pt'/>"
                + "<font size='10pt'/><value><text/></value></field>");
        assertEquals(LH + 10, XfaGrowableHeight.height(f, "one", 100), EPS, "1 line + top(4) + bottom(6)");
    }

    @Test
    void widthDrivenWrapGrowsHeight() throws Exception {
        // Same text, narrower box ⇒ more wrapped lines ⇒ taller. Verified against TextLayoutHelper itself.
        Element f = field("<field name='m'><font size='10pt'/><value><text/></value></field>");
        String text = "the quick brown fox jumps over the lazy dog again and again";
        int wideLines = TextLayoutHelper.wrapText(text, "Helvetica", 10, 400).size();
        int narrowLines = TextLayoutHelper.wrapText(text, "Helvetica", 10, 80).size();
        assertTrue(narrowLines > wideLines, "narrower wraps to more lines (precondition)");
        assertEquals(wideLines * LH, XfaGrowableHeight.height(f, text, 400), EPS, "wide height");
        assertEquals(narrowLines * LH, XfaGrowableHeight.height(f, text, 80), EPS, "narrow height");
    }

    /* helpers */

    private static Element field(String fieldXml) throws Exception {
        Document d = parse("<template xmlns='" + TPL + "'>" + fieldXml + "</template>");
        return (Element) d.getDocumentElement().getElementsByTagNameNS(TPL, "field").item(0);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
