package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.xmp.XmpNamespaceRegistry;
import org.aspose.pdf.engine.xmp.XmpParser;
import org.aspose.pdf.engine.xmp.XmpProperty;
import org.aspose.pdf.engine.xmp.XmpWriter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for XMP metadata parsing, writing, and Document integration.
public class XmpMetadataTest {

    // ── Parser tests ──

    @Test
    public void testParseSimpleProperties() {
        String xmp = buildXmp(
                "<xmp:CreatorTool>TestTool</xmp:CreatorTool>\n" +
                "<pdf:Producer>TestProducer</pdf:Producer>\n" +
                "<xmp:Rating>5</xmp:Rating>");

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        assertEquals("TestTool", props.get("xmp:CreatorTool").getValue());
        assertEquals("TestProducer", props.get("pdf:Producer").getValue());
        assertEquals("5", props.get("xmp:Rating").getValue());
    }

    @Test
    public void testParseLangAlt() {
        String xmp = buildXmp(
                "<dc:title>\n" +
                "  <rdf:Alt>\n" +
                "    <rdf:li xml:lang=\"x-default\">My Title</rdf:li>\n" +
                "    <rdf:li xml:lang=\"en-US\">English Title</rdf:li>\n" +
                "  </rdf:Alt>\n" +
                "</dc:title>");

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        XmpProperty title = props.get("dc:title");
        assertNotNull(title);
        assertEquals(XmpProperty.ValueType.LANG_ALT, title.getType());
        assertEquals("My Title", title.getValue()); // x-default
        assertEquals(2, title.getLangAltEntries().size());
    }

    @Test
    public void testParseOrderedArray() {
        String xmp = buildXmp(
                "<dc:creator>\n" +
                "  <rdf:Seq>\n" +
                "    <rdf:li>Author One</rdf:li>\n" +
                "    <rdf:li>Author Two</rdf:li>\n" +
                "  </rdf:Seq>\n" +
                "</dc:creator>");

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        XmpProperty creator = props.get("dc:creator");
        assertNotNull(creator);
        assertEquals(XmpProperty.ValueType.SEQ, creator.getType());
        assertEquals("Author One", creator.getValue());
        assertEquals(2, creator.getArrayItems().size());
    }

    @Test
    public void testParseUnorderedArray() {
        String xmp = buildXmp(
                "<dc:subject>\n" +
                "  <rdf:Bag>\n" +
                "    <rdf:li>keyword1</rdf:li>\n" +
                "    <rdf:li>keyword2</rdf:li>\n" +
                "    <rdf:li>keyword3</rdf:li>\n" +
                "  </rdf:Bag>\n" +
                "</dc:subject>");

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        XmpProperty subject = props.get("dc:subject");
        assertNotNull(subject);
        assertEquals(XmpProperty.ValueType.BAG, subject.getType());
        assertEquals(3, subject.getArrayItems().size());
    }

    @Test
    public void testParseAttributeShorthand() {
        String xmp = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "<rdf:Description rdf:about=\"\"\n" +
                "  xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                "  xmp:Rating=\"3\"\n" +
                "  xmp:CreatorTool=\"ShorthandTool\"/>\n" +
                "</rdf:RDF>\n" +
                "</x:xmpmeta>\n" +
                "<?xpacket end=\"w\"?>";

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        assertEquals("3", props.get("xmp:Rating").getValue());
        assertEquals("ShorthandTool", props.get("xmp:CreatorTool").getValue());
    }

    @Test
    public void testParseMultipleDescriptions() {
        String xmp = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "<rdf:Description rdf:about=\"\" xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\">\n" +
                "  <xmp:CreatorTool>Tool1</xmp:CreatorTool>\n" +
                "</rdf:Description>\n" +
                "<rdf:Description rdf:about=\"\" xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\">\n" +
                "  <pdf:Producer>Producer1</pdf:Producer>\n" +
                "</rdf:Description>\n" +
                "</rdf:RDF>\n" +
                "</x:xmpmeta>\n" +
                "<?xpacket end=\"w\"?>";

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(xmp.getBytes(StandardCharsets.UTF_8), registry);

        assertEquals("Tool1", props.get("xmp:CreatorTool").getValue());
        assertEquals("Producer1", props.get("pdf:Producer").getValue());
    }

