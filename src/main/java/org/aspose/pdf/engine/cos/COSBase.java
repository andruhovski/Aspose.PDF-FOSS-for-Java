package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Abstract base class for all nine PDF COS object types (§7.3, ISO 32000-1:2008).
 * <p>
 * Every PDF object — boolean, integer, real, name, string, null, array, dictionary, stream —
 * extends this class. COSBase provides infrastructure for:
 * <ul>
 *   <li>Serialization to PDF syntax via {@link #writeTo(OutputStream)}</li>
 *   <li>Indirect object references (an object may be "direct" or "indirect" with an object key)</li>
 *   <li>Visitor pattern via {@link #accept(ICOSVisitor)}</li>
 * </ul>
 */
public abstract class COSBase {

    private static final Logger LOG = Logger.getLogger(COSBase.class.getName());

    /** Key for indirect objects; null for direct objects. */
    private COSObjectKey objectKey;

    /** Whether this object has been modified since loading. */
    private boolean dirty;

    /**
     * Returns {@code true} if this object was modified since loading.
     *
     * @return whether the object is dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag on this object.
     *
     * @param dirty {@code true} to mark as modified
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Marks this object as modified (convenience for subclasses).
     */
    protected void markDirty() {
        this.dirty = true;
    }

    /**
     * Serialize this object to PDF syntax (ASCII bytes).
     *
     * @param os the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeTo(OutputStream os) throws IOException;

    /**
     * Accept a visitor for type-safe traversal.
     *
     * @param visitor the visitor
     * @param <T>     the return type
     * @return the visitor result
     */
    public abstract <T> T accept(ICOSVisitor<T> visitor);

    /**
     * Returns whether this object is an indirect object (has an object key).
     *
     * @return {@code true} if this object has an assigned object number and generation
     */
    public boolean isIndirect() {
        return objectKey != null;
    }

    /**
     * Returns the indirect object key, or {@code null} for direct objects.
     *
     * @return the object key, or {@code null}
     */
    public COSObjectKey getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the indirect object key. Called by the parser when reading "N G obj".
     *
     * @param key the object key, or {@code null} to make this a direct object
     */
    public void setObjectKey(COSObjectKey key) {
        this.objectKey = key;
        if (key != null) {
            LOG.fine(() -> "Object assigned key: " + key);
        }
    }

    /**
     * Tracks dictionaries and arrays currently being serialized on this thread so
     * that direct (non-indirect) cyclic references — sometimes produced by
     * malformed PDFs or after in-place mutations — emit a {@code null} placeholder
     * instead of recursing into a StackOverflowError. Indirect cycles are already
     * broken by writing object references in {@code COSDictionary.writeTo} and
     * {@code COSArray.writeTo}; this guard covers the remaining case.
     */
    private static final ThreadLocal<Set<COSBase>> WRITING_STACK =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /**
     * Marks {@code node} as "currently being serialized" on this thread and
     * returns {@code true} if it had not been marked already. Callers must pair
     * a successful enter with a matching {@link #leaveWriting(COSBase)} in a
     * {@code try/finally} block.
     *
     * @param node the dictionary or array about to be written
     * @return {@code true} if entry succeeded, {@code false} if {@code node} is
     *         already on the writing stack (i.e. a direct cycle was hit)
     */
    protected static boolean enterWriting(COSBase node) {
        return WRITING_STACK.get().add(node);
    }

    /**
     * Pops {@code node} off the per-thread "currently being serialized" stack.
     *
     * @param node the dictionary or array that has finished writing
     */
    protected static void leaveWriting(COSBase node) {
        Set<COSBase> stack = WRITING_STACK.get();
        stack.remove(node);
        if (stack.isEmpty()) {
            WRITING_STACK.remove();
        }
    }
}