package org.aspose.pdf.devices;

import org.aspose.pdf.Page;
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextExtractionOptions;
import org.aspose.pdf.text.TextExtractionOptions.TextFormattingMode;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Extracts text from a PDF page and writes it to an output stream.
 * <p>
 * Mirrors {@code Aspose.Pdf.Devices.TextDevice}: the device carries an
 * {@link TextExtractionOptions} (default formatting mode {@code Pure}) and
 * an {@link Charset} (default UTF-16LE — the analogue of .NET
 * {@code Encoding.Unicode}). Text is extracted via {@link TextAbsorber} and
 * then encoded with the configured charset.
 * </p>
 */
public class TextDevice {

    private static final Logger LOG = Logger.getLogger(TextDevice.class.getName());

    /** {@link Charset} matching .NET {@code Encoding.Unicode} (UTF-16 little-endian, no BOM). */
    public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_16LE;

    private TextExtractionOptions extractionOptions;
    private Charset encoding;

    /** Creates a TextDevice with default options ({@code Pure} mode) and Unicode encoding. */
    public TextDevice() {
        this(new TextExtractionOptions(TextFormattingMode.Pure), DEFAULT_ENCODING);
    }

    /** Creates a TextDevice with the given encoding and default options ({@code Pure} mode). */
    public TextDevice(Charset encoding) {
        this(new TextExtractionOptions(TextFormattingMode.Pure), encoding);
    }

    /** Creates a TextDevice with the given options and default Unicode encoding. */
    public TextDevice(TextExtractionOptions options) {
        this(options, DEFAULT_ENCODING);
    }

    /** Creates a TextDevice with the given options and encoding. */
    public TextDevice(TextExtractionOptions options, Charset encoding) {
        this.extractionOptions = options != null
                ? options : new TextExtractionOptions(TextFormattingMode.Pure);
        this.encoding = encoding != null ? encoding : DEFAULT_ENCODING;
    }

    /** Returns the current extraction options. */
    public TextExtractionOptions getExtractionOptions() {
        return extractionOptions;
    }

    /** Sets the extraction options (null is replaced with the default {@code Pure} options). */
    public void setExtractionOptions(TextExtractionOptions options) {
        this.extractionOptions = options != null
                ? options : new TextExtractionOptions(TextFormattingMode.Pure);
    }

    /** Returns the current output encoding. */
    public Charset getEncoding() {
        return encoding;
    }

    /** Sets the output encoding (null is replaced with the default Unicode/UTF-16LE). */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding != null ? encoding : DEFAULT_ENCODING;
    }

    /**
     * Extracts text from a page and writes it to the output stream using the
     * configured encoding. The output stream is <em>not</em> closed by this
     * method — callers retain ownership.
     *
     * @param page   the PDF page to extract text from
     * @param output the output stream
     * @throws IOException if extraction or writing fails
     */
    public void process(Page page, OutputStream output) throws IOException {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("OutputStream must not be null");
        }

        TextAbsorber absorber = new TextAbsorber(extractionOptions);
        page.accept(absorber);
        String text = absorber.getText();
        if (text != null && !text.isEmpty()) {
            output.write(text.getBytes(encoding));
        }
        LOG.fine(() -> "TextDevice extracted " + (text != null ? text.length() : 0) + " chars to "
                + encoding.name());
    }

    /**
     * Extracts text from a page and writes it to the file at {@code outputFile}
     * using the configured encoding. Creates/overwrites the file.
     *
     * @param page       the PDF page to extract text from
     * @param outputFile path to the output file
     * @throws IOException if extraction or writing fails
     */
    public void process(Page page, String outputFile) throws IOException {
        if (outputFile == null) {
            throw new IllegalArgumentException("outputFile must not be null");
        }
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            process(page, out);
        }
    }
}
