package org.aspose.pdf.tests.engine.xml;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.XfdfImporter;
import org.aspose.pdf.engine.xml.SecureXml;
import org.aspose.pdf.facades.PdfBookmarkEditor;
import org.aspose.pdf.forms.TextBoxField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// XXE hardening tests for [SecureXml] and the two untrusted-XML entry points that
/// use it ([XfdfImporter], [PdfBookmarkEditor#importBookmarksWithXML]): a
/// crafted `<!DOCTYPE ... SYSTEM "file:///...">` must NOT read local files — the
/// DOCTYPE is rejected (well-formed error) or the entity stays unexpanded. Legitimate
/// XML still imports.
public class SecureXmlTest {

    private static final String SECRET = "TOP-SECRET-FILE-CONTENT-12345";

    @TempDir
    Path tmp;

    private Path secretFile() throws IOException {
        Path f = tmp.resolve("secret.txt");
        Files.write(f, SECRET.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    private static String fileUrl(Path p) {
        return p.toUri().toString(); // file:///...
    }

    // ---------------- SecureXml factory itself ----------------

    @Test
    public void doctypeIsRejected_entityNeverResolved() throws Exception {
        String xml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"" + fileUrl(secretFile()) + "\">]>"
                + "<foo>&xxe;</foo>";
        try {
            org.w3c.dom.Document doc = SecureXml.newBuilder(true)
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            // if some JAXP impl ignores disallow-doctype-decl, the entity must still be empty
            assertFalse(doc.getDocumentElement().getTextContent().contains(SECRET),
                    "external entity must not be expanded");
        } catch (org.xml.sax.SAXException expected) {
            // disallow-doctype-decl → well-formed error; the file was never read
            assertFalse(String.valueOf(expected.getMessage()).contains(SECRET));
        }
    }

    @Test
    public void plainXmlParsesNormally() throws Exception {
        org.w3c.dom.Document doc = SecureXml.newBuilder(false)
                .parse(new ByteArrayInputStream("<root><a>1</a></root>".getBytes(StandardCharsets.UTF_8)));
        assertEquals("root", doc.getDocumentElement().getNodeName());
    }

    // ---------------- XFDF import ----------------

    @Test
    public void xfdfXxePayloadDoesNotLeakFile() throws Exception {
        String xfdf = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE xfdf [<!ENTITY xxe SYSTEM \"" + fileUrl(secretFile()) + "\">]>"
                + "<xfdf><fields><field name=\"f1\"><value>&xxe;</value></field></fields></xfdf>";
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextBoxField f1 = new TextBoxField(page, new org.aspose.pdf.Rectangle(50, 700, 250, 720));
            f1.setPartialName("f1");
            doc.getForm().add(f1);
            try {
                XfdfImporter.importXfdf(doc, new ByteArrayInputStream(xfdf.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException expected) {
                // DOCTYPE disallowed → parse error surfaced as IOException; must not leak content
                assertFalse(String.valueOf(expected.getMessage()).contains(SECRET));
            }
            String value = doc.getForm().get("f1").getValue();
            assertTrue(value == null || !value.contains(SECRET),
                    "XXE entity value must not be injected into the field");
        }
    }

    @Test
    public void legitimateXfdfStillImports() throws Exception {
        String xfdf = "<?xml version=\"1.0\"?><xfdf>"
                + "<fields><field name=\"f1\"><value>hello</value></field></fields></xfdf>";
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextBoxField f1 = new TextBoxField(page, new org.aspose.pdf.Rectangle(50, 700, 250, 720));
            f1.setPartialName("f1");
            doc.getForm().add(f1);
            XfdfImporter.importXfdf(doc, new ByteArrayInputStream(xfdf.getBytes(StandardCharsets.UTF_8)));
            assertEquals("hello", doc.getForm().get("f1").getValue());
        }
    }

    // ---------------- Bookmark XML import ----------------

    @Test
    public void bookmarkXxePayloadDoesNotLeakFile() throws Exception {
        String xml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE Bookmarks [<!ENTITY xxe SYSTEM \"" + fileUrl(secretFile()) + "\">]>"
                + "<Bookmarks><Bookmark><Title>&xxe;</Title><PageNumber>1</PageNumber></Bookmark></Bookmarks>";
        Path xmlFile = tmp.resolve("bookmarks_xxe.xml");
        Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

        try (Document doc = new Document()) {
            doc.getPages().add();
            PdfBookmarkEditor editor = new PdfBookmarkEditor();
            editor.bindPdf(doc);
            try {
                editor.importBookmarksWithXML(xmlFile.toString());
            } catch (IOException expected) {
                assertFalse(String.valueOf(expected.getMessage()).contains(SECRET));
            }
            // whatever happened, no outline title may carry the secret
            for (org.aspose.pdf.OutlineItemCollection item : doc.getOutlines()) {
                assertFalse(String.valueOf(item.getTitle()).contains(SECRET),
                        "XXE entity value must not be injected into a bookmark title");
            }
        }
    }

    @Test
    public void legitimateBookmarkXmlStillImports() throws Exception {
        String xml = "<?xml version=\"1.0\"?>"
                + "<Bookmarks><Bookmark><Title>Chapter 1</Title><PageNumber>1</PageNumber></Bookmark></Bookmarks>";
        Path xmlFile = tmp.resolve("bookmarks_ok.xml");
        Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

        try (Document doc = new Document()) {
            doc.getPages().add();
            PdfBookmarkEditor editor = new PdfBookmarkEditor();
            editor.bindPdf(doc);
            editor.importBookmarksWithXML(xmlFile.toString());
            boolean found = false;
            for (org.aspose.pdf.OutlineItemCollection item : doc.getOutlines()) {
                if ("Chapter 1".equals(item.getTitle())) found = true;
            }
            assertTrue(found, "legitimate bookmark XML must still import");
        }
    }
}
