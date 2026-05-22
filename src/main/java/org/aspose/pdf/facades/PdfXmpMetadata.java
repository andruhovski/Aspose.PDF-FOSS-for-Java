package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.XmpMetadata;
import org.aspose.pdf.XmpValue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin facade over {@link Document#getMetadata()}, mirroring
 * {@code Aspose.Pdf.Facades.PdfXmpMetadata}. Exposes XMP property access via
 * {@link #get(String)}, {@link #add(String, String)}, {@link #contains(String)}
 * and the conventional bind-pdf / save lifecycle of the legacy facade family.
 */
public class PdfXmpMetadata implements Closeable {

    private static final Logger LOG = Logger.getLogger(PdfXmpMetadata.class.getName());

    private Document document;
    private boolean ownsDocument;

    /** Empty PdfXmpMetadata. Call {@link #bindPdf(String)} or {@link #bindPdf(Document)} before use. */
    public PdfXmpMetadata() {
    }

    /** Bound to {@code inputFile}. */
    public PdfXmpMetadata(String inputFile) {
        bindPdf(inputFile);
    }

    /** Bound to an already-loaded document. */
    public PdfXmpMetadata(Document document) {
        bindPdf(document);
    }

    /** Returns the bound document, or {@code null}. */
    public Document getDocument() {
        return document;
    }

    /** Loads a fresh {@link Document} from {@code inputFile}. */
    public boolean bindPdf(String inputFile) {
        try {
            this.document = new Document(inputFile);
            this.ownsDocument = true;
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /** Loads a fresh {@link Document} from {@code inputStream}. */
    public boolean bindPdf(InputStream inputStream) {
        try {
            this.document = new Document(inputStream);
            this.ownsDocument = true;
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /** Binds an already-loaded document. */
    public boolean bindPdf(Document document) {
        this.document = document;
        this.ownsDocument = false;
        return document != null;
    }

    /** Returns the {@link XmpMetadata} of the bound document. */
    public XmpMetadata getXmpMetadata() {
        if (document == null) return null;
        try {
            return document.getMetadata();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read document metadata", e);
            return null;
        }
    }

    /** Returns whether the XMP bag contains a property with the given key. */
    public boolean contains(String key) {
        XmpMetadata m = getXmpMetadata();
        return m != null && m.contains(key);
    }

    /**
     * Returns the property value for {@code key}, or {@code null} when absent.
     * Mirrors the C# indexer {@code metadata[key]}.
     */
    public XmpValue get(String key) {
        XmpMetadata m = getXmpMetadata();
        return m != null ? m.get(key) : null;
    }

    /** Adds (or replaces) a string property under {@code key}. */
    public void add(String key, String value) {
        XmpMetadata m = getXmpMetadata();
        if (m != null) m.add(key, value);
    }

    /** Adds (or replaces) a typed property under {@code key}. */
    public void add(String key, XmpValue value) {
        XmpMetadata m = getXmpMetadata();
        if (m != null) m.add(key, value);
    }

    /** Sets a string property under {@code key} (replace semantics). */
    public void set(String key, String value) {
        XmpMetadata m = getXmpMetadata();
        if (m != null) m.set(key, value);
    }

    /** Removes the property under {@code key}, if present. */
    public void remove(String key) {
        XmpMetadata m = getXmpMetadata();
        if (m != null) m.remove(key);
    }

    /** Saves the bound document to {@code outputFile}. */
    public boolean save(String outputFile) {
        if (document == null) return false;
        try {
            document.requestFullRewrite();
            document.save(outputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PdfXmpMetadata to file: " + outputFile, e);
            return false;
        }
    }

    @Override
    public void close() {
        if (document != null && ownsDocument) {
            try { document.close(); } catch (IOException ignored) {}
        }
        document = null;
    }
}
