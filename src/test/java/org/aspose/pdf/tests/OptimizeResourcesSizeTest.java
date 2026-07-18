package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.text.Position;
import org.aspose.pdf.text.TextBuilder;
import org.aspose.pdf.text.TextFragment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// `optimizeResources()` must not make the file LARGER than a plain save
/// (PDFNEWNET-30310): optimization emits a compact object-stream rewrite, not a
/// linearized layout whose hint-stream overhead can outweigh the pruning.
public class OptimizeResourcesSizeTest {

    private static byte[] buildDoc() throws Exception {
        Document doc = new Document();
        for (int p = 0; p < 3; p++) {
            Page page = doc.getPages().add();
            TextBuilder tb = new TextBuilder(page);
            for (int i = 0; i < 20; i++) {
                TextFragment tf = new TextFragment("Line " + i + " of page " + p + " with some filler text.");
                tf.getTextState().setFontSize(11);
                tf.setPosition(new Position(72, 800 - i * 20));
                tb.appendText(tf);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return baos.toByteArray();
    }

    @Test
    public void optimizeDoesNotGrowFile() throws Exception {
        Path src = Files.createTempFile("optsize", ".pdf");
        Files.write(src, buildDoc());
        long plainSize = Files.size(src);

        Path out = Files.createTempFile("optsize_out", ".pdf");
        try (Document doc = new Document(src.toString())) {
            doc.optimizeResources();
            doc.save(out.toString());
        }
        long optimizedSize = Files.size(out);

        // the optimized output must be readable and no larger than the plain save
        try (Document reopened = new Document(out.toString())) {
            assertEquals(3, reopened.getPages().getCount(), "optimized file must round-trip its pages");
        }
        assertTrue(optimizedSize <= plainSize,
                "optimized file grew: " + optimizedSize + " > " + plainSize);

        Files.deleteIfExists(src);
        Files.deleteIfExists(out);
    }
}
