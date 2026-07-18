package org.aspose.pdf.engine.font.ttf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// FE.3 — host font discovery: [FontDiskLookup#loadStyled] finds a font by family name in a
/// configured directory, returns its bytes, and falls through cleanly (never throws) on a missing dir,
/// missing family, or empty dir. Uses the synthetic [MinimalTtf] fixture only — NO dependency on
/// any proprietary host font (CI-safe).
public class FontDiskLookupStyledTest {

    private static byte[] fixtureTtf() {
        Map<Character, Integer> g = new LinkedHashMap<>();
        g.put('A', 700);
        return MinimalTtf.build("TestFont", g);
    }

    @Test
    void resolvesFamilyInConfiguredDir(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        byte[] found = FontDiskLookup.loadStyled("TestFont", false, false, new String[]{dir.toString()});
        assertNotNull(found, "family resolved from the configured dir");
        // parses back to the same font
        assertTrue(new TrueTypeReader(found).getNumGlyphs() >= 2, "resolved bytes are a valid TTF");
    }

    @Test
    void emptyDirFallsThroughToNull(@TempDir Path dir) {
        byte[] found = FontDiskLookup.loadStyled("TotallyUnknownFamily12345", false, false,
                new String[]{dir.toString()});
        assertNull(found, "no font in dir and not on host → null (clean fallback)");
    }

    @Test
    void missingDirNeverThrows() {
        byte[] found = FontDiskLookup.loadStyled("TestFont", true, true,
                new String[]{"Z:/no/such/dir/anywhere"});
        // may be null (not on host) — the point is it does not throw
        assertTrue(found == null || found.length > 0, "missing dir handled gracefully");
    }

    @Test
    void styledRequestStillEmbedsAvailableFace(@TempDir Path dir) throws Exception {
        // only a regular face exists; a bold request must still resolve *a* face (last-resort fallback).
        Files.write(dir.resolve("testfont.ttf"), fixtureTtf());
        byte[] bold = FontDiskLookup.loadStyled("TestFont", true, false, new String[]{dir.toString()});
        assertNotNull(bold, "bold request falls back to the available regular face");
    }
}
