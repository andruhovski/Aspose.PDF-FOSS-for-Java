package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// L1.1 + L1.3 oracles for the flowed-root Layout DOM.
///
/// **B (formula):** placed Y values equal the cumulative sum of preceding sibling
/// heights; a positioned child offsets from its flowed parent's placed origin; a growable
/// container's height equals the sum of its children + insets.
///
/// **C (self-consistency):** flowed siblings are monotonic top-to-bottom with no
/// overlap; container height is conserved (= Σ children + spacing); the single-region fill
/// reports overflow = contentHeight − regionHeight without splitting.
public class XfaFlowLayoutTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double EPS = 1e-6;
    private static final double LH = TextLayoutHelper.getLineHeight("Helvetica", 10);

    /// A 400×500 content region wrapper to make overflow assertions concrete.
    private static String wrap(String rootChildren, String rootAttrs) {
        return "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' " + rootAttrs + ">" + rootChildren + "</subform>"
                + "<pageSet><pageArea><contentArea w='400pt' h='500pt'/></pageArea></pageSet>"
                + "</template>";
    }

    /* --------------------------------- B oracle --------------------------------- */

    @Test
    void threeFixedSubformsStackAtCumulativeY() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='a' h='30pt'/>"
                        + "<subform name='b' h='40pt'/>"
                        + "<subform name='c' h='50pt'/>",
                "layout='tb'"), null);

        List<XfaLayoutNode> kids = r.root.getChildren();
        assertEquals(3, kids.size(), "three flow children");
        assertEquals(0, kids.get(0).getY(), EPS, "first at Y=0");
        assertEquals(30, kids.get(1).getY(), EPS, "second at Y=30 (after a)");
        assertEquals(70, kids.get(2).getY(), EPS, "third at Y=70 (after a+b)");
        assertEquals(120, r.contentHeight, EPS, "root height = 30+40+50");
        assertEquals(400, r.regionWidth, EPS);
        assertEquals(500, r.regionHeight, EPS);
    }

    @Test
    void positionedChildOffsetsFromFlowedParentY() throws Exception {
        // root flow: subform 'a' (h=40) then subform 's2' (flowed, placed at Y=40) holding a
        // positioned field at (x=10,y=15). The field's absolute Y = parent flow Y (40) + 15 = 55.
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='a' h='40pt'/>"
                        + "<subform name='s2' layout='tb'>"
                        + "  <field name='p' x='10pt' y='15pt' w='100pt' h='20pt'>"
                        + "    <ui><textEdit/></ui><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'"), null);

        XfaLayoutNode s2 = r.root.getChildren().get(1);
        assertEquals(40, s2.getY(), EPS, "s2 flowed to Y=40");
        XfaLayoutNode p = s2.getChildren().get(0);
        assertTrue(p.isPositioned(), "the field is positioned (has x/y)");
        assertEquals(10, p.getX(), EPS, "X offset = contentLeft(0) + x(10)");
        assertEquals(55, p.getY(), EPS, "Y = parent flow Y(40) + child y(15)");
        assertEquals(20, p.getHeight(), EPS, "fixed h preserved");
    }

    @Test
    void growableSubformHeightEqualsSumOfChildrenPlusInsets() throws Exception {
        // subform 'box' (flowed, no h, margin top=5 bottom=7) holding two fixed fields 20 + 30.
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='box' layout='tb'>"
                        + "  <margin topInset='5pt' bottomInset='7pt'/>"
                        + "  <field name='f1' w='100pt' h='20pt'><value><text/></value></field>"
                        + "  <field name='f2' w='100pt' h='30pt'><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'"), null);

        XfaLayoutNode box = r.root.getChildren().get(0);
        assertTrue(box.isGrowable(), "no fixed h ⇒ growable container");
        // children stack inside the content origin (topInset=5): f1 at Y=5, f2 at Y=25.
        assertEquals(5, box.getChildren().get(0).getY(), EPS, "f1 at topInset");
        assertEquals(25, box.getChildren().get(1).getY(), EPS, "f2 after f1");
        assertEquals(5 + 20 + 30 + 7, box.getHeight(), EPS, "top + Σchildren + bottom inset");
    }

    @Test
    void dataDrivenFieldHeightReflectsBoundLineCount() throws Exception {
        // The bound multi-line value makes the growable field taller than a single-line one.
        String tplXml = wrap(
                "<field name='Memo' w='300pt'><ui><textEdit/></ui>"
                        + "<font size='10pt'/><value><text/></value></field>",
                "layout='tb'");
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1><Memo>L1\nL2\nL3</Memo></form1></xfa:data>";

        XfaFlowLayout.Result r = layout(tplXml, dataXml);
        XfaLayoutNode memo = r.root.getChildren().get(0);
        assertEquals(3 * LH, memo.getHeight(), EPS, "3 bound lines ⇒ 3 × lineHeight");
        assertTrue(memo.isGrowable(), "data-driven height");
        assertEquals(3 * LH, r.contentHeight, EPS, "root grows to its one child");
    }

    /* --------------------------------- C oracle --------------------------------- */

    @Test
    void flowedSiblingsAreMonotonicAndNonOverlapping() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='a' h='30pt'/>"
                        + "<field name='f' w='100pt' h='25pt'><value><text/></value></field>"
                        + "<subform name='c' h='15pt'/>",
                "layout='tb'"), null);

        List<XfaLayoutNode> kids = r.root.getChildren();
        double prevBottom = 0;
        for (XfaLayoutNode k : kids) {
            assertTrue(k.getY() + EPS >= prevBottom,
                    "child Y (" + k.getY() + ") not above previous bottom (" + prevBottom + ")");
            prevBottom = k.getBottom();
        }
        // height conservation: root bottom = last child bottom (no insets here).
        assertEquals(prevBottom, r.root.getBottom(), EPS, "container height conserved");
        assertEquals(70, r.contentHeight, EPS, "30+25+15");
    }

    @Test
    void overflowIsMeasuredNotSplit() throws Exception {
        // 12 fixed 60pt subforms = 720pt of content in a 500pt region ⇒ overflow 220pt, ONE root.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append("<subform name='s").append(i).append("' h='60pt'/>");
        }
        XfaFlowLayout.Result r = layout(wrap(sb.toString(), "layout='tb'"), null);

        assertEquals(720, r.contentHeight, EPS, "12 × 60");
        assertEquals(720 - 500, r.overflow, EPS, "overflow = content − region");
        assertTrue(r.overflows(), "content exceeds the single region (L3 must paginate)");
        assertEquals(12, r.root.getChildren().size(), "no splitting — all 12 placed in one Layout DOM");
    }

    @Test
    void fittingContentReportsNoOverflow() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='a' h='100pt'/><subform name='b' h='100pt'/>",
                "layout='tb'"), null);
        assertEquals(200, r.contentHeight, EPS);
        assertFalse(r.overflows(), "200 < 500 region ⇒ fits");
        assertTrue(r.overflow < 0, "negative overflow = headroom");
    }

    @Test
    void regionComesFromContentArea() throws Exception {
        XfaFlowLayout.Result r = layout(wrap("<subform name='a' h='10pt'/>", "layout='tb'"), null);
        assertEquals(400, r.regionWidth, EPS, "contentArea w");
        assertEquals(500, r.regionHeight, EPS, "contentArea h");
    }

    /* helpers */

    private static XfaFlowLayout.Result layout(String templateXml, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(templateXml));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        FormDom dom = new BindingEngine().merge(tpl, data);
        return XfaFlowLayout.layout(dom, tpl);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
