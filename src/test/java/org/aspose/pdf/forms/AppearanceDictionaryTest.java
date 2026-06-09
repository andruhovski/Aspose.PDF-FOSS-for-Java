package org.aspose.pdf.forms;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.XForm;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppearanceDictionaryTest {

    @Test
    void emptyAppearance_returnsEmptyStateSet_andNullNormal() {
        AppearanceDictionary ap = new AppearanceDictionary(new PdfDictionary());
        assertTrue(ap.getStateNames().isEmpty());
        assertNull(ap.getNormal());
        assertNull(ap.get("Off"));
        assertFalse(ap.isMultiState());
    }

    @Test
    void singleStateStream_returnsViaGetNormal() {
        PdfDictionary apDict = new PdfDictionary();
        PdfStream stream = new PdfStream();
        stream.set(PdfName.TYPE, PdfName.of("XObject"));
        stream.set(PdfName.SUBTYPE, PdfName.of("Form"));
        stream.setDecodedData(new byte[]{'B', 'T'});
        apDict.set(PdfName.N, stream);

        AppearanceDictionary ap = new AppearanceDictionary(apDict);
        XForm n = ap.getNormal();
        assertNotNull(n);
        assertEquals("N", n.getName());
        assertSame(stream, n.getPdfStream());
        assertFalse(ap.isMultiState());
        assertTrue(ap.getStateNames().isEmpty());
    }

    @Test
    void multiStateDict_listsStates_andReturnsByName() {
        PdfDictionary apDict = new PdfDictionary();
        PdfDictionary nDict = new PdfDictionary();
        PdfStream offStream = new PdfStream();
        PdfStream yesStream = new PdfStream();
        nDict.set(PdfName.of("Off"), offStream);
        nDict.set(PdfName.of("Yes"), yesStream);
        apDict.set(PdfName.N, nDict);

        AppearanceDictionary ap = new AppearanceDictionary(apDict);
        assertTrue(ap.isMultiState());

        Set<String> names = ap.getStateNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("Off"));
        assertTrue(names.contains("Yes"));

        assertSame(offStream, ap.get("Off").getPdfStream());
        assertSame(yesStream, ap.get("Yes").getPdfStream());
        assertNull(ap.get("Maybe"));
        assertNull(ap.get(null));
    }

    @Test
    void multiState_getNormalReturnsNull() {
        PdfDictionary apDict = new PdfDictionary();
        PdfDictionary nDict = new PdfDictionary();
        nDict.set(PdfName.of("Off"), new PdfStream());
        apDict.set(PdfName.N, nDict);

        AppearanceDictionary ap = new AppearanceDictionary(apDict);
        assertNull(ap.getNormal(), "getNormal must return null when /N is a dict");
    }

    @Test
    void ctor_nullApDict_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AppearanceDictionary(null));
    }

    @Test
    void field_getAppearance_lazyCreatesApDict() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextBoxField tf = new TextBoxField(page, new Rectangle(0, 0, 100, 20));
            // before any setValue, /AP is absent
            assertNull(tf.getPdfDictionary().get("AP"));

            AppearanceDictionary ap = tf.getAppearance();
            assertNotNull(ap);
            // /AP sub-dict should be created lazily
            assertNotNull(tf.getPdfDictionary().get("AP"));
            assertTrue(ap.getStateNames().isEmpty());
            assertNull(ap.getNormal());
        }
    }

    @Test
    void field_getAppearance_afterSetValue_returnsNormal() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextBoxField tf = new TextBoxField(page, new Rectangle(0, 0, 100, 20));
            tf.setValue("hello");

            AppearanceDictionary ap = tf.getAppearance();
            XForm n = ap.getNormal();
            assertNotNull(n, "TextBoxField.setValue should produce a single-state /AP/N stream");
            assertFalse(ap.isMultiState());
        }
    }
}
