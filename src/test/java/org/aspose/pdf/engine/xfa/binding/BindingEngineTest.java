package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// A4.2/A4.3/A4.4: occur expansion, empty merge, the four match modes, Form DOM API.
public class BindingEngineTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    private final BindingEngine engine = new BindingEngine();

    /* ----------------------------- A4.2 occur ----------------------- */

    @Test
    void emptyMergeUsesOccurInitialClamped() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='row'><occur initial='3' min='0' max='-1'/>"
                + "<field name='c'/></subform></subform></template>");
        FormDom dom = engine.mergeEmpty(tpl);
        XfaNode root = dom.getRoot();
        assertEquals(3, root.getChildren("subform").size(), "empty merge expands to initial=3");
        assertTrue(dom.isEmptyMerge());
    }

    @Test
    void occurClampsToMinAndMax() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='row'><occur initial='1' min='2' max='5'/><field name='c'/></subform>"
                + "</subform></template>");
        // empty merge: initial=1 but min=2 -> 2
        assertEquals(2, engine.mergeEmpty(tpl).getRoot().getChildren("subform").size());
    }

    @Test
    void dataMergeExpandsToRecordCount() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Detail'><occur initial='1' min='0' max='-1'/>"
                + "<field name='Price'/></subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Detail><Price>10</Price></Detail>"
                + "<Detail><Price>20</Price></Detail>"
                + "<Detail><Price>30</Price></Detail></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals(3, dom.getRoot().getChildren("subform").size(), "one instance per data record");
        assertEquals("10", dom.fieldByPath("form1.Detail[0].Price").getValue());
        assertEquals("30", dom.fieldByPath("form1.Detail[2].Price").getValue());
    }

    /* -------------------------- A4.3 empty merge -------------------- */

    @Test
    void emptyMergeAppliesTemplateDefaultsNoData() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Greeting'><value><text>Hello</text></value>"
                + "<ui><textEdit/></ui></field></subform></template>");
        FormDom dom = engine.mergeEmpty(tpl);
        FormField f = dom.fieldByName("Greeting");
        assertNotNull(f);
        assertEquals("Hello", f.getValue(), "template default value");
        assertEquals("textEdit", f.getUiType());
        assertEquals(FormField.BindingKind.UNBOUND, f.getKind());
    }

    /* -------------------------- A4.3 match modes ------------------- */

    @Test
    void matchOnceBindsFirstMatchingDataNode() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Name'><value><text/></value></field></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Name>Alice</Name></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField f = dom.fieldByName("Name");
        assertEquals("Alice", f.getValue());
        assertEquals(FormField.BindingKind.ONCE, f.getKind());
    }

    @Test
    void matchDataRefBindsViaSom() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Display'><bind match='dataRef' ref='$data.form1.Real'/>"
                + "<value><text/></value></field></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Real>FromRef</Real></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField f = dom.fieldByName("Display");
        assertEquals("FromRef", f.getValue());
        assertEquals(FormField.BindingKind.DATAREF, f.getKind());
    }

    @Test
    void matchGlobalBindsGloballyNamedNode() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='inner'>"
                + "<field name='CompanyName'><bind match='global'/><value><text/></value></field>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<CompanyName>Acme</CompanyName></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField f = dom.fieldByName("CompanyName");
        assertEquals("Acme", f.getValue(), "global finds the node regardless of nesting");
        assertEquals(FormField.BindingKind.GLOBAL, f.getKind());
    }

    @Test
    void matchNoneIsTemplateOnly() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Calc'><bind match='none'/><value><text>default</text></value></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Calc>shouldBeIgnored</Calc></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField f = dom.fieldByName("Calc");
        assertEquals("default", f.getValue(), "match=none ignores data");
        assertEquals(FormField.BindingKind.NONE, f.getKind());
    }

    /* -------------------------- A4.4 Form DOM API ------------------ */

    @Test
    void formDomEnumeratesFieldsWithValuesAndUi() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='A'><value><text/></value><ui><textEdit/></ui></field>"
                + "<field name='B'><value><text/></value><ui><numericEdit/></ui></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<A>x</A><B>42</B></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals(2, dom.getFields().size());
        assertEquals("x", dom.fieldByPath("form1.A").getValue());
        assertEquals("numericEdit", dom.fieldByPath("form1.B").getUiType());
        assertTrue(dom.getDeferredScriptPredicates().isEmpty());
    }

    @Test
    void scriptPredicateInDataRefIsFlaggedNotEvaluated() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='F'><bind match='dataRef' ref='$data.form1.Item[Sum(x) &gt; 0]'/>"
                + "<value><text/></value></field></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1/></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertFalse(dom.getDeferredScriptPredicates().isEmpty(), "script predicate flagged");
        assertTrue(dom.getDeferredScriptPredicates().get(0).contains("Sum"));
    }

    @Test
    void choiceItemsCarried() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Color'><ui><choiceList/></ui>"
                + "<items><text>Red</text><text>Green</text><text>Blue</text></items>"
                + "<value><text/></value></field></subform></template>");
        FormDom dom = engine.mergeEmpty(tpl);
        FormField f = dom.fieldByName("Color");
        assertEquals(3, f.getItems().size());
        assertEquals("Green", f.getItems().get(1));
    }

    /* ----------------------------- helpers -------------------------- */

    private static Template tpl(String xml) throws Exception {
        return (Template) XfaNodeFactory.load(parse(xml));
    }

    /// Parses a full `<xfa:data>...</xfa:data>` string into its typed data root.
    private static XfaNode data(String xml) throws Exception {
        return XfaNodeFactory.load(parse(xml));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
