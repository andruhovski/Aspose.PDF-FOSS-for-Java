package org.aspose.pdf.forms;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.XForm;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfNull;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// F-10 regression tests — [CheckboxField] must NOT leave
/// [PdfNull] placeholders in `/AP/N` after construction.
/// Previously `new CheckboxField(page, rect)` stored
/// `apN.set("Yes", PdfNull.INSTANCE)` so `getAppearance().get("Yes")`
/// returned null and PDF viewers depended on `/NeedAppearances true`.
class CheckboxFieldAppearanceTest {

    @Test
    void newCheckbox_withRect_hasNonNullAppearance() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));
            // F-10: both states must be real streams
            AppearanceDictionary ap = cb.getAppearance();
            assertNotNull(ap, "/AP must exist");
            assertTrue(ap.isMultiState(), "checkbox /AP/N must be a dictionary");
            Set<String> states = ap.getStateNames();
            assertTrue(states.contains("Yes"), "expected /Yes state, got " + states);
            assertTrue(states.contains("Off"), "expected /Off state, got " + states);
            assertNotNull(ap.get("Yes"), "/AP/N/Yes must be a real Form-XObject stream");
            assertNotNull(ap.get("Off"), "/AP/N/Off must be a real Form-XObject stream");
        }
    }

    @Test
    void newCheckbox_withRect_noPdfNullInAPN() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));

            PdfDictionary apDict = (PdfDictionary) cb.getPdfDictionary().get(PdfName.of("AP"));
            PdfDictionary apN = (PdfDictionary) apDict.get(PdfName.N);
            for (PdfName key : apN.keySet()) {
                PdfBase v = apN.get(key);
                assertFalse(v instanceof PdfNull,
                        "F-10 regression: PdfNull placeholder for state " + key.getName());
                assertTrue(v instanceof PdfStream,
                        "state " + key.getName() + " must be a stream, was " + v.getClass().getSimpleName());
            }
        }
    }

    @Test
    void newCheckbox_appearanceContainsZapfDingbatsGlyph() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));

            XForm onState = cb.getAppearance().get("Yes");
            assertNotNull(onState);
            byte[] data = onState.getPdfStream().getDecodedData();
            String body = new String(data, java.nio.charset.StandardCharsets.ISO_8859_1);
            assertTrue(body.contains("/ZaDb"), "on-state should use ZapfDingbats font");
            assertTrue(body.contains("Tj"), "on-state should show text");
        }
    }

    @Test
    void offState_isEmptyStream_notPdfNull() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));

            XForm offState = cb.getAppearance().get("Off");
            assertNotNull(offState);
            byte[] data = offState.getPdfStream().getDecodedData();
            String body = new String(data, java.nio.charset.StandardCharsets.ISO_8859_1);
            // Off state still has q/Q wrapper but no Tj
            assertTrue(body.contains("q"));
            assertTrue(body.contains("Q"));
            assertFalse(body.contains("Tj"), "off-state must not draw the glyph");
        }
    }

    @Test
    void setStyle_regeneratesAppearance() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));

            String checkBody = new String(
                    cb.getAppearance().get("Yes").getPdfStream().getDecodedData(),
                    java.nio.charset.StandardCharsets.ISO_8859_1);
            assertTrue(checkBody.contains("(4)"), "default Check glyph = '4'");

            cb.setStyle(BoxStyle.Cross);
            String crossBody = new String(
                    cb.getAppearance().get("Yes").getPdfStream().getDecodedData(),
                    java.nio.charset.StandardCharsets.ISO_8859_1);
            assertTrue(crossBody.contains("(8)"), "Cross glyph = '8'");
        }
    }

    @Test
    void setExportValue_regeneratesAppearanceWithNewStateName() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            CheckboxField cb = new CheckboxField(page, new Rectangle(50, 100, 70, 120));
            assertNotNull(cb.getAppearance().get("Yes"));

            cb.setExportValue("Choice1");
            Set<String> states = cb.getAppearance().getStateNames();
            assertTrue(states.contains("Choice1"),
                    "after setExportValue, on-state name should be Choice1; got " + states);
            assertNotNull(cb.getAppearance().get("Choice1"));
            // Off remains
            assertNotNull(cb.getAppearance().get("Off"));
        }
    }

    @Test
    void noArgCtor_appearanceIsBlankUntilRectSet() throws Exception {
        // The no-arg ctor leaves /AP/N as empty dict; regen on demand.
        CheckboxField cb = new CheckboxField();
        AppearanceDictionary ap = cb.getAppearance();
        assertTrue(ap.getStateNames().isEmpty(), "no-rect → no states yet");

        cb.setRect(new Rectangle(10, 10, 30, 30));
        cb.regenerateAppearance();
        assertNotNull(cb.getAppearance().get("Yes"));
        assertNotNull(cb.getAppearance().get("Off"));
    }

    @Test
    void appearanceIsIncomplete_detectsPdfNullPlaceholders() {
        PdfDictionary widget = new PdfDictionary();
        PdfDictionary ap = new PdfDictionary();
        PdfDictionary apN = new PdfDictionary();
        apN.set(PdfName.of("Yes"), PdfNull.INSTANCE);
        apN.set(PdfName.of("Off"), PdfNull.INSTANCE);
        ap.set(PdfName.N, apN);
        widget.set(PdfName.of("AP"), ap);
        assertTrue(FieldAppearanceBuilder.isAppearanceIncomplete(widget),
                "PdfNull placeholders should trigger incomplete = true");
    }
}
