package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Collection of Form XObjects from a resource dictionary's /XObject entry
 * (ISO 32000-1:2008, §8.10).
 *
 * <p>Filters the /XObject sub-dictionary to entries whose stream has
 * {@code /Subtype /Form}. The collection is a live view: changes to the
 * underlying dictionary are reflected on the next access (results are not
 * cached). Aspose-convention 1-based indexed access via {@link #get(int)} is
 * provided; iteration order matches dictionary insertion order.</p>
 */
public class XFormCollection implements Iterable<XForm> {

    private static final Logger LOG = Logger.getLogger(XFormCollection.class.getName());

    private final PdfDictionary xobjectDict;
    private final PDFParser parser;

    /**
     * Creates a view over an /XObject dictionary, exposing only Form XObjects.
     *
     * @param xobjectDict the /XObject dictionary (must not be null)
     * @param parser      the PDF parser for indirect-reference resolution (may be null)
     * @throws IllegalArgumentException if {@code xobjectDict} is null
     */
    public XFormCollection(PdfDictionary xobjectDict, PDFParser parser) {
        if (xobjectDict == null) {
            throw new IllegalArgumentException("XObject dictionary must not be null");
        }
        this.xobjectDict = xobjectDict;
        this.parser = parser;
    }

    /**
     * Returns the number of Form XObjects in this collection.
     *
     * @return the count of /Subtype /Form entries
     */
    public int size() {
        int count = 0;
        for (PdfName key : xobjectDict.keySet()) {
            if (isForm(resolve(xobjectDict.get(key)))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns true when no Form XObjects are present.
     *
     * @return true if {@link #size()} == 0
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the XForm at the given 1-based index (Aspose convention).
     *
     * @param index 1-based index in iteration order
     * @return the XForm at that position
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public XForm get(int index) {
        List<XForm> forms = collectForms();
        if (index < 1 || index > forms.size()) {
            throw new IndexOutOfBoundsException(
                    "Form index " + index + " out of range [1, " + forms.size() + "]");
        }
        return forms.get(index - 1);
    }

    /**
     * Returns the XForm registered under the given resource name (e.g. {@code "Fm1"}),
     * or {@code null} if the name is absent or the entry is not a Form XObject.
     *
     * @param name the resource name (without leading slash)
     * @return the matching XForm, or null
     */
    public XForm get(String name) {
        if (name == null) {
            return null;
        }
        PdfBase resolved = resolve(xobjectDict.get(name));
        if (!isForm(resolved)) {
            return null;
        }
        return new XForm((PdfStream) resolved, name, parser);
    }

    /**
     * Returns all Form XObject names in iteration order. Lazily computed.
     *
     * @return immutable list of names; empty if none
     */
    public List<String> getNames() {
        List<String> result = new ArrayList<>();
        for (PdfName key : xobjectDict.keySet()) {
            if (isForm(resolve(xobjectDict.get(key)))) {
                result.add(key.getName());
            }
        }
        return result;
    }

    @Override
    public Iterator<XForm> iterator() {
        final List<XForm> forms = collectForms();
        return new Iterator<XForm>() {
            int idx = 0;
            @Override public boolean hasNext() { return idx < forms.size(); }
            @Override public XForm next() {
                if (!hasNext()) throw new NoSuchElementException();
                return forms.get(idx++);
            }
        };
    }

    private List<XForm> collectForms() {
        List<XForm> out = new ArrayList<>();
        for (PdfName key : xobjectDict.keySet()) {
            PdfBase resolved = resolve(xobjectDict.get(key));
            if (isForm(resolved)) {
                out.add(new XForm((PdfStream) resolved, key.getName(), parser));
            }
        }
        return out;
    }

    private PdfBase resolve(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference XObject entry: " + e.getMessage());
                return null;
            }
        }
        return value;
    }

    private static boolean isForm(PdfBase value) {
        if (!(value instanceof PdfStream)) {
            return false;
        }
        PdfBase subtype = ((PdfStream) value).get(PdfName.SUBTYPE);
        return subtype instanceof PdfName && "Form".equals(((PdfName) subtype).getName());
    }
}
