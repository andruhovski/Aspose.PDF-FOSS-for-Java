package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.optimization.OptimizationOptions;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Guards the `removePrivateInfo` pass: XMP metadata and PieceInfo
/// payloads are detached and their streams dropped from the output.
public class RemovePrivateInfoTest {

    @TempDir
    Path tempDir;

    @Test
    public void privatePayloadsAreStripped() throws Exception {
        // Build a doc, then decorate its catalog with an XMP blob + PieceInfo.
        Path plain = tempDir.resolve("plain.pdf");
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            org.aspose.pdf.text.TextFragment tf = new org.aspose.pdf.text.TextFragment("content");
            tf.setPosition(new org.aspose.pdf.text.Position(72, 700));
            new org.aspose.pdf.text.TextBuilder(page).appendText(tf);
            doc.save(plain.toString());
        }

        Path decorated = tempDir.resolve("decorated.pdf");
        try (Document doc = new Document(plain.toString())) {
            PdfDictionary catalog = doc.getParser().getCatalog();
            byte[] xmp = new byte[40_000];
            java.util.Arrays.fill(xmp, (byte) 'x');
            PdfStream metadata = new PdfStream();
            metadata.set(PdfName.of("Type"), PdfName.of("Metadata"));
            metadata.set(PdfName.of("Subtype"), PdfName.of("XML"));
            metadata.setDecodedData(xmp);
            catalog.set(PdfName.of("Metadata"), metadata);
            PdfDictionary pieceInfo = new PdfDictionary();
            PdfDictionary appData = new PdfDictionary();
            appData.set(PdfName.of("LastModified"),
                    new org.aspose.pdf.engine.pdfobjects.PdfString(
                            "D:20260101000000".getBytes(StandardCharsets.US_ASCII)));
            pieceInfo.set(PdfName.of("SomeApp"), appData);
            catalog.set(PdfName.of("PieceInfo"), pieceInfo);
            doc.save(decorated.toString());
        }
        long decoratedSize = Files.size(decorated);
        assertTrue(decoratedSize > 30_000, "premise: decorated file carries the XMP blob");

        Path out = tempDir.resolve("stripped.pdf");
        try (Document doc = new Document(decorated.toString())) {
            OptimizationOptions options = new OptimizationOptions();
            options.setRemovePrivateInfo(true);
            options.setRemoveUnusedObjects(true);
            options.setRemoveUnusedStreams(true);
            doc.optimizeResources(options);
            doc.save(out.toString());
        }
        long strippedSize = Files.size(out);
        assertTrue(strippedSize < decoratedSize - 30_000,
                "XMP payload must be gone: " + strippedSize + " vs " + decoratedSize);

        try (Document reopened = new Document(out.toString())) {
            assertEquals(1, reopened.getPages().getCount());
            PdfDictionary catalog = reopened.getParser().getCatalog();
            assertNull(catalog.get("Metadata"), "catalog /Metadata must be removed");
            assertNull(catalog.get("PieceInfo"), "catalog /PieceInfo must be removed");
            org.aspose.pdf.text.TextFragmentAbsorber abs =
                    new org.aspose.pdf.text.TextFragmentAbsorber("content");
            reopened.getPages().get(1).accept(abs);
            assertEquals(1, abs.getTextFragments().size(), "page content untouched");
        }
    }

    /// Without the flag, metadata must survive optimization.
    @Test
    public void metadataKeptWithoutFlag() throws Exception {
        Path plain = tempDir.resolve("keep.pdf");
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.save(plain.toString());
        }
        Path decorated = tempDir.resolve("keep_meta.pdf");
        try (Document doc = new Document(plain.toString())) {
            PdfStream metadata = new PdfStream();
            metadata.set(PdfName.of("Type"), PdfName.of("Metadata"));
            metadata.set(PdfName.of("Subtype"), PdfName.of("XML"));
            metadata.setDecodedData("<x:xmpmeta/>".getBytes(StandardCharsets.US_ASCII));
            doc.getParser().getCatalog().set(PdfName.of("Metadata"), metadata);
            doc.save(decorated.toString());
        }
        Path out = tempDir.resolve("keep_out.pdf");
        try (Document doc = new Document(decorated.toString())) {
            doc.optimizeResources();   // default options: no private-info removal
            doc.save(out.toString());
        }
        try (Document reopened = new Document(out.toString())) {
            assertTrue(reopened.getParser().getCatalog().get("Metadata") != null,
                    "metadata must survive when removePrivateInfo is off");
        }
    }
}
