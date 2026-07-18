package org.aspose.pdf;

import java.util.logging.Logger;

/// Represents the `/Collection` entry in a PDF Catalog (ISO 32000-1:2008
/// §12.3.5 — "Collections", also known as PDF portfolios).
///
/// This minimal facade exists so that legacy Aspose code of the form
///
/// ```
/// doc.setCollection(new Collection());
///   doc.getCollection().add(fileSpec);
/// ```
///
/// compiles and round-trips: each [#add(FileSpecification)] call is
/// forwarded straight to the document's [EmbeddedFileCollection], so the
/// embedded files end up in the `/Names &rarr; /EmbeddedFiles` name tree
/// (which is what most PDF viewers actually look at to enumerate portfolio
/// items). The catalog also gets a `/Collection` dictionary stamped on
/// it with the standard `/View /D` entry so readers know to switch into
/// collection-display mode.
///
/// Full split-screen layout schemas, sort fields, and folder hierarchies
/// (the rest of §12.3.5.5) are not implemented — this is just enough surface
/// area to satisfy the regression tests that exercise the basic
/// "portfolio = list of attachments" case.
public class Collection {

    private static final Logger LOG = Logger.getLogger(Collection.class.getName());

    /// The owning document; bound by [Document#setCollection(Collection)].
    private Document document;

    /// Default empty collection.
    public Collection() {
    }

    /// Internal: associates the collection with its document.
    void bind(Document document) {
        this.document = document;
    }

    /// Adds `fs` to the document's embedded files (a portfolio is, in
    /// essence, a list of embedded files marked with a `/Collection`
    /// entry on the catalog).
    ///
    /// @param fs the file specification to embed
    public void add(FileSpecification fs) {
        if (document == null) {
            throw new IllegalStateException(
                    "Collection has not been bound to a document — assign via Document.setCollection() first");
        }
        if (fs == null) return;
        document.getEmbeddedFiles().add(fs);
    }

    /// Number of items currently in the collection.
    public int getCount() {
        return document != null ? document.getEmbeddedFiles().size() : 0;
    }
}
