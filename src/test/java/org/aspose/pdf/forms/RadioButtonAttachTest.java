package org.aspose.pdf.forms;

import org.aspose.pdf.Color;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.annotations.Border;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Promo 12 — {@link RadioButtonField#add(RadioButtonOptionField)} attach API
 * plus per-option decorators (setBorder/setCaption/setColor/setDefaultAppearance).
 * Closes the RadioButton cluster that blocked 5+ tests in audit 15b.
 */
class RadioButtonAttachTest {

    @Test
    void add_attachesOptionToKidsAndSetsParent() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("color");

            RadioButtonOptionField red = new RadioButtonOptionField(page,
                    new Rectangle(50, 700, 70, 720));
            red.setOptionName("Red");
            radio.add(red);

            PdfArray kids = (PdfArray) radio.getPdfDictionary().get(PdfName.of("Kids"));
            assertNotNull(kids);
            assertEquals(1, kids.size());
            assertSame(red.getPdfDictionary(), kids.get(0));

            assertSame(radio.getPdfDictionary(),
                    red.getPdfDictionary().get(PdfName.of("Parent")));
        }
    }

    @Test
    void add_generatesAppearanceWhenIncomplete() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            RadioButtonOptionField opt = new RadioButtonOptionField(page,
                    new Rectangle(50, 700, 70, 720));
            opt.setOptionName("Red");

            // Pre-attach: setOptionName installed empty placeholder streams
            // via emptyAppearanceStream(); attach should regenerate with real
            // Form-XObjects via FieldAppearanceBuilder.
            radio.add(opt);

            AppearanceDictionary ap = opt.getAppearance();
            assertNotNull(ap.get("Red"));
            assertNotNull(ap.get("Off"));
            // The newly-generated streams have non-zero BBox
            PdfStream on = ap.get("Red").getPdfStream();
            PdfArray bbox = (PdfArray) on.get(PdfName.BBOX);
            assertNotNull(bbox);
            assertTrue(bbox.getFloat(2, 0) > 0, "BBox width should be > 0");
            assertTrue(bbox.getFloat(3, 0) > 0, "BBox height should be > 0");
        }
    }

    @Test
    void add_multipleOptions_allInKids() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);

            RadioButtonOptionField red = new RadioButtonOptionField(page,
                    new Rectangle(50, 700, 70, 720));
            red.setOptionName("Red");
            RadioButtonOptionField green = new RadioButtonOptionField(page,
                    new Rectangle(50, 670, 70, 690));
            green.setOptionName("Green");
            RadioButtonOptionField blue = new RadioButtonOptionField(page,
                    new Rectangle(50, 640, 70, 660));
            blue.setOptionName("Blue");

            radio.add(red);
            radio.add(green);
            radio.add(blue);

            assertEquals(3, radio.getOptions().size());
            assertEquals("Red",   radio.getOptions().get(0).getOptionValue());
            assertEquals("Green", radio.getOptions().get(1).getOptionValue());
            assertEquals("Blue",  radio.getOptions().get(2).getOptionValue());
        }
    }

    @Test
    void add_null_throws() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            assertThrows(IllegalArgumentException.class, () -> radio.add(null));
        }
    }

    @Test
    void pageRectCtor_setsRect() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page, new Rectangle(10, 20, 30, 40));
            Rectangle r = radio.getRect();
            assertNotNull(r);
            assertEquals(10, r.getLLX(), 1e-6);
            assertEquals(40, r.getURY(), 1e-6);
        }
    }

    @Test
    void option_setBorder_storesBS() {
        RadioButtonOptionField opt = new RadioButtonOptionField();
        Border b = new Border((org.aspose.pdf.annotations.Annotation) null);
        b.setWidth(2.5);
        opt.setBorder(b);
        PdfDictionary bs = (PdfDictionary) opt.getPdfDictionary().get(PdfName.of("BS"));
        assertNotNull(bs);
        assertEquals("Border",
                ((PdfName) bs.get(PdfName.TYPE)).getName());

        Border got = opt.getBorder();
        assertNotNull(got);
        assertEquals(2.5, got.getWidth(), 1e-6);
    }

    @Test
    void option_setBorder_nullRemovesEntry() {
        RadioButtonOptionField opt = new RadioButtonOptionField();
        Border b = new Border((org.aspose.pdf.annotations.Annotation) null);
        b.setWidth(1);
        opt.setBorder(b);
        assertNotNull(opt.getPdfDictionary().get(PdfName.of("BS")));
        opt.setBorder(null);
        assertNull(opt.getPdfDictionary().get(PdfName.of("BS")));
    }

    @Test
    void option_setCaption_storesInMKCA() {
        RadioButtonOptionField opt = new RadioButtonOptionField();
        opt.setCaption("Red option");
        assertEquals("Red option", opt.getCaption());
        // /MK/CA storage check
        PdfDictionary mk = (PdfDictionary) opt.getPdfDictionary().get(PdfName.of("MK"));
        assertNotNull(mk);
        assertNotNull(mk.get(PdfName.of("CA")));
    }

    @Test
    void option_setColor_storesInMKBC() {
        RadioButtonOptionField opt = new RadioButtonOptionField();
        Color red = Color.fromRgb(1f, 0f, 0f);
        opt.setColor(red);
        Color got = opt.getColor();
        assertNotNull(got);
        // /MK/BC array of 3 floats
        PdfDictionary mk = (PdfDictionary) opt.getPdfDictionary().get(PdfName.of("MK"));
        PdfArray bc = (PdfArray) mk.get(PdfName.of("BC"));
        assertNotNull(bc);
        assertEquals(3, bc.size());
    }

    @Test
    void option_setDefaultAppearance_storesInDA() {
        RadioButtonOptionField opt = new RadioButtonOptionField();
        opt.setDefaultAppearance("/Helv 12 Tf 0 g");
        assertEquals("/Helv 12 Tf 0 g", opt.getDefaultAppearance());
        opt.setDefaultAppearance((String) null);
        assertNull(opt.getDefaultAppearance());
    }

    @Test
    void option_regenerateAppearance_producesFormXObjects() throws Exception {
        RadioButtonOptionField opt = new RadioButtonOptionField((Page) null,
                new Rectangle(0, 0, 12, 12));
        opt.setOptionName("Red");
        opt.regenerateAppearance();

        PdfDictionary ap = (PdfDictionary) opt.getPdfDictionary().get(PdfName.of("AP"));
        PdfDictionary apN = (PdfDictionary) ap.get(PdfName.N);
        PdfBase red = apN.get(PdfName.of("Red"));
        PdfBase off = apN.get(PdfName.of("Off"));
        assertTrue(red instanceof PdfStream, "Red appearance should be a stream");
        assertTrue(off instanceof PdfStream, "Off appearance should be a stream");
        // Sanity: stream contains ZapfDingbats invocation
        String body = new String(((PdfStream) red).getDecodedData(),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(body.contains("/ZaDb"));
    }
}