    // ── Writer round-trip tests ──

    @Test
    public void testWriteAndParseRoundTrip() {
        Map<String, XmpProperty> original = new LinkedHashMap<>();
        original.put("xmp:CreatorTool", new XmpProperty("xmp:CreatorTool", "OpenPDF", XmpProperty.ValueType.SIMPLE));
        original.put("pdf:Producer", new XmpProperty("pdf:Producer", "TestProducer", XmpProperty.ValueType.SIMPLE));

        XmpProperty title = new XmpProperty("dc:title", "My Title", XmpProperty.ValueType.LANG_ALT);
        title.addLangAltEntry("x-default", "My Title");
        original.put("dc:title", title);

        XmpProperty creator = new XmpProperty("dc:creator", "Author", XmpProperty.ValueType.SEQ);
        creator.addArrayItem("Author");
        original.put("dc:creator", creator);

        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        byte[] serialized = XmpWriter.serialize(original, registry);
        assertTrue(serialized.length > 0);

        String xml = new String(serialized, StandardCharsets.UTF_8);
        assertTrue(xml.contains("xpacket begin"));
        assertTrue(xml.contains("xpacket end"));

        // Parse back
        Map<String, XmpProperty> parsed = XmpParser.parse(serialized, new XmpNamespaceRegistry());
        assertEquals("OpenPDF", parsed.get("xmp:CreatorTool").getValue());
        assertEquals("TestProducer", parsed.get("pdf:Producer").getValue());
        assertEquals("My Title", parsed.get("dc:title").getValue());
        assertEquals("Author", parsed.get("dc:creator").getValue());
    }

    // ── XmpMetadata API tests ──

    @Test
    public void testXmpMetadataGetSet() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("xmp:CreatorTool", new XmpValue("TestTool"));
        meta.set("pdf:Producer", "TestProducer");

