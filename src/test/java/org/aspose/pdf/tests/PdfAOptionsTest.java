package org.aspose.pdf.tests;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.ConvertSoftMaskAction;
import org.aspose.pdf.FontEmbeddingOptions;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.PdfFormatConversionOptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for PdfFormatConversionOptions advanced properties.
public class PdfAOptionsTest {

    @Test
    public void testConversionOptionsDefaults() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        // Verify defaults
        assertTrue(opts.isEmbedFonts());
        assertTrue(opts.isSubsetFonts());
        assertEquals("Helvetica", opts.getDefaultFontName());
        assertFalse(opts.isIgnoreResourceFontErrors());
        assertEquals(ConvertSoftMaskAction.Default, opts.getSoftMaskAction());
    }

    @Test
    public void testEmbedFonts() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        opts.setEmbedFonts(false);
        assertFalse(opts.isEmbedFonts());
        opts.setEmbedFonts(true);
        assertTrue(opts.isEmbedFonts());
    }

    @Test
    public void testSubsetFonts() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        opts.setSubsetFonts(false);
        assertFalse(opts.isSubsetFonts());
    }

    @Test
    public void testDefaultFontName() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        opts.setDefaultFontName("Courier");
        assertEquals("Courier", opts.getDefaultFontName());
    }

    @Test
    public void testDefaultFontNameNull() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        assertThrows(IllegalArgumentException.class, () -> opts.setDefaultFontName(null));
    }

    @Test
    public void testIgnoreResourceFontErrors() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        opts.setIgnoreResourceFontErrors(true);
        assertTrue(opts.isIgnoreResourceFontErrors());
    }

    @Test
    public void testSoftMaskAction() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        opts.setSoftMaskAction(ConvertSoftMaskAction.ConvertToStencilMask);
        assertEquals(ConvertSoftMaskAction.ConvertToStencilMask, opts.getSoftMaskAction());
    }

    @Test
    public void testSoftMaskActionNull() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(
                (String) null, PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);

        assertThrows(IllegalArgumentException.class, () -> opts.setSoftMaskAction(null));
    }

    @Test
    public void testConvertSoftMaskActionEnum() {
        assertEquals(2, ConvertSoftMaskAction.values().length);
        assertNotNull(ConvertSoftMaskAction.valueOf("Default"));
        assertNotNull(ConvertSoftMaskAction.valueOf("ConvertToStencilMask"));
    }

    @Test
    public void testSingleArgumentConstructorUsesDeleteAction() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(PdfFormat.PDF_A_2B);

        assertEquals(PdfFormat.PDF_A_2B, opts.getFormat());
        assertEquals(ConvertErrorAction.Delete, opts.getErrorAction());
    }

    @Test
    public void testDefaultOptionsFactory() {
        PdfFormatConversionOptions opts = PdfFormatConversionOptions.getDefault();

        assertEquals(PdfFormat.PDF_A_1B, opts.getFormat());
        assertEquals(ConvertErrorAction.Delete, opts.getErrorAction());
    }

    @Test
    public void testAdditionalPdfaOptionsRoundTrip() {
        PdfFormatConversionOptions opts = new PdfFormatConversionOptions(PdfFormat.PDF_A_2B);

        opts.setOptimizeFileSize(true);
        opts.setIsLowMemoryMode(true);
        opts.setTransferInfo(true);
        opts.setIccProfileFileName("sRGB.icc");
        opts.setExcludeFontsStrategy(
                PdfFormatConversionOptions.RemoveFontsStrategy.SubsetFonts.getValue()
                        | PdfFormatConversionOptions.RemoveFontsStrategy.RemoveDuplicatedFonts.getValue());

        FontEmbeddingOptions fontOptions = opts.getFontEmbeddingOptions();
        fontOptions.setUseDefaultSubstitution(true);

        assertTrue(opts.isOptimizeFileSize());
        assertTrue(opts.isLowMemoryMode());
        assertTrue(opts.isTransferInfo());
        assertEquals("sRGB.icc", opts.getIccProfileFileName());
        assertEquals(3, opts.getExcludeFontsStrategy());
        assertTrue(opts.getFontEmbeddingOptions().isUseDefaultSubstitution());
    }

    @Test
    public void testPdfa4FormatsExist() {
        assertTrue(PdfFormat.PDF_A_4.isPdfA4());
        assertTrue(PdfFormat.PDF_A_4E.isPdfA4());
        assertTrue(PdfFormat.PDF_A_4F.isPdfA4());
        assertEquals("2.0", PdfFormat.v_2_0.getPdfVersion());
    }
}
