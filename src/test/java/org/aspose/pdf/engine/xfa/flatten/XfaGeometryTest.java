package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/// A5.1: XFA top-left layout coords -> absolute PDF bottom-left Rectangle.
public class XfaGeometryTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final double EPS = 1e-6;

    @Test
    void simpleFieldTopLeftYFlip() throws Exception {
        XfaNode field = field("<field name='f' x='10pt' y='20pt' w='100pt' h='30pt'/>");
        Rectangle r = XfaGeometry.resolve(field, 200);
        assertRect(r, 10, 150, 110, 180); // ury=200-20, lly=200-(20+30)
    }

    @Test
    void nestedSubformAccumulatesOffsets() throws Exception {
        // field is inside a subform offset by (5,5); abs top-left = (15,25)
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' x='5pt' y='5pt'>"
                + "<field name='f' x='10pt' y='20pt' w='100pt' h='30pt'/>"
                + "</subform></template>").getDocumentElement();
        Element f = (Element) root.getElementsByTagNameNS(TPL, "field").item(0);
        Rectangle r = XfaGeometry.resolve(XfaNodeFactory.wrap(f, null), 200);
        assertRect(r, 15, 145, 115, 175);
    }

    @Test
    void anchorBottomLeftShiftsUp() throws Exception {
        // y is the bottom-left corner (from top); top-left y = 50 - 30 = 20
        XfaNode field = field("<field name='f' x='10pt' y='50pt' w='100pt' h='30pt' anchorType='bottomLeft'/>");
        Rectangle r = XfaGeometry.resolve(field, 200);
        assertRect(r, 10, 150, 110, 180);
    }

    @Test
    void anchorTopRightShiftsLeft() throws Exception {
        // x is the top-right corner; top-left x = 110 - 100 = 10
        XfaNode field = field("<field name='f' x='110pt' y='20pt' w='100pt' h='30pt' anchorType='topRight'/>");
        Rectangle r = XfaGeometry.resolve(field, 200);
        assertRect(r, 10, 150, 110, 180);
    }

    @Test
    void millimetreUnitsConvertToPoints() throws Exception {
        // 25.4mm == 72pt
        XfaNode field = field("<field name='f' x='25.4mm' y='0mm' w='25.4mm' h='25.4mm'/>");
        Rectangle r = XfaGeometry.resolve(field, 144);
        assertRect(r, 72, 72, 144, 144); // llx=72, urx=72+72=144, ury=144-0=144, lly=144-(0+72)=72
    }

    @Test
    void exclGroupOptionAccumulatesGroupOffset() throws Exception {
        // an option field inside an exclGroup at (10,10): abs top-left = (10+5, 10+5) = (15,15)
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<exclGroup name='G' x='10pt' y='10pt'>"
                + "<field name='opt' x='5pt' y='5pt' w='20pt' h='20pt'/>"
                + "</exclGroup></template>").getDocumentElement();
        Element opt = (Element) root.getElementsByTagNameNS(TPL, "field").item(0);
        Rectangle r = XfaGeometry.resolve(XfaNodeFactory.wrap(opt, null), 200);
        assertRect(r, 15, 165, 35, 185); // ury=200-15, lly=200-(15+20)
    }

    @Test
    void flowedLayoutWithoutXyIsUnresolved() throws Exception {
        XfaNode field = field("<field name='f' w='100pt' h='30pt'/>"); // no x/y
        assertNull(XfaGeometry.resolve(field, 200), "flowed layout -> null (Stage-C gap, not faked)");
    }

    /* helpers */

    private static XfaNode field(String fieldXml) throws Exception {
        Element root = parse("<template xmlns='" + TPL + "'>" + fieldXml + "</template>").getDocumentElement();
        Element f = (Element) root.getElementsByTagNameNS(TPL, "field").item(0);
        return XfaNodeFactory.wrap(f, null);
    }

    private static void assertRect(Rectangle r, double llx, double lly, double urx, double ury) {
        assertEquals(llx, r.getLLX(), EPS, "llx");
        assertEquals(lly, r.getLLY(), EPS, "lly");
        assertEquals(urx, r.getURX(), EPS, "urx");
        assertEquals(ury, r.getURY(), EPS, "ury");
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
