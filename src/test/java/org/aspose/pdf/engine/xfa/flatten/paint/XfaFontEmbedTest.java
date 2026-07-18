package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.font.ttf.MinimalTtf;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// FE.1–FE.3 end-to-end, deterministic (synthetic [MinimalTtf] fixture only — no proprietary font):
///
///   - **FE.3 host + FE.1 embed**: a draw whose typeface is found in `-Dxfa.fontDir` is painted
///     with the embedded font; the saved PDF carries `/FontFile2`+`/Type0` and reopens.
///   - **FE.2 source reuse**: that saved PDF's embedded font is reused (priority 1) by a resolver
///     built from it — resolved via `SOURCE_PDF`, even with no host dir configured.
///   - **priority + disabled + fallback**: source beats host; `xfa.embedFonts=false` embeds
///     nothing; an unknown family falls back to `null` (substitution).
public class XfaFontEmbedTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    private static byte[] fixtureTtf() {
        Map<Character, Integer> g = new LinkedHashMap<>();
        g.put('A', 700);
        g.put('B', 800);
        return MinimalTtf.build("TestFont", g);
    }

    private static String template(String typeface) {
        return "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='d' x='10pt' y='10pt' w='200pt' h='20pt'>"
                + "  <font typeface='" + typeface + "' size='12pt'/><value><text>AB</text></value></draw>"
                + "</subform></template>";
    }

    @Test
    void hostFontIsResolvedEmbeddedAndReopens(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        byte[] pdf = withFontDir(dir, () -> paint(template("TestFont"), XfaFontResolver.create(null)));

        String s = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(s.contains("/FontFile2"), "embedded TrueType program present");
        assertTrue(s.contains("/Type0") && s.contains("/CIDFontType2"), "composite font graph present");
        try (Document re = new Document(new ByteArrayInputStream(pdf))) {
            assertEquals(1, re.getPages().getCount(), "reopen-valid");
        }
    }

    @Test
    void resolverReportsHostResolution(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        withFontDir(dir, () -> {
            XfaFontResolver fr = XfaFontResolver.create(null);
            XfaFontResolver.Embedded e = fr.resolve("TestFont", false, false);
            assertNotNull(e, "host font resolved");
            assertEquals(XfaFontResolver.Source.HOST, fr.resolvedVia().get("testfont"));
            // An unknown family now resolves to a guaranteed Unicode fallback face when the OS
            // provides one (Arial/DejaVu/Liberation), so non-WinAnsi text isn't lost to '?'. On a
            // machine with none of those it stays null (the legacy standard-14 substitution).
            XfaFontResolver.Embedded fb = fr.resolve("NoSuchFamilyXYZ", false, false);
            if (fb != null) {
                assertEquals(XfaFontResolver.Source.FALLBACK, fr.resolvedVia().get("nosuchfamilyxyz"),
                        "unknown family resolved via Unicode fallback");
            }
            return new byte[0];
        });
    }

    @Test
    void sourcePdfFontIsReusedAtPriorityOne(@TempDir Path dir) throws Exception {
        // 1) make a PDF that embeds the fixture font (via host discovery)
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        byte[] pdf = withFontDir(dir, () -> paint(template("TestFont"), XfaFontResolver.create(null)));
        // 2) reopen it as a SOURCE and build a resolver from it WITHOUT any host dir configured
        System.clearProperty("xfa.fontDir");
        try (Document source = new Document(new ByteArrayInputStream(pdf))) {
            XfaFontResolver fr = XfaFontResolver.create(source);
            XfaFontResolver.Embedded e = fr.resolve("TestFont", false, false);
            assertNotNull(e, "source-embedded font reused");
            assertEquals(XfaFontResolver.Source.SOURCE_PDF, fr.resolvedVia().get("testfont"),
                    "priority 1: reused from the source PDF, not the host");
        }
    }

    @Test
    void disabledResolverEmbedsNothing(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        String prev = System.getProperty("xfa.embedFonts");
        System.setProperty("xfa.embedFonts", "false");
        try {
            XfaFontResolver fr = withFontDirValue(dir, XfaFontResolver::create);
            assertFalse(fr.isEnabled(), "disabled");
            assertNull(fr.resolve("TestFont", false, false), "embedding off → no embed");
            byte[] pdf = paint(template("TestFont"), fr);
            assertFalse(new String(pdf, StandardCharsets.ISO_8859_1).contains("/FontFile2"),
                    "no embedded program when disabled");
        } finally {
            if (prev == null) {
                System.clearProperty("xfa.embedFonts");
            } else {
                System.setProperty("xfa.embedFonts", prev);
            }
        }
    }

    /* ------------------------------- helpers ------------------------------- */

    private interface Job {
        byte[] run() throws Exception;
    }

    private static byte[] withFontDir(Path dir, Job job) throws Exception {
        String prev = System.getProperty("xfa.fontDir");
        System.setProperty("xfa.fontDir", dir.toString());
        try {
            return job.run();
        } finally {
            if (prev == null) {
                System.clearProperty("xfa.fontDir");
            } else {
                System.setProperty("xfa.fontDir", prev);
            }
        }
    }

    private static XfaFontResolver withFontDirValue(Path dir, java.util.function.Function<Document, XfaFontResolver> f) {
        System.setProperty("xfa.fontDir", dir.toString());
        try {
            return f.apply(null);
        } finally {
            System.clearProperty("xfa.fontDir");
        }
    }

    private static byte[] paint(String xml, XfaFontResolver resolver) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(xml));
        FormDom dom = new BindingEngine().merge(tpl, null);
        Document out = new Document();
        XfaPainter.paint(out, dom, tpl, resolver);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        out.save(bos);
        out.close();
        return bos.toByteArray();
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
