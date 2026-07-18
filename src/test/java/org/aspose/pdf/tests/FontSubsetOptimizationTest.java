package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.optimization.OptimizationOptions;
import org.aspose.pdf.text.Font;
import org.aspose.pdf.text.Position;
import org.aspose.pdf.text.TextBuilder;
import org.aspose.pdf.text.TextFragment;
import org.aspose.pdf.text.TextFragmentAbsorber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Guards the `subsetFonts` pass: a document embedding a full TrueType
/// program must shrink substantially when only a handful of glyphs is used,
/// and the text must extract identically afterwards.
public class FontSubsetOptimizationTest {

    @TempDir
    Path tempDir;

    private static final String SYSTEM_TTF = "C:/Windows/Fonts/arial.ttf";
    private static final String SAMPLE = "Subset me: Hello, fonts!";

    private Path buildDocWithEmbeddedFont(byte[] ttf) throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextFragment tf = new TextFragment(SAMPLE);
            Font font = new Font("Arial");
            font.setFontData(ttf);
            font.setEmbedded(true);
            tf.getTextState().setFont(font);
            tf.getTextState().setFontName("Arial");
            tf.getTextState().setFontSize(14);
            page.getParagraphs().add(tf);
            Path out = tempDir.resolve("embedded.pdf");
            doc.save(out.toString());
            return out;
        }
    }

    @Test
    public void subsetShrinksEmbeddedFontAndKeepsText() throws Exception {
        Assumptions.assumeTrue(Files.exists(Paths.get(SYSTEM_TTF)),
                "system arial.ttf not available");
        byte[] ttf = Files.readAllBytes(Paths.get(SYSTEM_TTF));
        Path src = buildDocWithEmbeddedFont(ttf);
        long plainSize = Files.size(src);
        Assumptions.assumeTrue(plainSize > 200_000,
                "premise: the full font program dominates the file");

        Path out = tempDir.resolve("subset.pdf");
        try (Document doc = new Document(src.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setSubsetFonts(true);
            options.setRemoveUnusedObjects(true);
            options.setRemoveUnusedStreams(true);
            options.setLinkDuplicateStreams(true);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        long subsetSize = Files.size(out);
        assertTrue(subsetSize < plainSize / 2,
                "glyph stripping must at least halve the file: "
                        + subsetSize + " vs " + plainSize);

        try (Document reopened = new Document(out.toString())) {
            TextFragmentAbsorber abs = new TextFragmentAbsorber(SAMPLE);
            reopened.getPages().get(1).accept(abs);
            assertEquals(1, abs.getTextFragments().size(),
                    "text must extract identically from the subset font");
        }
    }

    /// Without the flag the font program must stay untouched.
    @Test
    public void noSubsetWithoutFlag() throws Exception {
        Assumptions.assumeTrue(Files.exists(Paths.get(SYSTEM_TTF)),
                "system arial.ttf not available");
        byte[] ttf = Files.readAllBytes(Paths.get(SYSTEM_TTF));
        Path src = buildDocWithEmbeddedFont(ttf);
        long plainSize = Files.size(src);

        Path out = tempDir.resolve("nosubset.pdf");
        try (Document doc = new Document(src.toString())) {
            doc.optimizeResources();   // defaults exclude subsetting
            doc.save(out.toString());
        }
        // Lossless passes may shave a little, but the glyph payload stays.
        assertTrue(Files.size(out) > plainSize / 2,
                "font program must not be stripped without subsetFonts");
    }
}
