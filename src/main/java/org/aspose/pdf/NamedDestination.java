package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSString;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents a reference to a named destination in a PDF document
 * (ISO 32000-1:2008, §12.3.2.3).
 *
 * <p>Unlike {@link ExplicitDestination}, which carries page+coordinates inline,
 * {@code NamedDestination} stores a name that resolves through the document's
 * name tree ({@code /Names→/Dests}) or legacy {@code /Dests} dictionary at
 * use time.</p>
 *
 * <p>Used as the destination of {@link GoToAction} or
 * {@link OutlineItemCollection}.</p>
 *
 * @see NamedDestinations
 * @see ExplicitDestination
 */
public class NamedDestination implements IAppointment {

    private static final Logger LOG = Logger.getLogger(NamedDestination.class.getName());

    private final Document document;
    private final String name;

    /**
     * Creates a named destination reference.
     *
     * @param document the document whose name tree will be used to resolve the name
     * @param name     the destination name (must match an entry in
     *                 {@code doc.getNamedDestinations()} when the document is used)
     */
    public NamedDestination(Document document, String name) {
        this.document = Objects.requireNonNull(document, "document must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    /**
     * Returns the name this destination references.
     *
     * @return the destination name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the document this destination is associated with.
     *
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Resolves the name to an explicit destination via the document's name tree.
     *
     * @return the explicit destination, or {@code null} if not found
     */
    public ExplicitDestination resolve() {
        try {
            NamedDestinations nd = document.getNamedDestinations();
            return nd != null ? nd.get(name) : null;
        } catch (IOException e) {
            LOG.warning("Failed to resolve named destination '" + name + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Serializes this NamedDestination as a PDF byte string for inclusion in
     * an action or outline-item dictionary. The string carries the destination
     * name verbatim — resolution happens at use time via the document's
     * {@code /Names→/Dests} name tree.
     *
     * @return the COS string representation
     */
    public COSBase toCos() {
        return new COSString(name);
    }

    @Override
    public String toString() {
        return "NamedDestination[" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedDestination)) return false;
        NamedDestination that = (NamedDestination) o;
        return document == that.document && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(document), name);
    }
}
