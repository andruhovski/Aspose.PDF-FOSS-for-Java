package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator;
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
 * B3.2 — {@code .instanceManager} (dynamic subform add/remove). B/C oracle: {@code _name.count}
 * reflects the instance list; add/remove/setInstances mutate within {@code <occur min max>} limits;
 * a new instance's fields exist and compute; instanceIndex is correct; mutations flow into calculate
 * and the paginated render.
 */
public class XfaInstanceManagerTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    @Test
    void managerCountAndAccessor() throws Exception {
        FormDom dom = merge(table(2, 0, -1, null), data(2));
        Template tpl = load(table(2, 0, -1, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        XfaScriptNode root = host.formRoot();
        assertEquals(2.0, host.run("_Detail.count", root), "two static instances");
        assertTrue(host.run("typeof _Detail.addInstance", root).equals("function"), "manager exposes addInstance");
    }

    @Test
    void addInstanceGrowsListWithinMax() throws Exception {
        FormDom dom = merge(table(1, 0, 3, null), data(1));
        Template tpl = load(table(1, 0, 3, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        XfaScriptNode root = host.formRoot();
        assertEquals(1.0, host.run("_Detail.count", root));
        host.run("_Detail.addInstance(0); _Detail.addInstance(0);", root);
        assertEquals(3.0, host.run("_Detail.count", root), "grew 1→3");
        host.run("_Detail.addInstance(0);", root); // at max=3 → no-op
        assertEquals(3.0, host.run("_Detail.count", root), "capped at max=3");
    }

    @Test
    void removeInstanceShrinksWithinMin() throws Exception {
        FormDom dom = merge(table(3, 1, -1, null), data(3));
        Template tpl = load(table(3, 1, -1, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        XfaScriptNode root = host.formRoot();
        host.run("_Detail.removeInstance(0);", root);
        assertEquals(2.0, host.run("_Detail.count", root), "3→2");
        host.run("_Detail.removeInstance(0); _Detail.removeInstance(0);", root); // min=1
        assertEquals(1.0, host.run("_Detail.count", root), "floored at min=1");
    }

    @Test
    void setInstancesClampsToOccurLimits() throws Exception {
        FormDom dom = merge(table(2, 1, 4, null), data(2));
        Template tpl = load(table(2, 1, 4, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        XfaScriptNode root = host.formRoot();
        host.run("_Detail.setInstances(10);", root);   // clamp to max=4
        assertEquals(4.0, host.run("_Detail.count", root));
        host.run("_Detail.setInstances(0);", root);    // clamp to min=1
        assertEquals(1.0, host.run("_Detail.count", root));
    }

    @Test
    void instanceManagerAccessorBridgesToManager() throws Exception {
        // B3.3 IM closure: node.instanceManager bridges to the _<name> manager (the qualified-access
        // pattern 34 corpus forms use, which B3.2 left unwired — only _name was exposed).
        FormDom dom = merge(table(2, 0, 4, null), data(2));
        Template tpl = load(table(2, 0, 4, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        XfaScriptNode root = host.formRoot();
        assertEquals(2.0, host.run("xfa.resolveNode('Detail').instanceManager.count", root));
        host.run("xfa.resolveNode('Detail').instanceManager.addInstance(0);", root);
        assertEquals(3.0, host.run("_Detail.count", root), "instanceManager.addInstance grew the list");
    }

    @Test
    void newInstanceFieldsComputeAndTotal() throws Exception {
        // initialize adds a Detail row; a Total field sums every Detail.Amount including the new one.
        String t = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='Detail' layout='row'><occur min='1' max='-1'/>"
                + "  <field name='Amount'><ui><numericEdit/></ui><value><decimal/></value></field></subform>"
                + "<field name='Total'><ui><numericEdit/></ui><value><decimal/></value>"
                + "  <calculate><script contentType='application/x-javascript'>"
                + "var s=0; var rows=this.parent.resolveNodes('Detail[*].Amount'); for(var i=0;i&lt;rows.length;i++){ s+=Number(rows[i].rawValue); } s;"
                + "</script></calculate></field>"
                + "<event activity='initialize'><script contentType='application/x-javascript'>"
                + "var n=_Detail.addInstance(0); n.resolveNode('Amount').rawValue = 100;"
                + "</script></event>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='400pt' h='600pt'/></pageArea></pageSet></template>";
        FormDom dom = merge(t, "<xfa:data xmlns:xfa='" + DATA + "'><form1><Detail><Amount>30</Amount></Detail></form1></xfa:data>");
        Template tpl = load(t);

        XfaScripting.Result r = XfaScripting.execute(dom, tpl);
        // initialize added one Detail (1→2); Total sums 30 + 100.
        assertTrue(r.events >= 1);
        assertEquals("130", dom.fieldByName("Total").getValue(), "Total includes the added instance: " + r.errors);

        // flows into render
        Document doc = new Document();
        XfaPaginator.Result pr = XfaPaginator.paint(doc, dom, tpl);
        assertTrue(pr.texts > 0, "added instance + total painted");
        doc.close();
    }

    @Test
    void nonVariableSubformHasNoManager() throws Exception {
        // fixed occurrence (min==max) ⇒ no instanceManager injected.
        FormDom dom = merge(table(2, 2, 2, null), data(2));
        Template tpl = load(table(2, 2, 2, null));
        XfaScriptHost host = new XfaScriptHost(dom, tpl);
        assertEquals("undefined", host.run("typeof _Detail", host.formRoot()), "fixed subform → no _Detail manager");
    }

    /* ------------------------------ fixtures ------------------------------ */

    private static String table(int rows, int min, int max, String ignored) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            data.append("");
        }
        return "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='Detail' layout='row'><occur min='" + min + "' max='" + max + "'/>"
                + "  <field name='Amount'><ui><numericEdit/></ui><value><decimal/></value></field></subform>"
                + "</subform>"
                + "<pageSet><pageArea><contentArea w='400pt' h='600pt'/></pageArea></pageSet></template>";
    }

    private static String data(int rows) {
        StringBuilder sb = new StringBuilder("<xfa:data xmlns:xfa='" + DATA + "'><form1>");
        for (int i = 0; i < rows; i++) {
            sb.append("<Detail><Amount>").append((i + 1) * 10).append("</Amount></Detail>");
        }
        return sb.append("</form1></xfa:data>").toString();
    }

    private static FormDom merge(String tplXml, String dataXml) throws Exception {
        Template tpl = load(tplXml);
        XfaNode d = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        return new BindingEngine().merge(tpl, d);
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
