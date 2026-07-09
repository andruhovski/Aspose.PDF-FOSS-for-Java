package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Document;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FIX.3 — contentArea origin (P3): a {@code pageSet} nested inside the root subform (the
 * 408975 shape) is now found, and the contentArea's {@code x}/{@code y} contributes to the
 * positioned coordinate base.
 */
public class XfaContentAreaOriginTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double EPS = 1e-3;

    @Test
    void contentAreaNestedInSubformIsFoundAndOriginApplied() throws Exception {
        // pageSet is a child of the root subform; contentArea origin (36,48).
        Template tpl = (Template) XfaNodeFactory.load(parse("<template xmlns='" + TPL + "'>"
                + "<subform name='form1' x='0pt' y='0pt'>"
                + "<field name='Name' x='10pt' y='10pt' w='100pt' h='20pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "<pageSet><pageArea><contentArea x='36pt' y='48pt' w='500pt' h='700pt'/></pageArea></pageSet>"
                + "</subform></template>"));
        XfaNode data = XfaNodeFactory.load(parse("<xfa:data xmlns:xfa='" + DATA + "'><form1><Name>Alice</Name></form1></xfa:data>"));

        Document doc = new Document();
        XfaFlattener.Result r = XfaFlattener.flatten(doc, new BindingEngine().merge(tpl, data),
                tpl, XfaFlattener.XfaPolicy.DROP, null);

        assertEquals(1, r.contentAreaCount, "nested-in-subform contentArea is now found (P3)");
        Rectangle rect = doc.getForm().get("form1.Name").getRect();
        // origin applied: llx = 36 + 10 = 46 (was 10 before the P3 fix)
        assertEquals(46, rect.getLLX(), EPS, "contentArea X origin included in the base");
        assertEquals(146, rect.getURX(), EPS);
        assertTrue(r.geometryFallback == 0, "field still positioned");
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
