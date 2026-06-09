package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Recursively clones a PDF object graph from one document so the result is independent
 * of the source document and can be safely inserted into a target document.
 *
 * <p>Cycle-safe via an identity-keyed visited map populated before recursion
 * (put-before-recurse). Shared resources — e.g. the same font referenced by
 * many pages — are cloned once and reused through the same map.</p>
 *
 * <p>Streams are copied via their decoded bytes, because the encoded bytes
 * may be encrypted with the source document's key and would be
 * unintelligible to the target.</p>
 *
 * <p><b>Iterative fill.</b> Cloning is split into two phases — allocation
 * (creating an empty clone of every encountered node and recording the
 * source→clone mapping) and population (copying contents into each empty
 * clone). The population phase is driven by a {@link ArrayDeque work queue}
 * rather than recursion, so traversal of arbitrarily deep PDF graphs (deep
 * page trees, deeply nested structure trees, layered Form XObjects, etc.)
 * runs in O(1) Java stack depth instead of O(graph depth). PDFNET-40631_1
 * is the canonical regression — its 80-page deep page-tree clone used to
 * blow the default JVM stack.</p>
 *
 * <p>This class is single-use: create a new instance for each import operation.</p>
 */
public final class PdfObjectCloner {

    private static final Logger LOG = Logger.getLogger(PdfObjectCloner.class.getName());

