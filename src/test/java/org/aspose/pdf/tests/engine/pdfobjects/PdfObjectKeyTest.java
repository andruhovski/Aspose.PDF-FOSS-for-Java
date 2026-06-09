package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PdfObjectKey}.
 */
public class PdfObjectKeyTest {

    @Test
    public void equalsAndHashCodeForIdenticalKeys() {
        PdfObjectKey key1 = new PdfObjectKey(10, 0);
        PdfObjectKey key2 = new PdfObjectKey(10, 0);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void notEqualsForDifferentObjectNumbers() {
        PdfObjectKey key1 = new PdfObjectKey(10, 0);
        PdfObjectKey key2 = new PdfObjectKey(11, 0);
        assertNotEquals(key1, key2);
    }

    @Test
    public void notEqualsForDifferentGenerations() {
        PdfObjectKey key1 = new PdfObjectKey(10, 0);
        PdfObjectKey key2 = new PdfObjectKey(10, 1);
        assertNotEquals(key1, key2);
    }

    @Test
    public void compareToSortsByObjectNumberFirst() {
        List<PdfObjectKey> keys = new ArrayList<>();
        keys.add(new PdfObjectKey(5, 0));
        keys.add(new PdfObjectKey(1, 0));
        keys.add(new PdfObjectKey(3, 0));
        keys.add(new PdfObjectKey(1, 1));
        Collections.sort(keys);

        assertEquals(1, keys.get(0).getObjectNumber());
        assertEquals(0, keys.get(0).getGenerationNumber());
        assertEquals(1, keys.get(1).getObjectNumber());
        assertEquals(1, keys.get(1).getGenerationNumber());
        assertEquals(3, keys.get(2).getObjectNumber());
        assertEquals(5, keys.get(3).getObjectNumber());
    }

    @Test
    public void toStringFormat() {
        PdfObjectKey key = new PdfObjectKey(12, 0);
        assertEquals("12 0", key.toString());
    }

    @Test
    public void invalidObjectNumberThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(-1, 0));
    }

    @Test
    public void invalidGenerationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(1, 65536));
    }

    @Test
    public void maxGeneration() {
        PdfObjectKey key = new PdfObjectKey(1, 65535);
        assertEquals(65535, key.getGenerationNumber());
    }

    @Test
    public void rejectsGenerationBoundary() {
        assertDoesNotThrow(() -> new PdfObjectKey(1, 0));
        assertDoesNotThrow(() -> new PdfObjectKey(1, 65535));
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(1, 65536));
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(1, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> new PdfObjectKey(1, -1));
    }
}
