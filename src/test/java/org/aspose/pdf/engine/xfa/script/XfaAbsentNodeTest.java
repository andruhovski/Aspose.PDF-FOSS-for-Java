package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.aspose.pdf.engine.script.js.runtime.Undefined;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// B3.5.1 / B3.5.2 — the **absent node** (null-object for unresolved SOM) and the node
/// sub-property accessors. Objective fixtures: an unresolved `resolveNode`/`item` returns
/// an absent node (not `undefined`) whose value reads are benign empties and that is
/// _falsy_ (so guards terminate), and the real-node `access`/`isNull` accessors
/// read/write the model.
public class XfaAbsentNodeTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    /* ---------------------------- B3.5.1 absent node ---------------------------- */

    @Test
    void resolveNodeOfMissingPathReturnsAbsentNodeNotUndefined() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        Object missing = host.run("xfa.resolveNode('DoesNotExist')", root);
        assertNotNull(missing);
        assertTrue(missing instanceof XfaAbsentNode, "missing SOM path → absent node");
    }

    @Test
    void valueReadsOnAbsentNodeAreBenignEmptiesNoThrow() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        // rawValue/value/presence/name on an unresolved node — no throw, sensible empties.
        assertEquals("", host.run("xfa.resolveNode('Nope').rawValue", root));
        assertEquals("", host.run("xfa.resolveNode('Nope').value", root));
        assertEquals("visible", host.run("xfa.resolveNode('Nope').presence", root));
        assertEquals("", host.run("xfa.resolveNode('Nope').name", root));
        assertEquals(Boolean.TRUE, host.run("xfa.resolveNode('Nope').isNull", root));
        assertEquals(0.0, host.run("xfa.resolveNode('Nope').nodes.length", root));
    }

    @Test
    void chainedAbsentPathDoesNotCrash() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        // a deep dotted step off an absent node stays absent (no "property of undefined").
        assertEquals("", host.run("xfa.resolveNode('Nope').a.b.c.rawValue", root));
        // and node-method calls on it are benign no-ops, not "is not a function".
        assertEquals("", host.run("xfa.resolveNode('Nope').nodes.append(null); xfa.resolveNode('Nope').rawValue", root));
    }

    @Test
    void absentNodeIsFalsySoGuardsTerminate() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        // if (resolveNode(missing)) is FALSE — matches Acrobat's null-for-missing (a truthy null-object
        // would make this branch run, and `while (n) n = n.parent` spin forever).
        assertEquals("no", host.run("xfa.resolveNode('Nope') ? 'yes' : 'no'", root));
        assertEquals(Boolean.TRUE, host.run("!xfa.resolveNode('Nope')", root));
        // a bounded guard loop terminates (would hang on a truthy null-object).
        assertEquals(0.0, host.run(
                "var n = xfa.resolveNode('Nope'); var i = 0; while (n) { i++; n = n.parent; } i", root));
    }

    @Test
    void writesToAbsentNodeAreDroppedNoThrow() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        // assigning rawValue on an unresolved node is a no-op (nowhere to store), does not throw.
        assertEquals("ok", host.run("xfa.resolveNode('Nope').rawValue = 5; 'ok'", root));
    }

    @Test
    void resolveNodeOfPresentSiblingStillResolvesRealNode() throws Exception {
        XfaScriptHost host = host(field("A") + field("B"), data("A", "1", "B", "x"));
        XfaScriptNode root = host.formRoot();
        Object b = host.run("xfa.resolveNode('B')", root);
        assertFalse(b instanceof XfaAbsentNode, "an existing path resolves to a real node, not absent");
        assertEquals("x", host.run("xfa.resolveNode('B').rawValue", root));
    }

    /* ---------------------------- B3.5.2 sub-accessors ---------------------------- */

    @Test
    void accessAccessorReadsDefaultAndWritesThrough() throws Exception {
        XfaScriptHost host = host(field("A"), data("A", "1"));
        XfaScriptNode a = host.wrap(host.formRoot().node);
        XfaScriptNode fieldA = (XfaScriptNode) host.run("xfa.resolveNode('A')", a);
        assertEquals("open", fieldA.get("access"), "default access is open");
        host.run("xfa.resolveNode('A').access = 'readOnly';", a);
        assertEquals("readOnly", fieldA.get("access"), "access write-through to the model");
    }

    @Test
    void isNullReflectsWhetherFieldHasValue() throws Exception {
        XfaScriptHost host = host(field("A") + field("Empty"), data("A", "1"));
        XfaScriptNode root = host.formRoot();
        assertEquals(Boolean.FALSE, host.run("xfa.resolveNode('A').isNull", root), "bound field is not null");
        assertEquals(Boolean.TRUE, host.run("xfa.resolveNode('Empty').isNull", root), "unbound field is null");
    }

    /* ---------------------------- helpers ---------------------------- */

    private static XfaScriptHost host(String fields, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(
                "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>" + fields + "</subform></template>"));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        FormDom dom = new BindingEngine().merge(tpl, data);
        return new XfaScriptHost(dom, tpl);
    }

    private static String field(String name) {
        return "<field name='" + name + "'><ui><textEdit/></ui><value><text/></value></field>";
    }

    private static String data(String... kv) {
        StringBuilder sb = new StringBuilder("<xfa:data xmlns:xfa='" + DATA + "'><form1>");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append('<').append(kv[i]).append('>').append(kv[i + 1]).append("</").append(kv[i]).append('>');
        }
        return sb.append("</form1></xfa:data>").toString();
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
