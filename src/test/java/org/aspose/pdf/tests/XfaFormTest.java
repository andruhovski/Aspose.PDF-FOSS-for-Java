package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.forms.*;
import org.aspose.pdf.forms.xfa.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/// Tests for XFA Forms support: XfaPacketParser, XfaNamespaceContext, XfaForm, and Form integration.
public class XfaFormTest {

    // ── Test XML Data ──

    private static final String TEMPLATE_XML =
        "<template xmlns=\"http://www.xfa.org/schema/xfa-template/3.0/\">" +
        "  <subform name=\"form1\">" +
        "    <subform name=\"Page1\">" +
        "      <field name=\"TextField1\">" +
        "        <ui><textEdit/></ui>" +
        "      </field>" +
        "      <field name=\"CheckBox1\">" +
        "        <ui><checkButton/></ui>" +
        "        <items><integer>1</integer><integer>0</integer></items>" +
        "      </field>" +
        "      <subform name=\"Address\">" +
        "        <field name=\"City\"><ui><textEdit/></ui></field>" +
        "        <field name=\"Zip\"><ui><textEdit/></ui></field>" +
        "      </subform>" +
        "    </subform>" +
        "    <field name=\"TopLevel\"><ui><textEdit/></ui></field>" +
        "  </subform>" +
        "</template>";

    private static final String DATASETS_XML =
        "<xfa:datasets xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">" +
        "  <xfa:data>" +
        "    <form1>" +
        "      <Page1>" +
        "        <TextField1>Hello World</TextField1>" +
        "        <CheckBox1>1</CheckBox1>" +
        "        <Address>" +
        "          <City>Berlin</City>" +
        "          <Zip>10115</Zip>" +
        "        </Address>" +
        "      </Page1>" +
        "      <TopLevel>top value</TopLevel>" +
        "    </form1>" +
        "  </xfa:data>" +
        "</xfa:datasets>";

    private static final String CONFIG_XML =
        "<config xmlns=\"http://www.xfa.org/schema/xci/3.0/\">" +
        "  <present><pdf><version>1.7</version></pdf></present>" +
        "</config>";

    private static final String TEMPLATE_26_XML =
        "<template xmlns=\"http://www.xfa.org/schema/xfa-template/2.6/\">" +
        "  <subform name=\"root\">" +
        "    <field name=\"Field1\"><ui><textEdit/></ui></field>" +
        "  </subform>" +
        "</template>";

    private static final String TEMPLATE_WITH_UNNAMED_SUBFORM =
        "<template xmlns=\"http://www.xfa.org/schema/xfa-template/3.0/\">" +
        "  <subform name=\"form1\">" +
        "    <subform>" +  // unnamed subform
        "      <field name=\"InnerField\"><ui><textEdit/></ui></field>" +
        "    </subform>" +
        "  </subform>" +
        "</template>";

    private static final String DATASETS_WITH_UNNAMED =
        "<xfa:datasets xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">" +
        "  <xfa:data>" +
        "    <form1>" +
        "      <InnerField>inner value</InnerField>" +
        "    </form1>" +
        "  </xfa:data>" +
        "</xfa:datasets>";

    private static final String EMPTY_DATASETS_XML =
        "<xfa:datasets xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">" +
        "  <xfa:data/>" +
        "</xfa:datasets>";

    private static final String XDP_XML =
        "<xdp:xdp xmlns:xdp=\"http://ns.adobe.com/xdp/\">" +
        TEMPLATE_XML +
        DATASETS_XML +
        "</xdp:xdp>";

    // ── Helpers ──

    private PdfDictionary buildXfaAcroForm(String templateXml, String datasetsXml) {
        PdfDictionary acroForm = new PdfDictionary();
        PdfArray fields = new PdfArray();
        acroForm.set(PdfName.of("Fields"), fields);

        PdfArray xfaArray = new PdfArray();

        // "template" packet
        xfaArray.add(new PdfString("template".getBytes(StandardCharsets.UTF_8)));
        PdfStream tplStream = new PdfStream();
        tplStream.setDecodedData(templateXml.getBytes(StandardCharsets.UTF_8));
        xfaArray.add(tplStream);

        // "datasets" packet
        xfaArray.add(new PdfString("datasets".getBytes(StandardCharsets.UTF_8)));
        PdfStream dsStream = new PdfStream();
        dsStream.setDecodedData(datasetsXml.getBytes(StandardCharsets.UTF_8));
        xfaArray.add(dsStream);

        acroForm.set(PdfName.of("XFA"), xfaArray);
        return acroForm;
    }

