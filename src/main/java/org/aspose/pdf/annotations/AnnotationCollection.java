package org.aspose.pdf.annotations;

import org.aspose.pdf.Page;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.engine.parser.PDFParser;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Collection of annotations on a page (ISO 32000-1:2008, §12.5).
 * Wraps the /Annots array. Uses 1-based indexing.
 */
public class AnnotationCollection implements Iterable<Annotation> {
    private static final Logger LOG = Logger.getLogger(AnnotationCollection.class.getName());
    private final PdfArray annotsArray;
    private final Page page;
    private final PDFParser parser;
    private List<Annotation> annotations;

    /**
     * Constructs an annotation collection wrapping the given /Annots PdfArray.
     *
     * @param annotsArray the PDF array of annotation dictionaries (or references); if null, an empty array is used
     * @param page        the page these annotations belong to
     * @param parser      the PDF parser for resolving indirect references
     */
    public AnnotationCollection(PdfArray annotsArray, Page page, PDFParser parser) {
        this.annotsArray = annotsArray != null ? annotsArray : new PdfArray();
        this.page = page;
        this.parser = parser;
    }

    /**
     * Returns the annotation at the specified 1-based index.
     *
     * @param index the 1-based index
     * @return the annotation at the given index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Annotation get(int index) {
        ensureLoaded();
        if (index < 1 || index > annotations.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of [1," + annotations.size() + "]");
        return annotations.get(index - 1);
    }

    /**
     * Returns the number of annotations in this collection.
     *
     * @return the annotation count
     */
    public int getCount() { ensureLoaded(); return annotations.size(); }

    /**
     * Returns the number of annotations in this collection.
     *
     * @return the annotation count
     */
    public int size() { return getCount(); }

    /**
     * Adds an annotation to this collection and the underlying PDF array.
     *
     * @param annotation the annotation to add
     */
    public void add(Annotation annotation) {
        // Do NOT force ensureLoaded() here: each Page.getAnnotations() hands back a
        // fresh, unloaded wrapper, so loading the whole /Annots array on every add()
        // makes a sequence of N adds cost O(N^2) (e.g. HTML→PDF with thousands of
        // <a href> links — see PDFNET_40534). Appending to the PDF array is enough;
        // a later ensureLoaded() rebuilds the list from the array, new entry included.
        if (annotations != null) {
            annotations.add(annotation);
        }
        PdfDictionary annotDict = annotation.getPdfDictionary();
        // Newly-created annotation dicts are DIRECT objects. A /Annots array must
        // hold INDIRECT references (ISO 32000-1 §12.5.2); appending a direct dict to
        // an existing /Annots that already holds indirect refs corrupts the array on
        // save and drops every annotation on reload (PDFNET_42398). Register the dict
        // as an indirect object and store a reference instead. Dicts already loaded
        // from the file (objectKey set) are referenced as-is.
        PdfBase entry = annotDict;
        if (annotDict != null && annotDict.getObjectKey() == null && page != null) {
            org.aspose.pdf.Document doc = page.getOwningDocument();
            if (doc != null) {
                entry = doc.registerImportedObject(annotDict);
            }
        }
        annotsArray.add(entry);
    }

    /**
     * Removes the annotation at the specified 1-based index.
     *
     * @param index the 1-based index of the annotation to remove
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void delete(int index) {
        ensureLoaded();
        if (index < 1 || index > annotations.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of [1," + annotations.size() + "]");
        Annotation removed = annotations.remove(index - 1);
        // Remove matching dict from PdfArray
        for (int i = 0; i < annotsArray.size(); i++) {
            PdfBase item = resolveRef(annotsArray.get(i));
            if (item == removed.getPdfDictionary()) { annotsArray.remove(i); break; }
        }
    }

    /**
     * Removes the specified annotation from this collection.
     *
     * @param annotation the annotation to remove
     */
    public void delete(Annotation annotation) {
        ensureLoaded();
        annotations.remove(annotation);
        for (int i = 0; i < annotsArray.size(); i++) {
            PdfBase item = resolveRef(annotsArray.get(i));
            if (item == annotation.getPdfDictionary()) { annotsArray.remove(i); break; }
        }
    }

    /**
     * Returns an iterator over the annotations in this collection.
     *
     * @return an iterator
     */
    @Override
    public Iterator<Annotation> iterator() { ensureLoaded(); return annotations.iterator(); }

    private void ensureLoaded() {
        if (annotations != null) return;
        annotations = new ArrayList<>();
        for (int i = 0; i < annotsArray.size(); i++) {
            PdfBase item = resolveRef(annotsArray.get(i));
            if (item instanceof PdfDictionary) {
                annotations.add(Annotation.fromDictionary((PdfDictionary) item, page));
            }
        }
    }

    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) val).dereference(); }
            catch (Exception e) { return null; }
        }
        return val;
    }
}
