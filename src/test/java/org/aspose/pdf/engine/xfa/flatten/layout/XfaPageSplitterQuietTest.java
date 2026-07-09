package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Audit hygiene guard: the production pagination path must write NOTHING to
 * {@code System.out}/{@code System.err} unless a debug property
 * ({@code -Dxfa.dumpSplit} / {@code -Dxfa.dumpTree}) is explicitly set — the
 * split/tree dumps are diagnostics, not consumer-visible noise. Also proves the
 * dump still works when the flag IS set (so the diagnostics stay usable).
 */
public class XfaPageSplitterQuietTest {

    private static final String TPL = "http://www.xfa.org/schema/xfa-template/3.0/";

    /** A flowed multi-page table layout (600pt of rows in a 150pt region ⇒ 4 pages). */
    private static XfaFlowLayout.Result multiPageLayout() throws Exception {
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            rows.append("<subform name='r").append(i).append("' layout='row'>")
                .append("<field name='c").append(i).append("' h='30pt'><value><text/></value></field></subform>");
        }
        String tpl = "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' layout='tb'>"
                + "  <subform name='tbl' layout='table' columnWidths='200pt'>" + rows + "</subform>"
                + "</subform>"
                + "<pageSet><pageArea name='PA1'><contentArea w='300pt' h='150pt'/></pageArea></pageSet>"
                + "</template>";
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document doc = f.newDocumentBuilder()
                .parse(new ByteArrayInputStream(tpl.getBytes(StandardCharsets.UTF_8)));
        Template template = (Template) XfaNodeFactory.load(doc);
        FormDom dom = new BindingEngine().merge(template, null);
        return XfaFlowLayout.layout(dom, template);
    }

    @Test
    public void paginationIsSilentWithoutDebugFlags() throws Exception {
        String savedSplit = System.clearProperty("xfa.dumpSplit");
        String savedTree = System.clearProperty("xfa.dumpTree");
        PrintStream realOut = System.out, realErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out, true, "UTF-8"));
            System.setErr(new PrintStream(err, true, "UTF-8"));

            XfaFlowLayout.Result lay = multiPageLayout();
            XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(lay);
            XfaPaginator.PaginatedLayout pag = XfaPaginator.paginate(lay, plan, null);
            assertTrue(pag.pageCount() > 1, "layout must actually paginate (multi-page)");
        } finally {
            System.setOut(realOut);
            System.setErr(realErr);
            if (savedSplit != null) System.setProperty("xfa.dumpSplit", savedSplit);
            if (savedTree != null) System.setProperty("xfa.dumpTree", savedTree);
        }
        assertEquals("", out.toString("UTF-8"), "pagination must not write to System.out");
        assertEquals("", err.toString("UTF-8"), "pagination must not write to System.err");
    }

    @Test
    public void dumpSplitStillWorksWhenFlagSet() throws Exception {
        String savedSplit = System.getProperty("xfa.dumpSplit");
        PrintStream realErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            System.setProperty("xfa.dumpSplit", "true");
            System.setErr(new PrintStream(err, true, "UTF-8"));
            XfaFlowLayout.Result lay = multiPageLayout();
            XfaPageSplitter.split(lay);
        } finally {
            System.setErr(realErr);
            if (savedSplit == null) System.clearProperty("xfa.dumpSplit");
            else System.setProperty("xfa.dumpSplit", savedSplit);
        }
        assertTrue(err.toString("UTF-8").contains("xfa.dumpSplit"),
                "the diagnostic dump must still be available under the flag");
    }
}
