package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Represents an embedded file specification (ISO 32000-1:2008, §7.11.3, Table 44).
 */
public class FileSpecification {

    private static final Logger LOG = Logger.getLogger(FileSpecification.class.getName());

    private final PdfDictionary dict;

    /** Wraps an existing file specification dictionary. */
    public FileSpecification(PdfDictionary dict) {
        this.dict = dict != null ? dict : new PdfDictionary();
    }

    /**
     * Creates a file specification from {@code file}, with no description.
     * Equivalent to {@code FileSpecification(file, null)}.
     *
     * @param file the file path
     */
    public FileSpecification(String file) {
        this(file, null);
    }

    /**
     * Creates a file specification with the given file name and description.
     *
     * @param file        the file name
     * @param description the human-readable description of the file
     */
    public FileSpecification(String file, String description) {
        this.dict = new PdfDictionary();
        dict.set(PdfName.of("Type"), PdfName.of("Filespec"));
        if (file != null) {
            Path path = Path.of(file);
            String storedName = path.getFileName() != null ? path.getFileName().toString() : file;
            setName(storedName);
            setUnicodeFileName(storedName);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                try {
                    setEmbeddedFileData(Files.readAllBytes(path));
                } catch (IOException e) {
                    LOG.fine(() -> "Failed to read embedded file data from '" + file + "': " + e.getMessage());
                }
            }
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /** Creates a file specification from an InputStream. */
    public FileSpecification(InputStream stream, String name) throws IOException {
        this.dict = new PdfDictionary();
        dict.set(PdfName.of("Type"), PdfName.of("Filespec"));
        setName(name);
        setUnicodeFileName(name);
        setEmbeddedFileData(readAll(stream));
    }

    // ── Properties ──

    /** /F — file name. */
    public String getName() {
        PdfBase f = dict.get("F");
        if (f instanceof PdfString) return ((PdfString) f).getString();
        if (f instanceof PdfName) return ((PdfName) f).getName();
        return null;
    }

    /** Sets the file name (/F). */
    public void setName(String name) {
        dict.set(PdfName.of("F"), new PdfString(name));
    }

    /** /UF — Unicode file name. */
    public String getUnicodeFileName() {
        PdfBase uf = dict.get("UF");
        return (uf instanceof PdfString) ? ((PdfString) uf).getString() : null;
    }

    /** Sets the Unicode file name (/UF). */
    public void setUnicodeFileName(String name) {
        dict.set(PdfName.of("UF"), new PdfString(name));
    }

    /** /Desc — description. */
    public String getDescription() {
        PdfBase desc = dict.get("Desc");
        return (desc instanceof PdfString) ? ((PdfString) desc).getString() : null;
    }

    /** Sets the description (/Desc). */
    public void setDescription(String desc) {
        dict.set(PdfName.of("Desc"), new PdfString(desc));
    }

    /** /AFRelationship. */
    public String getRelationship() { return dict.getNameAsString("AFRelationship"); }

    /** Sets the AF relationship. */
    public void setRelationship(String rel) {
        dict.set(PdfName.of("AFRelationship"), PdfName.of(rel));
    }

    /** MIME type from embedded stream /Subtype. */
    public String getMIMEType() {
        PdfStream s = getEmbeddedStream();
        return s != null ? s.getNameAsString("Subtype") : null;
    }

    /** Sets the MIME type on the embedded stream. */
    public void setMIMEType(String mimeType) {
        PdfStream s = getEmbeddedStream();
        if (s != null) s.set(PdfName.of("Subtype"), PdfName.of(mimeType));
    }

    /** Returns file params (size, dates, checksum). */
    public FileParams getParams() {
        PdfStream s = getEmbeddedStream();
        if (s == null) return null;
        PdfBase p = s.get("Params");
        return (p instanceof PdfDictionary) ? new FileParams((PdfDictionary) p) : null;
    }

    /**
     * Returns the embedded file contents as an InputStream.
     *
     * @return the embedded file content stream, or null if no data is available
     * @throws IOException if reading the stream data fails
     */
    public InputStream getContents() throws IOException {
        byte[] data = getData();
        return data != null ? new java.io.ByteArrayInputStream(data) : null;
    }

    /**
     * Sets the MIME type (alias for {@link #setMIMEType(String)}).
     *
     * @param mimeType the MIME type string
     */
    public void setMimeType(String mimeType) {
        setMIMEType(mimeType);
    }

    /**
     * Returns the MIME type (alias for {@link #getMIMEType()}).
     *
     * @return the MIME type string, or null
     */
    public String getMimeType() {
        return getMIMEType();
    }

    /** Returns the embedded file data. */
    public byte[] getData() throws IOException {
        PdfStream s = getEmbeddedStream();
        return s != null ? s.getDecodedData() : null;
    }

    /** Returns the embedded file stream from /EF/F. */
    public PdfStream getEmbeddedStream() {
        PdfBase ef = dict.get("EF");
        if (ef instanceof PdfDictionary) {
            PdfBase f = resolveRef(((PdfDictionary) ef).get("F"));
            if (f instanceof PdfStream) return (PdfStream) f;
        }
        return null;
    }

    private void setEmbeddedFileData(byte[] data) {
        PdfStream stream = new PdfStream(new PdfDictionary(), data);
        stream.set(PdfName.of("Type"), PdfName.of("EmbeddedFile"));
        PdfDictionary params = new PdfDictionary();
        params.set(PdfName.of("Size"), PdfInteger.valueOf(data.length));
        stream.set(PdfName.of("Params"), params);
        PdfDictionary ef = new PdfDictionary();
        ef.set(PdfName.of("F"), stream);
        dict.set(PdfName.of("EF"), ef);
    }

    /** Returns the underlying dictionary. */
    public PdfDictionary getPdfDictionary() { return dict; }

    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) val).dereference(); } catch (Exception e) { return null; }
        }
        return val;
    }

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toByteArray();
    }
}
