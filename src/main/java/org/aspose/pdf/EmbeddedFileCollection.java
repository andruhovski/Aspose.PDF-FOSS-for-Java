package org.aspose.pdf;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/// Collection of embedded files (attachments) in a PDF document.
///
/// Wraps the `/Names → /EmbeddedFiles` name tree in the catalog
/// (ISO 32000-1:2008, §7.11.4). Traversal and mutation go through
/// [NameTree], which keeps `/Names` sorted and `/Limits`
/// in sync after every insert/delete. Uses 1-based indexing (Aspose
/// convention).
///
public class EmbeddedFileCollection implements Iterable<FileSpecification> {

    private static final Logger LOG = Logger.getLogger(EmbeddedFileCollection.class.getName());

    private static final PdfName NAMES = PdfName.of("Names");
    private static final PdfName EMBEDDED_FILES = PdfName.of("EmbeddedFiles");

    private final Document document;
    private final PDFParser parser;
    private List<FileSpecification> files;

    EmbeddedFileCollection(Document document, PDFParser parser) {
        this.document = document;
        this.parser = parser;
    }

    /// Returns the file at the given 1-based index.
    public FileSpecification get(int index) {
        ensureLoaded();
        if (index < 1 || index > files.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of [1," + files.size() + "]");
        return files.get(index - 1);
    }

    /// Returns the number of embedded files.
    public int getCount() { ensureLoaded(); return files.size(); }

    /// Returns the number of embedded files (alias).
    public int size() { return getCount(); }

    /// Adds a file specification.
    public void add(FileSpecification fs) {
        ensureLoaded();
        files.add(fs);
        addToNameTree(fs);
    }

    /// Returns the file specification with the given name.
    /// Searches by the /F (file name) entry of each file specification.
    ///
    /// @param name the file name to search for
    /// @return the matching FileSpecification, or null if not found
    public FileSpecification get(String name) {
        ensureLoaded();
        if (name == null) return null;
        for (FileSpecification fs : files) {
            if (name.equals(fs.getName())) return fs;
        }
        return null;
    }

    /// Removes all embedded files.
    public void delete() {
        ensureLoaded();
        files.clear();
        try {
            PdfDictionary catalog = document.getCatalog();
            PdfBase names = resolveRef(catalog.get(NAMES));
            if (names instanceof PdfDictionary) {
                ((PdfDictionary) names).remove(EMBEDDED_FILES);
            }
        } catch (IOException e) {
            LOG.warning(() -> "Failed to remove EmbeddedFiles: " + e.getMessage());
        }
    }

    /// Removes by 1-based index.
    public void delete(int index) {
        ensureLoaded();
        FileSpecification removed = files.remove(index - 1);
        if (removed != null) {
            removeFromNameTreeByName(removed.getName());
        }
    }

    /// Removes the first embedded file with the given name (Aspose semantics —
    /// if a portfolio carries multiple attachments under the same `/F`,
    /// only one is removed per call).
    ///
    /// @param name the file name to remove
    public void delete(String name) {
        ensureLoaded();
        if (name == null) return;
        for (Iterator<FileSpecification> it = files.iterator(); it.hasNext(); ) {
            if (name.equals(it.next().getName())) {
                it.remove();
                removeFromNameTreeByName(name);
                return;
            }
        }
    }

    @Override
    public Iterator<FileSpecification> iterator() {
        ensureLoaded();
        return files.iterator();
    }

    // ────────────────────────────────────────────────────────────────────
    //  Name-tree backing — all reads and writes go through NameTree
    // ────────────────────────────────────────────────────────────────────

    private void ensureLoaded() {
        if (files != null) return;
        files = new ArrayList<>();
        try {
            PdfDictionary efRoot = embeddedFilesRoot(false);
            if (efRoot == null) return;
            for (Map.Entry<String, PdfBase> entry : new NameTree(efRoot).entries()) {
                PdfBase value = entry.getValue();
                if (value instanceof PdfDictionary) {
                    files.add(new FileSpecification((PdfDictionary) value));
                }
            }
        } catch (IOException e) {
            LOG.warning(() -> "Failed to load embedded files: " + e.getMessage());
        }
    }

    private void addToNameTree(FileSpecification fs) {
        try {
            PdfDictionary efRoot = embeddedFilesRoot(true);
            if (efRoot == null) return;
            String name = fs.getName() != null ? fs.getName() : "attachment" + files.size();
            new NameTree(efRoot).put(name, fs.getPdfDictionary());
        } catch (IOException e) {
            LOG.warning(() -> "Failed to add to name tree: " + e.getMessage());
        }
    }

    private void removeFromNameTreeByName(String name) {
        if (name == null) return;
        try {
            PdfDictionary efRoot = embeddedFilesRoot(false);
            if (efRoot != null) {
                new NameTree(efRoot).remove(name);
            }
        } catch (IOException e) {
            LOG.warning(() -> "Failed to remove name-tree entry '" + name + "': " + e.getMessage());
        }
    }

    /// Returns the `/Names → /EmbeddedFiles` root dictionary, optionally
    /// creating it (and its parent `/Names`) when `createIfMissing`
    /// is true.
    private PdfDictionary embeddedFilesRoot(boolean createIfMissing) throws IOException {
        PdfDictionary catalog = document.getCatalog();
        PdfBase names = resolveRef(catalog.get(NAMES));
        if (!(names instanceof PdfDictionary)) {
            if (!createIfMissing) return null;
            PdfDictionary fresh = new PdfDictionary();
            catalog.set(NAMES, fresh);
            names = fresh;
        }
        PdfBase ef = resolveRef(((PdfDictionary) names).get(EMBEDDED_FILES));
        if (!(ef instanceof PdfDictionary)) {
            if (!createIfMissing) return null;
            PdfDictionary fresh = new PdfDictionary();
            ((PdfDictionary) names).set(EMBEDDED_FILES, fresh);
            ef = fresh;
        }
        return (PdfDictionary) ef;
    }

    private PdfBase resolveRef(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) val).dereference(); } catch (Exception e) { return null; }
        }
        return val;
    }
}
