package org.aspose.pdf.engine.xfa.flatten.layout;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// L4.3 — simplex/duplex page qualification. B/C oracle: under `relation="duplexPaginated"` a
/// `<pageArea oddOrEven="odd">` that would naturally fall on an even physical side gets a
/// blank page inserted before it so it lands odd; the page count reflects the inserted blank, and
/// the content is untouched. `simplexPaginated` imposes no constraint (no blanks).
public class XfaDuplexTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    @Test
    void duplexInsertsBlankSoOddQualifiedPageAreaLandsOdd() throws Exception {
        // 8 rows × 30pt / 150pt region ⇒ 2 content pages. PA1 (page 1), PA2 qualified ODD wants page 2,
        // but page 2 is even ⇒ a blank is inserted so PA2 content lands on physical page 3 (odd).
        XfaPaginator.PaginatedLayout pag = paginate(template("duplexPaginated"));

        assertEquals(3, pag.pageCount(), "a blank page is inserted to satisfy the odd qualification");
        // page 1 = PA1 content, page 2 = inserted blank, page 3 = PA2 content (odd side).
        assertEquals("PA1", areaName(pag, 0), "page 1 hosts PA1");
        assertTrue(pag.pages.get(1).units.isEmpty(), "page 2 is the inserted blank");
        assertNull(pag.pages.get(1).region.pageArea, "blank page has no pageArea");
        assertEquals("PA2", areaName(pag, 2), "PA2 content lands on physical page 3 (odd)");
        assertTrue(!pag.pages.get(2).units.isEmpty(), "the qualified content page is non-empty");
    }

    @Test
    void simplexImposesNoConstraint() throws Exception {
        // Same template but simplexPaginated ⇒ no blanks, the 2 content pages stay back-to-back.
        XfaPaginator.PaginatedLayout pag = paginate(template("simplexPaginated"));

        assertEquals(2, pag.pageCount(), "simplex inserts no blank pages");
        for (XfaPaginator.PageLayout pl : pag.pages) {
            assertTrue(!pl.units.isEmpty(), "no blank pages under simplex");
        }
    }

    /* --------------------------------- helpers --------------------------------- */

    private static String template(String relation) {
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='c").append(i)
                .append("' h='30pt'><ui><textEdit/></ui><value><text/></value></field></subform>");
        }
        return "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + rows + "</subform>"
                + "</subform>"
                + "<pageSet relation='" + relation + "'>"
                + "  <pageArea name='PA1'><occur max='1'/><contentArea w='300pt' h='150pt'/></pageArea>"
                + "  <pageArea name='PA2' oddOrEven='odd'><contentArea w='300pt' h='150pt'/></pageArea>"
                + "</pageSet></template>";
    }

    private static String areaName(XfaPaginator.PaginatedLayout pag, int page) {
        return pag.pages.get(page).region.pageArea == null
                ? null : pag.pages.get(page).region.pageArea.getAttribute("name");
    }

    private static XfaPaginator.PaginatedLayout paginate(String tplXml) throws Exception {
        Template t = (Template) XfaNodeFactory.load(parse(tplXml));
        FormDom dom = new BindingEngine().merge(t, null);
        XfaFlowLayout.Result lay = XfaFlowLayout.layout(dom, t);
        return XfaPaginator.paginate(lay, XfaPageSplitter.split(lay), t);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
