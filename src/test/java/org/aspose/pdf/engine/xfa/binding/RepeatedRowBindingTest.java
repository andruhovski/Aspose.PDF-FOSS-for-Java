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

/// A4-FIX2: repeated-row / repeated-subform record binding + residual deep dataRef.
public class RepeatedRowBindingTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private final BindingEngine engine = new BindingEngine();

    /// (A) occur-based rows: one Row instance per data record, same-named cells distinct.
    @Test
    void occurRowsBindEachRecordDistinctly() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Table'>"
                + "  <subform name='Row'><occur min='0' max='-1'/>"
                + "    <field name='Code'><value><text/></value></field>"
                + "    <field name='Amount'><value><text/></value></field>"
                + "  </subform>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><Table>"
                + "<Row><Code>A</Code><Amount>10</Amount></Row>"
                + "<Row><Code>B</Code><Amount>20</Amount></Row>"
                + "<Row><Code>C</Code><Amount>30</Amount></Row>"
                + "</Table></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("A", dom.fieldByPath("form1.Table.Row[0].Code").getValue());
        assertEquals("10", dom.fieldByPath("form1.Table.Row[0].Amount").getValue());
        assertEquals("B", dom.fieldByPath("form1.Table.Row[1].Code").getValue());
        assertEquals("C", dom.fieldByPath("form1.Table.Row[2].Code").getValue());
        assertEquals("30", dom.fieldByPath("form1.Table.Row[2].Amount").getValue());
    }

    /// (B) explicit repeated same-named Cell subforms, each one field, paired by order.
    @Test
    void explicitRepeatedCellsPairByIndex() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='Table'>"
                + "<subform name='Cell'><field name='NAICS'><value><text/></value></field></subform>"
                + "<subform name='Cell'><field name='Website'><value><text/></value></field></subform>"
                + "<subform name='Cell'><field name='Province'><value><text/></value></field></subform>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><Table>"
                + "<Cell><NAICS>211113</NAICS></Cell>"
                + "<Cell><Website>example.com</Website></Cell>"
                + "<Cell><Province>AB</Province></Cell>"
                + "</Table></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("211113", dom.fieldByName("NAICS").getValue());
        assertEquals("example.com", dom.fieldByName("Website").getValue());
        assertEquals("AB", dom.fieldByName("Province").getValue());
    }

    /// (C) template inserts a Row subform the data lacks (ancestor) AND repeats it — cells
    ///  must bind to the flat data Cells in order. (sampleFile Page3 shape.)
    @Test
    void repeatedRowWrapperAncestorMatchesFlatCells() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'><subform name='Table'>"
                + "<subform name='Row'>"
                + "  <subform name='Cell'><field name='A'><value><text/></value></field></subform>"
                + "  <subform name='Cell'><field name='B'><value><text/></value></field></subform>"
                + "</subform>"
                + "<subform name='Row'>"
                + "  <subform name='Cell'><field name='C'><value><text/></value></field></subform>"
                + "  <subform name='Cell'><field name='D'><value><text/></value></field></subform>"
                + "</subform>"
                + "</subform></subform></template>");
        // data has Cells directly under Table (no Row level)
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1><Table>"
                + "<Cell><A>1</A></Cell><Cell><B>2</B></Cell>"
                + "<Cell><C>3</C></Cell><Cell><D>4</D></Cell>"
                + "</Table></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("1", dom.fieldByName("A").getValue());
        assertEquals("2", dom.fieldByName("B").getValue());
        assertEquals("3", dom.fieldByName("C").getValue());
        assertEquals("4", dom.fieldByName("D").getValue());
    }

    /// (E) deep dataRef root accessor (TEST\_PATIENT root.InputConn.\* / poland data.X.Y shape).
    @Test
    void deepDataRefRootAccessor() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='root'>"
                + "<subform name='wrap'><bind match='none'/>"
                + "  <field name='ClientName'><bind match='dataRef' ref='$record.InputConn.ClientName'/><value><text/></value></field>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><root>"
                + "<InputConn><ClientName>CLIENT TEST</ClientName></InputConn></root></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("CLIENT TEST", dom.fieldByName("ClientName").getValue());
    }

    /// (F) same-named field in two different data groups -> distinct nodes (spec non-unique names).
    @Test
    void sameNamedFieldsInDifferentGroupsBindDistinct() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Buyer'><field name='Name'><value><text/></value></field></subform>"
                + "<subform name='Seller'><field name='Name'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Buyer><Name>Alice</Name></Buyer><Seller><Name>Bob</Name></Seller>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("Alice", dom.fieldByPath("form1.Buyer.Name").getValue());
        assertEquals("Bob", dom.fieldByPath("form1.Seller.Name").getValue());
    }

    /// (G) record nested below an extra wrapper data group (poland shape): the root
    ///  subform name is a grandchild of the datasets data node, and relative dataRefs
    ///  must resolve against the descended record.
    @Test
    void recordNestedUnderWrapperGroupBinds() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='Deklaracja'>"
                + "<subform name='Strona1'><bind match='none'/>"
                + "  <subform name='Naglowek'><bind match='dataRef' ref='$.Naglowek'/>"
                + "    <field name='KodFormularza'><bind match='dataRef' ref='$.KodFormularza'/><value><text/></value></field>"
                + "  </subform>"
                + "</subform></subform></template>");
        // datasets nests an extra <wrapper> level above Deklaracja
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><wrapper>"
                + "<Deklaracja><Naglowek><KodFormularza>PIT-8C</KodFormularza></Naglowek></Deklaracja>"
                + "</wrapper></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("PIT-8C", dom.fieldByName("KodFormularza").getValue(),
                "record found below the wrapper; $.Naglowek/$.KodFormularza resolve against it");
    }

    /// (H) data encloses the value in extra groups the template does not mirror
    ///  (TEST\_PATIENT: template form1/ClientInformation, data root/InputConn/ClientName).
    ///  An automatic field must descend through the unmatched groups as a last resort.
    @Test
    void automaticFieldDescendsThroughUnmatchedDataGroups() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='ClientInformation'><bind match='none'/>"
                + "  <field name='ClientName'><value><text/></value></field>"
                + "  <field name='DocumentKey'><value><text/></value></field>"
                + "</subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><root>"
                + "<InputConn><ClientName>CLIENT TEST</ClientName><DocumentKey>126188</DocumentKey></InputConn>"
                + "</root></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("CLIENT TEST", dom.fieldByName("ClientName").getValue());
        assertEquals("126188", dom.fieldByName("DocumentKey").getValue());
    }

    /// (I) descent is LAST RESORT: a direct/ancestor match still wins over a deeper
    ///  same-named value, and descent does not double-consume.
    @Test
    void directMatchStillWinsOverDeepDescent() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<field name='Code'><value><text/></value></field>"
                + "<subform name='extra'><bind match='none'/><field name='Note'><value><text/></value></field></subform>"
                + "</subform></template>");
        // Code has a direct value at form1 level; Note only exists deep under grp
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Code>SHALLOW</Code><grp><Code>DEEP</Code><Note>N1</Note></grp>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("SHALLOW", dom.fieldByName("Code").getValue(), "direct match wins, not the deep Code");
        assertEquals("N1", dom.fieldByName("Note").getValue(), "Note found by descent");
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
