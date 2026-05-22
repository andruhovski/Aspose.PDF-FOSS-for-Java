package org.aspose.pdf;

import org.aspose.pdf.engine.cos.*;

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

    private final COSDictionary dict;

    /** Wraps an existing file specification dictionary. */
    public FileSpecification(COSDictionary dict) {
        this.dict = dict != null ? dict : new COSDictionary();
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
        this.dict = new COSDictionary();
        dict.set(COSName.of("Type"), COSName.of("Filespec"));
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
        this.dict = new COSDictionary();
        dict.set(COSName.of("Type"), COSName.of("Filespec"));
        setName(name);
        setUnicodeFileName(name);
        setEmbeddedFileData(readAll(stream));
    }

    // ── Properties ──

    /** /F — file name. */
    public String getName() {
        COSBase f = dict.get("F");
        if (f instanceof COSString) return ((COSString) f).getString();
        if (f instanceof COSName) return ((COSName) f).getName();
        return null;
    }

    /** Sets the file name (/F). */
    public void setName(String name) {
        dict.set(COSName.of("F"), new COSString(name.getBytes(StandardCharsets.UTF_8)));
    }

    /** /UF — Unicode file name. */
    public String getUnicodeFileName() {
        COSBase uf = dict.get("UF");
        return (uf instanceof COSString) ? ((COSString) uf).getString() : null;
    }

    /** Sets the Unicode file name (/UF). */
    public void setUnicodeFileName(String name) {
        dict.set(COSName.of("UF"), new COSString(name.getBytes(StandardCharsets.UTF_8)));
    }

    /** /Desc — description. */
    public String getDescription() {
        COSBase desc = dict.get("Desc");
        return (desc instanceof COSString) ? ((COSString) desc).getString() : null;
    }

    /** Sets the description (/Desc). */
    public void setDescription(String desc) {
        dict.set(COSName.of("Desc"), new COSString(desc.getBytes(StandardCharsets.UTF_8)));
    }

    /** /AFRelationship. */
    public String getRelationship() { return dict.getNameAsString("AFRelationship"); }

    /** Sets the AF relationship. */
    public void setRelationship(String rel) {
        dict.set(COSName.of("AFRelationship"), COSName.of(rel));
    }

    /** MIME type from embedded stream /Subtype. */
    public String getMIMEType() {
        COSStream s = getEmbeddedStream();
        return s != null ? s.getNameAsString("Subtype") : null;
    }

    /** Sets the MIME type on the embedded stream. */
    public void setMIMEType(String mimeType) {
        COSStream s = getEmbeddedStream();
        if (s != null) s.set(COSName.of("Subtype"), COSName.of(mimeType));
    }

    /** Returns file params (size, dates, checksum). */
    public FileParams getParams() {
        COSStream s = getEmbeddedStream();
        if (s == null) return null;
        COSBase p = s.get("Params");
        return (p instanceof COSDictionary) ? new FileParams((COSDictionary) p) : null;
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
        COSStream s = getEmbeddedStream();
        return s != null ? s.getDecodedData() : null;
    }

    /** Returns the embedded file stream from /EF/F. */
    public COSStream getEmbeddedStream() {
        COSBase ef = dict.get("EF");
        if (ef instanceof COSDictionary) {
            COSBase f = resolveRef(((COSDictionary) ef).get("F"));
            if (f instanceof COSStream) return (COSStream) f;
        }
        return null;
    }

    private void setEmbeddedFileData(byte[] data) {
        COSStream stream = new COSStream(new COSDictionary(), data);
        stream.set(COSName.of("Type"), COSName.of("EmbeddedFile"));
        COSDictionary params = new COSDictionary();
        params.set(COSName.of("Size"), COSInteger.valueOf(data.length));
        stream.set(COSName.of("Params"), params);
        COSDictionary ef = new COSDictionary();
        ef.set(COSName.of("F"), stream);
        dict.set(COSName.of("EF"), ef);
    }

    /** Returns the underlying dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }

    private COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try { return ((COSObjectReference) val).dereference(); } catch (Exception e) { return null; }
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
