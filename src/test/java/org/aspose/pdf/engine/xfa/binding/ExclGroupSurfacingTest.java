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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// A4-EXCL: an exclGroup surfaces its bound selection value + chosen option; no double-count.
public class ExclGroupSurfacingTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private final BindingEngine engine = new BindingEngine();

    private static final String OPTIONS =
            "<field name='o1'><value><text>ja</text></value></field>"
          + "<field name='o2'><value><text>nein</text></value></field>"
          + "<field name='o3'><value><text>vielleicht</text></value></field>";

    /// data selects option 2 (nein) -> exclGroup reports nein and identifies option index 1.
    @Test
    void exclGroupSurfacesSelectedValueAndOption() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='TF_Haus'>"
                + "<exclGroup name='Haus'>" + OPTIONS + "</exclGroup>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><TF_Haus>"
                + "<Haus>nein</Haus></TF_Haus></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField g = dom.fieldByName("Haus");
        assertEquals("nein", g.getValue(), "exclGroup surfaces the bound selection value");
        assertEquals("exclGroup", g.getUiType());
        assertEquals(FormField.BindingKind.ONCE, g.getKind());
        assertEquals(3, g.getItems().size(), "all option on-values present");
        assertEquals(1, g.getSelectedItemIndex(), "option 2 (nein) chosen");
    }

    /// no data -> no value, no phantom selection.
    @Test
    void exclGroupWithNoDataReportsNoSelection() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='TF_Haus'>"
                + "<exclGroup name='Haus'>" + OPTIONS + "</exclGroup>"
                + "</subform></subform></template>");
        FormDom dom = engine.mergeEmpty(tpl);
        FormField g = dom.fieldByName("Haus");
        assertNull(g.getValue(), "no data -> no selection value");
        assertEquals(-1, g.getSelectedItemIndex(), "no phantom selection");
        assertEquals(FormField.BindingKind.UNBOUND, g.getKind());
    }

    /// template default selection + no data -> reports the default.
    @Test
    void exclGroupReportsTemplateDefaultWhenNoData() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='TF_Haus'>"
                + "<exclGroup name='Haus'><value><text>ja</text></value>" + OPTIONS + "</exclGroup>"
                + "</subform></subform></template>");
        FormDom dom = engine.mergeEmpty(tpl);
        FormField g = dom.fieldByName("Haus");
        assertEquals("ja", g.getValue(), "template default selection");
        assertEquals(0, g.getSelectedItemIndex(), "default maps to option 1 (ja)");
    }

    /// enumeration: exclGroup + 3 option fields -> ONE selectable value, not four.
    @Test
    void exclGroupDoesNotDoubleCountOptions() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='TF_Haus'>"
                + "<exclGroup name='Haus'>" + OPTIONS + "</exclGroup>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><TF_Haus>"
                + "<Haus>nein</Haus></TF_Haus></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        int haus = 0, options = 0;
        for (FormField f : dom.getFields()) {
            if ("Haus".equals(f.getName())) haus++;
            if ("o1".equals(f.getName()) || "o2".equals(f.getName()) || "o3".equals(f.getName())) options++;
        }
        assertEquals(1, haus, "one Form DOM value for the exclGroup");
        assertEquals(0, options, "option fields are NOT enumerated as separate scalar fields");
        assertEquals(1, dom.getFields().size(), "exactly one selectable value total");
    }

    /// the option structure is kept intact in the Form DOM (for A5 radio mapping).
    @Test
    void exclGroupKeepsOptionChildrenInFormDom() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='TF_Haus'>"
                + "<exclGroup name='Haus'>" + OPTIONS + "</exclGroup>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><TF_Haus>"
                + "<Haus>nein</Haus></TF_Haus></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField g = dom.fieldByName("Haus");
        assertTrue(g.getFormNode().getChildren("field").size() == 3, "3 option fields kept under the exclGroup");
    }

    /* helpers */
    private static Template tpl(String xml) throws Exception {
        return (Template) XfaNodeFactory.load(parse(xml));
    }
    private static XfaNode data(String xml) throws Exception {
        return XfaNodeFactory.load(parse(xml));
    }
    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
