package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.font.Type1Font;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Type1Font].
public class Type1FontTest {

    @Test
    public void testStandard14Helvetica() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.TYPE, PdfName.of("Font"));
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals("Helvetica", font.getBaseFont());
    }

    @Test
    public void testDecodeAscii() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        String decoded = font.decode("Hello".getBytes());
        assertEquals("Hello", decoded);
    }

    @Test
    public void testGetWidthA() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(667.0, font.getWidth(65), 0.1); // 'A'
    }

    @Test
    public void testGetWidthSpace() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(278.0, font.getWidth(32), 0.1); // space
    }

    @Test
    public void testCustomEncoding() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Helvetica"));

        // Custom encoding with Differences
        PdfDictionary encDict = new PdfDictionary();
        encDict.set(PdfName.of("BaseEncoding"), PdfName.of("WinAnsiEncoding"));
        PdfArray diff = new PdfArray();
        diff.add(PdfInteger.valueOf(65));
        diff.add(PdfName.of("Euro"));
        encDict.set(PdfName.of("Differences"), diff);
        fontDict.set(PdfName.ENCODING, encDict);

        Type1Font font = new Type1Font(fontDict, null);
        // Code 65 now maps to "Euro" glyph
        String decoded = font.decode(new byte[]{65});
        // Euro char = U+20AC
        assertEquals("\u20AC", decoded);
    }

    @Test
    public void testCustomWidths() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("CustomFont"));
        fontDict.set(PdfName.of("FirstChar"), PdfInteger.valueOf(32));

        PdfArray widths = new PdfArray();
        widths.add(PdfInteger.valueOf(250)); // code 32 = space
        widths.add(PdfInteger.valueOf(300)); // code 33 = !
        fontDict.set(PdfName.WIDTHS, widths);

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(250.0, font.getWidth(32), 0.1);
        assertEquals(300.0, font.getWidth(33), 0.1);
    }

    @Test
    public void testCourierWidth() throws IOException {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        fontDict.set(PdfName.of("BaseFont"), PdfName.of("Courier"));

        Type1Font font = new Type1Font(fontDict, null);
        // All Courier chars are 600
        assertEquals(600.0, font.getWidth(65), 0.1);
        assertEquals(600.0, font.getWidth(97), 0.1);
    }
}
