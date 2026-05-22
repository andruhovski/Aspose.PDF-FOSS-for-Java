package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSName}.
 */
public class COSNameTest {

    @Test
    public void predefinedTypeIdentity() {
        assertSame(COSName.TYPE, COSName.of("Type"));
    }

    @Test
    public void interning() {
        assertSame(COSName.of("Custom"), COSName.of("Custom"));
    }

    @Test
    public void getName() {
        assertEquals("Custom", COSName.of("Custom").getName());
    }

    @Test
    public void writeToSimpleName() throws IOException {
        assertWritesTo("/Type", COSName.of("Type"));
    }

    @Test
    public void writeToNameWithSpaces() throws IOException {
        assertWritesTo("/Name#20With#20Spaces", COSName.of("Name With Spaces"));
    }

    @Test
    public void writeToNameWithHash() throws IOException {
        assertWritesTo("/#23Hash", COSName.of("#Hash"));
    }

    @Test
    public void writeToEmptyName() throws IOException {
        assertWritesTo("/", COSName.of(""));
    }

    @Test
    public void writeToNameWithNonAscii() throws IOException {
        // "Ω" is U+03A9, UTF-8 = CE A9
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        COSName.of("\u03A9mega").writeTo(baos);
        String result = baos.toString("US-ASCII");
        assertTrue(result.startsWith("/#CE#A9"), "Expected hex-escaped non-ASCII, got: " + result);
    }

    @Test
    public void fromPdfTokenWithHexEscape() {
        COSName name = COSName.fromPdfToken("Name#20Test");
        assertEquals("Name Test", name.getName());
    }

    @Test
    public void fromPdfTokenSimple() {
        COSName name = COSName.fromPdfToken("Type");
        assertSame(COSName.TYPE, name);
    }

    @Test
    public void compareTo() {
        assertTrue(COSName.of("A").compareTo(COSName.of("B")) < 0);
        assertTrue(COSName.of("Type").compareTo(COSName.of("Width")) < 0);
        assertEquals(0, COSName.of("Same").compareTo(COSName.of("Same")));
    }

    @Test
    public void threadSafety() throws InterruptedException {
        int threads = 100;
        CountDownLatch latch = new CountDownLatch(threads);
        Set<COSName> instances = ConcurrentHashMap.newKeySet();
        String name = "ThreadTestName_" + System.nanoTime();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    instances.add(COSName.of(name));
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertEquals(1, instances.size(), "All threads should get the same instance");
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(COSName.of("Test"), COSName.of("Test"));
        assertEquals(COSName.of("Test").hashCode(), COSName.of("Test").hashCode());
        assertNotEquals(COSName.of("A"), COSName.of("B"));
    }

    @Test
    public void toStringIncludesSlash() {
        assertEquals("/Type", COSName.TYPE.toString());
    }

    private void assertWritesTo(String expected, COSName name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        name.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
