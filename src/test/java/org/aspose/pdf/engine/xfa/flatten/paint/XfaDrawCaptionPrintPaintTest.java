package org.aspose.pdf.engine.xfa.flatten.paint;

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

/// C2-PAINTFIX coverage on synthetic fixtures:
///
///   - **FIX.1** — a text `<draw>` (a label) with x/y but NO w/h auto-sizes and IS
///     painted (previously skipped because its rect had zero area → \~12/13 labels never showed).
///   - **FIX.3** — an auto-width draw places its text at the LEFT edge (its x), not shifted left
///     by the text width (the right-anchor-at-x mispositioning).
///   - **FIX.4** — `relevant="-print"` objects (buttons, on-screen hints) are NOT painted
///     in the print/flatten output; `printSkipped` counts them.
public class XfaDrawCaptionPrintPaintTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    // Label draw at x=100pt, no w/h, right-aligned para (auto-sizes to content).
    // A print-excluded button-ish draw that must NOT paint. A normal sized draw for control.
    private static final String TEMPLATE =
            "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
            + "<draw name='Label' x='100pt' y='100pt'>"
            + "  <font typeface='Arial' size='14pt' weight='bold'/>"
            + "  <para hAlign='right' vAlign='bottom'/>"
            + "  <value><text>Date:</text></value></draw>"
            + "<draw name='ScreenNote' x='10pt' y='10pt' w='150pt' h='20pt' relevant='-print'>"
            + "  <font typeface='Arial' size='11pt'/><value><text>SCREENONLY</text></value></draw>"
            + "<draw name='Body' x='10pt' y='200pt' w='200pt' h='20pt'>"
            + "  <font typeface='Arial' size='12pt'/><value><text>BodyText</text></value></draw>"
            + "</subform></template>";

    @Test
    void zeroSizeLabelPaintsLeftAnchoredAndPrintExcludedIsSkipped() throws Exception {
        FormDom dom = merge(TEMPLATE);
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, 300, r).toByteArray(),
                StandardCharsets.US_ASCII);

        // FIX.1 — the zero-w/h label draw IS painted.
        assertTrue(content.contains("(Date:) Tj"), "zero-w/h label draw must be painted");
        // FIX.4 — relevant='-print' draw is NOT painted, and is counted.
        assertFalse(content.contains("SCREENONLY"), "relevant='-print' object must not paint");
        assertEquals(1, r.printSkipped, "one print-excluded object skipped");
        // control — a normal sized draw still paints.
        assertTrue(content.contains("(BodyText) Tj"), "normal draw still paints");

        // FIX.3 — the auto-width label's text x is at its draw x (100pt), NOT 100 - textWidth.
        Matcher m = Pattern.compile(
                "1\\.0000 0\\.0000 0\\.0000 1\\.0000 ([0-9.]+) [0-9.]+ Tm\\s*\\(Date:\\) Tj").matcher(content);
        assertTrue(m.find(), "label text matrix present");
        double tx = Double.parseDouble(m.group(1));
        assertTrue(tx >= 99.0 && tx <= 103.0,
                "auto-width label left-anchored at its x (~100pt), not shifted left; was tx=" + tx);
    }

    @Test
    void printExcludedSubtreeFullySuppressed() throws Exception {
        // a +screen-only token list (no print) also excludes from print
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='f' x='0pt' y='0pt' relevant='+screen'>"
                + "<draw name='d' x='5pt' y='5pt' w='80pt' h='10pt'>"
                + "  <font typeface='Arial' size='10pt'/><value><text>HIDDENONPRINT</text></value></draw>"
                + "</subform></template>";
        FormDom dom = merge(tpl);
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, 300, r).toByteArray(),
                StandardCharsets.US_ASCII);
        assertFalse(content.contains("HIDDENONPRINT"), "relevant='+screen' (no print) excluded from print");
        assertTrue(r.printSkipped >= 1, "screen-only subtree skipped for print");
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
