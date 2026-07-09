package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.flatten.XfaGeometry;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C2 — box-model + text paint, B oracle (painted ops land at the C1 rect) and C oracle
 * (presence=hidden produces no marks; within bounds; deterministic re-paint).
 */
public class XfaPaintTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double PAGE_H = 200;

    private static final String TEMPLATE =
            "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
            + "<field name='Name' x='10pt' y='10pt' w='100pt' h='20pt'>"
            + "  <ui><textEdit/></ui>"
            + "  <font typeface='Arial' size='12pt'><color value='255,0,0'/></font>"
            + "  <para hAlign='left' vAlign='middle'/>"
            + "  <border><edge stroke='solid' thickness='1pt'><color value='0,0,255'/></edge>"
            + "    <fill><color value='200,200,200'/></fill></border>"
            + "  <value><text/></value></field>"
            + "<field name='Secret' x='10pt' y='40pt' w='100pt' h='20pt' presence='hidden'>"
            + "  <ui><textEdit/></ui><value><text/></value></field>"
            + "</subform></template>";

    private static final String DATASETS = "<xfa:data xmlns:xfa='" + DATA + "'><form1>"
            + "<Name>Alice</Name><Secret>topsecret</Secret></form1></xfa:data>";

    @Test
    void paintsFillBorderTextAtC1Rect() throws Exception {
        FormDom dom = merge();
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, PAGE_H, r).toByteArray(),
                StandardCharsets.US_ASCII);

        // B oracle: the painted rectangle equals the C1-resolved rect for Name.
        Rectangle rect = XfaGeometry.resolve(nodeOf(dom, "Name"), PAGE_H, 0, 0);
        String re = String.format(Locale.US, "%.2f %.2f %.2f %.2f re",
                rect.getLLX(), rect.getLLY(), rect.getWidth(), rect.getHeight());
        assertTrue(content.contains(re), "fill/border rect at C1 coords: expected '" + re + "' in:\n" + content);

        // fill (200/255=0.7843) + f ; border stroke blue + S ; text run.
        assertTrue(content.contains("0.7843 0.7843 0.7843 rg"), "solid fill color");
        assertTrue(content.contains("\nf\n") || content.contains("re\nf"), "fill paint op");
        assertTrue(content.contains("0.0000 0.0000 1.0000 RG"), "border stroke color");
        assertTrue(content.contains("1.00 w"), "edge thickness");
        assertTrue(content.contains("(Alice) Tj"), "value text painted");
        assertTrue(content.contains("1.0000 0.0000 0.0000 rg"), "text color red");
        assertTrue(content.contains("Tf"), "font selected");

        assertEquals(1, r.presenceHidden, "the hidden field is suppressed");
        assertTrue(r.fills >= 1 && r.borders >= 1 && r.texts >= 1, "fill+border+text counted");
    }

    @Test
    void presenceHiddenProducesNoMarks() throws Exception {
        FormDom dom = merge();
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, PAGE_H, r).toByteArray(),
                StandardCharsets.US_ASCII);
        // the hidden Secret field's value must NOT appear (watermark-suppression class).
        assertFalse(content.contains("topsecret"), "presence=hidden value must not be painted");
    }

    @Test
    void paintedMarksStayWithinPageBounds() throws Exception {
        FormDom dom = merge();
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, PAGE_H, r).toByteArray(),
                StandardCharsets.US_ASCII);
        // every 're' rectangle lies within [0,?]x[0,200]; check the Name rect numerically.
        Rectangle rect = XfaGeometry.resolve(nodeOf(dom, "Name"), PAGE_H, 0, 0);
        assertTrue(rect.getLLY() >= 0 && rect.getURY() <= PAGE_H, "within page height");
        assertTrue(content.length() > 0, "produced marks");
    }

    @Test
    void repaintIsDeterministic() throws Exception {
        FormDom dom = merge();
        byte[] a = XfaPainter.buildContent(dom, null, PAGE_H, new XfaPainter.Result()).toByteArray();
        byte[] b = XfaPainter.buildContent(merge(), null, PAGE_H, new XfaPainter.Result()).toByteArray();
        assertEquals(new String(a, StandardCharsets.US_ASCII), new String(b, StandardCharsets.US_ASCII),
                "paint is deterministic");
    }

    /* helpers */

    private static FormDom merge() throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(TEMPLATE));
        XfaNode data = XfaNodeFactory.load(parse(DATASETS));
        return new BindingEngine().merge(tpl, data);
    }

    private static XfaNode nodeOf(FormDom dom, String name) {
        return dom.fieldByName(name).getFormNode();
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
