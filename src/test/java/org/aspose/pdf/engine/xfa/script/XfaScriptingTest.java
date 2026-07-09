package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B3.1 — XFA JavaScript host binding + load-time execution. A (SOM bridge + accessors + util) and
 * B (calculate topological + cycles, validate report-only, initialize/ready) over the merged Form
 * DOM, asserting computed values land on the FormField (so they reach the render track).
 */
public class XfaScriptingTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    /* ------------------------------ PART A ------------------------------ */

    @Test
    void resolveNodeBridgesToSomAndReadsSiblingValue() throws Exception {
        FormDom dom = merge(form(
                field("A", "calc", null, null),
                field("B", null, null, null)),
                data("A", "7", "B", "x"));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode a = host.wrap(dom.fieldByName("A").getFormNode());
        // resolveNode from the parent reaches sibling B; rawValue reads its bound value.
        XfaScriptNode parent = (XfaScriptNode) a.get("parent");
        Object b = host.run("xfa.resolveNode('B').rawValue", parent);
        assertEquals("x", b);
    }

    @Test
    void rawValueReadAndWriteUpdatesFormField() throws Exception {
        FormDom dom = merge(form(field("Total", null, null, null)), data("Total", "1"));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode t = host.wrap(dom.fieldByName("Total").getFormNode());
        assertEquals("1", t.get("rawValue"));
        host.run("this.rawValue = 42;", t);
        assertEquals("42", dom.fieldByName("Total").getValue(), "write-through to the FormField value");
        assertEquals("Total", t.get("name"));
    }

    @Test
    void utilPrintdAndScandRoundTrip() {
        Object out = org.aspose.pdf.engine.script.js.Engine.eval("1+1"); // engine sanity
        assertEquals(2.0, out);
        // printd formats; scand parses back — exercised through a host so util is installed.
        FormDom dom = emptyDom();
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode root = host.formRoot();
        assertEquals("2020-01-15", host.run("util.printd('YYYY-MM-DD', util.scand('MM/DD/YYYY','01/15/2020'))", root));
        assertEquals("15.01.2020", host.run("util.printd('DD.MM.YYYY', util.scand('YYYY-MM-DD','2020-01-15'))", root));
        assertEquals("Jan 15, 2020", host.run("util.printd('MMM D, YYYY', util.scand('YYYY-MM-DD','2020-01-15'))", root));
    }

    /* ------------------------------ PART B ------------------------------ */

    @Test
    void calculateDerivesValueFromSiblings() throws Exception {
        FormDom dom = merge(form(
                field("Qty", null, null, null),
                field("Price", null, null, null),
                field("Total", null, "Qty.rawValue * Price.rawValue", null)),
                data("Qty", "3", "Price", "4", "Total", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(1, r.calculates);
        assertEquals("12", dom.fieldByName("Total").getValue(), "Total = Qty*Price computed and written");
        assertTrue(r.valuesProduced >= 1);
    }

    @Test
    void calculateRunsInTopologicalDependencyOrder() throws Exception {
        // C depends on B (computed), B depends on A (data). Declared C, B so order must be fixed.
        // numeric fields → rawValue is a number (so + sums, not concatenates).
        FormDom dom = merge(form(
                numField("A", null),
                numField("C", "B.rawValue + 1"),
                numField("B", "A.rawValue + 1")),
                data("A", "10", "B", "", "C", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertTrue(r.cycles.isEmpty(), "no cycle");
        assertEquals("11", dom.fieldByName("B").getValue(), "B = A+1 computed first");
        assertEquals("12", dom.fieldByName("C").getValue(), "C = B+1 computed after B");
    }

    @Test
    void calculateCycleIsDetectedNotLooped() throws Exception {
        FormDom dom = merge(form(
                numField("X", "Y.rawValue + 1"),
                numField("Y", "X.rawValue + 1")),
                data("X", "0", "Y", "0"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertFalse(r.cycles.isEmpty(), "cycle reported");
        // both still execute once (document order fallback) — no infinite loop, run completes
        assertEquals(2, r.calculates);
    }

    @Test
    void validateFlagsInvalidReportOnly() throws Exception {
        FormDom dom = merge(form(
                field("Age", null, null, "this.rawValue >= 18"),
                field("Ok", null, null, "this.rawValue >= 18")),
                data("Age", "10", "Ok", "20"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(2, r.validates);
        assertEquals(1, r.invalid, "Age<18 flagged");
        assertTrue(r.invalidFields.toString().contains("Age"));
        // report-only: the value is untouched
        assertEquals("10", dom.fieldByName("Age").getValue());
    }

    @Test
    void initializeSeedsValueForRender() throws Exception {
        FormDom dom = merge(form(
                fieldWithEvent("Stamp", "initialize", "this.rawValue = 'SEEDED';")),
                null);
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(1, r.events);
        assertEquals(1, r.eventsOk);
        assertEquals("SEEDED", dom.fieldByName("Stamp").getValue(), "initialize seeded the value");
    }

    @Test
    void scriptErrorIsCategorisedNotFatal() throws Exception {
        FormDom dom = merge(form(
                field("Bad", null, "this.nope.nope()", null),
                field("Good", null, "'ok'", null)),
                data("Bad", "", "Good", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(1, r.calculatesFailed, "the throwing calc is caught");
        assertEquals("ok", dom.fieldByName("Good").getValue(), "the good calc still runs");
        assertTrue(r.errors.toString().contains("calculate:Bad"));
    }

    @Test
    void scriptObjectLibraryFunctionsAreCallable() throws Exception {
        // A form-level <variables> script library defines a helper that a field calculate calls.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<variables><script contentType='application/x-javascript'>"
                + "function dbl(x){ return x * 2; }</script></variables>"
                + numField("Qty", null)
                + numField("Total", "dbl(Qty.rawValue)")
                + "</subform></template>";
        FormDom dom = merge(tpl, data("Qty", "21"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "library helper resolved: " + r.errors);
        assertEquals("42", dom.fieldByName("Total").getValue());
    }

    /* ------------------------------ B3.3 PART 1 — script-object exposure ------------------------------ */

    @Test
    void namedScriptObjectResolvesQualifiedAndBare() throws Exception {
        // <script name='fnGroup'> defines addArrayGroup; the corpus calls it BOTH qualified
        // (fnGroup.addArrayGroup, 88% of calls) and bare (addArrayGroup). Both must resolve.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<variables><script name='fnGroup' contentType='application/x-javascript'>"
                + "function addArrayGroup(x){ return x * 2; }</script></variables>"
                + numField("Qty", null)
                + numField("Q", "fnGroup.addArrayGroup(Qty.rawValue)")
                + numField("B", "addArrayGroup(Qty.rawValue)")
                + "</subform></template>";
        FormDom dom = merge(tpl, data("Qty", "21"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "qualified + bare both resolved: " + r.errors);
        assertEquals("42", dom.fieldByName("Q").getValue(), "fnGroup.addArrayGroup() resolved");
        assertEquals("42", dom.fieldByName("B").getValue(), "bare addArrayGroup() still resolved");
    }

    @Test
    void twoLibrariesOneCallsTheOthersQualifiedHelper() throws Exception {
        // lib 'A' defines base(); lib 'B' defines viaA() which calls A.base() qualified. Load order
        // preserved; a field calc invoking B.viaA() must reach A.base().
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<variables><script name='A' contentType='application/x-javascript'>"
                + "function base(){ return 10; }</script></variables>"
                + "<variables><script name='B' contentType='application/x-javascript'>"
                + "function viaA(){ return A.base() + 1; }</script></variables>"
                + numField("Total", "B.viaA()")
                + "</subform></template>";
        FormDom dom = merge(tpl, data("Total", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "cross-library qualified call resolved: " + r.errors);
        assertEquals("11", dom.fieldByName("Total").getValue());
    }

    @Test
    void hoistedHelpersExposedEvenWhenLibraryTopLevelThrows() throws Exception {
        // A library whose top-level statement throws (a B3-DIAG H failure) still exposes its hoisted
        // function declarations via the script object, so qualified helper calls resolve anyway.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<variables><script name='scoUtil' contentType='application/x-javascript'>"
                + "function calc(x){ return x + 1; } var boom = missingThing.value;"
                + "</script></variables>"
                + numField("Total", "scoUtil.calc(40)")
                + "</subform></template>";
        FormDom dom = merge(tpl, data("Total", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "hoisted helper exposed despite load throw: " + r.errors);
        assertEquals("41", dom.fieldByName("Total").getValue());
    }

    /* ------------------------------ B3.3 PART 2 — node child + field-member accessors ------------------------------ */

    @Test
    void dottedChildNavigationResolvesNestedField() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='page1'><subform name='inner'>" + numField("leaf", null)
                + "</subform></subform></subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1><page1><inner><leaf>5</leaf>"
                + "</inner></page1></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScriptHost host = new XfaScriptHost(dom);
        // a.b.c dotted navigation over the canonical tree (the narrativeSubform2 path shape)
        assertEquals(5.0, host.run("this.page1.inner.leaf.rawValue", host.formRoot()));
    }

    @Test
    void countReturnsSameNameInstanceCount() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='Detail'><occur min='0' max='-1'/>" + numField("x", null)
                + "</subform></subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Detail><x>1</x></Detail><Detail><x>2</x></Detail></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScriptHost host = new XfaScriptHost(dom);
        assertEquals(2.0, host.run("xfa.resolveNode('Detail').count", host.formRoot()), "two Detail instances");
    }

    @Test
    void toolTipMemberReadAndWriteDoNotThrow() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<field name='F'><ui><textEdit/></ui><value><text/></value>"
                + "<assist><toolTip>orig</toolTip></assist></field></subform></template>";
        FormDom dom = merge(tpl, null);
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode f = host.wrap(dom.fieldByName("F").getFormNode());
        // the corpus pattern t.toolTip.value = "..." must resolve and not throw
        assertEquals("ok", host.run("this.toolTip.value = 'updated'; 'ok'", f));
    }

    @Test
    void libraryTopLevelNavigationNowLoads() throws Exception {
        // the narrativeSubform2 H-failure shape: a library whose top-level dotted navigation used to
        // throw "Cannot read property of undefined" now loads (PART 2 child navigation).
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='page1'><subform name='inner'>" + numField("leaf", null)
                + "</subform></subform>"
                + "<variables><script name='nav' contentType='application/x-javascript'>"
                + "var leafVal = page1.inner.leaf.rawValue; function getLeaf(){ return leafVal; }"
                + "</script></variables>"
                + numField("Total", "nav.getLeaf()") + "</subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1><page1><inner><leaf>9</leaf>"
                + "</inner></page1></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.scriptLibsFailed, "navigation library now loads (was an H failure): " + r.errors);
        assertEquals("9", dom.fieldByName("Total").getValue());
    }

    /* ------------------------------ B3.4 — node/host methods ------------------------------ */

    @Test
    void createNodeBuildsAttachesAndFlows() throws Exception {
        FormDom dom = merge(form(field("Host", null, null, null)), data("Host", ""));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode root = host.formRoot();
        // createNode returns a node carrying the requested class + name
        assertEquals("dataValue", host.run("xfa.datasets.createNode('dataValue','x').className", root));
        assertEquals("New", host.run("xfa.form.createNode('field','New').name", root));
        // a created field appended to the tree is reachable via the parent (flows into compute)
        host.run("var f = xfa.form.createNode('field','New'); this.nodes.append(f);", root);
        assertEquals("New", host.run("this.New.name", root), "appended node reachable by name");
        assertEquals(1.0, host.run("xfa.resolveNodes('New').length", root), "and via SOM");
        assertEquals("New", host.run("xfa.resolveNodes('New').item(0).name", root), "resolveNodes().item(i)");
    }

    @Test
    void isPropertySpecifiedReflectsExplicitProperties() throws Exception {
        FormDom dom = merge(form(field("F", null, null, null)), data("F", "1"));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode f = host.wrap(dom.fieldByName("F").getFormNode());
        assertEquals(Boolean.TRUE, host.run("this.isPropertySpecified('ui')", f), "explicit <ui> child");
        assertEquals(Boolean.TRUE, host.run("this.isPropertySpecified('name')", f), "explicit name attribute");
        assertEquals(Boolean.FALSE, host.run("this.isPropertySpecified('access')", f), "access not set");
    }

    @Test
    void addItemClearItemsAndItemQueriesMutateOptions() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<field name='Dd'><ui><choiceList/></ui><value><text/></value></field></subform></template>";
        FormDom dom = merge(tpl, data("Dd", ""));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode f = host.wrap(dom.fieldByName("Dd").getFormNode());
        host.run("this.addItem('Red','R'); this.addItem('Green','G');", f);
        assertEquals("Red", host.run("this.getDisplayItem(0)", f));
        assertEquals("G", host.run("this.boundItem('Green')", f), "bound value paired with display");
        host.run("this.rawValue = 'G';", f);
        assertEquals(1.0, host.run("this.selectedIndex", f), "value G is the 2nd bound item");
        host.run("this.clearItems();", f);
        assertEquals("", host.run("this.getDisplayItem(0)", f), "items cleared");
    }

    @Test
    void execEventRunsHandlerSynchronouslyAndInactiveFailsCleanly() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<field name='Btn'><ui><button/></ui><value><text/></value>"
                + "<event activity='click'><script contentType='application/x-javascript'>"
                + "Target.rawValue = 'clicked';</script></event></field>"
                + field("Target", null, null, null) + "</subform></template>";
        FormDom dom = merge(tpl, data("Target", ""));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode btn = host.wrap(dom.fieldByName("Btn").getFormNode());
        assertEquals(Boolean.TRUE, host.run("this.execEvent('click')", btn), "handler ran");
        assertEquals("clicked", dom.fieldByName("Target").getValue(), "synchronous effect visible");
        // inactive node → fails cleanly, no effect
        host.run("Target.rawValue = ''; this.presence = 'inactive';", btn);
        assertEquals(Boolean.FALSE, host.run("this.execEvent('click')", btn), "inactive → false");
        assertEquals("", dom.fieldByName("Target").getValue(), "inactive handler did not run");
    }

    /* ------------------------------ SOM-R — script-context scope resolution ------------------------------ */

    @Test
    void resolveNodeBareSiblingResolvesAcrossScope() throws Exception {
        // The headline correctness case: a calculate on `total` reads its siblings via
        // xfa.resolveNode("price")/("qty"). Before SOM-R these resolved nothing (price/qty are not
        // children of `total`) → "" → 0. Now the scope search ascends to the shared container.
        FormDom dom = merge(form(
                numField("price", null),
                numField("qty", null),
                numField("total", "xfa.resolveNode('price').rawValue * xfa.resolveNode('qty').rawValue")),
                data("price", "4", "qty", "3", "total", ""));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "no error: " + r.errors);
        assertEquals("12", dom.fieldByName("total").getValue(), "resolveNode sibling values multiplied");
    }

    @Test
    void resolveNodeAscendsPastSubformToAncestorSibling() throws Exception {
        // `derived` lives in subform grp; `base` is its sibling within grp. The scope search ascends
        // from `derived` (no match among its own children) to grp, where `base` matches.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='grp'>" + numField("base", null)
                + numField("derived", "xfa.resolveNode('base').rawValue + 100") + "</subform>"
                + "</subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1><grp><base>5</base><derived></derived>"
                + "</grp></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "no error: " + r.errors);
        assertEquals("105", dom.fieldByName("derived").getValue(), "base (5) + 100 via scope ascent");
    }

    @Test
    void resolveNodesSetPredicateSumsRepeatedRows() throws Exception {
        // Set axis [*] over repeated Item subforms, summed in JS — the canonical row-total pattern.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='Item'><occur min='0' max='-1'/>" + numField("amount", null) + "</subform>"
                + numField("Total", "var s=0; var ns=xfa.resolveNodes('Item[*].amount');"
                        + " for (var i=0;i<ns.length;i++) s += ns.item(i).rawValue; s")
                + "</subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Item><amount>10</amount></Item><Item><amount>20</amount></Item>"
                + "<Item><amount>30</amount></Item><Total></Total></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "no error: " + r.errors);
        assertEquals("60", dom.fieldByName("Total").getValue(), "sum of Item[*].amount = 10+20+30");
    }

    @Test
    void scopeSearchPrefersNearestContainer() throws Exception {
        // Two `rate` fields at different depths: one at form1 (far), one in grp beside `derived` (near).
        // The nearest-container rule must pick grp's rate, not the form-level one.
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + numField("rate", null)
                + "<subform name='grp'>" + numField("rate", null)
                + numField("derived", "xfa.resolveNode('rate').rawValue") + "</subform>"
                + "</subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1><rate>1</rate>"
                + "<grp><rate>9</rate><derived></derived></grp></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.calculatesFailed, "no error: " + r.errors);
        assertEquals("9", dom.fieldByName("derived").getValue(), "nearest (grp) rate wins, not form-level");
    }

    @Test
    void thisResolveNodeReadsSiblingFromFieldContext() throws Exception {
        // this.resolveNode("Other") from a field also ascends (the node-method bridge shares the path).
        FormDom dom = merge(form(
                field("Src", null, null, null),
                field("Dst", null, null, null)),
                data("Src", "hello", "Dst", ""));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode dst = host.wrap(dom.fieldByName("Dst").getFormNode());
        assertEquals("hello", host.run("this.resolveNode('Src').rawValue", dst),
                "sibling Src reached from the Dst field context");
    }

    /* ------------------------------ B.4 render flow ------------------------------ */

    @Test
    void computedValuesFlowIntoRenderTrack() throws Exception {
        // initialize seeds a label + a calculate derives a total; both must reach the painter.
        FormDom dom = merge(
                "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                        + fieldWithEvent("Title", "initialize", "this.rawValue = 'INVOICE';")
                        + numField("Qty", null) + numField("Price", null)
                        + numField("Total", "Qty.rawValue * Price.rawValue")
                        + "</subform>"
                        + "<pageSet><pageArea><contentArea w='400pt' h='600pt'/></pageArea></pageSet></template>",
                data("Qty", "6", "Price", "7"));

        XfaScripting.Result sr = XfaScripting.execute(dom);
        assertEquals("INVOICE", dom.fieldByName("Title").getValue());
        assertEquals("42", dom.fieldByName("Total").getValue());

        // the same Form DOM now feeds the render track — the computed values are painted.
        org.aspose.pdf.engine.xfa.model.template.Template tpl =
                (org.aspose.pdf.engine.xfa.model.template.Template) XfaNodeFactory.load(parse(
                "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'/>"
                        + "<pageSet><pageArea><contentArea w='400pt' h='600pt'/></pageArea></pageSet></template>"));
        org.aspose.pdf.Document doc = new org.aspose.pdf.Document();
        org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator.Result pr =
                org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator.paint(doc, dom, tpl);
        assertTrue(pr.texts > 0, "computed/seeded values painted as text; got " + pr.texts);
        assertTrue(sr.valuesProduced >= 1);
        doc.close();
    }

    /* ------------------------------ fixtures ------------------------------ */

    private static String field(String name, String unusedCalcMarker, String calcScript, String validateScript) {
        StringBuilder sb = new StringBuilder("<field name='" + name + "'><ui><textEdit/></ui><value><text/></value>");
        if (calcScript != null) {
            sb.append("<calculate><script contentType='application/x-javascript'>")
              .append(esc(calcScript)).append("</script></calculate>");
        }
        if (validateScript != null) {
            sb.append("<validate><script contentType='application/x-javascript'>")
              .append(esc(validateScript)).append("</script></validate>");
        }
        return sb.append("</field>").toString();
    }

    private static String numField(String name, String calcScript) {
        StringBuilder sb = new StringBuilder("<field name='" + name + "'><ui><numericEdit/></ui><value><decimal/></value>");
        if (calcScript != null) {
            sb.append("<calculate><script contentType='application/x-javascript'>")
              .append(esc(calcScript)).append("</script></calculate>");
        }
        return sb.append("</field>").toString();
    }

    private static String fieldWithEvent(String name, String activity, String script) {
        return "<field name='" + name + "'><ui><textEdit/></ui><value><text/></value>"
                + "<event activity='" + activity + "'><script contentType='application/x-javascript'>"
                + esc(script) + "</script></event></field>";
    }

    private static String form(String... fields) {
        return "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + String.join("", fields) + "</subform></template>";
    }

    private static String data(String... kv) {
        StringBuilder sb = new StringBuilder("<xfa:data xmlns:xfa='" + DATA + "'><form1>");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append('<').append(kv[i]).append('>').append(kv[i + 1])
              .append("</").append(kv[i]).append('>');
        }
        return sb.append("</form1></xfa:data>").toString();
    }

    private static FormDom emptyDom() {
        try {
            return merge(form(field("dummy", null, null, null)), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