    private PdfDictionary buildXfaAcroFormWithConfig(String templateXml, String datasetsXml, String configXml) {
        PdfDictionary acroForm = buildXfaAcroForm(templateXml, datasetsXml);
        PdfArray xfaArray = (PdfArray) acroForm.get("XFA");

        xfaArray.add(new PdfString("config".getBytes(StandardCharsets.UTF_8)));
        PdfStream cfgStream = new PdfStream();
        cfgStream.setDecodedData(configXml.getBytes(StandardCharsets.UTF_8));
        xfaArray.add(cfgStream);

        return acroForm;
    }

    private PdfDictionary buildXfaSingleStream(String xdpXml) {
        PdfDictionary acroForm = new PdfDictionary();
        PdfArray fields = new PdfArray();
        acroForm.set(PdfName.of("Fields"), fields);

        PdfStream xfaStream = new PdfStream();
        xfaStream.setDecodedData(xdpXml.getBytes(StandardCharsets.UTF_8));
        acroForm.set(PdfName.of("XFA"), xfaStream);

        return acroForm;
    }

    // ══════════════════════════════════════════════
    // XfaPacketParser Tests
    // ══════════════════════════════════════════════

    @Test
    public void testParseXfaArray_extractsTemplateAndDatasets() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        assertNotNull(parser.getPacket("template"), "template packet should exist");
        assertNotNull(parser.getPacket("datasets"), "datasets packet should exist");
    }

    @Test
    public void testParseXfaStream_singleStream() throws IOException {
        PdfDictionary acroForm = buildXfaSingleStream(XDP_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        assertNotNull(parser.getPacket("template"), "template from single stream");
        assertNotNull(parser.getPacket("datasets"), "datasets from single stream");
    }

    @Test
    public void testParseXfaArray_missingPackets_handledGracefully() throws IOException {
        // Only template, no datasets
        PdfDictionary acroForm = new PdfDictionary();
        PdfArray xfaArray = new PdfArray();
        xfaArray.add(new PdfString("template".getBytes(StandardCharsets.UTF_8)));
        PdfStream tplStream = new PdfStream();
        tplStream.setDecodedData(TEMPLATE_XML.getBytes(StandardCharsets.UTF_8));
        xfaArray.add(tplStream);
        acroForm.set(PdfName.of("XFA"), xfaArray);
        acroForm.set(PdfName.of("Fields"), new PdfArray());

        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));
        assertNotNull(parser.getPacket("template"));
        assertNull(parser.getPacket("datasets"));
    }

    @Test
    public void testGetXDP_assemblesFullDocument() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        org.w3c.dom.Document xdp = parser.getXDP();
        assertNotNull(xdp);
        assertEquals("xdp", xdp.getDocumentElement().getLocalName());
    }

    @Test
    public void testGetAllPackets() throws IOException {
        PdfDictionary acroForm = buildXfaAcroFormWithConfig(TEMPLATE_XML, DATASETS_XML, CONFIG_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        Map<String, org.w3c.dom.Document> all = parser.getAllPackets();
        assertTrue(all.containsKey("template"));
        assertTrue(all.containsKey("datasets"));
        assertTrue(all.containsKey("config"));
    }

    // ══════════════════════════════════════════════
    // XfaNamespaceContext Tests
    // ══════════════════════════════════════════════

    @Test
    public void testNamespaceContext_tplPrefix() throws Exception {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        XfaNamespaceContext ctx = new XfaNamespaceContext(
            parser.getPacket("template"), parser.getPacket("datasets"));

        String uri = ctx.getNamespaceURI("tpl");
        assertTrue(uri.startsWith("http://www.xfa.org/schema/xfa-template/"),
            "tpl should map to template namespace");
    }

    @Test
    public void testNamespaceContext_xfaPrefix() throws Exception {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        XfaNamespaceContext ctx = new XfaNamespaceContext(
            parser.getPacket("template"), parser.getPacket("datasets"));

        String uri = ctx.getNamespaceURI("xfa");
        assertTrue(uri.startsWith("http://www.xfa.org/schema/xfa-data/"),
            "xfa should map to data namespace");
    }

    @Test
    public void testNamespaceContext_detectsTemplateVersion() throws Exception {
        // Template with 2.6 namespace
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_26_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        XfaNamespaceContext ctx = new XfaNamespaceContext(
            parser.getPacket("template"), parser.getPacket("datasets"));

        assertEquals("http://www.xfa.org/schema/xfa-template/2.6/",
            ctx.getNamespaceURI("tpl"), "Should detect 2.6 template version");
    }

    @Test
    public void testNamespaceContext_xdpPrefix() throws Exception {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaPacketParser parser = new XfaPacketParser(acroForm.get("XFA"));

        XfaNamespaceContext ctx = new XfaNamespaceContext(
            parser.getPacket("template"), parser.getPacket("datasets"));

        assertEquals("http://ns.adobe.com/xdp/", ctx.getNamespaceURI("xdp"));
    }

    // ══════════════════════════════════════════════
    // XfaForm Field Access Tests
    // ══════════════════════════════════════════════

    @Test
    public void testGetFieldValue_simpleField() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("Hello World", xfa.get("form1.Page1.TextField1"));
    }

    @Test
    public void testGetFieldValue_withIndex() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("Hello World", xfa.get("form1[0].Page1[0].TextField1[0]"));
    }

    @Test
    public void testGetFieldValue_nestedSubform() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("Berlin", xfa.get("form1.Page1.Address.City"));
    }

    @Test
    public void testGetFieldValue_topLevel() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("top value", xfa.get("form1.TopLevel"));
    }

    @Test
    public void testGetFieldValue_nonExistent_returnsNull() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertNull(xfa.get("form1.Page1.NoSuchField"));
    }

    @Test
    public void testSetFieldValue_existingField() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        xfa.set("form1.Page1.TextField1", "New Value");
        assertEquals("New Value", xfa.get("form1.Page1.TextField1"));
    }

    @Test
    public void testSetFieldValue_createsIntermediateNodes() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        xfa.set("form1.Page1.NewField", "created");
        assertEquals("created", xfa.get("form1.Page1.NewField"));
    }

    @Test
    public void testGetFieldValue_checkbox() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("1", xfa.get("form1.Page1.CheckBox1"));
    }

    @Test
    public void testGetFieldValue_zip() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals("10115", xfa.get("form1.Page1.Address.Zip"));
    }

    // ══════════════════════════════════════════════
    // Field Names Tests
    // ══════════════════════════════════════════════

    @Test
    public void testGetFieldNames_returnsAllFields() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        String[] names = xfa.getFieldNames();
        java.util.List<String> list = java.util.Arrays.asList(names);

        assertTrue(list.contains("form1.Page1.TextField1"), "Should contain TextField1");
        assertTrue(list.contains("form1.Page1.CheckBox1"), "Should contain CheckBox1");
        assertTrue(list.contains("form1.Page1.Address.City"), "Should contain City");
        assertTrue(list.contains("form1.Page1.Address.Zip"), "Should contain Zip");
        assertTrue(list.contains("form1.TopLevel"), "Should contain TopLevel");
    }

    @Test
    public void testGetFieldNames_countCorrect() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertEquals(5, xfa.getFieldNames().length);
    }

    @Test
    public void testGetFieldNames_unnamedSubform_transparent() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_WITH_UNNAMED_SUBFORM, DATASETS_WITH_UNNAMED);
        XfaForm xfa = new XfaForm(acroForm);

        String[] names = xfa.getFieldNames();
        // Unnamed subform should be transparent — field path should be form1.InnerField
        assertEquals(1, names.length);
        assertEquals("form1.InnerField", names[0]);
    }

    // ══════════════════════════════════════════════
    // Template Access Tests
    // ══════════════════════════════════════════════

    @Test
    public void testGetTemplate_returnsDocument() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        org.w3c.dom.Document tpl = xfa.getTemplate();
        assertNotNull(tpl);
        assertEquals("template", tpl.getDocumentElement().getLocalName());
    }

    @Test
    public void testGetDatasets_returnsDocument() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertNotNull(xfa.getDatasets());
    }

    @Test
    public void testGetFieldTemplate_findsFieldNode() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        org.w3c.dom.Node node = xfa.getFieldTemplate("form1.Page1.TextField1");
        assertNotNull(node, "Should find template field node");
        assertEquals("field", node.getLocalName());
        assertEquals("TextField1", ((org.w3c.dom.Element) node).getAttribute("name"));
    }

    @Test
    public void testGetFieldTemplate_returnsNullForMissing() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertNull(xfa.getFieldTemplate("form1.NoField"));
    }

    @Test
    public void testGetFieldTemplate_nestedField() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        org.w3c.dom.Node node = xfa.getFieldTemplate("form1.Page1.Address.City");
        assertNotNull(node, "Should find nested template field");
        assertEquals("City", ((org.w3c.dom.Element) node).getAttribute("name"));
    }

    @Test
    public void testGetNamespaceManager_resolvesTpl() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        NamespaceContext ns = xfa.getNamespaceManager();
        assertNotNull(ns);
        String tplUri = ns.getNamespaceURI("tpl");
        assertNotNull(tplUri);
        assertTrue(tplUri.contains("xfa-template"));
    }

    // ══════════════════════════════════════════════
    // Form Integration Tests
    // ══════════════════════════════════════════════

    @Test
    public void testFormGetType_withXfa_returnsXFA() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        Form form = new Form(acroForm, null, null);

        assertNotEquals(Form.FormType.Standard, form.getType());
    }

    @Test
    public void testFormGetType_withoutXfa_returnsStandard() {
        PdfDictionary acroForm = new PdfDictionary();
        acroForm.set(PdfName.of("Fields"), new PdfArray());
        Form form = new Form(acroForm, null, null);

        assertEquals(Form.FormType.Standard, form.getType());
    }

    @Test
    public void testFormGetXFA_returnsXfaForm() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        Form form = new Form(acroForm, null, null);

        XfaForm xfa = form.getXFA();
        assertNotNull(xfa, "getXFA() should return non-null for XFA form");
    }

    @Test
    public void testFormSetType_standard_removesXfa() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        Form form = new Form(acroForm, null, null);

        assertNotNull(acroForm.get("XFA"), "XFA should exist before setType");
        form.setType(Form.FormType.Standard);

        assertEquals(Form.FormType.Standard, form.getType());
        assertNull(acroForm.get("XFA"), "XFA should be removed after setType(Standard)");
    }

    @Test
    public void testFormGetXFA_noXfa_returnsNull() {
        PdfDictionary acroForm = new PdfDictionary();
        acroForm.set(PdfName.of("Fields"), new PdfArray());
        Form form = new Form(acroForm, null, null);

        assertNull(form.getXFA());
    }

    // ══════════════════════════════════════════════
    // WriteBack Tests
    // ══════════════════════════════════════════════

    @Test
    public void testSetValue_writesBackToCOS() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        xfa.set("form1.Page1.TextField1", "Updated");

        // Re-parse from COS to verify persistence
        XfaForm xfa2 = new XfaForm(acroForm);
        assertEquals("Updated", xfa2.get("form1.Page1.TextField1"));
    }

    // ══════════════════════════════════════════════
    // Edge Cases
    // ══════════════════════════════════════════════

    @Test
    public void testHashSubform_fieldName() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_WITH_UNNAMED_SUBFORM, DATASETS_WITH_UNNAMED);
        XfaForm xfa = new XfaForm(acroForm);

        // #subform segments are skipped in data navigation
        assertEquals("inner value", xfa.get("form1[0].#subform[0].InnerField[0]"));
    }

    @Test
    public void testMixedIndicesAndNone() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // Mixed form: some segments with indices, some without
        assertEquals("Hello World", xfa.get("form1[0].Page1.TextField1[0]"));
    }

    @Test
    public void testEmptyDatasets_getReturnsNull() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, EMPTY_DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertNull(xfa.get("form1.Page1.TextField1"));
    }

    @Test
    public void testConfig_getReturnsDocument() throws IOException {
        PdfDictionary acroForm = buildXfaAcroFormWithConfig(TEMPLATE_XML, DATASETS_XML, CONFIG_XML);
        XfaForm xfa = new XfaForm(acroForm);

        org.w3c.dom.Document config = xfa.getConfig();
        assertNotNull(config, "Config packet should be accessible");
        assertEquals("config", config.getDocumentElement().getLocalName());
    }

    @Test
    public void testXDP_getReturnsDocument() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        org.w3c.dom.Document xdp = xfa.getXDP();
        assertNotNull(xdp, "XDP should be assembled");
        assertEquals("xdp", xdp.getDocumentElement().getLocalName());
    }

    @Test
    public void testSetFieldImage() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        byte[] imageData = {(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header bytes
        xfa.setFieldImage("form1.Page1.TextField1", new ByteArrayInputStream(imageData));

        String val = xfa.get("form1.Page1.TextField1");
        assertNotNull(val);
        // Verify it's base64 encoded
        byte[] decoded = java.util.Base64.getDecoder().decode(val);
        assertArrayEquals(imageData, decoded);
    }

    @Test
    public void testGetFieldValue_nullFieldName() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        assertNull(xfa.get(null));
        assertNull(xfa.get(""));
    }

    @Test
    public void testSomParsing_viaGetWithIndices() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_XML, DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // Indexed access should resolve correctly
        assertEquals("Berlin", xfa.get("form1[0].Page1[0].Address[0].City[0]"));
    }

    @Test
    public void testSomParsing_hashSubformSkipped() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_WITH_UNNAMED_SUBFORM, DATASETS_WITH_UNNAMED);
        XfaForm xfa = new XfaForm(acroForm);

        // #subform is skipped in data navigation, just pass through
        assertEquals("inner value", xfa.get("form1.#subform[0].InnerField"));
    }

    // ══════════════════════════════════════════════
    // Repeated-instance set/get (indexed rows)
    // ══════════════════════════════════════════════

    /// A repeated row subform bound via a renamed MULTI-COMPONENT dataRef (the common SAP/LiveCycle
    ///  table shape: "body" rows stored as IM\_ITEMS/DATA groups).
    private static final String TEMPLATE_TABLE_XML =
        "<template xmlns=\"http://www.xfa.org/schema/xfa-template/3.0/\">" +
        "  <subform name=\"data\">" +
        "    <subform name=\"item_table\">" +
        "      <bind match=\"none\"/>" +
        "      <subform name=\"body\">" +
        "        <occur min=\"1\" max=\"-1\"/>" +
        "        <bind match=\"dataRef\" ref=\"$.IM_ITEMS.DATA[*]\"/>" +
        "        <field name=\"EBELP\"><ui><textEdit/></ui>" +
        "          <bind match=\"dataRef\" ref=\"$.EBELP\"/>" +
        "        </field>" +
        "      </subform>" +
        "    </subform>" +
        "  </subform>" +
        "</template>";

    private static final String DATASETS_TABLE_XML =
        "<xfa:datasets xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">" +
        "  <xfa:data>" +
        "    <data>" +
        "      <IM_ITEMS>" +
        "        <DATA><EBELP>0010</EBELP></DATA>" +
        "        <DATA><EBELP>0020</EBELP></DATA>" +
        "      </IM_ITEMS>" +
        "    </data>" +
        "  </xfa:data>" +
        "</xfa:datasets>";

    /// A repeated subform whose rows are absent from the (sparse) data entirely.
    private static final String TEMPLATE_ROWS_XML =
        "<template xmlns=\"http://www.xfa.org/schema/xfa-template/3.0/\">" +
        "  <subform name=\"form1\">" +
        "    <subform name=\"Zaznam\">" +
        "      <occur min=\"1\" max=\"-1\" initial=\"3\"/>" +
        "      <field name=\"cislo\"><ui><textEdit/></ui></field>" +
        "    </subform>" +
        "  </subform>" +
        "</template>";

    @Test
    public void testSetIndexedRow_renamedBoundPath_writesRealRowNode() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_TABLE_XML, DATASETS_TABLE_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // both rows resolve through the renamed IM_ITEMS/DATA groups
        assertEquals("0010", xfa.get("data.item_table.body[0].EBELP"));
        assertEquals("0020", xfa.get("data.item_table.body[1].EBELP"));

        // writing row 1 must hit the SECOND DATA group, not collapse onto row 0
        xfa.set("data.item_table.body[1].EBELP", "row-two");
        assertEquals("0010", xfa.get("data.item_table.body[0].EBELP"));
        assertEquals("row-two", xfa.get("data.item_table.body[1].EBELP"));
    }

    @Test
    public void testSetIndexedRow_beyondExisting_padsBoundInstances() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_TABLE_XML, DATASETS_TABLE_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // row 3 does not exist (data has 2 rows): DATA groups are padded and the write lands
        // at its own position, leaving existing rows intact
        xfa.set("data.item_table.body[3].EBELP", "row-four");
        assertEquals("0010", xfa.get("data.item_table.body[0].EBELP"));
        assertEquals("0020", xfa.get("data.item_table.body[1].EBELP"));
        assertEquals("row-four", xfa.get("data.item_table.body[3].EBELP"));
    }

    @Test
    public void testSetIndexedRows_sparseData_noCollapse() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_ROWS_XML, EMPTY_DATASETS_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // no data rows exist at all; each explicitly indexed write must keep its own instance
        xfa.set("form1.Zaznam[0].cislo", "one");
        xfa.set("form1.Zaznam[1].cislo", "two");
        xfa.set("form1.Zaznam[2].cislo", "three");
        assertEquals("one", xfa.get("form1.Zaznam[0].cislo"));
        assertEquals("two", xfa.get("form1.Zaznam[1].cislo"));
        assertEquals("three", xfa.get("form1.Zaznam[2].cislo"));
    }

    @Test
    public void testGetIndexedRow_missingInstance_returnsNullNotOtherRow() throws IOException {
        PdfDictionary acroForm = buildXfaAcroForm(TEMPLATE_TABLE_XML, DATASETS_TABLE_XML);
        XfaForm xfa = new XfaForm(acroForm);

        // only 2 rows exist; an explicitly indexed missing instance must NOT fall back to row 0
        assertNull(xfa.get("data.item_table.body[5].EBELP"));
    }
}
