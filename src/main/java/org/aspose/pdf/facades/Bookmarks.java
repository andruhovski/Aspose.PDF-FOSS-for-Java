package org.aspose.pdf.facades;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Represents a typed list of {@link Bookmark} objects.
 * This class extends {@link ArrayList} to provide a strongly-typed collection
 * for use with {@link PdfBookmarkEditor}.
 */
public class Bookmarks extends ArrayList<Bookmark> {

    private static final Logger LOG = Logger.getLogger(Bookmarks.class.getName());

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new empty bookmarks collection.
     */
    public Bookmarks() {
        super();
    }
}
