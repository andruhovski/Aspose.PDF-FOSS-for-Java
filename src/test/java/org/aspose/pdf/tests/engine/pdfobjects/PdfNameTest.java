package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PdfName}.
 */
public class PdfNameTest {

    @Test
    public void predefinedTypeIdentity() {
        assertSame(PdfName.TYPE, PdfName.of("Type"));
    }

    @Test
    public void interning() {
        assertSame(PdfName.of("Custom"), PdfName.of("Custom"));
    }

    @Test
    public void getName() {
        assertEquals("Custom", PdfName.of("Custom").getName());
    }

    @Test
    public void writeToSimpleName() throws IOException {
        assertWritesTo("/Type", PdfName.of("Type"));
    }

    @Test
    public void writeToNameWithSpaces() throws IOException {
        assertWritesTo("/Name#20With#20Spaces", PdfName.of("Name With Spaces"));
    }

    @Test
    public void writeToNameWithHash() throws IOException {
        assertWritesTo("/#23Hash", PdfName.of("#Hash"));
    }

    @Test
    public void writeToEmptyName() throws IOException {
        assertWritesTo("/", PdfName.of(""));
    }

    @Test
    public void writeToNameWithNonAscii() throws IOException {
        // "Ω" is U+03A9, UTF-8 = CE A9
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfName.of("\u03A9mega").writeTo(baos);
        String result = baos.toString("US-ASCII");
        assertTrue(result.startsWith("/#CE#A9"), "Expected hex-escaped non-ASCII, got: " + result);
    }

    @Test
    public void fromPdfTokenWithHexEscape() {
        PdfName name = PdfName.fromPdfToken("Name#20Test");
        assertEquals("Name Test", name.getName());
    }

    @Test
    public void fromPdfTokenSimple() {
        PdfName name = PdfName.fromPdfToken("Type");
        assertSame(PdfName.TYPE, name);
    }

    @Test
    public void compareTo() {
        assertTrue(PdfName.of("A").compareTo(PdfName.of("B")) < 0);
        assertTrue(PdfName.of("Type").compareTo(PdfName.of("Width")) < 0);
        assertEquals(0, PdfName.of("Same").compareTo(PdfName.of("Same")));
    }

    @Test
    public void threadSafety() throws InterruptedException {
        int threads = 100;
        CountDownLatch latch = new CountDownLatch(threads);
        Set<PdfName> instances = ConcurrentHashMap.newKeySet();
        String name = "ThreadTestName_" + System.nanoTime();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    instances.add(PdfName.of(name));
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
        assertEquals(PdfName.of("Test"), PdfName.of("Test"));
        assertEquals(PdfName.of("Test").hashCode(), PdfName.of("Test").hashCode());
        assertNotEquals(PdfName.of("A"), PdfName.of("B"));
    }

    @Test
    public void toStringIncludesSlash() {
        assertEquals("/Type", PdfName.TYPE.toString());
    }

    private void assertWritesTo(String expected, PdfName name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        name.writeTo(baos);
        assertEquals(expected, baos.toString("US-ASCII"));
    }
}
