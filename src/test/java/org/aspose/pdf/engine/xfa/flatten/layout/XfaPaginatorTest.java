package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L3 — applies the split to a paginated Layout DOM and emits multi-page output. C oracle:
 * content conserved across pages (Σ = whole, nothing dropped/duplicated), page count ==
 * SplitPlan, rows rebased to the page top; pageArea selection (ordered + occur); positioned
 * page-subform forms (the 408975 pattern) emit one page per page-subform; per-page paint works
 * and reload is stable.
 */
public class XfaPaginatorTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double EPS = 1e-6;

    /* ----------------------------- L3.1 apply split ----------------------------- */

    @Test
    void flowTablePaginatesConservedAndRebased() throws Exception {
        // 20 rows × 30pt = 600pt of content in a 150pt region ⇒ multi-page.
        XfaFlowLayout.Result lay = tableLayout(20, 30, 150);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(lay);
        XfaPaginator.PaginatedLayout pag = XfaPaginator.paginate(lay, plan, null /* medium default */);

        assertEquals(XfaPaginator.Mode.FLOWED, pag.mode);
        assertEquals(plan.pageCount(), pag.pageCount(), "page count == SplitPlan");

        int totalUnits = 0;
        for (XfaPaginator.PageLayout pl : pag.pages) {
            assertTrue(pl.units.size() > 0, "no empty page");
            // each page's first unit is rebased to the content-region top.
            assertEquals(0, pl.units.get(0).getY(), EPS, "first unit rebased to page top");
            // no unit extends above the page top (Y >= 0).
            for (XfaLayoutNode u : pl.units) {
                assertTrue(u.getY() >= -EPS, "unit not above page top");
            }
            totalUnits += pl.units.size();
        }
        assertEquals(20, totalUnits, "content conserved: every row on exactly one page");
    }

    /* --------------------------- L3.2 pageArea selection --------------------------- */

    @Test
    void pageAreasAssignedInOrderWithOccurLimits() throws Exception {
        // 25 rows × 30 in a 150pt region ⇒ 5 pages. pageSet: PA1 occur max=2, then PA2 (repeats).
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='c").append(i).append("' h='30pt'><value><text/></value></field></subform>");
        }
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + rows + "</subform>"
                + "</subform>"
                + "<pageSet>"
                + "  <pageArea name='PA1'><occur max='2'/><contentArea w='300pt' h='150pt'/></pageArea>"
                + "  <pageArea name='PA2'><contentArea w='300pt' h='150pt'/></pageArea>"
                + "</pageSet></template>";

        XfaFlowLayout.Result lay = layout(tpl, null);
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(lay);
        XfaPaginator.PaginatedLayout pag = XfaPaginator.paginate(lay, plan, (Template) XfaNodeFactory.load(parse(tpl)));

        assertEquals(5, pag.pageCount(), "25 rows / 5-per-page ⇒ 5 pages");
        String[] expected = {"PA1", "PA1", "PA2", "PA2", "PA2"};
        for (int i = 0; i < 5; i++) {
            String name = pag.pages.get(i).region.pageArea.getAttribute("name");
            assertEquals(expected[i], name, "page " + i + " pageArea (ordered + occur max=2)");
        }
    }

    /* --------------------------- L3.3 emit + paint --------------------------- */

    @Test
    void emitsMultiplePagesPaintedAndReloadStable() throws Exception {
        String tpl = tableTemplate(20, 30, 150, true /* bound values */);
        FormDom dom = merge(tpl, tableData(20));
        Template t = (Template) XfaNodeFactory.load(parse(tpl));

        Document doc = new Document();
        XfaPaginator.Result r = XfaPaginator.paint(doc, dom, t);

        assertEquals(XfaPaginator.Mode.FLOWED, r.mode);
        assertTrue(r.pages >= 4, "20×30 / 150 paginated to multiple pages; got " + r.pages);
        assertEquals(r.pages, doc.getPages().getCount(), "emitted page count matches");
        assertTrue(r.painted > 0, "objects painted across pages");
        assertTrue(r.texts > 0, "bound values painted");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        try (Document re = new Document(new ByteArrayInputStream(bos.toByteArray()))) {
            assertEquals(r.pages, re.getPages().getCount(), "reload stable, page count preserved");
        }
        doc.close();
    }

    /* ------------------- positioned page-subform pattern (408975) ------------------- */

    @Test
    void positionedPageSubformsEmitOnePagePerSubform() throws Exception {
        // a positioned root with 3 page-sized subforms (the 408975 pattern) ⇒ 3 pages.
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1'>" // positioned root (no layout)
                + page("p1", "Alpha") + page("p2", "Beta") + page("p3", "Gamma")
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='600pt' h='790pt'/></pageArea></pageSet></template>";
        FormDom dom = merge(tpl, null);
        Template t = (Template) XfaNodeFactory.load(parse(tpl));

        XfaFlowLayout.Result lay = XfaFlowLayout.layout(dom, t);
        XfaPaginator.PaginatedLayout pag = XfaPaginator.paginate(lay, XfaPageSplitter.split(lay), t);
        assertEquals(XfaPaginator.Mode.POSITIONED_PAGES, pag.mode);
        assertEquals(3, pag.pageCount(), "one page per page-sized subform");

        Document doc = new Document();
        XfaPaginator.Result r = XfaPaginator.paint(doc, dom, t);
        assertEquals(3, r.pages);
        assertEquals(3, doc.getPages().getCount());
        assertTrue(r.painted > 0);
        doc.close();
    }

    @Test
    void truePositionedFormStaysSinglePage() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1'>"
                + "  <field name='a' x='10pt' y='10pt' w='100pt' h='20pt'><value><text/></value></field>"
                + "  <field name='b' x='10pt' y='40pt' w='100pt' h='20pt'><value><text/></value></field>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='600pt' h='790pt'/></pageArea></pageSet></template>";
        FormDom dom = merge(tpl, null);
        Template t = (Template) XfaNodeFactory.load(parse(tpl));

        XfaFlowLayout.Result lay = XfaFlowLayout.layout(dom, t);
        XfaPaginator.PaginatedLayout pag = XfaPaginator.paginate(lay, XfaPageSplitter.split(lay), t);
        assertEquals(XfaPaginator.Mode.POSITIONED_SINGLE, pag.mode);

        Document doc = new Document();
        XfaPaginator.Result r = XfaPaginator.paint(doc, dom, t);
        assertEquals(1, r.pages, "true positioned form is single-page");
        assertEquals(1, doc.getPages().getCount());
        doc.close();
    }

    /* helpers */

    private static String page(String name, String value) {
        return "<subform name='" + name + "' x='0pt' y='0pt' w='600pt' h='790pt'>"
                + "<field name='f_" + name + "' x='20pt' y='20pt' w='200pt' h='20pt'>"
                + "<ui><textEdit/></ui><value><text>" + value + "</text></value></field></subform>";
    }

    private static String tableTemplate(int n, double rowH, double regionH, boolean bound) {
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < n; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='Cell").append(i).append("' h='").append((int) rowH)
                .append("pt'><ui><textEdit/></ui><value><text/></value></field></subform>");
        }
        return "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + rows + "</subform>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='300pt' h='" + (int) regionH + "pt'/></pageArea></pageSet></template>";
    }

    private static String tableData(int n) {
        StringBuilder sb = new StringBuilder("<xfa:data xmlns:xfa='" + DATA + "'><form1>");
        for (int i = 0; i < n; i++) {
            sb.append("<Cell").append(i).append(">V").append(i).append("</Cell").append(i).append('>');
        }
        return sb.append("</form1></xfa:data>").toString();
    }

    private static XfaFlowLayout.Result tableLayout(int n, double rowH, double regionH) throws Exception {
        return layout(tableTemplate(n, rowH, regionH, false), null);
    }

    private static XfaFlowLayout.Result layout(String tplXml, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(tplXml));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        FormDom dom = new BindingEngine().merge(tpl, data);
        return XfaFlowLayout.layout(dom, tpl);
    }

    private static FormDom merge(String tplXml, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(tplXml));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        return new BindingEngine().merge(tpl, data);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
