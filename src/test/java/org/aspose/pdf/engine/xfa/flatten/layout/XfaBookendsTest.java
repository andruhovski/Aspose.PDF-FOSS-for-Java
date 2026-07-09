package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L4.1 (leaders/trailers at explicit breaks) and L4.2 (bookends — overflow leaders/trailers at
 * every region boundary). B/C oracle: leader is the first object of each page it appears on,
 * trailer the last; bookend repetition is capped by the leader subform's {@code <occur max>};
 * heights account for the boilerplate (content shifts down by the leader height, fits the region);
 * the flowed content is conserved (every data unit on exactly one page).
 */
public class XfaBookendsTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    /* ------------------------------- L4.2 bookends ------------------------------- */

    @Test
    void overflowLeaderRepeatsAtopEveryPage() throws Exception {
        // 20 rows × 30pt into a 150pt region with a 20pt overflow header → header on every page.
        String tpl = bookendTable(20, 30, 150, "hdr", 20, /*occurMax*/ -1, /*trailer*/ null, 0);
        FormDom dom = merge(tpl);
        Template t = load(tpl);

        XfaPaginator.PaginatedLayout pag = paginate(dom, t);
        assertEquals(XfaPaginator.Mode.FLOWED, pag.mode);
        assertTrue(pag.pageCount() >= 3, "20×30 with reserved header paginates to several pages; got " + pag.pageCount());

        int dataRows = 0;
        for (XfaPaginator.PageLayout pl : pag.pages) {
            // leader is the first object on the page, at the very top.
            XfaLayoutNode first = pl.units.get(0);
            assertEquals("hdr", name(first.getSource()), "header is first object on the page");
            assertEquals(0.0, first.getY(), 1e-6, "header sits at the page top");
            // content sits below the header (shifted down by the header height).
            for (int i = 1; i < pl.units.size(); i++) {
                assertTrue(pl.units.get(i).getY() >= first.getHeight() - 1e-6,
                        "content is below the header");
                if (name(pl.units.get(i).getSource()).startsWith("r")) {
                    dataRows++;
                }
            }
            // everything fits the 150pt region (header + content).
            double bottom = 0;
            for (XfaLayoutNode u : pl.units) {
                bottom = Math.max(bottom, u.getBottom());
            }
            assertTrue(bottom <= 150 + 1e-6, "page content (incl. header) fits the region: " + bottom);
        }
        assertEquals(20, dataRows, "content conserved: every data row appears exactly once");
    }

    @Test
    void overflowLeaderOccurMaxCapsRepetition() throws Exception {
        // Same table, but the header subform <occur max='2'> ⇒ it repeats on the first 2 pages only.
        String tpl = bookendTable(20, 30, 150, "hdr", 20, /*occurMax*/ 2, null, 0);
        XfaPaginator.PaginatedLayout pag = paginate(merge(tpl), load(tpl));

        int withHeader = 0;
        for (XfaPaginator.PageLayout pl : pag.pages) {
            if ("hdr".equals(name(pl.units.get(0).getSource()))) {
                withHeader++;
            }
        }
        assertEquals(2, withHeader, "occur max=2 caps the header to the first 2 pages");
        assertTrue(pag.pageCount() > 2, "there are more pages than the cap; got " + pag.pageCount());
    }

    @Test
    void overflowTrailerIsLastObjectOnEachPage() throws Exception {
        // A 12pt overflow trailer (e.g. a running-total row) at the bottom of every page.
        String tpl = bookendTable(20, 30, 150, null, 0, 0, "ftr", 12);
        XfaPaginator.PaginatedLayout pag = paginate(merge(tpl), load(tpl));

        for (XfaPaginator.PageLayout pl : pag.pages) {
            XfaLayoutNode last = pl.units.get(pl.units.size() - 1);
            assertEquals("ftr", name(last.getSource()), "trailer is the last object on the page");
            for (XfaLayoutNode u : pl.units) {
                assertTrue(last.getY() + 1e-6 >= u.getY(), "trailer is the lowest object on the page");
            }
            double bottom = 0;
            for (XfaLayoutNode u : pl.units) {
                bottom = Math.max(bottom, u.getBottom());
            }
            assertTrue(bottom <= 150 + 1e-6, "page (incl. trailer) fits the region");
        }
    }

    /* ----------------------------- L4.1 explicit break ----------------------------- */

    @Test
    void explicitBreakPlacesTrailerLastAndLeaderFirst() throws Exception {
        // Three stacked fields; field 'b' forces a page break before it, carrying a leader+trailer.
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <field name='a' h='30pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "  <field name='b' h='30pt'><break before='pageArea' leader='lh' trailer='lt'/>"
                + "      <ui><textEdit/></ui><value><text/></value></field>"
                + "  <field name='c' h='30pt'><ui><textEdit/></ui><value><text/></value></field>"
                + "</subform>"
                + "<subform name='lh'><draw name='LD' w='200pt' h='15pt'><value><text>CONTINUED</text></value></draw></subform>"
                + "<subform name='lt'><draw name='TR' w='200pt' h='12pt'><value><text>more overleaf</text></value></draw></subform>"
                + "<pageSet><pageArea><contentArea w='300pt' h='500pt'/></pageArea></pageSet></template>";
        FormDom dom = merge(tpl);
        Template t = load(tpl);

        XfaPaginator.PaginatedLayout pag = paginate(dom, t);
        assertEquals(2, pag.pageCount(), "the forced break splits into 2 pages");

        XfaPaginator.PageLayout p1 = pag.pages.get(0);
        XfaPaginator.PageLayout p2 = pag.pages.get(1);

        // trailer is the last object on the page being left (page 1).
        assertEquals("lt", name(p1.units.get(p1.units.size() - 1).getSource()), "trailer last on page 1");
        // leader is the first object on the new page (page 2).
        assertEquals("lh", name(p2.units.get(0).getSource()), "leader first on page 2");
        assertEquals(0.0, p2.units.get(0).getY(), 1e-6, "leader at the top of page 2");

        // content conserved: a on page 1, b and c on page 2.
        assertTrue(sources(p1).contains("a"), "field a on page 1");
        assertTrue(sources(p2).contains("b") && sources(p2).contains("c"), "fields b,c on page 2");
    }

    /* --------------------------------- control --------------------------------- */

    @Test
    void noBookendsLeavesPaginationUntouched() throws Exception {
        // A plain flowed table with no overflow/break boundary content paginates exactly as L3 did.
        String tpl = bookendTable(20, 30, 150, null, 0, 0, null, 0);
        XfaPaginator.PaginatedLayout withApi = paginate(merge(tpl), load(tpl));

        // every page's first/last object is a data row (no inserted boilerplate).
        int rows = 0;
        for (XfaPaginator.PageLayout pl : withApi.pages) {
            for (XfaLayoutNode u : pl.units) {
                assertTrue(name(u.getSource()).startsWith("r"), "only data rows, no boilerplate");
                rows++;
            }
        }
        assertEquals(20, rows, "all 20 rows, nothing added");
    }

    /* --------------------------------- helpers --------------------------------- */

    /** A flowed table form with an optional overflow leader (header) and/or trailer (footer) subform. */
    private static String bookendTable(int n, int rowH, int regionH, String leaderName, int leaderH,
                                       int occurMax, String trailerName, int trailerH) {
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < n; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='c").append(i).append("' h='").append(rowH)
                .append("pt'><ui><textEdit/></ui><value><text/></value></field></subform>");
        }
        StringBuilder overflow = new StringBuilder();
        if (leaderName != null) {
            overflow.append("<overflow leader='").append(leaderName).append("'/>");
        }
        if (trailerName != null) {
            // a separate <overflow> is fine; the resolver reads leader/trailer off the first decl.
            overflow.append("<overflow trailer='").append(trailerName).append("'/>");
        }
        StringBuilder leaderSub = new StringBuilder();
        if (leaderName != null) {
            leaderSub.append("<subform name='").append(leaderName).append("' layout='row'>");
            if (occurMax >= 0) {
                leaderSub.append("<occur max='").append(occurMax).append("'/>");
            }
            leaderSub.append("<draw name='H' w='200pt' h='").append(leaderH)
                     .append("pt'><value><text>HEADER</text></value></draw></subform>");
        }
        StringBuilder trailerSub = new StringBuilder();
        if (trailerName != null) {
            trailerSub.append("<subform name='").append(trailerName).append("' layout='row'>")
                      .append("<draw name='F' w='200pt' h='").append(trailerH)
                      .append("pt'><value><text>TOTAL</text></value></draw></subform>");
        }
        return "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + overflow + rows + "</subform>"
                + "</subform>"
                + leaderSub + trailerSub
                + "<pageSet><pageArea><contentArea w='300pt' h='" + regionH + "pt'/></pageArea></pageSet></template>";
    }

    private static XfaPaginator.PaginatedLayout paginate(FormDom dom, Template t) {
        XfaFlowLayout.Result lay = XfaFlowLayout.layout(dom, t);
        XfaBookends.Spec bk = XfaBookends.overflow(dom, t, lay.regionWidth);
        double splitH = lay.regionHeight - bk.leaderHeight() - bk.trailerHeight();
        if (splitH < 1) {
            splitH = lay.regionHeight;
        }
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(lay, splitH);
        return XfaPaginator.paginate(lay, plan, t, bk, dom);
    }

    private static List<String> sources(XfaPaginator.PageLayout pl) {
        List<String> out = new ArrayList<>();
        for (XfaLayoutNode u : pl.units) {
            out.add(name(u.getSource()));
        }
        return out;
    }

    private static String name(Element el) {
        if (el == null) {
            return "";
        }
        String n = el.getAttribute("name");
        return n == null ? "" : n;
    }

    private static FormDom merge(String tplXml) throws Exception {
        Template tpl = load(tplXml);
        return new BindingEngine().merge(tpl, null);
    }

    private static Template load(String tplXml) throws Exception {
        return (Template) XfaNodeFactory.load(parse(tplXml));
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
