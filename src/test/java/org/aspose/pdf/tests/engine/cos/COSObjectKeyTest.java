package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSObjectKey}.
 */
public class COSObjectKeyTest {

    @Test
    public void equalsAndHashCodeForIdenticalKeys() {
        COSObjectKey key1 = new COSObjectKey(10, 0);
        COSObjectKey key2 = new COSObjectKey(10, 0);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void notEqualsForDifferentObjectNumbers() {
        COSObjectKey key1 = new COSObjectKey(10, 0);
        COSObjectKey key2 = new COSObjectKey(11, 0);
        assertNotEquals(key1, key2);
    }

    @Test
    public void notEqualsForDifferentGenerations() {
        COSObjectKey key1 = new COSObjectKey(10, 0);
        COSObjectKey key2 = new COSObjectKey(10, 1);
        assertNotEquals(key1, key2);
    }

    @Test
    public void compareToSortsByObjectNumberFirst() {
        List<COSObjectKey> keys = new ArrayList<>();
        keys.add(new COSObjectKey(5, 0));
        keys.add(new COSObjectKey(1, 0));
        keys.add(new COSObjectKey(3, 0));
        keys.add(new COSObjectKey(1, 1));
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
        COSObjectKey key = new COSObjectKey(12, 0);
        assertEquals("12 0", key.toString());
    }

    @Test
    public void invalidObjectNumberThrows() {
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(-1, 0));
    }

    @Test
    public void invalidGenerationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(1, 65536));
    }

    @Test
    public void maxGeneration() {
        COSObjectKey key = new COSObjectKey(1, 65535);
        assertEquals(65535, key.getGenerationNumber());
    }

    @Test
    public void rejectsGenerationBoundary() {
        assertDoesNotThrow(() -> new COSObjectKey(1, 0));
        assertDoesNotThrow(() -> new COSObjectKey(1, 65535));
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(1, 65536));
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(1, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> new COSObjectKey(1, -1));
    }
}
