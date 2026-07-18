package org.aspose.pdf.optimization;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/// Unit tests for [OptimizationOptions] and
/// [Document#optimizeResources(OptimizationOptions)].
public class OptimizationOptionsTest {

    @Test
    void defaults_matchAsposeCtor() {
        // Aspose's parameterless ctor enables the three structural passes
        // (removeUnusedObjects/removeUnusedStreams/linkDuplicateStreams);
        // the lossy/font passes stay off (PDFNET_39956 parity).
        OptimizationOptions o = new OptimizationOptions();
        assertTrue(o.isRemoveUnusedObjects());
        assertTrue(o.isRemoveUnusedStreams());
        assertTrue(o.isLinkDuplicateStreams());
        assertFalse(o.isCompressImages());
        assertFalse(o.isSubsetFonts());
        assertFalse(o.isUnembedFonts());
        assertFalse(o.isAllowReusePageContent());
        assertEquals(100, o.getImageQuality());
    }

    @Test
    void all_enablesStandardPasses() {
        OptimizationOptions o = OptimizationOptions.all();
        assertTrue(o.isRemoveUnusedObjects());
        assertTrue(o.isRemoveUnusedStreams());
        assertTrue(o.isLinkDuplicateStreams());
        assertTrue(o.isSubsetFonts());
        assertTrue(o.isCompressImages());
    }

    @Test
    void imageQuality_isClamped() {
        OptimizationOptions o = new OptimizationOptions();
        o.setImageQuality(250);
        assertEquals(100, o.getImageQuality());
        o.setImageQuality(-5);
        assertEquals(1, o.getImageQuality());
        o.setImageQuality(80);
        assertEquals(80, o.getImageQuality());
    }

    @Test
    void optimizeResources_nullIsNoOp() throws Exception {
        Document doc = new Document();
        doc.getPages().add();
        doc.optimizeResources(null); // must not throw
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        assertTrue(bos.size() > 0);
        doc.close();
    }

    @Test
    void optimizeResources_savesValidDocument() throws Exception {
        Document doc = new Document();
        Page p = doc.getPages().add();
        assertNotNull(p);
        doc.optimizeResources(OptimizationOptions.all());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);
        byte[] out = bos.toByteArray();
        assertTrue(out.length > 0);
        // Re-open to confirm the optimised output is a parseable PDF.
        try (Document reopened = new Document(new java.io.ByteArrayInputStream(out))) {
            assertEquals(1, reopened.getPages().getCount());
        }
        doc.close();
    }
}
