package org.aspose.pdf.tests;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.font.Type1Font;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Type1Font}.
 */
public class Type1FontTest {

    @Test
    public void testStandard14Helvetica() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.TYPE, COSName.of("Font"));
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals("Helvetica", font.getBaseFont());
    }

    @Test
    public void testDecodeAscii() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        String decoded = font.decode("Hello".getBytes());
        assertEquals("Hello", decoded);
    }

    @Test
    public void testGetWidthA() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(667.0, font.getWidth(65), 0.1); // 'A'
    }

    @Test
    public void testGetWidthSpace() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(278.0, font.getWidth(32), 0.1); // space
    }

    @Test
    public void testCustomEncoding() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Helvetica"));

        // Custom encoding with Differences
        COSDictionary encDict = new COSDictionary();
        encDict.set(COSName.of("BaseEncoding"), COSName.of("WinAnsiEncoding"));
        COSArray diff = new COSArray();
        diff.add(COSInteger.valueOf(65));
        diff.add(COSName.of("Euro"));
        encDict.set(COSName.of("Differences"), diff);
        fontDict.set(COSName.ENCODING, encDict);

        Type1Font font = new Type1Font(fontDict, null);
        // Code 65 now maps to "Euro" glyph
        String decoded = font.decode(new byte[]{65});
        // Euro char = U+20AC
        assertEquals("\u20AC", decoded);
    }

    @Test
    public void testCustomWidths() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("CustomFont"));
        fontDict.set(COSName.of("FirstChar"), COSInteger.valueOf(32));

        COSArray widths = new COSArray();
        widths.add(COSInteger.valueOf(250)); // code 32 = space
        widths.add(COSInteger.valueOf(300)); // code 33 = !
        fontDict.set(COSName.WIDTHS, widths);

        Type1Font font = new Type1Font(fontDict, null);
        assertEquals(250.0, font.getWidth(32), 0.1);
        assertEquals(300.0, font.getWidth(33), 0.1);
    }

    @Test
    public void testCourierWidth() throws IOException {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set(COSName.of("Subtype"), COSName.of("Type1"));
        fontDict.set(COSName.of("BaseFont"), COSName.of("Courier"));

        Type1Font font = new Type1Font(fontDict, null);
        // All Courier chars are 600
        assertEquals(600.0, font.getWidth(65), 0.1);
        assertEquals(600.0, font.getWidth(97), 0.1);
    }
}
