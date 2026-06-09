package org.aspose.pdf.tests.generation;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.forms.ComboBoxField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 22 Part 3 — F-10 sibling for {@link ComboBoxField}. A combo box must
 * generate a real {@code /AP/N} appearance stream (not a {@code PdfNull}
 * placeholder, not a missing entry) so strict viewers render the selected text.
 */
class ComboBoxAppearanceTest {

    private static PdfStream normalAppearance(ComboBoxField cb) {
        PdfBase ap = cb.getPdfDictionary().get(PdfName.of("AP"));
        assertTrue(ap instanceof PdfDictionary, "/AP should be a dictionary");
        PdfBase n = ((PdfDictionary) ap).get(PdfName.N);
        assertTrue(n instanceof PdfStream, "/AP/N should be a real Form XObject stream");
        return (PdfStream) n;
    }

    @Test
    @DisplayName("ComboBox ctor builds a real /AP/N Form XObject (no PdfNull)")
    void ctor_buildsAppearanceStream() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            ComboBoxField cb = new ComboBoxField(page, new Rectangle(50, 100, 250, 130));
            PdfStream n = normalAppearance(cb);
            assertEquals(PdfName.of("Form"), n.get(PdfName.SUBTYPE));
            assertNotNull(n.get(PdfName.BBOX));
        }
    }

    @Test
    @DisplayName("setSelected refreshes the appearance to show the chosen value")
    void setSelected_rendersValue() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            ComboBoxField cb = new ComboBoxField(page, new Rectangle(50, 100, 250, 130));
            cb.setPartialName("country");
            cb.addOption("USA");
            cb.addOption("UK");
            cb.setSelected("UK");
            String content = new String(normalAppearance(cb).getDecodedData(), StandardCharsets.ISO_8859_1);
            assertTrue(content.contains("(UK)"), "appearance should paint the selected value, got: " + content);
            assertTrue(content.contains("Tf"), "appearance should set a font");
        }
    }

    @Test
    @DisplayName("Zero-area combo box silently skips appearance generation (F-10)")
    void zeroAreaRect_skipsAppearance() throws IOException {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            // Degenerate rect must not throw and must not crash regeneration.
            ComboBoxField cb = assertDoesNotThrow(() ->
                    new ComboBoxField(page, new Rectangle(50, 100, 50, 130)));
            PdfBase ap = cb.getPdfDictionary().get(PdfName.of("AP"));
            if (ap instanceof PdfDictionary) {
                assertFalse(((PdfDictionary) ap).get(PdfName.N) instanceof PdfStream,
                        "degenerate rect should not produce an /AP/N stream");
            }
        }
    }
}
