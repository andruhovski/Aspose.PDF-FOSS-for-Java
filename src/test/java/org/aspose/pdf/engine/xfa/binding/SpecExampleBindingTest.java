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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// A5.0 verification gate: reproduces the XFA 3.0 specification's documented
/// data-binding worked examples and asserts the merged [FormDom] node-for-node
/// against the documented Form DOM result. Unlike [BindingEngineTest] (which
/// uses synthetic fixtures whose data parallels the template), these fixtures are
/// the spec's own examples — the oracle the A4 merge must reproduce before any
/// flattening is built on top of it.
///
/// References are to _XFA Specification 3.0_, chapter "Basic Data Binding
/// to Produce the XFA Form DOM" (pp.170-206) and chapter 3 "Object Models in XFA"
/// (the Receipt example, pp.86-89).
public class SpecExampleBindingTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    private final BindingEngine engine = new BindingEngine();

    /* ===================================================================
     * Example 3.4 / 3.5 / 3.6 — the Receipt example (Chapter 3).
     * Two Detail line-items (occur-expanded) bound directly by name, the
     * Receipt-level Sub_Total/Tax/Total_Price bound directly, and a
     * Final_Price field bound by dataRef SOM to $data.Receipt.Total_Price
     * (the grand total, 334.80).
     * =================================================================== */
    @Test
    void receiptExampleDirectAndDataRef() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'>"
                + "<subform name='Receipt'>"
                + "  <subform name='Detail'><occur min='0' max='-1'/>"
                + "     <field name='Description'><value><text/></value></field>"
                + "     <field name='Units'><value><text/></value></field>"
                + "     <field name='Unit_Price'><value><text/></value></field>"
                + "     <field name='Total_Price'><value><text/></value></field>"
                + "  </subform>"
                + "  <field name='Sub_Total'><value><text/></value></field>"
                + "  <field name='Tax'><value><text/></value></field>"
                + "  <field name='Total_Price'><value><text/></value></field>"
                + "  <field name='Final_Price'><bind match='dataRef' ref='$data.Receipt.Total_Price'/>"
                + "     <value><text/></value></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<Receipt>"
                + "  <Detail><Description>Giant Slingshot</Description><Units>1</Units>"
                + "    <Unit_Price>250.00</Unit_Price><Total_Price>250.00</Total_Price></Detail>"
                + "  <Detail><Description>Road Runner Bait, large bag</Description><Units>5</Units>"
                + "    <Unit_Price>12.00</Unit_Price><Total_Price>60.00</Total_Price></Detail>"
                + "  <Sub_Total>310.00</Sub_Total><Tax>24.80</Tax><Total_Price>334.80</Total_Price>"
                + "</Receipt></xfa:data>");

        FormDom dom = engine.merge(tpl, data);

        // occur expansion: exactly two Detail instances (one per data record)
        assertEquals(2, dom.getRoot().getChildren("subform").size(), "two Detail line-items");

        // Detail[0]
        assertEquals("Giant Slingshot", dom.fieldByPath("Receipt.Detail[0].Description").getValue());
        assertEquals("1", dom.fieldByPath("Receipt.Detail[0].Units").getValue());
        assertEquals("250.00", dom.fieldByPath("Receipt.Detail[0].Unit_Price").getValue());
        assertEquals("250.00", dom.fieldByPath("Receipt.Detail[0].Total_Price").getValue());
        // Detail[1]
        assertEquals("Road Runner Bait, large bag", dom.fieldByPath("Receipt.Detail[1].Description").getValue());
        assertEquals("5", dom.fieldByPath("Receipt.Detail[1].Units").getValue());
        assertEquals("12.00", dom.fieldByPath("Receipt.Detail[1].Unit_Price").getValue());
        assertEquals("60.00", dom.fieldByPath("Receipt.Detail[1].Total_Price").getValue());
        // Receipt-level direct matches
        assertEquals("310.00", dom.fieldByPath("Receipt.Sub_Total").getValue());
        assertEquals("24.80", dom.fieldByPath("Receipt.Tax").getValue());
        assertEquals("334.80", dom.fieldByPath("Receipt.Total_Price").getValue());
        // Example 3.6: dataRef to the grand total (different field name -> data node)
        FormField fp = dom.fieldByPath("Receipt.Final_Price");
        assertNotNull(fp, "Final_Price field present");
        assertEquals("334.80", fp.getValue(), "dataRef binds to $data.Receipt.Total_Price");
        assertEquals(FormField.BindingKind.DATAREF, fp.getKind());
    }

    /* ===================================================================
     * Example 4.49 / 4.50 — Simple Example of Data Binding (registration).
     * Flat template, flat data, names match one-for-one (direct match).
     * The documented Form DOM binds all seven fields, including apt="".
     * =================================================================== */
    @Test
    void registrationSimpleDirectMatch() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'>"
                + "<subform name='registration'>"
                + "  <field name='first'><value><text/></value></field>"
                + "  <field name='last'><value><text/></value></field>"
                + "  <field name='apt'><value><text/></value></field>"
                + "  <field name='street'><value><text/></value></field>"
                + "  <field name='city'><value><text/></value></field>"
                + "  <field name='country'><value><text/></value></field>"
                + "  <field name='postalcode'><value><text/></value></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<registration>"
                + "  <first>Jack</first><last>Spratt</last><apt></apt>"
                + "  <street>99 Candlestick Lane</street><city>London</city>"
                + "  <country>UK</country><postalcode>SW1</postalcode>"
                + "</registration></xfa:data>");

        FormDom dom = engine.merge(tpl, data);
        assertEquals("Jack", dom.fieldByPath("registration.first").getValue());
        assertEquals("Spratt", dom.fieldByPath("registration.last").getValue());
        assertEquals("", dom.fieldByPath("registration.apt").getValue(), "empty apt binds to empty string");
        assertEquals("99 Candlestick Lane", dom.fieldByPath("registration.street").getValue());
        assertEquals("London", dom.fieldByPath("registration.city").getValue());
        assertEquals("UK", dom.fieldByPath("registration.country").getValue());
        assertEquals("SW1", dom.fieldByPath("registration.postalcode").getValue());
        assertEquals(FormField.BindingKind.ONCE, dom.fieldByPath("registration.first").getKind());
    }

    /* ===================================================================
     * Example 4.51 / 4.52 — same flat data, template with the address fields
     * moved into an `address` subform.  The spec mandates ANCESTOR/SCOPE
     * matching: apt/street/city/country/postalcode data values (children of
     * registration) bind to the like-named fields inside address.
     * Documented Form DOM: registration.address.{apt="",street=...,city=...}.
     * =================================================================== */
    @Test
    void registrationAncestorScopeMatch() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'>"
                + "<subform name='registration'>"
                + "  <field name='first'><value><text/></value></field>"
                + "  <field name='last'><value><text/></value></field>"
                + "  <subform name='address'>"
                + "     <field name='apt'><value><text/></value></field>"
                + "     <field name='street'><value><text/></value></field>"
                + "     <field name='city'><value><text/></value></field>"
                + "     <field name='country'><value><text/></value></field>"
                + "     <field name='postalcode'><value><text/></value></field>"
                + "  </subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<registration>"
                + "  <first>Jack</first><last>Spratt</last><apt></apt>"
                + "  <street>99 Candlestick Lane</street><city>London</city>"
                + "  <country>UK</country><postalcode>SW1</postalcode>"
                + "</registration></xfa:data>");

        FormDom dom = engine.merge(tpl, data);
        assertEquals("Jack", dom.fieldByPath("registration.first").getValue());
        assertEquals("Spratt", dom.fieldByPath("registration.last").getValue());
        // Ancestor/scope match (spec p.180-181): address fields bind to registration-level data.
        assertEquals("", dom.fieldByPath("registration.address.apt").getValue());
        assertEquals("99 Candlestick Lane", dom.fieldByPath("registration.address.street").getValue());
        assertEquals("London", dom.fieldByPath("registration.address.city").getValue());
        assertEquals("UK", dom.fieldByPath("registration.address.country").getValue());
        assertEquals("SW1", dom.fieldByPath("registration.address.postalcode").getValue());
    }

    /* ===================================================================
     * Example 4.53 — sibling match. Template encloses ALL fields in
     * address; the data has first/last OUTSIDE the address data group.
     * Documented Form DOM: address.first="Jack", address.last="Spratt"
     * bind via sibling match; address.{apt,street,city} bind directly.
     * =================================================================== */
    @Test
    void registrationSiblingMatch() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'>"
                + "<subform name='registration'>"
                + "  <subform name='address'>"
                + "     <field name='first'><value><text/></value></field>"
                + "     <field name='last'><value><text/></value></field>"
                + "     <field name='apt'><value><text/></value></field>"
                + "     <field name='street'><value><text/></value></field>"
                + "     <field name='city'><value><text/></value></field>"
                + "  </subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<registration>"
                + "  <first>Jack</first><last>Spratt</last>"
                + "  <address><apt>7</apt><street>99 Candlestick Lane</street><city>London</city></address>"
                + "</registration></xfa:data>");

        FormDom dom = engine.merge(tpl, data);
        // sibling match (spec p.183): first/last (siblings of address data group) bind to address fields
        assertEquals("Jack", dom.fieldByPath("registration.address.first").getValue());
        assertEquals("Spratt", dom.fieldByPath("registration.address.last").getValue());
        // direct matches inside the address data group
        assertEquals("7", dom.fieldByPath("registration.address.apt").getValue());
        assertEquals("99 Candlestick Lane", dom.fieldByPath("registration.address.street").getValue());
        assertEquals("London", dom.fieldByPath("registration.address.city").getValue());
    }

    /* ===================================================================
     * match=global (spec p.174) and match=none — re-asserted against the
     * spec's documented semantics (global binds outside the local context;
     * none keeps the template value and ignores data).
     * =================================================================== */
    @Test
    void matchGlobalAndNonePerSpec() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'>"
                + "<subform name='form1'>"
                + "  <subform name='inner'>"
                + "    <field name='CompanyName'><bind match='global'/><value><text/></value></field>"
                + "  </subform>"
                + "  <field name='Calc'><bind match='none'/><value><text>fromTemplate</text></value></field>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<form1><CompanyName>Acme</CompanyName><Calc>fromData</Calc></form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        FormField company = dom.fieldByName("CompanyName");
        assertEquals("Acme", company.getValue(), "global binds across nesting");
        assertEquals(FormField.BindingKind.GLOBAL, company.getKind());
        FormField calc = dom.fieldByName("Calc");
        assertEquals("fromTemplate", calc.getValue(), "match=none ignores data");
        assertEquals(FormField.BindingKind.NONE, calc.getKind());
    }

    /* ----------------------------- helpers -------------------------- */

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
