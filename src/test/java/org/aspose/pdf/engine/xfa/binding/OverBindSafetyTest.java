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

/**
 * A4-SAFETY: adversarial tests that try to make the A4-FIX2 last-resort descent bind a
 * WRONG value (a same-named value from an unrelated / index-mismatched node). The oracle
 * is the XFA binding chapter's nearest + index-consistent + never-override-direct rule:
 * the descent is legitimate only when no closer match exists AND it does not cross into a
 * data group claimed by another template subform. "Bind nothing" beats "bind wrong".
 */
public class OverBindSafetyTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private final BindingEngine engine = new BindingEngine();

    /** (1a) decoy same-name inside a data group CLAIMED by another template subform:
     *  the outer field must NOT steal it across the subform boundary. */
    @Test
    void descentDoesNotCrossIntoAnotherSubformsDataGroup() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Code'><value><text/></value></field>"
                + "<subform name='Section'><field name='Value'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Section><Value>V</Value><Code>DECOY</Code></Section>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("V", dom.fieldByPath("form1.Section.Value").getValue());
        String code = dom.fieldByPath("form1.Code").getValue();
        assertTrue(code == null || code.isEmpty(),
                "form1.Code must NOT steal Section.Code via descent; got '" + code + "'");
    }

    /** (1b) legitimate transparent-group descent is preserved (TEST_PATIENT shape):
     *  data nests the value under a group that is NOT a template subform. */
    @Test
    void descentStillBindsThroughTrulyTransparentGroup() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Info'><bind match='none'/><field name='Name'><value><text/></value></field></subform>"
                + "</subform></template>");
        // 'Holder' is not a template subform -> transparent
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Holder><Name>Alice</Name></Holder></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("Alice", dom.fieldByName("Name").getValue(), "transparent-group descent preserved");
    }

    /** (2) a nearer (sibling) match must win over a deeper same-named value. */
    @Test
    void nearerSiblingWinsOverDeepDescent() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='A'><field name='V'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<A></A><V>SIB</V><Deep><V>FAR</V></Deep>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("SIB", dom.fieldByName("V").getValue(), "sibling match wins, descent must not override");
    }

    /** (3) index ambiguity: each repeated instance binds its OWN record by index. */
    @Test
    void repeatedInstancesBindIndexCorrectRecords() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Row'><occur min='0' max='-1'/><field name='Amount'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Row><Amount>10</Amount></Row><Row><Amount>20</Amount></Row><Row><Amount>30</Amount></Row>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("10", dom.fieldByPath("form1.Row[0].Amount").getValue());
        assertEquals("20", dom.fieldByPath("form1.Row[1].Amount").getValue());
        assertEquals("30", dom.fieldByPath("form1.Row[2].Amount").getValue());
    }

    /** (4) direct match always wins over descent (regression lock from A4-FIX2). */
    @Test
    void directMatchWinsOverDescent() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Code'><value><text/></value></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Code>SHALLOW</Code><grp><Code>DEEP</Code></grp></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("SHALLOW", dom.fieldByName("Code").getValue());
    }

    /** (5) cross-row isolation: row 2 must not receive row 1's value via descent. */
    @Test
    void repeatedRowsDoNotCrossBindViaDescent() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='Table'>"
                + "<subform name='Row'><occur min='0' max='-1'/><field name='Code'><value><text/></value></field></subform>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><Table>"
                + "<Row><Code>A</Code></Row><Row><Code>B</Code></Row>"
                + "</Table></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("A", dom.fieldByPath("form1.Table.Row[0].Code").getValue());
        assertEquals("B", dom.fieldByPath("form1.Table.Row[1].Code").getValue());
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