    /** Keys skipped when cloning a page dictionary. */
    public static final Set<String> PAGE_STOP_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("Parent", "StructParents", "StructParent")));

    /** Keys skipped when cloning an annotation dictionary. */
    public static final Set<String> ANNOT_STOP_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("P", "Dest", "StructParent")));

    /** Keys skipped in any dictionary. */
    public static final Set<String> GLOBAL_STOP_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("ParentTree")));

    /** visited: source PdfBase -> its clone. Prevents cycles and reuses shared refs. */
    private final IdentityHashMap<PdfBase, PdfBase> visited = new IdentityHashMap<>();

    /** createdRefs: cloned-indirect-body -> the PdfObjectReference we built for it.
     *  Guarantees registry idempotency: second dereference of the same source ref
     *  returns the same target ref (not a duplicate). */
    private final IdentityHashMap<PdfBase, PdfObjectReference> createdRefs = new IdentityHashMap<>();

    /** Pending fills — populated by {@code enqueue*} helpers, drained by {@link #drain()}. */
    private final ArrayDeque<FillJob> pending = new ArrayDeque<>();

    private final ReferenceRegistry registry;

    /** Allocates object keys in the target document. */
    public interface ReferenceRegistry {
        /** Stores {@code cloned} under a newly allocated key and returns
         *  a reference pointing to it. Called exactly once per distinct clone. */
        PdfObjectReference registerIndirect(PdfBase cloned);
    }

    /** Single (source → empty-clone) pair waiting to be populated. */
    private static final class FillJob {
        final PdfBase src;
        final PdfBase clone;
        final Set<String> stops;
        FillJob(PdfBase src, PdfBase clone, Set<String> stops) {
            this.src = src;
            this.clone = clone;
            this.stops = stops;
        }
    }

    public PdfObjectCloner(ReferenceRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("registry must not be null");
        this.registry = registry;
    }

    // ─── Public entry points ────────────────────────────────────────────────

    /** Clones a page dictionary (skipping /Parent and /StructParents). */
    public PdfDictionary clonePageDict(PdfDictionary srcPageDict) throws IOException {
        if (srcPageDict == null) throw new IllegalArgumentException("srcPageDict must not be null");
        PdfDictionary out = enqueueDict(srcPageDict, PAGE_STOP_KEYS);
        drain();
        return out;
    }

    /** Clones an annotation dictionary (skipping /P and /Dest, clearing /A/D). */
    public PdfDictionary cloneAnnotationDict(PdfDictionary srcAnnot) throws IOException {
        if (srcAnnot == null) throw new IllegalArgumentException("srcAnnot must not be null");
        PdfDictionary out = enqueueDict(srcAnnot, ANNOT_STOP_KEYS);
        drain();
        // Post-process — must run AFTER drain populated /A.
        PdfBase action = out.get("A");
        if (action instanceof PdfDictionary) {
            ((PdfDictionary) action).remove(PdfName.of("D"));
        }
        return out;
    }

    /** Clones an arbitrary PDF object (no extra stop-keys beyond the global set). */
    public PdfBase cloneAny(PdfBase src) throws IOException {
        PdfBase out = cloneInternal(src);
        drain();
        return out;
    }

    /**
     * Evicts {@code src} from the visited cache so the next {@code clone*}
     * call on it produces a fresh clone instead of returning the previously
     * cached one. Does not touch sub-objects — fonts, images and other
     * indirectly-referenced sub-resources reachable from {@code src} keep
     * their cached clones, preserving cross-page deduplication.
     * <p>
     * Used by callers that legitimately need distinct clones of the same
     * source object across multiple invocations (e.g. importing the same
     * page into different /Kids slots — see
     * {@code DocumentPageImporter.importPage} and PDFNEWNET-31533_3).
     */
    public void forgetSource(PdfBase src) {
        if (src == null) return;
        visited.remove(src);
    }

    // ─── Iterative core ─────────────────────────────────────────────────────

    /**
     * Returns the clone of {@code src} immediately. Container types
     * (dict/array/stream/ref) get an empty clone enqueued for later population;
     * scalars are returned directly. Bounded stack depth — never recurses into
     * fill operations.
     */
    private PdfBase cloneInternal(PdfBase src) throws IOException {
        if (src == null) return null;

        if (src instanceof PdfName || src instanceof PdfBoolean
                || src instanceof PdfInteger || src instanceof PdfFloat
                || src instanceof PdfNull) {
            return src;
        }

        if (src instanceof PdfString) {
            return new PdfString(((PdfString) src).getBytes().clone());
        }

        PdfBase hit = visited.get(src);
        if (hit != null) return hit;

        if (src instanceof PdfObjectReference) return cloneRef((PdfObjectReference) src);
        if (src instanceof PdfStream)          return enqueueStream((PdfStream) src);
        if (src instanceof PdfDictionary)      return enqueueDict((PdfDictionary) src, Collections.emptySet());
        if (src instanceof PdfArray)           return enqueueArray((PdfArray) src);

        LOG.warning(() -> "PdfObjectCloner: unknown type " + src.getClass().getName() + "; sharing as-is");
        return src;
    }

    private PdfBase cloneRef(PdfObjectReference srcRef) throws IOException {
        PdfBase resolved;
        try {
            resolved = srcRef.dereference();
        } catch (IOException e) {
            LOG.warning(() -> "PdfObjectCloner: failed to dereference " + srcRef + ": " + e.getMessage());
            return null;
        } catch (RuntimeException e) {
            if (isMalformedReferenceFailure(e)) {
                LOG.warning(() -> "PdfObjectCloner: skipping malformed indirect reference " + srcRef + ": " + e.getMessage());
                return null;
            }
            throw e;
        }
        if (resolved == null || resolved instanceof PdfNull) return null;

        // Indirect refs to scalars: just inline the scalar value — no point
        // round-tripping through an extra indirect object in the target.
        if (resolved instanceof PdfName || resolved instanceof PdfBoolean
                || resolved instanceof PdfInteger || resolved instanceof PdfFloat) {
            return resolved;
        }
        if (resolved instanceof PdfString) {
            return new PdfString(((PdfString) resolved).getBytes().clone());
        }

        PdfBase existingClone = visited.get(resolved);
        if (existingClone != null) {
            PdfObjectReference existingRef = createdRefs.get(existingClone);
            if (existingRef != null) return existingRef;
            // Clone exists but was cloned as direct (not indirect). Promote to indirect now.
            PdfObjectReference promoted = registry.registerIndirect(existingClone);
            createdRefs.put(existingClone, promoted);
            return promoted;
        }

        PdfBase empty = createEmptyOfSameKind(resolved);
        visited.put(resolved, empty);                    // put-BEFORE-enqueue
        PdfObjectReference targetRef = registry.registerIndirect(empty);
        createdRefs.put(empty, targetRef);
        pending.addLast(new FillJob(resolved, empty, Collections.emptySet()));
        return targetRef;
    }

    private boolean isMalformedReferenceFailure(RuntimeException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Object number must be non-negative")
                || message.contains("Generation number must be non-negative");
    }

    private PdfBase createEmptyOfSameKind(PdfBase src) {
        if (src instanceof PdfStream)     return new PdfStream();
        if (src instanceof PdfDictionary) return new PdfDictionary();
        if (src instanceof PdfArray)      return new PdfArray();
        throw new IllegalStateException("Cannot create empty clone for " + src.getClass().getName());
    }

    private PdfDictionary enqueueDict(PdfDictionary src, Set<String> extraStops) {
        PdfBase hit = visited.get(src);
        if (hit != null) return (PdfDictionary) hit;
        PdfDictionary out = new PdfDictionary();
        visited.put(src, out);                            // put-BEFORE-enqueue
        pending.addLast(new FillJob(src, out, extraStops));
        return out;
    }

    private PdfArray enqueueArray(PdfArray src) {
        PdfBase hit = visited.get(src);
        if (hit != null) return (PdfArray) hit;
        PdfArray out = new PdfArray(src.size());
        visited.put(src, out);
        pending.addLast(new FillJob(src, out, Collections.emptySet()));
        return out;
    }

    private PdfStream enqueueStream(PdfStream src) {
        PdfBase hit = visited.get(src);
        if (hit != null) return (PdfStream) hit;
        PdfStream out = new PdfStream();
        visited.put(src, out);
        pending.addLast(new FillJob(src, out, Collections.emptySet()));
        return out;
    }

    /**
     * Processes pending fill jobs until the queue is empty. Each filled value
     * may discover new sub-objects, which append their own jobs to the back of
     * the queue — classic breadth-first traversal with bounded stack depth.
     */
    private void drain() throws IOException {
        while (!pending.isEmpty()) {
            FillJob job = pending.pollFirst();
            if (job.src instanceof PdfStream) {
                fillStream((PdfStream) job.src, (PdfStream) job.clone);
            } else if (job.src instanceof PdfDictionary) {
                fillDict((PdfDictionary) job.src, (PdfDictionary) job.clone, job.stops);
            } else if (job.src instanceof PdfArray) {
                fillArray((PdfArray) job.src, (PdfArray) job.clone);
            } else {
                throw new IllegalStateException("Unexpected type in fill queue: "
                        + job.src.getClass().getName());
            }
        }
    }

    private void fillDict(PdfDictionary src, PdfDictionary out, Set<String> extraStops) throws IOException {
        for (Map.Entry<PdfName, PdfBase> e : src) {
            String keyName = e.getKey().getValue();
            if (GLOBAL_STOP_KEYS.contains(keyName) || extraStops.contains(keyName)) continue;
            PdfBase clonedVal = cloneInternal(e.getValue());
            if (clonedVal != null) out.set(e.getKey(), clonedVal);
        }
    }

    private void fillArray(PdfArray src, PdfArray out) throws IOException {
        for (int i = 0; i < src.size(); i++) {
            PdfBase clonedVal = cloneInternal(src.get(i));
            out.add(clonedVal != null ? clonedVal : PdfNull.INSTANCE);
        }
    }

    private void fillStream(PdfStream src, PdfStream out) throws IOException {
        for (Map.Entry<PdfName, PdfBase> e : src) {
            String keyName = e.getKey().getValue();
            if (GLOBAL_STOP_KEYS.contains(keyName)) continue;
            if ("Length".equals(keyName)) continue;       // re-emitted by writer
            PdfBase clonedVal = cloneInternal(e.getValue());
            if (clonedVal != null) out.set(e.getKey(), clonedVal);
        }
        // Prefer encoded bytes when source is unencrypted — avoids re-encoding
        // through filters that do not implement encode() (JPXDecode, DCTDecode,
        // CCITTFaxDecode). Fall back to decoded bytes only when the source
        // stream is ciphertext.
        if (src.hasActiveDecryptor()) {
            byte[] decoded = src.getDecodedData();
            out.setDecodedData(decoded != null ? decoded : new byte[0]);
        } else {
            byte[] encoded = src.getEncodedData();
            out.setEncodedData(encoded != null ? encoded : new byte[0]);
        }
    }
}
