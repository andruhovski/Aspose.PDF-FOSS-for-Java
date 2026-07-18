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

/// C1 (positioned-layout geometry) — the B oracle: absolute coordinates asserted
/// against the spec's own accumulation formula `Px = Σ(Cx + Mx + Ox)`, by
/// hand, with no reference image. Covers all nine `anchorType` values, margin
/// insets, multi-level positioned nesting, the contentArea base origin, the Y-flip,
/// and the positioned-vs-flowed gating. (C1.2 adds rotation in a sibling test.)
public class XfaLayoutGeometryTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final double EPS = 1e-6;

    /* ----------------------------- anchorType (all 9) ----------------------------- */

    @Test
    void allNineAnchorTypesResolveToTheSameBoxTopLeft() throws Exception {
        // x=100,y=100 is the anchor point; w=40,h=20; page 200.
        // Each anchorType shifts the box so its named point sits at (100,100).
        // box top-left (in top-left space) per anchor, then PDF rect via Y-flip:
        assertAnchor("topLeft",      100,  80, 140, 100);
        assertAnchor("topCenter",     80,  80, 120, 100);
        assertAnchor("topRight",      60,  80, 100, 100);
        assertAnchor("middleLeft",   100,  90, 140, 110);
        assertAnchor("middleCenter",  80,  90, 120, 110);
        assertAnchor("middleRight",   60,  90, 100, 110);
        assertAnchor("bottomLeft",   100, 100, 140, 120);
        assertAnchor("bottomCenter",  80, 100, 120, 120);
        assertAnchor("bottomRight",   60, 100, 100, 120);
    }

    /// Asserts the PDF rect (llx,lly,urx,ury) for a w=40,h=20 box anchored at (100,100), page 200.
    private void assertAnchor(String anchor, double llx, double lly, double urx, double ury) throws Exception {
        XfaNode field = field("<field name='f' x='100pt' y='100pt' w='40pt' h='20pt' anchorType='" + anchor + "'/>");
        Rectangle r = XfaGeometry.resolve(field, 200);
        assertRect(anchor, r, llx, lly, urx, ury);
    }

    /* --------------------------------- margins ------------------------------------ */

    @Test
    void containerMarginInsetShiftsChildren() throws Exception {
        // subform at (20,20) with content insets left=8 top=6; field at (10,10) inside.
        // abs top-left = (20+8+10, 20+6+10) = (38,36). w=50,h=12, page 200.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' x='20pt' y='20pt'>"
                + "  <margin leftInset='8pt' topInset='6pt'/>"
                + "  <field name='f' x='10pt' y='10pt' w='50pt' h='12pt'/>"
                + "</subform></template>").getDocumentElement();
        Rectangle r = XfaGeometry.resolve(wrapField(root), 200);
        assertRect("margin", r, 38, 200 - 48, 88, 200 - 36); // lly=200-(36+12), ury=200-36
    }

    /* ------------------------------ nested positioning ----------------------------- */

    @Test
    void twoLevelNestingWithMarginsAccumulates() throws Exception {
        // outer (10,10) margin(5,5); inner (20,20) margin(3,3); field (1,1) w=10 h=10; page 100.
        // abs top-left = 10+5+20+3+1 = 39 on each axis.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='outer' x='10pt' y='10pt'>"
                + "  <margin leftInset='5pt' topInset='5pt'/>"
                + "  <subform name='inner' x='20pt' y='20pt'>"
                + "    <margin leftInset='3pt' topInset='3pt'/>"
                + "    <field name='f' x='1pt' y='1pt' w='10pt' h='10pt'/>"
                + "  </subform></subform></template>").getDocumentElement();
        Rectangle r = XfaGeometry.resolve(wrapField(root), 100);
        assertRect("nested", r, 39, 100 - 49, 49, 100 - 39);
    }

    @Test
    void positionedContainerWithoutXySitsAtContentOrigin() throws Exception {
        // subform has NO x/y (sits at parent content origin = 0,0) but a margin inset.
        // abs top-left = (0 + 4 + 5, 0 + 4 + 5) = (9,9). Distinguishes container (0 offset)
        // from a leaf (which would be unresolved without x/y).
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s'>"
                + "  <margin leftInset='4pt' topInset='4pt'/>"
                + "  <field name='f' x='5pt' y='5pt' w='10pt' h='10pt'/>"
                + "</subform></template>").getDocumentElement();
        Rectangle r = XfaGeometry.resolve(wrapField(root), 100);
        assertRect("no-xy container", r, 9, 100 - 19, 19, 100 - 9);
    }

    /* ----------------------------- contentArea base ------------------------------- */

    @Test
    void contentAreaBaseOriginOffsetsTheWholeChain() throws Exception {
        // base (36,48) = the contentArea origin in the page; field directly at (10,10).
        // abs top-left = (46,58). w=20,h=10, page 200.
        XfaNode field = field("<field name='f' x='10pt' y='10pt' w='20pt' h='10pt'/>");
        Rectangle r = XfaGeometry.resolve(field, 200, 36, 48);
        assertRect("base", r, 46, 200 - 68, 66, 200 - 58); // lly=200-(58+10), ury=200-58
    }

    /* --------------------------- positioned vs flowed ----------------------------- */

    @Test
    void flowedAncestorMakesLeafUnresolved() throws Exception {
        for (String layout : new String[]{"tb", "lr-tb", "rl-tb", "row", "table"}) {
            Element root = parse("<template xmlns='" + TPL + "'>"
                    + "<subform name='s' layout='" + layout + "'>"
                    + "  <field name='f' x='10pt' y='10pt' w='20pt' h='10pt'/>"
                    + "</subform></template>").getDocumentElement();
            assertNull(XfaGeometry.resolve(wrapField(root), 200),
                    "layout=" + layout + " is flowed -> leaf unresolved (C3 placeholder)");
        }
    }

    @Test
    void positionedAncestorIsResolvedNotGated() throws Exception {
        // explicit layout='position' must NOT gate.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='s' layout='position' x='5pt' y='5pt'>"
                + "  <field name='f' x='10pt' y='20pt' w='100pt' h='30pt'/>"
                + "</subform></template>").getDocumentElement();
        Rectangle r = XfaGeometry.resolve(wrapField(root), 200);
        assertRect("position", r, 15, 145, 115, 175);
    }

    @Test
    void deepPositionedInsideFlowedIsStillGated() throws Exception {
        // a positioned inner subform nested inside a flowed outer subform: still flowed region.
        Element root = parse("<template xmlns='" + TPL + "'>"
                + "<subform name='outer' layout='tb'>"
                + "  <subform name='inner' layout='position' x='5pt' y='5pt'>"
                + "    <field name='f' x='10pt' y='10pt' w='20pt' h='10pt'/>"
                + "  </subform></subform></template>").getDocumentElement();
        assertNull(XfaGeometry.resolve(wrapField(root), 200),
                "any flowed ancestor on the path -> unresolved");
    }

    /* --------------------------------- helpers ------------------------------------ */

    private static XfaNode field(String fieldXml) throws Exception {
        Element root = parse("<template xmlns='" + TPL + "'>" + fieldXml + "</template>").getDocumentElement();
        return wrapField(root);
    }

    /// Wraps the first `<field>` descendant of a parsed template root.
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
