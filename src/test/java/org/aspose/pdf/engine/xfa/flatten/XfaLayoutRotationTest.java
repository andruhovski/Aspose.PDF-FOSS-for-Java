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

/// C1.2 — container rotation. The `rotate` attribute (XFA degrees,
/// counterclockwise) rotates a container/field frame about its anchor point; nested
/// rotation composes. Expected rectangles are hand-computed from the rotation of the
/// box corners about the container anchor, then Y-flipped — the B oracle.
public class XfaLayoutRotationTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final double EPS = 1e-6;

    @Test
    void rotate180AboutContainerAnchor() throws Exception {
        // subform anchor (100,100), rotate 180; field +(10,5), 4x2; page 200.
        // unrotated field box would be (110,105)-(114,107); 180° about (100,100) maps it
        // to (86,93)-(90,95) in top-left space. Y-flip(200): llx=86,urx=90,ury=107,lly=105.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' x='100pt' y='100pt' rotate='180'>"
                + "  <field name='f' x='10pt' y='5pt' w='4pt' h='2pt'/>"
                + "</subform></template>").getDocumentElement();
        assertRect("rot180", XfaGeometry.resolve(wrapField(root), 200), 86, 105, 90, 107);
    }

    @Test
    void rotate90SwapsExtentAndPlacesByFormula() throws Exception {
        // subform anchor (100,100), rotate 90 CCW; field +(20,0), w=10 h=4; page 200.
        // field top-left (120,100) -> rel (20,0) -> 90CCW (y-down) (0,-20) -> (100,80).
        // far corner (130,104) -> rel (30,4) -> (4,-30) -> (104,70). box (100,70)-(104,80).
        // Y-flip(200): llx=100,urx=104,ury=130,lly=120. Note width=4 (=orig h), height=10.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' x='100pt' y='100pt' rotate='90'>"
                + "  <field name='f' x='20pt' y='0pt' w='10pt' h='4pt'/>"
                + "</subform></template>").getDocumentElement();
        assertRect("rot90", XfaGeometry.resolve(wrapField(root), 200), 100, 120, 104, 130);
    }

    @Test
    void nestedNinetyPlusNinetyComposesTo180() throws Exception {
        // outer (100,100) rotate 90, inner (0,0) rotate 90 -> net 180 about (100,100).
        // field +(10,5), 4x2 -> identical to the single rotate-180 case.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='outer' x='100pt' y='100pt' rotate='90'>"
                + "  <subform name='inner' x='0pt' y='0pt' rotate='90'>"
                + "    <field name='f' x='10pt' y='5pt' w='4pt' h='2pt'/>"
                + "  </subform></subform></template>").getDocumentElement();
        assertRect("nested 90+90", XfaGeometry.resolve(wrapField(root), 200), 86, 105, 90, 107);
    }

    @Test
    void rotateZeroIsIdentity() throws Exception {
        // explicit rotate='0' must equal no rotation: field (10,20) w100 h30 page200.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' x='5pt' y='5pt' rotate='0'>"
                + "  <field name='f' x='10pt' y='20pt' w='100pt' h='30pt'/>"
                + "</subform></template>").getDocumentElement();
        assertRect("rot0", XfaGeometry.resolve(wrapField(root), 200), 15, 145, 115, 175);
    }

    @Test
    void leafOwnRotationRotatesItsBox() throws Exception {
        // rotation on the leaf itself (no container offset): field anchor (50,50), rotate 90,
        // w=10 h=4. corners rel to anchor: (0,0),(10,0),(0,4),(10,4) -> 90CCW (y-down) (x,y)->(y,-x):
        // (0,0),(0,-10),(4,0),(4,-10) -> abs +(50,50): (50,50),(50,40),(54,50),(54,40).
        // box (50,40)-(54,50). Y-flip(200): llx=50,urx=54,ury=160,lly=150.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<field name='f' x='50pt' y='50pt' w='10pt' h='4pt' rotate='90'/>"
                + "</template>").getDocumentElement();
        assertRect("leaf rot90", XfaGeometry.resolve(wrapField(root), 200), 50, 150, 54, 160);
    }

    /* helpers */

    private static XfaNode wrapField(Element root) {
        Element f = (Element) root.getElementsByTagNameNS(TPL, "field").item(0);
        return XfaNodeFactory.wrap(f, null);
    }

    private static void assertRect(String msg, Rectangle r, double llx, double lly, double urx, double ury) {
        assertEquals(llx, r.getLLX(), EPS, msg + " llx");
        assertEquals(lly, r.getLLY(), EPS, msg + " lly");
        assertEquals(urx, r.getURX(), EPS, msg + " urx");
        assertEquals(ury, r.getURY(), EPS, msg + " ury");
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
