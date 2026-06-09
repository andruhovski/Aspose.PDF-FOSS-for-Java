package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.parser.ContentStreamParser;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Represents a Form XObject (ISO 32000-1:2008, §8.10).
 * <p>
 * A form XObject is a self-contained content stream with its own resources,
 * bounding box, and optional transformation matrix. It is invoked via the
 * "Do" operator and can contain any graphics operators.
 * </p>
 */
public class XForm {

    private static final Logger LOG = Logger.getLogger(XForm.class.getName());

    private final PdfStream stream;
    private final String name;
    private final PDFParser parser;

    /**
     * Creates an XForm from a form XObject stream.
     *
     * @param stream the form XObject PdfStream
     * @param name   the resource name (e.g., "Fm1")
     * @param parser the PDF parser (may be null)
     */
    public XForm(PdfStream stream, String name, PDFParser parser) {
        this.stream = stream != null ? stream : new PdfStream();
        this.name = name;
        this.parser = parser;
    }

    /**
     * Returns the resource name of this form XObject.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the bounding box (/BBox) of this form XObject.
     *
     * @return the bounding box, or null if absent
     */
    public Rectangle getBBox() {
        PdfBase bbox = stream.get("BBox");
        if (bbox instanceof PdfArray && ((PdfArray) bbox).size() == 4) {
            return Rectangle.fromPdfArray((PdfArray) bbox);
        }
        return null;
    }

    /**
     * Returns the transformation matrix (/Matrix).
     * Defaults to the identity matrix if absent.
     *
     * @return the matrix
     */
    public Matrix getMatrix() {
        PdfBase m = stream.get("Matrix");
        if (m instanceof PdfArray && ((PdfArray) m).size() == 6) {
            return Matrix.fromPdfArray((PdfArray) m);
        }
        return Matrix.IDENTITY;
    }

    /**
     * Returns the form's own /Resources dictionary.
     *
     * @return the Resources, or null if absent
     */
    public Resources getResources() {
        PdfBase res = resolveRef(stream.get("Resources"));
        if (res instanceof PdfDictionary) {
            return new Resources((PdfDictionary) res);
        }
        return null;
    }

    /**
     * Parses the content stream of this form XObject into operators.
     *
     * @return the parsed operator collection
     * @throws IOException if parsing fails
     */
    public OperatorCollection getContents() throws IOException {
        return ContentStreamParser.parseToCollection(stream);
    }

    /**
     * Replaces the content stream of this form XObject with the serialized form
     * of {@code contents}. Mirrors C# {@code XForm.Contents = …} semantics so
     * mutations made to the {@link OperatorCollection} returned by
     * {@link #getContents()} can be persisted back to the PdfStream.
     *
     * @param contents the new operator collection to serialize
     */
    public void setContents(OperatorCollection contents) {
        if (contents == null) {
            stream.setDecodedData(new byte[0]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Operator op : contents) {
            sb.append(op.toString()).append('\n');
        }
        stream.setDecodedData(sb.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    /**
     * Returns the underlying PDF stream.
     *
     * @return the form XObject stream
     */
    public PdfStream getPdfStream() {
        return stream;
    }

    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference: " + e.getMessage());
                return null;
            }
        }
        return val;
    }
}
