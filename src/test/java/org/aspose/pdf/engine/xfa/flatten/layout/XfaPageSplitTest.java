package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// L2 PART B — overflow + split-point C oracle. The splitter FINDS page boundaries (it never
/// splits): N fixed rows in region H split after floor(H/rowH) rows; a keep-together group that
/// straddles the boundary moves wholly past it; a forced break sets the boundary regardless of
/// remaining height; the split partitions the units with no loss (content conservation).
public class XfaPageSplitTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final double EPS = 1e-6;

    /* --------------------------- split arithmetic --------------------------- */

    @Test
    void splitsAfterFloorOfRegionOverRowHeight() throws Exception {
        // 10 rows of 30pt in a 100pt region ⇒ 3 fit per page (floor(100/30)=3).
        List<XfaLayoutNode> units = rows(10, 30);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 100);

        assertTrue(plan.overflows(), "content exceeds one page");
        assertEquals(3, plan.splitPoints.get(0).boundaryIndex, "page 1 ends after floor(100/30)=3 rows");
        assertEquals(4, plan.pageCount(), "10 rows / 3-per-page ⇒ 4 pages");
        // split boundaries at 3, 6, 9
        assertEquals(3, plan.splitPoints.get(0).boundaryIndex);
        assertEquals(6, plan.splitPoints.get(1).boundaryIndex);
        assertEquals(9, plan.splitPoints.get(2).boundaryIndex);
    }

    @Test
    void contentIsConservedAcrossTheSplit() throws Exception {
        List<XfaLayoutNode> units = rows(10, 30);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 100);

        List<int[]> ranges = plan.pageRanges();
        assertEquals(plan.pageCount(), ranges.size(), "one range per page");
        int covered = 0, prevEnd = 0;
        for (int[] rg : ranges) {
            assertEquals(prevEnd, rg[0], "ranges are contiguous (no gap/overlap)");
            assertTrue(rg[1] > rg[0], "non-empty page");
            covered += rg[1] - rg[0];
            prevEnd = rg[1];
        }
        assertEquals(units.size(), covered, "every unit is on exactly one page (nothing dropped)");
        assertEquals(units.size(), prevEnd, "last page ends at the final unit");
    }

    @Test
    void fittingContentProducesNoSplit() throws Exception {
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(rows(3, 30), 500);
        assertFalse(plan.overflows(), "3×30 fits in 500");
        assertEquals(1, plan.pageCount());
        assertTrue(plan.splitPoints.isEmpty());
    }

    @Test
    void overTallSingleUnitGetsItsOwnPage() throws Exception {
        // a unit taller than the whole region cannot be left off a page (L2 does not split mid-unit).
        List<XfaLayoutNode> units = new ArrayList<>();
        units.add(unit(0, 600, plain()));    // taller than the 100pt region
        units.add(unit(600, 30, plain()));
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 100);
        assertEquals(1, plan.splitPoints.get(0).boundaryIndex, "the over-tall unit occupies page 1 alone");
        assertEquals(2, plan.pageCount());
    }

    /* ------------------------------- keep ---------------------------------- */

    @Test
    void keepTogetherGroupMovesWhollyPastBoundary() throws Exception {
        // 5 rows of 30 in a 100pt region. Without keep, the split would fall after row 3
        // (rows 0,1,2 fit). Marking rows 2+3 keep-together forces the split BEFORE row 2,
        // so the group {2,3} moves wholly to the next page.
        List<XfaLayoutNode> units = new ArrayList<>();
        units.add(unit(0, 30, plain()));
        units.add(unit(30, 30, plain()));
        units.add(unit(60, 30, keepNext()));  // row2 keeps with row3
        units.add(unit(90, 30, plain()));
        units.add(unit(120, 30, plain()));
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 100);

        assertEquals(2, plan.splitPoints.get(0).boundaryIndex, "split before the kept group (row 2)");
        List<int[]> ranges = plan.pageRanges();
        assertEquals(0, ranges.get(0)[0]);
        assertEquals(2, ranges.get(0)[1], "page 1 = rows 0,1");
        // rows 2 and 3 (the kept group) are together on page 2
        assertTrue(ranges.get(1)[0] <= 2 && ranges.get(1)[1] >= 4, "kept group {2,3} on the same later page");
    }

    @Test
    void keepPreviousAlsoGroups() throws Exception {
        // symmetric: row3 declares keep-previous, binding it to row2.
        List<XfaLayoutNode> units = new ArrayList<>();
        units.add(unit(0, 30, plain()));
        units.add(unit(30, 30, plain()));
        units.add(unit(60, 30, plain()));
        units.add(unit(90, 30, keepPrev())); // row3 keeps with row2
        units.add(unit(120, 30, plain()));
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 100);
        assertEquals(2, plan.splitPoints.get(0).boundaryIndex, "split before the kept pair (row2,row3)");
    }

    /* ------------------------------- break --------------------------------- */

    @Test
    void breakBeforeForcesSplitEvenWhenItFits() throws Exception {
        // everything fits in 500pt, but row 3 carries a forced break-before.
        List<XfaLayoutNode> units = new ArrayList<>();
        units.add(unit(0, 20, plain()));
        units.add(unit(20, 20, plain()));
        units.add(unit(40, 20, plain()));
        units.add(unit(60, 20, breakBefore()));
        units.add(unit(80, 20, plain()));
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 500);

        assertTrue(plan.overflows(), "forced break creates a page boundary");
        assertEquals(3, plan.splitPoints.get(0).boundaryIndex, "break-before row 3 sets the boundary");
        assertEquals(2, plan.pageCount());
    }

    @Test
    void breakAfterForcesSplit() throws Exception {
        List<XfaLayoutNode> units = new ArrayList<>();
        units.add(unit(0, 20, plain()));
        units.add(unit(20, 20, breakAfter())); // break after row 1
        units.add(unit(40, 20, plain()));
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.splitUnits(units, 500);
        assertEquals(2, plan.splitPoints.get(0).boundaryIndex, "boundary right after row 1");
        assertEquals(2, plan.pageCount());
    }

    /* --------------------------- integration ------------------------------- */

    @Test
    void endToEndTableOverflowFindsSplitsConserved() throws Exception {
        // a table of 20 rows (30pt each = 600pt) in a 200pt region ⇒ multi-page split.
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='c").append(i).append("' h='30pt'><value><text/></value></field></subform>");
        }
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + rows + "</subform>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='300pt' h='200pt'/></pageArea></pageSet></template>";

        XfaFlowLayout.Result layout = layout(tpl, null);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(layout);

        assertTrue(plan.overflows(), "20 rows overflow a 200pt region");
        assertEquals(20, plan.unitCount, "table rows became the breakable units");
        // content conserved: ranges partition all 20 rows.
        int covered = 0, prev = 0;
        for (int[] rg : plan.pageRanges()) {
            assertEquals(prev, rg[0]);
            covered += rg[1] - rg[0];
            prev = rg[1];
        }
        assertEquals(20, covered);
        assertEquals(20, prev);
    }

    @Test
    void positionedRootStaysSinglePage() throws Exception {
        // A positioned (non-flowed) root places children by absolute C1 coords — no flow split.
        // Even with content positioned beyond the region, the form is single-page (C1 unchanged).
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1'>" // no layout ⇒ positioned/default
                + "  <field name='a' x='10pt' y='10pt' w='100pt' h='400pt'><value><text/></value></field>"
                + "  <field name='b' x='10pt' y='420pt' w='100pt' h='400pt'><value><text/></value></field>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='300pt' h='200pt'/></pageArea></pageSet></template>";
        XfaFlowLayout.Result layout = layout(tpl, null);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(layout);
        assertEquals(1, plan.pageCount(), "positioned form is single-page (no flow pagination)");
        assertFalse(plan.overflows());
    }

    /* helpers */

    private static List<XfaLayoutNode> rows(int n, double h) throws Exception {
        List<XfaLayoutNode> units = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            units.add(unit(i * h, h, plain()));
        }
        return units;
    }

    private static XfaLayoutNode unit(double y, double h, Element src) {
        String kind = src.getLocalName() != null ? src.getLocalName() : src.getNodeName();
        return new XfaLayoutNode(src, kind, 0, y, 100, h);
    }

    private static Element plain() throws Exception {
        return el("<field xmlns='" + TPL + "'/>");
    }

    private static Element keepNext() throws Exception {
        return el("<field xmlns='" + TPL + "'><keep next='contentArea'/></field>");
    }

    private static Element keepPrev() throws Exception {
        return el("<field xmlns='" + TPL + "'><keep previous='contentArea'/></field>");
    }

    private static Element breakBefore() throws Exception {
        return el("<field xmlns='" + TPL + "'><break before='contentArea'/></field>");
    }

    private static Element breakAfter() throws Exception {
        return el("<field xmlns='" + TPL + "'><break after='contentArea'/></field>");
    }

    private static Element el(String xml) throws Exception {
        return parse(xml).getDocumentElement();
    }

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
