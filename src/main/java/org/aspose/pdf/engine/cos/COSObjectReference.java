package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * PDF indirect object reference (§7.3.10, ISO 32000-1:2008).
 * <p>
 * References an object by its number and generation. Written as {@code N G R}
 * (e.g., {@code 12 0 R}). Supports lazy resolution: the actual object is loaded
 * on first access via an {@link ObjectResolver}.
 * </p>
 */
public final class COSObjectReference extends COSBase {

    private static final Logger LOG = Logger.getLogger(COSObjectReference.class.getName());

    private final COSObjectKey key;
    private COSBase resolvedObject;
    private ObjectResolver resolver;

    /**
     * Functional interface for lazy loading of indirect objects.
     */
    @FunctionalInterface
    public interface ObjectResolver {
        /**
         * Resolves the object identified by the given key.
         *
         * @param key the object key
         * @return the resolved object, or null
         * @throws IOException if an I/O error occurs during loading
         */
        COSBase resolve(COSObjectKey key) throws IOException;
    }

    /**
     * Creates a reference from a key.
     *
     * @param key the indirect object key
     * @throws IllegalArgumentException if key is null
     */
    public COSObjectReference(COSObjectKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        this.key = key;
    }

    /**
     * Creates a reference from object and generation numbers.
     *
     * @param objectNumber     the object number
     * @param generationNumber the generation number
     */
    public COSObjectReference(int objectNumber, int generationNumber) {
        this(new COSObjectKey(objectNumber, generationNumber));
    }

    /**
     * Creates a reference from a key with an optional resolver.
     *
     * @param key      the indirect object key
     * @param resolver the resolver for lazy loading, may be null
     */
    public COSObjectReference(COSObjectKey key, ObjectResolver resolver) {
        this(key);
        this.resolver = resolver;
    }

    /**
     * Returns the indirect object key.
     *
     * @return the key
     */
    public COSObjectKey getKey() {
        return key;
    }

    /**
     * Resolves the reference to the actual object. The result is cached.
     * If the resolver returns null, {@link COSNull#INSTANCE} is returned.
     *
     * @return the resolved object
     * @throws IOException           if an I/O error occurs
     * @throws IllegalStateException if no resolver has been set
     */
    public COSBase dereference() throws IOException {
        if (resolvedObject != null) {
            return resolvedObject;
        }
        if (resolver == null) {
            throw new IllegalStateException("No resolver set for reference " + key);
        }
        LOG.fine(() -> "Resolving indirect reference: " + key);
        resolvedObject = resolver.resolve(key);
        if (resolvedObject == null) {
            resolvedObject = COSNull.INSTANCE;
        }
        return resolvedObject;
    }

    /**
     * Sets the resolver for lazy loading. Called by the parser.
     *
     * @param resolver the resolver
     */
    public void setResolver(ObjectResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        String ref = key.getObjectNumber() + " " + key.getGenerationNumber() + " R";
        os.write(ref.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        // A reference is not directly visited; it should be dereferenced first.
        // However, for completeness in serialization, we write the reference form.
        // Visitors that encounter a reference should dereference it themselves.
        try {
            COSBase resolved = dereference();
            return resolved.accept(visitor);
        } catch (IOException e) {
            throw new RuntimeException("Failed to dereference object " + key, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSObjectReference)) return false;
        return key.equals(((COSObjectReference) o).key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "COSObjectReference{" + key + " R}";
    }
}
