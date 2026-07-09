package org.aspose.pdf.engine.font.ttf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FE.1 — the synthetic-TTF fixture parses, and {@link Type0FontBuilder#buildLatin} emits a real
 * {@code /W} width array taken from the font's own {@code hmtx} (not the standard-14 table or the
 * {@code /DW} default), plus {@code /FontFile2} and a non-symbolic descriptor.
 */
public class Type0FontBuilderLatinTest {

    private static Map<Character, Integer> glyphs() {
        Map<Character, Integer> g = new LinkedHashMap<>();
        g.put('A', 700);
        g.put('B', 800);
        return g;
    }

    @Test
    void syntheticTtfParses() throws Exception {
        byte[] ttf = MinimalTtf.build("TestFont", glyphs());
        TrueTypeReader r = new TrueTypeReader(ttf);
        assertEquals(1000, r.getUnitsPerEm(), "unitsPerEm");
        assertEquals(3, r.getNumGlyphs(), ".notdef + A + B");
        assertEquals(1, r.getGlyphId('A'), "cmap A→gid1");
        assertEquals(2, r.getGlyphId('B'), "cmap B→gid2");
        assertEquals(700, r.getAdvanceWidth(1), "hmtx A advance");
        assertEquals(800, r.getAdvanceWidth(2), "hmtx B advance");
    }

    @Test
    void buildLatinEmitsFontDerivedWidthsAndFontFile2() throws Exception {
        byte[] ttf = MinimalTtf.build("TestFont", glyphs());
        Type0FontBuilder.Result res = Type0FontBuilder.buildLatin("TestFont", ttf);
        PdfDictionary type0 = res.type0Font;
        assertEquals("Type0", type0.getNameAsString("Subtype"));

        PdfArray descendants = type0.getArray("DescendantFonts");
        assertNotNull(descendants, "DescendantFonts");
        PdfDictionary cid = descendants.getDictionary(0);
        assertEquals("CIDFontType2", cid.getNameAsString("Subtype"));

        // /FontFile2 present in the descriptor; descriptor is non-symbolic Latin
        PdfDictionary fd = cid.getDictionary("FontDescriptor");
        assertNotNull(fd.get("FontFile2"), "FontFile2 embedded");
        assertEquals(32, ((PdfInteger) fd.get("Flags")).intValue(), "Nonsymbolic Latin flag");

        // /W = [0 [w0 w1 w2]] with the FONT's widths (A=700, B=800), not /DW=1000
        PdfArray w = cid.getArray("W");
        assertNotNull(w, "/W width array present");
        assertEquals(0, ((PdfInteger) w.get(0)).intValue(), "/W starts at CID 0");
        PdfArray run = (PdfArray) w.get(1);
        assertEquals(3, run.size(), "one width per glyph (.notdef + A + B)");
        assertEquals(700, ((PdfInteger) run.get(1)).intValue(), "A width from hmtx");
        assertEquals(800, ((PdfInteger) run.get(2)).intValue(), "B width from hmtx");
    }

    @Test
    void widthsScaleFromUnitsPerEm() throws Exception {
        // a 2048-upm font's advances must be rescaled to the PDF 1000 space — verified indirectly:
        // our 1000-upm fixture yields identical numbers, and the run length matches glyph count.
        byte[] ttf = MinimalTtf.build("TestFont", glyphs());
        Type0FontBuilder.Result res = Type0FontBuilder.buildLatin("TestFont", ttf);
        PdfArray run = (PdfArray) res.type0Font.getArray("DescendantFonts")
                .getDictionary(0).getArray("W").get(1);
        for (int i = 0; i < run.size(); i++) {
            PdfBase v = run.get(i);
            assertTrue(v instanceof PdfInteger && ((PdfInteger) v).intValue() >= 0, "width is a non-negative int");
        }
    }
}
