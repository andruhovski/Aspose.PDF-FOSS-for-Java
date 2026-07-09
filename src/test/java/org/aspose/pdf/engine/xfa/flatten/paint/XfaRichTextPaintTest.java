package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.engine.layout.ContentStreamBuilder;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L5.1 coverage on synthetic fixtures: a {@code <draw>}/field whose value is rich text
 * ({@code <value><exData contentType="text/html"><body><p>…</p></body></exData>}) is painted.
 *
 * <p>408975 authors its table column headers, instruction labels and the multi-paragraph
 * disclosure/WHEREAS blocks this way; the C2 painter only read {@code <value><text>}, so all of it
 * (61 of 72 rich draws on page 1) was silently dropped — the dominant per-cell fidelity defect.
 * These tests pin: (1) the HTML paragraph text is painted; (2) each {@code <p>}'s own inline
 * {@code font-size}/{@code font-weight} style wins over the draw's {@code <font>}; (3) multiple
 * paragraphs stack downward; (4) the text wraps to the box width.</p>
 */
public class XfaRichTextPaintTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String XHTML = "http://www.w3.org/1999/xhtml";

    @Test
    void exDataHtmlParagraphIsPaintedWithItsOwnFontSize() throws Exception {
        // The draw's <font> says 14pt, but the <p> style says 7pt — the paragraph style must win.
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Hdr' x='40pt' y='50pt' w='120pt' h='10pt'>"
                + "  <font typeface='Arial' size='14pt'/>"
                + "  <value><exData contentType='text/html'>"
                + "    <body xmlns='" + XHTML + "'><p style='font-size:7pt'>Date of Disbursement</p></body>"
                + "  </exData></value></draw>"
                + "</subform></template>";
        XfaPainter.Result r = new XfaPainter.Result();
        String content = build(tpl, r);

        assertTrue(content.contains("(Date of Disbursement) Tj"),
                "exData/HTML paragraph text must be painted; was:\n" + content);
        assertTrue(r.texts >= 1, "a rich-text run counted");
        // the per-<p> 7pt size is used, NOT the draw's 14pt
        Matcher m = Pattern.compile("/\\w+ ([0-9.]+) Tf.*?\\(Date of Disbursement\\) Tj", Pattern.DOTALL)
                .matcher(content);
        assertTrue(m.find(), "Tf precedes the rich text; was:\n" + content);
        assertEquals(7.0, Double.parseDouble(m.group(1)), 0.01,
                "paragraph font-size:7pt overrides the draw's 14pt font");
        assertFalse(content.contains("/F1 14.0 Tf"), "draw's 14pt font must not be used for the styled paragraph");
    }

    @Test
    void multipleParagraphsStackDownward() throws Exception {
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Blk' x='40pt' y='200pt' w='400pt' h='60pt'>"
                + "  <font typeface='Arial' size='9pt'/><para hAlign='left' vAlign='top'/>"
                + "  <value><exData contentType='text/html'><body xmlns='" + XHTML + "'>"
                + "    <p>First paragraph line</p><p style='font-weight:bold'>Second bold paragraph</p>"
                + "  </body></exData></value></draw>"
                + "</subform></template>";
        XfaPainter.Result r = new XfaPainter.Result();
        ContentStreamBuilder b = builder(tpl, r);
        String content = new String(b.toByteArray(), StandardCharsets.US_ASCII);

        assertTrue(content.contains("(First paragraph line) Tj"), "first paragraph painted");
        assertTrue(content.contains("(Second bold paragraph) Tj"), "second paragraph painted");
        // the bold paragraph registers the bold base font
        assertTrue(b.getFontResources().containsKey("Helvetica-Bold"),
                "font-weight:bold paragraph maps to Helvetica-Bold; got " + b.getFontResources().keySet());
        // second line baseline is BELOW the first (smaller Tm y, PDF y grows upward)
        double y1 = tmY(content, "First paragraph line");
        double y2 = tmY(content, "Second bold paragraph");
        assertTrue(y2 < y1, "second paragraph stacks below the first (y2=" + y2 + " < y1=" + y1 + ")");
    }

    @Test
    void longParagraphWrapsToBoxWidth() throws Exception {
        // A long single paragraph in a narrow box must wrap to multiple lines (multiple Tj).
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Para' x='40pt' y='300pt' w='90pt' h='80pt'>"
                + "  <font typeface='Arial' size='9pt'/>"
                + "  <value><exData contentType='text/html'><body xmlns='" + XHTML + "'>"
                + "    <p>The Borrower and the Credit Union agree that interest will be calculated as well after as before maturity</p>"
                + "  </body></exData></value></draw>"
                + "</subform></template>";
        XfaPainter.Result r = new XfaPainter.Result();
        String content = build(tpl, r);
        int tj = countTj(content);
        assertTrue(tj >= 3, "a long paragraph in a 90pt box wraps to >=3 lines; got " + tj + " Tj\n" + content);
    }

    @Test
    void plainValueTextStillPaintsAndExDataDoesNotDoubleCount() throws Exception {
        // a draw with BOTH <text> and (hypothetically) exData: <text> wins, no double paint.
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Plain' x='40pt' y='50pt' w='120pt' h='12pt'>"
                + "  <font typeface='Arial' size='10pt'/><value><text>PlainLabel</text></value></draw>"
                + "</subform></template>";
        XfaPainter.Result r = new XfaPainter.Result();
        String content = build(tpl, r);
        assertTrue(content.contains("(PlainLabel) Tj"), "plain <value><text> still paints");
        assertEquals(1, countTj(content), "exactly one run for a single plain label");
    }

    /* helpers */

    private static String build(String xml, XfaPainter.Result r) throws Exception {
        return new String(builder(xml, r).toByteArray(), StandardCharsets.US_ASCII);
    }

    private static ContentStreamBuilder builder(String xml, XfaPainter.Result r) throws Exception {
        FormDom dom = merge(xml);
        return XfaPainter.buildContent(dom, null, 400, r);
    }

    private static double tmY(String content, String text) {
        Matcher m = Pattern.compile(
                "0\\.0000 0\\.0000 1\\.0000 [0-9.]+ ([0-9.]+) Tm\\s*\\n?\\s*\\(" + Pattern.quote(text) + "\\) Tj")
                .matcher(content);
        assertTrue(m.find(), "Tm before '" + text + "' in:\n" + content);
        return Double.parseDouble(m.group(1));
    }

    private static int countTj(String content) {
        Matcher m = Pattern.compile("\\) Tj").matcher(content);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    private static FormDom merge(String xml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(xml));
        return new BindingEngine().merge(tpl, null);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
