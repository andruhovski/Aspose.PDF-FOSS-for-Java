package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Document;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.Form;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// C1.3 — the resolver wired into the flattener: positioned fields get REAL coordinates
/// (offset by the template contentArea origin), flowed fields keep a flagged placeholder,
/// and the self-consistency invariants hold (on-page, non-zero, positioned vs flowed split).
public class XfaPositionedFlattenTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double EPS = 1e-3;

    @Test
    void positionedFieldsUseContentAreaOriginAndNoPlaceholders() throws Exception {
        // contentArea origin (36,48); field form-pos (10,10) -> abs top-left (46,58).
        Template tpl = template(
                "<subform name='form1' x='0pt' y='0pt'>"
                + "<field name='Name' x='10pt' y='10pt' w='100pt' h='20pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "</subform>"
                + "<pageSet><pageArea name='P1'><contentArea x='36pt' y='48pt' w='500pt' h='700pt'/></pageArea></pageSet>");
        XfaNode data = load("<xfa:data xmlns:xfa='" + DATA + "'><form1><Name>Alice</Name></form1></xfa:data>");

        Document doc = new Document();
        XfaFlattener.Result r = XfaFlattener.flatten(doc, new BindingEngine().merge(tpl, data),
                tpl, XfaFlattener.XfaPolicy.DROP, null);

        assertEquals(1, r.contentAreaCount, "single static contentArea");
        assertEquals(0, r.geometryFallback, "fully positioned -> no placeholder");
        assertTrue(r.geometryResolved >= 1, "positioned field resolved");

        Rectangle rect = doc.getForm().get("form1.Name").getRect();
        double pageH = doc.getPages().get(1).getRect().getHeight();
        // base applied: llx is 46 (= 36 + 10), NOT 10. urx = 146. Y-flip against page height.
        assertEquals(46, rect.getLLX(), EPS, "contentArea X origin applied");
        assertEquals(146, rect.getURX(), EPS);
        assertEquals(pageH - 58, rect.getURY(), EPS, "top edge");
        assertEquals(pageH - 78, rect.getLLY(), EPS, "bottom edge");

        assertOnPageNonZero(doc);
    }

    @Test
    void mixedPositionedAndFlowedSplitsRealVsPlaceholder() throws Exception {
        // one positioned field + a flowed (layout=tb) subform whose field has no x/y.
        Template tpl = template(
                "<subform name='form1'>"
                + "<field name='Pos' x='10pt' y='10pt' w='100pt' h='20pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "<subform name='flow' layout='tb'>"
                + "  <field name='Flowed' w='100pt' h='20pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "</subform></subform>");
        XfaNode data = load("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Pos>P</Pos><flow><Flowed>F</Flowed></flow></form1></xfa:data>");

        Document doc = new Document();
        XfaFlattener.Result r = XfaFlattener.flatten(doc, new BindingEngine().merge(tpl, data),
                tpl, XfaFlattener.XfaPolicy.DROP, null);

        assertTrue(r.geometryResolved >= 1, "positioned field has real coords; got " + r.geometryResolved);
        assertTrue(r.geometryFallback >= 1, "flowed field stays a placeholder (C3); got " + r.geometryFallback);
        assertOnPageNonZero(doc);
    }

    /// Self-consistency invariant C: every created widget is on-page and has non-zero area.
    private static void assertOnPageNonZero(Document doc) throws Exception {
        Form form = doc.getForm();
        double pageW = doc.getPages().get(1).getRect().getWidth();
        double pageH = doc.getPages().get(1).getRect().getHeight();
        for (Field f : form) {
            Rectangle r = f.getRect();
            if (r == null) {
                continue;
            }
            assertTrue(r.getWidth() > 0 && r.getHeight() > 0, f.getPartialName() + " non-zero size");
            assertTrue(r.getLLX() >= -EPS && r.getURX() <= pageW + EPS, f.getPartialName() + " within page width");
            assertTrue(r.getLLY() >= -EPS && r.getURY() <= pageH + EPS, f.getPartialName() + " within page height");
        }
    }

    /* helpers */

    private static Template template(String body) throws Exception {
        return (Template) XfaNodeFactory.load(parse("<template xmlns='" + TPL + "'>" + body + "</template>"));
    }

    private static XfaNode load(String xml) throws Exception {
        return XfaNodeFactory.load(parse(xml));
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
