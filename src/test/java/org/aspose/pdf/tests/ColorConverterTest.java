package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.operators.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ColorConverter and ColorConversionStrategy.
 */
public class ColorConverterTest {

    /**
     * Helper: creates a document with a single page containing the given content stream.
     */
    private Document createDocWithContent(String content) throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();
        COSStream stream = new COSStream();
        stream.setDecodedData(content.getBytes(StandardCharsets.US_ASCII));
        page.getCOSDictionary().set(COSName.CONTENTS, stream);
        return doc;
    }

    @Test
    public void testConvertRgbToGrayscale() throws IOException {
        // Content stream with RGB color operator
        Document doc = createDocWithContent("1 0 0 rg\n0 1 0 RG\n");
        ColorConverter.convert(doc, ColorConversionStrategy.ConvertToGrayscale);

        Page page = doc.getPages().get(1);
        OperatorCollection ops = page.getContents();

        // Find the gray operators
        boolean foundGrayNonStroke = false;
        boolean foundGrayStroke = false;
        for (Operator op : ops) {
            if (op instanceof SetGray) {
                SetGray g = (SetGray) op;
                // RGB(1,0,0) -> gray = 0.299*1 + 0.587*0 + 0.114*0 = 0.299
                assertEquals(0.299, g.getGray(), 0.01, "Red should convert to gray ~0.299");
                foundGrayNonStroke = true;
            }
            if (op instanceof SetGrayStroke) {
                SetGrayStroke g = (SetGrayStroke) op;
                // RGB(0,1,0) -> gray = 0.299*0 + 0.587*1 + 0.114*0 = 0.587
                assertEquals(0.587, g.getGray(), 0.01, "Green should convert to gray ~0.587");
                foundGrayStroke = true;
            }
        }
        assertTrue(foundGrayNonStroke, "Should have non-stroking gray operator");
        assertTrue(foundGrayStroke, "Should have stroking gray operator");

        doc.close();
    }

    @Test
    public void testConvertRgbToCmyk() throws IOException {
        Document doc = createDocWithContent("1 0 0 rg\n");
        ColorConverter.convert(doc, ColorConversionStrategy.ConvertToCmyk);

        Page page = doc.getPages().get(1);
        OperatorCollection ops = page.getContents();

        boolean foundCmyk = false;
        for (Operator op : ops) {
            if (op instanceof SetCMYKColor) {
                SetCMYKColor cmyk = (SetCMYKColor) op;
                // RGB(1,0,0) -> CMYK: k=1-max(1,0,0)=0, c=(1-1-0)/1=0, m=(1-0)/1=1, y=(1-0)/1=1
                assertEquals(0.0, cmyk.getC(), 0.01, "Cyan should be 0");
                assertEquals(1.0, cmyk.getM(), 0.01, "Magenta should be 1");
                assertEquals(1.0, cmyk.getY(), 0.01, "Yellow should be 1");
                assertEquals(0.0, cmyk.getK(), 0.01, "Black should be 0");
                foundCmyk = true;
            }
        }
        assertTrue(foundCmyk, "Should have CMYK operator after conversion");

        doc.close();
    }

    @Test
    public void testConvertCmykToRgb() throws IOException {
        Document doc = createDocWithContent("0 1 1 0 k\n");
        ColorConverter.convert(doc, ColorConversionStrategy.ConvertToRgb);

        Page page = doc.getPages().get(1);
        OperatorCollection ops = page.getContents();

        boolean foundRgb = false;
        for (Operator op : ops) {
            if (op instanceof SetRGBColor) {
                SetRGBColor rgb = (SetRGBColor) op;
                // CMYK(0,1,1,0) -> RGB: r=(1-0)*(1-0)=1, g=(1-1)*(1-0)=0, b=(1-1)*(1-0)=0
                assertEquals(1.0, rgb.getR(), 0.01);
                assertEquals(0.0, rgb.getG(), 0.01);
                assertEquals(0.0, rgb.getB(), 0.01);
                foundRgb = true;
            }
        }
        assertTrue(foundRgb, "Should have RGB operator after CMYK->RGB conversion");

        doc.close();
    }

    @Test
    public void testConvertGrayToRgb() throws IOException {
        Document doc = createDocWithContent("0.5 g\n");
        ColorConverter.convert(doc, ColorConversionStrategy.ConvertToRgb);

        Page page = doc.getPages().get(1);
        OperatorCollection ops = page.getContents();

        boolean foundRgb = false;
        for (Operator op : ops) {
            if (op instanceof SetRGBColor) {
                SetRGBColor rgb = (SetRGBColor) op;
                assertEquals(0.5, rgb.getR(), 0.01);
                assertEquals(0.5, rgb.getG(), 0.01);
                assertEquals(0.5, rgb.getB(), 0.01);
                foundRgb = true;
            }
        }
        assertTrue(foundRgb, "Gray should convert to RGB with equal components");

        doc.close();
    }

    @Test
    public void testNoConversion() throws IOException {
        Document doc = createDocWithContent("1 0 0 rg\n0.5 G\n");
        ColorConverter.convert(doc, ColorConversionStrategy.None);

        Page page = doc.getPages().get(1);
        OperatorCollection ops = page.getContents();

        // Operators should remain unchanged
        boolean foundRgb = false;
        boolean foundGrayStroke = false;
        for (Operator op : ops) {
            if (op instanceof SetRGBColor) foundRgb = true;
            if (op instanceof SetGrayStroke) foundGrayStroke = true;
        }
        assertTrue(foundRgb, "RGB operator should remain with None strategy");
        assertTrue(foundGrayStroke, "Gray stroke operator should remain with None strategy");

        doc.close();
    }

    @Test
    public void testColorConversionFormulas() {
        // RGB to Gray
        assertEquals(0.299, ColorConverter.rgbToGray(1, 0, 0), 0.001);
        assertEquals(0.587, ColorConverter.rgbToGray(0, 1, 0), 0.001);
        assertEquals(0.114, ColorConverter.rgbToGray(0, 0, 1), 0.001);
        assertEquals(1.0, ColorConverter.rgbToGray(1, 1, 1), 0.001);
        assertEquals(0.0, ColorConverter.rgbToGray(0, 0, 0), 0.001);

        // RGB to CMYK
        double[] cmyk = ColorConverter.rgbToCmyk(1, 0, 0);
        assertEquals(0, cmyk[0], 0.001); // C
        assertEquals(1, cmyk[1], 0.001); // M
        assertEquals(1, cmyk[2], 0.001); // Y
        assertEquals(0, cmyk[3], 0.001); // K

        // Black
        double[] blackCmyk = ColorConverter.rgbToCmyk(0, 0, 0);
        assertEquals(1.0, blackCmyk[3], 0.001); // K should be 1

        // CMYK to RGB
        double[] rgb = ColorConverter.cmykToRgb(0, 1, 1, 0);
        assertEquals(1.0, rgb[0], 0.001); // R
        assertEquals(0.0, rgb[1], 0.001); // G
        assertEquals(0.0, rgb[2], 0.001); // B
    }

    @Test
    public void testColorConversionStrategyEnum() {
        assertEquals(4, ColorConversionStrategy.values().length);
        assertNotNull(ColorConversionStrategy.valueOf("None"));
        assertNotNull(ColorConversionStrategy.valueOf("ConvertToCmyk"));
        assertNotNull(ColorConversionStrategy.valueOf("ConvertToRgb"));
        assertNotNull(ColorConversionStrategy.valueOf("ConvertToGrayscale"));
    }

    @Test
    public void testPdfSaveOptionsColorConversion() {
        PdfSaveOptions options = new PdfSaveOptions();
        assertEquals(ColorConversionStrategy.None, options.getColorConversion(),
                "Default should be None");

        options.setColorConversion(ColorConversionStrategy.ConvertToGrayscale);
        assertEquals(ColorConversionStrategy.ConvertToGrayscale, options.getColorConversion());

        // null should default to None
        options.setColorConversion(null);
        assertEquals(ColorConversionStrategy.None, options.getColorConversion());
    }
}
