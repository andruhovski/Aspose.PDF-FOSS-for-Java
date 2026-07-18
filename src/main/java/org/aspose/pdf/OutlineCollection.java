package org.aspose.pdf;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/// Root outline collection — the /Outlines dictionary in the document catalog
/// (ISO 32000-1:2008, §12.3.3).
///
/// Represents the entire bookmark tree. Access via `document.getOutlines()`.
///
public class OutlineCollection implements Iterable<OutlineItemCollection> {

    private static final Logger LOG = Logger.getLogger(OutlineCollection.class.getName());

    private PdfDictionary outlinesDict;
    private final Document document;
    private final PDFParser parser;
    private List<OutlineItemCollection> items;

    /// Wraps an existing /Outlines dictionary.
    ///
    /// @param outlinesDict the /Outlines PDF dictionary
    /// @param document     the owning document (may be null)
    /// @param parser       the PDF parser (may be null)
    public OutlineCollection(PdfDictionary outlinesDict, Document document, PDFParser parser) {
        this.outlinesDict = outlinesDict != null ? outlinesDict : new PdfDictionary();
        this.document = document;
        this.parser = parser;
    }

    /// Creates an empty outline collection (when document has no bookmarks).
    ///
    /// @param document the owning document (may be null)
    /// @param parser   the PDF parser (may be null)
    public OutlineCollection(Document document, PDFParser parser) {
        this.outlinesDict = new PdfDictionary();
        outlinesDict.set(PdfName.TYPE, PdfName.of("Outlines"));
        outlinesDict.set(PdfName.of("Count"), PdfInteger.valueOf(0));
        this.document = document;
        this.parser = parser;
    }

    /// Adds a top-level bookmark.
    ///
    /// @param item the bookmark item
    public void add(OutlineItemCollection item) {
        ensureLoaded();
        // Materialize indirect references in the item dictionary to ensure
        // cross-document copies work (references from source parser resolved to direct values)
        materializeReferences(item.getPdfDictionary());
        items.add(item);
        rebuildLinks();
    }

    /// Resolves any indirect references in the dictionary to direct values.
    /// This is needed when copying outline items from one document to another.
    private void materializeReferences(PdfDictionary dict) {
        for (PdfName key : new ArrayList<>(dict.keySet())) {
            PdfBase val = dict.get(key.getName());
            if (val instanceof PdfObjectReference) {
                try {
                    PdfBase resolved = ((PdfObjectReference) val).dereference();
                    if (resolved != null) {
                        dict.set(key, resolved);
                    }
                } catch (Exception e) {
                    LOG.fine(() -> "Failed to resolve " + key + " in outline item: " + e.getMessage());
                }
            }
        }
    }

    /// Returns the bookmark at the given 1-based index.
    ///
    /// @param index the 1-based index
    /// @return the bookmark item
    public OutlineItemCollection get(int index) {
        ensureLoaded();
        if (index < 1 || index > items.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of [1," + items.size() + "]");
        }
        return items.get(index - 1);
    }

    /// Returns the first top-level outline item, or null if the collection is empty.
    ///
    /// @return the first outline item, or null
    public OutlineItemCollection getFirst() {
        ensureLoaded();
        return items.isEmpty() ? null : items.get(0);
    }

    /// Returns the last top-level outline item, or null if the collection is empty.
    ///
    /// @return the last outline item, or null
    public OutlineItemCollection getLast() {
        ensureLoaded();
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /// Returns the number of top-level bookmarks.
    ///
    /// @return the count
    public int getCount() {
        ensureLoaded();
        return items.size();
    }

    /// Returns the total number of visible items at all levels.
    ///
    /// @return the visible count
    public int getVisibleCount() {
        ensureLoaded();
        int count = items.size();
        for (OutlineItemCollection item : items) {
            count += item.getVisibleCount();
        }
        return count;
    }

    /// Removes a bookmark by 1-based index.
    ///
    /// @param index the 1-based index
    public void delete(int index) {
        ensureLoaded();
        items.remove(index - 1);
        rebuildLinks();
    }

    /// Removes all bookmarks.
    public void clear() {
        items = new ArrayList<>();
        rebuildLinks();
    }

    /// Deletes the first outline item with the specified title.
    ///
    /// @param title the title to search for
    public void delete(String title) {
        ensureLoaded();
        for (int i = 0; i < items.size(); i++) {
            if (title.equals(items.get(i).getTitle())) {
                items.remove(i);
                rebuildLinks();
                return;
            }
            // Search children recursively
            items.get(i).delete(title);
        }
    }

    /// Checks whether this collection contains the specified item.
    ///
    /// @param item the item to search for
    /// @return true if found
    public boolean contains(OutlineItemCollection item) {
        ensureLoaded();
        for (OutlineItemCollection child : items) {
            if (child == item) return true;
            if (child.contains(item)) return true;
        }
        return false;
    }

    @Override
    public Iterator<OutlineItemCollection> iterator() {
        ensureLoaded();
        return items.iterator();
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the /Outlines dictionary
    public PdfDictionary getPdfDictionary() { return outlinesDict; }

    PDFParser getParser() { return parser; }
    Document getDocument() { return document; }

    // ── Internal ──

    private void ensureLoaded() {
        if (items != null) return;
        items = new ArrayList<>();
        if (outlinesDict == null) return;
        PdfBase first = resolve(outlinesDict.get("First"));
        PdfBase current = first;
        int guard = 10000;
        while (current instanceof PdfDictionary && guard-- > 0) {
            PdfDictionary childDict = (PdfDictionary) current;
            items.add(new OutlineItemCollection(childDict, this, parser));
            current = resolve(childDict.get("Next"));
            if (current == first) break;
        }
        LOG.fine(() -> "Loaded " + items.size() + " top-level outline items");
    }

    private void rebuildLinks() {
        if (items.isEmpty()) {
            outlinesDict.remove(PdfName.of("First"));
            outlinesDict.remove(PdfName.of("Last"));
            outlinesDict.set(PdfName.of("Count"), PdfInteger.valueOf(0));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            PdfDictionary cd = items.get(i).getPdfDictionary();
            cd.set(PdfName.of("Parent"), outlinesDict);
            if (i == 0) outlinesDict.set(PdfName.of("First"), cd);
            if (i == items.size() - 1) outlinesDict.set(PdfName.of("Last"), cd);
            if (i > 0) cd.set(PdfName.of("Prev"), items.get(i - 1).getPdfDictionary());
            else cd.remove(PdfName.of("Prev"));
            if (i < items.size() - 1) cd.set(PdfName.of("Next"), items.get(i + 1).getPdfDictionary());
            else cd.remove(PdfName.of("Next"));
        }
        outlinesDict.set(PdfName.of("Count"), PdfInteger.valueOf(items.size()));
    }

    private PdfBase resolve(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) val).dereference(); }
            catch (Exception e) { return null; }
        }
        return val;
    }
}
