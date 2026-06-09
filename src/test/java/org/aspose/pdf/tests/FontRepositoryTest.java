package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.font.FontRepository;
import org.aspose.pdf.engine.font.PdfFont;
import org.aspose.pdf.engine.font.Type1Font;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FontRepository}.
 */
public class FontRepositoryTest {

    @Test
    public void testGetStandardFont() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.TYPE, PdfName.of("Font"));
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        PdfDictionary fontsDict = new PdfDictionary();
        fontsDict.set(PdfName.of("F1"), fontDict);

        FontRepository repo = new FontRepository();
        PdfFont font = repo.getFont(fontsDict, "F1", null);
        assertNotNull(font);
        assertTrue(font instanceof Type1Font);
        assertEquals("Helvetica", font.getBaseFont());
    }

    @Test
    public void testCaching() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Courier"));

        PdfDictionary fontsDict = new PdfDictionary();
        fontsDict.set(PdfName.of("F1"), fontDict);

        FontRepository repo = new FontRepository();
        PdfFont font1 = repo.getFont(fontsDict, "F1", null);
        PdfFont font2 = repo.getFont(fontsDict, "F1", null);
        assertSame(font1, font2, "Should return cached instance");
    }

    @Test
    public void testNullFontsDict() throws IOException {
        FontRepository repo = new FontRepository();
        assertNull(repo.getFont(null, "F1", null));
    }

    @Test
    public void testNullFontName() throws IOException {
        FontRepository repo = new FontRepository();
        assertNull(repo.getFont(new PdfDictionary(), null, null));
    }

    @Test
    public void testMissingFont() throws IOException {
        FontRepository repo = new FontRepository();
        PdfDictionary fontsDict = new PdfDictionary();
        PdfFont font = repo.getFont(fontsDict, "F1", null);
        assertNull(font);
    }

    @Test
    public void testClear() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        PdfDictionary fontsDict = new PdfDictionary();
        fontsDict.set(PdfName.of("F1"), fontDict);

        FontRepository repo = new FontRepository();
        PdfFont font1 = repo.getFont(fontsDict, "F1", null);
        repo.clear();
        PdfFont font2 = repo.getFont(fontsDict, "F1", null);
        assertNotSame(font1, font2, "After clear, should create new instance");
    }
}
