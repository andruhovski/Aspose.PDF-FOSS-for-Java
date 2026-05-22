package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
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

    private final COSStream stream;
    private final String name;
    private final PDFParser parser;

    /**
     * Creates an XForm from a form XObject stream.
     *
     * @param stream the form XObject COSStream
     * @param name   the resource name (e.g., "Fm1")
     * @param parser the PDF parser (may be null)
     */
    public XForm(COSStream stream, String name, PDFParser parser) {
        this.stream = stream != null ? stream : new COSStream();
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
        COSBase bbox = stream.get("BBox");
        if (bbox instanceof COSArray && ((COSArray) bbox).size() == 4) {
            return Rectangle.fromCOSArray((COSArray) bbox);
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
        COSBase m = stream.get("Matrix");
        if (m instanceof COSArray && ((COSArray) m).size() == 6) {
            return Matrix.fromCOSArray((COSArray) m);
        }
        return Matrix.IDENTITY;
    }

    /**
     * Returns the form's own /Resources dictionary.
     *
     * @return the Resources, or null if absent
     */
    public Resources getResources() {
        COSBase res = resolveRef(stream.get("Resources"));
        if (res instanceof COSDictionary) {
            return new Resources((COSDictionary) res);
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
     * {@link #getContents()} can be persisted back to the COSStream.
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
     * Returns the underlying COS stream.
     *
     * @return the form XObject stream
     */
    public COSStream getCOSStream() {
        return stream;
    }

    private COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference: " + e.getMessage());
                return null;
            }
        }
        return val;
    }
}
