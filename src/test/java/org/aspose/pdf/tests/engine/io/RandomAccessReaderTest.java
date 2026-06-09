package org.aspose.pdf.tests.engine.io;
import org.aspose.pdf.engine.io.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RandomAccessReader}.
 */
public class RandomAccessReaderTest {

    // ---- fromBytes: seek, read, position ----

    @Test
    public void fromBytes_seekReadPosition() throws IOException {
        byte[] data = "Hello, PDF!".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals(0, reader.getPosition());
            assertEquals(data.length, reader.getLength());
            assertEquals('H', reader.read());
            assertEquals(1, reader.getPosition());

            reader.seek(7);
            assertEquals(7, reader.getPosition());
            assertEquals('P', reader.read());
            assertEquals('D', reader.read());
            assertEquals('F', reader.read());
        }
    }

    // ---- fromFile: seek, read, position ----

    @Test
    public void fromFile_seekReadPosition(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.pdf");
        byte[] data = "Hello, PDF!".getBytes(StandardCharsets.US_ASCII);
        Files.write(file, data);

        try (RandomAccessReader reader = RandomAccessReader.fromFile(file)) {
            assertEquals(0, reader.getPosition());
            assertEquals(data.length, reader.getLength());
            assertEquals('H', reader.read());
            assertEquals(1, reader.getPosition());

            reader.seek(7);
            assertEquals('P', reader.read());
            assertEquals('D', reader.read());
            assertEquals('F', reader.read());
        }
    }

    // ---- readLine ----

    @Test
    public void readLine_lineFeed() throws IOException {
        byte[] data = "line1\nline2\n".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals("line1", reader.readLine());
            assertEquals("line2", reader.readLine());
        }
    }

    @Test
    public void readLine_carriageReturnLineFeed() throws IOException {
        byte[] data = "line1\r\nline2\r\n".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals("line1", reader.readLine());
            assertEquals("line2", reader.readLine());
        }
    }

    @Test
    public void readLine_noNewlineAtEnd() throws IOException {
        byte[] data = "lastline".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals("lastline", reader.readLine());
            assertNull(reader.readLine()); // EOF
        }
    }

    // ---- readFully ----

    @Test
    public void readFully_exactBytes() throws IOException {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            byte[] result = reader.readFully(3);
            assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
            assertEquals(3, reader.getPosition());
        }
    }

    @Test
    public void readFully_insufficientData() throws IOException {
        byte[] data = {0x01, 0x02};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertThrows(EOFException.class, () -> reader.readFully(5));
        }
    }

    @Test
    public void readFully_hugeRequestFailsWithoutAllocating() throws IOException {
        // A corrupt stream /Length can request gigabytes from a tiny file. The
        // EOF check must run BEFORE the array allocation — with -Xmx defaults a
        // 2 GB allocation would OOM the JVM instead of failing the one object.
        byte[] data = {0x01, 0x02, 0x03};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertThrows(EOFException.class, () -> reader.readFully(Integer.MAX_VALUE - 8));
            // Reader stays usable at its original position.
            assertEquals(0, reader.getPosition());
            assertArrayEquals(data, reader.readFully(3));
        }
    }

    // ---- peek ----

    @Test
    public void peek_doesNotChangePosition() throws IOException {
        byte[] data = "ABC".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals('A', reader.peek());
            assertEquals(0, reader.getPosition());
            assertEquals('A', reader.peek());
            assertEquals(0, reader.getPosition());
            assertEquals('A', reader.read()); // now consume
            assertEquals(1, reader.getPosition());
            assertEquals('B', reader.peek());
            assertEquals(1, reader.getPosition());
        }
    }

    // ---- findBackward ----

    @Test
    public void findBackward_findsPattern() throws IOException {
        // Simulate end of PDF file
        String content = "some data here\nstartxref\n12345\n%%EOF\n";
        byte[] data = content.getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            long pos = reader.findBackward("startxref".getBytes(StandardCharsets.US_ASCII),
                    reader.getLength() - 1);
            assertEquals(content.indexOf("startxref"), pos);
        }
    }

    @Test
    public void findBackward_patternNotFound() throws IOException {
        byte[] data = "no such pattern here".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            long pos = reader.findBackward("startxref".getBytes(StandardCharsets.US_ASCII),
                    reader.getLength() - 1);
            assertEquals(-1, pos);
        }
    }

    // ---- isEOF ----

    @Test
    public void isEOF_trueAtEnd() throws IOException {
        byte[] data = {0x01};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertFalse(reader.isEOF());
            reader.read();
            assertTrue(reader.isEOF());
        }
    }

    @Test
    public void isEOF_falseAtStart() throws IOException {
        byte[] data = {0x01, 0x02};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertFalse(reader.isEOF());
        }
    }

    // ---- Large data: seek to various positions ----

    @Test
    public void largeData_seekAndRead() throws IOException {
        // 10MB of patterned data
        int size = 10 * 1024 * 1024;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i & 0xFF);
        }
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals(size, reader.getLength());

            // Read at start
            assertEquals(0, reader.read());

            // Seek to middle
            reader.seek(size / 2);
            assertEquals((size / 2) & 0xFF, reader.read());

            // Seek near end
            reader.seek(size - 1);
            assertEquals((size - 1) & 0xFF, reader.read());
            assertTrue(reader.isEOF());
        }
    }

    // ---- fromStream ----

    @Test
    public void fromStream_readsEntireStream() throws IOException {
        byte[] original = "Stream content for PDF".getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream bais = new ByteArrayInputStream(original);
        try (RandomAccessReader reader = RandomAccessReader.fromStream(bais)) {
            assertEquals(original.length, reader.getLength());

            // Verify seek works (proves it's in-memory)
            reader.seek(7);
            assertEquals('c', reader.read());

            reader.seek(0);
            byte[] readBack = reader.readFully(original.length);
            assertArrayEquals(original, readBack);
        }
    }

    // ---- read(byte[], off, len) ----

    @Test
    public void readBulk_partialRead() throws IOException {
        byte[] data = "ABCDEFGHIJ".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            byte[] buf = new byte[5];
            int n = reader.read(buf, 0, 5);
            assertEquals(5, n);
            assertArrayEquals("ABCDE".getBytes(StandardCharsets.US_ASCII), buf);
            assertEquals(5, reader.getPosition());
        }
    }

    // ---- skip ----

    @Test
    public void skip_advancesPosition() throws IOException {
        byte[] data = "ABCDEFGHIJ".getBytes(StandardCharsets.US_ASCII);
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            reader.skip(3);
            assertEquals(3, reader.getPosition());
            assertEquals('D', reader.read());
        }
    }

    // ---- close ----

    @Test
    public void readAfterClose_throwsIOException() throws IOException {
        byte[] data = {0x01};
        RandomAccessReader reader = RandomAccessReader.fromBytes(data);
        reader.close();
        assertThrows(IOException.class, reader::read);
    }

    // ---- File-based peek ----

    @Test
    public void fromFile_peek(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("peek.bin");
        Files.write(file, new byte[]{0x41, 0x42, 0x43}); // ABC
        try (RandomAccessReader reader = RandomAccessReader.fromFile(file)) {
            assertEquals(0x41, reader.peek());
            assertEquals(0, reader.getPosition());
            assertEquals(0x41, reader.read());
            assertEquals(1, reader.getPosition());
        }
    }

    // ---- File-based findBackward ----

    @Test
    public void fromFile_findBackward(@TempDir Path tempDir) throws IOException {
        String content = "junk data\nstartxref\n42\n%%EOF\n";
        Path file = tempDir.resolve("find.pdf");
        Files.write(file, content.getBytes(StandardCharsets.US_ASCII));
        try (RandomAccessReader reader = RandomAccessReader.fromFile(file)) {
            long pos = reader.findBackward("startxref".getBytes(StandardCharsets.US_ASCII),
                    reader.getLength() - 1);
            assertEquals(content.indexOf("startxref"), pos);
        }
    }

    // ---- Edge: read returns -1 at EOF ----

    @Test
    public void read_returnsNegativeOneAtEOF() throws IOException {
        byte[] data = {};
        try (RandomAccessReader reader = RandomAccessReader.fromBytes(data)) {
            assertEquals(-1, reader.read());
            assertTrue(reader.isEOF());
        }
    }
}
