package org.aspose.pdf.engine.render;

import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link TextRenderer#remapForSymbolicCmap(Font, String, int)} -
 * the ISO 32000 (9.6.6.4) fallback for symbolic TrueType fonts whose only cmap
 * subtable is (3,0) Microsoft Symbol with glyphs mapped at {@code 0xF000 + code}.
 *
 * <p>Such fonts (e.g. the subset {@code AAAAAA+Times-New-Roman-Bold} embedded in
 * corpus file 29903.pdf) cannot display plain Unicode ('A' = U+0041) through
 * Java's font machinery; the glyph lives at U+F041 instead. Without the remap
 * every glyph rendered as a .notdef box. The corpus-level guard lives in
 * {@code SymbolicCmapRenderRegressionTest} (pdf-aspose-tests).</p>
 */
public class TextRendererSymbolicRemapTest {

    /** A regular Unicode-cmap font must pass through unchanged. */
    @Test
    public void displayableCharIsReturnedUnchanged() {
        Font dialog = new Font(Font.DIALOG, Font.PLAIN, 12);
        assumeTrue(dialog.canDisplay('A'), "JDK Dialog font must display 'A'");
        assertEquals("A", TextRenderer.remapForSymbolicCmap(dialog, "A", 'A'));
    }

    /** Codes above one byte are composite-font CIDs - never remapped. */
    @Test
    public void multiByteCodeIsNeverRemapped() {
        Font dialog = new Font(Font.DIALOG, Font.PLAIN, 12);
        String ch = String.valueOf((char) 0xFFFF);
        assertSame(ch, TextRenderer.remapForSymbolicCmap(dialog, ch, 0x1234));
    }

    /** Null/degenerate inputs come back as-is (defensive, no NPE). */
    @Test
    public void nullAndDegenerateInputsAreSafe() {
        Font dialog = new Font(Font.DIALOG, Font.PLAIN, 12);
        assertNull(TextRenderer.remapForSymbolicCmap(dialog, null, 0x41));
        assertEquals("AB", TextRenderer.remapForSymbolicCmap(dialog, "AB", 0x41));
        assertEquals("A", TextRenderer.remapForSymbolicCmap(null, "A", 0x41));
    }

    /**
     * Positive case: a symbol-encoded font (Wingdings/Symbol ship with Windows;
     * their cmap is (3,0) at F000+code, exactly like the PDF-embedded subsets)
     * must get 'A' remapped to U+F041.
     */
    @Test
    public void symbolFontRemapsToPrivateUseArea() {
        Font symbolFont = findSymbolFont();
        assumeTrue(symbolFont != null,
                "no installed (3,0)-cmap symbol font found; positive remap covered by the corpus regression test");
        assertEquals(String.valueOf((char) 0xF041),
                TextRenderer.remapForSymbolicCmap(symbolFont, "A", 0x41),
                symbolFont.getFontName() + " must remap 'A' (0x41) to U+F041");
    }

    /** An undisplayable char whose PUA twin is also absent stays unchanged. */
    @Test
    public void unmappableCharStaysUnchangedWhenPuaTwinAbsent() {
        Font dialog = new Font(Font.DIALOG, Font.PLAIN, 12);
        // U+FFFE is a noncharacter no font maps; its PUA twin U+F0FE must also
        // be absent from Dialog for this test to exercise the both-miss branch.
        assumeTrue(!dialog.canDisplay((char) 0xFFFE) && !dialog.canDisplay((char) 0xF0FE));
        String ch = String.valueOf((char) 0xFFFE);
        assertEquals(ch, TextRenderer.remapForSymbolicCmap(dialog, ch, 0xFE));
    }

    /** Finds an installed font that behaves like a (3,0) symbol cmap font. */
    private static Font findSymbolFont() {
        for (String name : new String[]{"Wingdings", "Symbol", "Webdings", "Wingdings 2"}) {
            Font f = new Font(name, Font.PLAIN, 12);
            // Font falls back to Dialog when the family is absent - verify the
            // symbol behaviour directly instead of trusting the name.
            if (!f.canDisplay('A') && f.canDisplay((char) 0xF041)) {
                return f;
            }
        }
        // Last resort: scan everything installed.
        for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            if (!f.canDisplay('A') && f.canDisplay((char) 0xF041)) {
                return f.deriveFont(Font.PLAIN, 12f);
            }
        }
        return null;
    }
}