        assertEquals("TestTool", meta.get("xmp:CreatorTool").toString());
        assertEquals("TestProducer", meta.get("pdf:Producer").toString());
        assertTrue(meta.contains("xmp:CreatorTool"));
        assertFalse(meta.contains("xmp:Unknown"));
    }

    @Test
    public void testXmpMetadataRemove() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("xmp:CreatorTool", "TestTool");
        assertTrue(meta.contains("xmp:CreatorTool"));

        meta.remove("xmp:CreatorTool");
        assertFalse(meta.contains("xmp:CreatorTool"));
        assertNull(meta.get("xmp:CreatorTool"));
    }

    @Test
    public void testXmpMetadataKeys() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("xmp:CreatorTool", "Tool");
        meta.set("pdf:Producer", "Producer");
        meta.set("dc:title", "Title");

        java.util.Collection<String> keys =meta.getKeys();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("xmp:CreatorTool"));
        assertTrue(keys.contains("pdf:Producer"));
        assertTrue(keys.contains("dc:title"));
    }

    @Test
    public void testXmpMetadataIteration() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("xmp:CreatorTool", "Tool1");
        meta.set("pdf:Producer", "Producer1");

        int count = 0;
        for (Map.Entry<String, XmpValue> entry : meta) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testXmpMetadataCustomNamespace() {
        XmpMetadata meta = new XmpMetadata();
        meta.registerNamespaceUri("custom", "http://example.com/custom/");
        meta.add("custom:MyProp", new XmpValue("MyValue"));

        assertEquals("MyValue", meta.get("custom:MyProp").toString());
        assertEquals("http://example.com/custom/", meta.getNamespaceUriByPrefix("custom"));
        assertEquals("custom", meta.getPrefixByNamespaceUri("http://example.com/custom/"));
    }

    @Test
    public void testXmpMetadataTypedValues() {
        XmpMetadata meta = new XmpMetadata();
        meta.add("xmp:Intg1", new XmpValue(111));
        meta.add("xmp:Double1", new XmpValue(111.11));
        meta.add("xmp:String1", new XmpValue("ABC"));
        meta.add("xmp:Date1", new XmpValue(new Date()));

        assertTrue(meta.get("xmp:Intg1").isInteger());
        assertEquals(111, meta.get("xmp:Intg1").toInteger());
        assertTrue(meta.get("xmp:Double1").isDouble());
        assertEquals(111.11, meta.get("xmp:Double1").toDouble(), 0.01);
        assertTrue(meta.get("xmp:String1").isString());
        assertEquals("ABC", meta.get("xmp:String1").toString());
        assertTrue(meta.get("xmp:Date1").isDateTime());
    }

    @Test
    public void testXmpMetadataLangAltViaApi() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("dc:title", new XmpValue("Test Title"));
        assertEquals("Test Title", meta.get("dc:title").toString());

        // Round-trip through serialization
        byte[] bytes = meta.getBytes();
        XmpMetadata meta2 = new XmpMetadata(bytes);
        assertEquals("Test Title", meta2.get("dc:title").toString());
    }

    @Test
    public void testXmpMetadataArrayViaApi() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("dc:creator", new XmpValue("Author One"));
        assertEquals("Author One", meta.get("dc:creator").toString());

        byte[] bytes = meta.getBytes();
        XmpMetadata meta2 = new XmpMetadata(bytes);
        assertEquals("Author One", meta2.get("dc:creator").toString());
    }

    @Test
    public void testXmpMetadataGetBytes() {
        XmpMetadata meta = new XmpMetadata();
        meta.set("xmp:CreatorTool", "ByteTest");
        byte[] bytes = meta.getBytes();

        String xml = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(xml.contains("xpacket"));
        assertTrue(xml.contains("ByteTest"));
        assertTrue(xml.contains("CreatorTool"));
    }

    @Test
    public void testEmptyMetadata() {
        XmpMetadata meta = new XmpMetadata();
        assertTrue(meta.getKeys().isEmpty());
        assertNull(meta.get("xmp:CreatorTool"));
        assertFalse(meta.contains("anything"));
    }

    // ── Document integration tests ──

    @Test
    public void testDocumentGetMetadata() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        XmpMetadata meta = doc.getMetadata();
        assertNotNull(meta);
        assertTrue(meta.getKeys().isEmpty());
    }

    @Test
    public void testDocumentSetAndSaveMetadata() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getMetadata().set("xmp:CreatorTool", new XmpValue("OpenPDF"));
        doc.getMetadata().set("dc:title", new XmpValue("Test Document"));
        doc.getMetadata().set("pdf:Producer", new XmpValue("OpenPDF Library"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();

        // Reopen and verify
        Document doc2 = new Document(new ByteArrayInputStream(out.toByteArray()));
        XmpMetadata meta2 = doc2.getMetadata();
        assertEquals("OpenPDF", meta2.get("xmp:CreatorTool").toString());
        assertEquals("Test Document", meta2.get("dc:title").toString());
        assertEquals("OpenPDF Library", meta2.get("pdf:Producer").toString());
        doc2.close();
    }

    @Test
    public void testDocumentCustomNamespaceRoundTrip() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getMetadata().registerNamespaceUri("custom", "http://example.com/custom/");
        doc.getMetadata().add("custom:Property1", new XmpValue("TestProperty"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();

        Document doc2 = new Document(new ByteArrayInputStream(out.toByteArray()));
        assertEquals("TestProperty", doc2.getMetadata().get("custom:Property1").toString());
        doc2.close();
    }

    @Test
    public void testDocumentTypedValuesRoundTrip() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getMetadata().add("xmp:Intg1", new XmpValue(111));
        doc.getMetadata().add("xmp:Double1", new XmpValue(111.11));
        doc.getMetadata().add("xmp:String1", new XmpValue("ABC"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();

        Document doc2 = new Document(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(doc2.getMetadata().get("xmp:Intg1").isInteger());
        assertEquals(111, doc2.getMetadata().get("xmp:Intg1").toInteger());
        assertTrue(doc2.getMetadata().get("xmp:Double1").isDouble());
        assertEquals(111.11, doc2.getMetadata().get("xmp:Double1").toDouble(), 0.01);
        assertEquals("ABC", doc2.getMetadata().get("xmp:String1").toString());
        doc2.close();
    }

    @Test
    public void testDocumentRawXmpSetGet() throws Exception {
        String xmpXml = buildXmp("<xmp:CreatorTool>RawTest</xmp:CreatorTool>");

        Document doc = new Document();
        doc.getPages().add();
        doc.setXmpMetadata(new ByteArrayInputStream(xmpXml.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();

        Document doc2 = new Document(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xmpOut = new ByteArrayOutputStream();
        doc2.getXmpMetadata(xmpOut);
        String result = new String(xmpOut.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(result.contains("RawTest"));
        doc2.close();
    }

    @Test
    public void testDocumentContainsAndRemove() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getMetadata().set("xmp:CreatorTool", "Tool");
        doc.getMetadata().set("pdf:Producer", "Producer");

        assertTrue(doc.getMetadata().contains("xmp:CreatorTool"));
        doc.getMetadata().remove("xmp:CreatorTool");
        assertFalse(doc.getMetadata().contains("xmp:CreatorTool"));

        // Save and verify removal persists
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();

        Document doc2 = new Document(new ByteArrayInputStream(out.toByteArray()));
        assertFalse(doc2.getMetadata().contains("xmp:CreatorTool"));
        assertTrue(doc2.getMetadata().contains("pdf:Producer"));
        doc2.close();
    }

    @Test
    public void testDocumentIterateKeys() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.getMetadata().set("xmp:CreatorTool", "T1");
        doc.getMetadata().set("pdf:Producer", "P1");
        doc.getMetadata().set("dc:title", "Title1");

        java.util.Collection<String> keys =doc.getMetadata().getKeys();
        assertEquals(3, keys.size());

        for (String key : keys) {
            assertNotNull(doc.getMetadata().get(key));
        }
        doc.close();
    }

    @Test
    public void testNamespaceRegistry() {
        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        assertEquals("http://purl.org/dc/elements/1.1/", registry.getUri("dc"));
        assertEquals("http://ns.adobe.com/xap/1.0/", registry.getUri("xmp"));
        assertEquals("http://ns.adobe.com/pdf/1.3/", registry.getUri("pdf"));
        assertEquals("dc", registry.getPrefix("http://purl.org/dc/elements/1.1/"));

        registry.register("custom", "http://example.com/");
        assertEquals("http://example.com/", registry.getUri("custom"));
        assertTrue(registry.hasPrefix("custom"));
    }

    @Test
    public void testParserRobustness_emptyInput() {
        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(new byte[0], registry);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testParserRobustness_nullInput() {
        XmpNamespaceRegistry registry = new XmpNamespaceRegistry();
        Map<String, XmpProperty> props = XmpParser.parse(null, registry);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testDefaultMetadataPropertiesConstants() {
        assertEquals("xmp:CreateDate", DefaultMetadataProperties.CreateDate);
        assertEquals("xmp:ModifyDate", DefaultMetadataProperties.ModifyDate);
        assertEquals("dc:title", DefaultMetadataProperties.Title);
        assertEquals("dc:creator", DefaultMetadataProperties.Creator);
        assertEquals("pdf:Keywords", DefaultMetadataProperties.Keywords);
        assertEquals("pdf:Producer", DefaultMetadataProperties.Producer);
        assertEquals("pdfaid:part", DefaultMetadataProperties.PdfAidPart);
    }

    @Test
    public void testPdfAidProperties() throws Exception {
        String xmp = buildXmp(
                "<pdfaid:part>1</pdfaid:part>\n" +
                "<pdfaid:conformance>B</pdfaid:conformance>");

        XmpMetadata meta = new XmpMetadata(xmp.getBytes(StandardCharsets.UTF_8));
        assertEquals("1", meta.get("pdfaid:part").toString());
        assertEquals("B", meta.get("pdfaid:conformance").toString());
    }

    // ── Helper ──

    private static String buildXmp(String body) {
        return "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "<rdf:Description rdf:about=\"\"\n" +
                "  xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                "  xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                "  xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                "  xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\"\n" +
                "  xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">\n" +
                body + "\n" +
                "</rdf:Description>\n" +
                "</rdf:RDF>\n" +
                "</x:xmpmeta>\n" +
                "<?xpacket end=\"w\"?>";
    }
}
