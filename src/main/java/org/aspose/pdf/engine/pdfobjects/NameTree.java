package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Read/write view over a PDF name tree (ISO 32000-1:2008, §7.9.6).
///
/// A name tree is a balanced tree whose leaves hold sorted `/Names`
/// pair arrays `[key0 value0 key1 value1 …]`, and whose intermediate
/// nodes carry `/Kids` arrays plus a `/Limits` pair
/// `[minKey maxKey]` for range pruning. This class hides the traversal
/// and provides the usual [#get], [#put], [#remove],
/// [#entries] primitives.
///
/// The implementation tolerates malformed inputs: missing `/Limits`,
/// unsorted `/Names`, mixed `/Names`+`/Kids` on a single
/// node — read paths fall back to linear scans and write paths repair
/// `/Limits` from observed data after every mutation. Per spec writes
/// keep `/Names` sorted and `/Limits` synced bottom-up.
///
/// This class does not rebalance the tree (no node splitting/merging).
/// Inserts go into the leaf whose range already covers the key, or whose
/// range is closest if the key is out of bounds; that leaf grows until the
/// caller decides to rebalance externally.
public final class NameTree {

    private static final Logger LOG = Logger.getLogger(NameTree.class.getName());

    private static final PdfName KIDS = PdfName.of("Kids");
    private static final PdfName NAMES = PdfName.of("Names");
    private static final PdfName LIMITS = PdfName.of("Limits");

    private final PdfDictionary root;

    /// Wraps the given root dictionary. The dictionary is the name-tree root
    /// itself (e.g. the `/Dests` sub-dictionary under
    /// `/Catalog/Names`) — _not_ the catalog.
    ///
    /// @param root the name-tree root, or `null` for an empty view
    public NameTree(PdfDictionary root) {
        this.root = root;
    }

    /// Returns the underlying root dictionary, or `null` if this view
    /// was constructed over a `null` root.
    ///
    /// @return the root dictionary
    public PdfDictionary getRoot() {
        return root;
    }

    /// Returns whether the tree has no entries.
    ///
    /// @return `true` when no name maps to any value
    public boolean isEmpty() {
        return root == null || countEntries(root) == 0;
    }

    /// Returns the number of (key, value) pairs in the tree.
    ///
    /// @return the entry count
    public int size() {
        return root == null ? 0 : countEntries(root);
    }

    /// Looks up a key. Uses `/Limits` to prune sub-trees when present.
    ///
    /// @param key the key to look up
    /// @return the associated value, or `null` if not found
    public PdfBase get(String key) {
        if (root == null || key == null) return null;
        return findInNode(root, key);
    }

    /// Returns whether the tree contains the given key.
    ///
    /// @param key the key
    /// @return `true` if a value is associated with `key`
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /// Returns all entries in tree order (which, for a conformant tree, is
    /// key-sorted order). The returned list is a snapshot — subsequent
    /// mutations do not affect it.
    ///
    /// @return the list of `(key, value)` entries
    public List<Map.Entry<String, PdfBase>> entries() {
        List<Map.Entry<String, PdfBase>> out = new ArrayList<>();
        if (root != null) collectEntries(root, out);
        return out;
    }

    /// Returns all keys in tree order. Convenience wrapper around
    /// [#entries()] for callers that only need the names.
    ///
    /// @return the list of keys
    public List<String> keys() {
        List<Map.Entry<String, PdfBase>> es = entries();
        List<String> out = new ArrayList<>(es.size());
        for (Map.Entry<String, PdfBase> e : es) out.add(e.getKey());
        return out;
    }

    /// Inserts or replaces a value. The host `/Names` array stays
    /// sorted by key and `/Limits` are re-synced bottom-up along the
    /// insertion path.
    ///
    /// @param key   the key to insert (must not be `null`)
    /// @param value the value to associate
    /// @return the previous value bound to `key`, or `null`
    /// @throws IllegalStateException if this view wraps a `null` root
    public PdfBase put(String key, PdfBase value) {
        requireRoot();
        if (key == null) throw new IllegalArgumentException("key must not be null");
        if (value == null) return remove(key);
        List<PdfDictionary> path = new ArrayList<>();
        PdfDictionary leaf = locateLeafForInsert(root, key, path);
        PdfBase previous = insertSorted(leaf, key, value);
        // The leaf itself is the last entry on the path — recompute limits
        // starting there and walking up to (but not including) the root.
        refreshLimitsAlongPath(path);
        return previous;
    }

    /// Removes a key. Empty leaves keep their (now empty) `/Names`
    /// array — the writer is content-preserving; pruning leaves is a job
    /// for an external rebalancing pass.
    ///
    /// @param key the key to remove
    /// @return the removed value, or `null` if the key was absent
    public PdfBase remove(String key) {
        if (root == null || key == null) return null;
        List<PdfDictionary> path = new ArrayList<>();
        PdfDictionary leaf = locateLeafForLookup(root, key, path);
        if (leaf == null) return null;
        PdfBase removed = removeFromLeaf(leaf, key);
        if (removed != null) refreshLimitsAlongPath(path);
        return removed;
    }

    /// Empties the tree: drops all `/Kids`, `/Limits` and any
    /// `/Names` array contents. Leaves an empty `/Names` on the
    /// root so subsequent inserts have a leaf to fall into.
    public void clear() {
        requireRoot();
        root.remove(KIDS);
        root.remove(LIMITS);
        root.set(NAMES, new PdfArray());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Reading
    // ────────────────────────────────────────────────────────────────────

    private PdfBase findInNode(PdfDictionary node, String key) {
        if (!keyInLimits(node, key)) return null;
        PdfBase names = resolve(node.get(NAMES));
        if (names instanceof PdfArray) {
            PdfBase found = findInLeaf((PdfArray) names, key);
            if (found != null) return found;
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) {
                    PdfBase r = findInNode((PdfDictionary) kid, key);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    private static PdfBase findInLeaf(PdfArray pairs, String key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            String k = asString(resolve(pairs.get(i)));
            if (key.equals(k)) return resolve(pairs.get(i + 1));
        }
        return null;
    }

    private static boolean keyInLimits(PdfDictionary node, String key) {
        PdfBase limits = resolve(node.get(LIMITS));
        if (!(limits instanceof PdfArray) || ((PdfArray) limits).size() < 2) return true;
        String min = asString(resolve(((PdfArray) limits).get(0)));
        String max = asString(resolve(((PdfArray) limits).get(1)));
        if (min != null && key.compareTo(min) < 0) return false;
        if (max != null && key.compareTo(max) > 0) return false;
        return true;
    }

    private static void collectEntries(PdfDictionary node, List<Map.Entry<String, PdfBase>> out) {
        PdfBase names = resolve(node.get(NAMES));
        if (names instanceof PdfArray) {
            PdfArray arr = (PdfArray) names;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                String k = asString(resolve(arr.get(i)));
                if (k != null) out.add(new AbstractMap.SimpleEntry<>(k, resolve(arr.get(i + 1))));
            }
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) collectEntries((PdfDictionary) kid, out);
            }
        }
    }

    private static int countEntries(PdfDictionary node) {
        int n = 0;
        PdfBase names = resolve(node.get(NAMES));
        if (names instanceof PdfArray) n += ((PdfArray) names).size() / 2;
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) n += countEntries((PdfDictionary) kid);
            }
        }
        return n;
    }

    // ────────────────────────────────────────────────────────────────────
    //  Writing
    // ────────────────────────────────────────────────────────────────────

    private PdfDictionary locateLeafForInsert(PdfDictionary node, String key, List<PdfDictionary> path) {
        path.add(node);
        PdfBase kids = resolve(node.get(KIDS));
        if (!(kids instanceof PdfArray) || ((PdfArray) kids).size() == 0) {
            // Treat as a leaf: ensure /Names exists so the insert has somewhere to land.
            if (!(resolve(node.get(NAMES)) instanceof PdfArray)) {
                node.set(NAMES, new PdfArray());
            }
            return node;
        }
        PdfDictionary chosen = pickKidForKey((PdfArray) kids, key);
        return locateLeafForInsert(chosen, key, path);
    }

    private PdfDictionary locateLeafForLookup(PdfDictionary node, String key, List<PdfDictionary> path) {
        path.add(node);
        if (!keyInLimits(node, key)) {
            path.remove(path.size() - 1);
            return null;
        }
        PdfBase names = resolve(node.get(NAMES));
        if (names instanceof PdfArray && containsKeyInPairs((PdfArray) names, key)) {
            return node;
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) {
                    PdfDictionary found = locateLeafForLookup((PdfDictionary) kid, key, path);
                    if (found != null) return found;
                }
            }
        }
        path.remove(path.size() - 1);
        return null;
    }

    private static boolean containsKeyInPairs(PdfArray pairs, String key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            if (key.equals(asString(resolve(pairs.get(i))))) return true;
        }
        return false;
    }

    /// Picks the kid whose /Limits range contains the key, or — if no range
    /// does — the best-fit kid (first kid for keys below all mins, last
    /// kid for keys above all maxes, nearest by min-distance otherwise).
    /// Kids without /Limits are treated as universal matches and chosen
    /// preferentially over fallback kids.
    private PdfDictionary pickKidForKey(PdfArray kids, String key) {
        PdfDictionary universalFallback = null;
        PdfDictionary nearestBelow = null;  // largest /Limits[1] still < key
        PdfDictionary nearestAbove = null;  // smallest /Limits[0] still > key
        String nearestBelowMax = null;
        String nearestAboveMin = null;
        PdfDictionary firstKid = null;
        PdfDictionary lastKid = null;
        for (int i = 0; i < kids.size(); i++) {
            PdfBase kidObj = resolve(kids.get(i));
            if (!(kidObj instanceof PdfDictionary)) continue;
            PdfDictionary kid = (PdfDictionary) kidObj;
            if (firstKid == null) firstKid = kid;
            lastKid = kid;
            PdfBase limits = resolve(kid.get(LIMITS));
            if (!(limits instanceof PdfArray) || ((PdfArray) limits).size() < 2) {
                if (universalFallback == null) universalFallback = kid;
                continue;
            }
            String min = asString(resolve(((PdfArray) limits).get(0)));
            String max = asString(resolve(((PdfArray) limits).get(1)));
            if (min != null && max != null && key.compareTo(min) >= 0 && key.compareTo(max) <= 0) {
                return kid;
            }
            if (max != null && key.compareTo(max) > 0
                    && (nearestBelowMax == null || max.compareTo(nearestBelowMax) > 0)) {
                nearestBelow = kid;
                nearestBelowMax = max;
            }
            if (min != null && key.compareTo(min) < 0
                    && (nearestAboveMin == null || min.compareTo(nearestAboveMin) < 0)) {
                nearestAbove = kid;
                nearestAboveMin = min;
            }
        }
        if (universalFallback != null) return universalFallback;
        if (nearestBelow != null) return nearestBelow;   // extends last in-range leaf upward
        if (nearestAbove != null) return nearestAbove;   // extends first in-range leaf downward
        if (lastKid != null) return lastKid;
        return firstKid;                                  // empty /Kids — unreachable in practice
    }

    /// Inserts (or overwrites) the pair in a sorted leaf `/Names`
    /// array. Returns the previous value if the key was already present.
    private PdfBase insertSorted(PdfDictionary leaf, String key, PdfBase value) {
        PdfBase namesObj = resolve(leaf.get(NAMES));
        PdfArray names;
        if (namesObj instanceof PdfArray) {
            names = (PdfArray) namesObj;
        } else {
            names = new PdfArray();
            leaf.set(NAMES, names);
        }
        int insertAt = names.size();
        for (int i = 0; i + 1 < names.size(); i += 2) {
            String k = asString(resolve(names.get(i)));
            if (k == null) continue;
            int cmp = key.compareTo(k);
            if (cmp == 0) {
                PdfBase old = resolve(names.get(i + 1));
                names.set(i + 1, value);
                return old;
            }
            if (cmp < 0) {
                insertAt = i;
                break;
            }
        }
        names.add(insertAt, makeKey(key));
        names.add(insertAt + 1, value);
        return null;
    }

    private PdfBase removeFromLeaf(PdfDictionary leaf, String key) {
        PdfBase namesObj = resolve(leaf.get(NAMES));
        if (!(namesObj instanceof PdfArray)) return null;
        PdfArray names = (PdfArray) namesObj;
        for (int i = 0; i + 1 < names.size(); i += 2) {
            String k = asString(resolve(names.get(i)));
            if (key.equals(k)) {
                PdfBase removed = resolve(names.get(i + 1));
                names.remove(i + 1);
                names.remove(i);
                return removed;
            }
        }
        return null;
    }

    /// Rebuilds `/Limits` on every node along the path except the
    /// root (the root in a name tree carries no `/Limits` per spec).
    /// Walks from leaf up so each ancestor sees a consistent child state.
    private void refreshLimitsAlongPath(List<PdfDictionary> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            PdfDictionary node = path.get(i);
            if (node == root) {
                // §7.9.6 Table 36: root must NOT carry /Limits.
                node.remove(LIMITS);
                continue;
            }
            refreshLimits(node);
        }
    }

    private void refreshLimits(PdfDictionary node) {
        String min = null;
        String max = null;
        PdfBase names = resolve(node.get(NAMES));
        if (names instanceof PdfArray) {
            PdfArray arr = (PdfArray) names;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                String k = asString(resolve(arr.get(i)));
                if (k == null) continue;
                if (min == null || k.compareTo(min) < 0) min = k;
                if (max == null || k.compareTo(max) > 0) max = k;
            }
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (!(kid instanceof PdfDictionary)) continue;
                PdfBase kidLimits = resolve(((PdfDictionary) kid).get(LIMITS));
                if (!(kidLimits instanceof PdfArray) || ((PdfArray) kidLimits).size() < 2) continue;
                String kMin = asString(resolve(((PdfArray) kidLimits).get(0)));
                String kMax = asString(resolve(((PdfArray) kidLimits).get(1)));
                if (kMin != null && (min == null || kMin.compareTo(min) < 0)) min = kMin;
                if (kMax != null && (max == null || kMax.compareTo(max) > 0)) max = kMax;
            }
        }
        if (min == null || max == null) {
            node.remove(LIMITS);
            return;
        }
        PdfArray limits = new PdfArray();
        limits.add(makeKey(min));
        limits.add(makeKey(max));
        node.set(LIMITS, limits);
    }

    // ────────────────────────────────────────────────────────────────────
    //  Utilities
    // ────────────────────────────────────────────────────────────────────

    private void requireRoot() {
        if (root == null) throw new IllegalStateException("NameTree has no backing root dictionary");
    }

    private static PdfBase resolve(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to dereference inside NameTree", e);
                return null;
            }
        }
        return value;
    }

    private static String asString(PdfBase value) {
        if (value instanceof PdfString) return ((PdfString) value).getString();
        if (value instanceof PdfName) return ((PdfName) value).getName();
        return null;
    }

    /// Builds the PdfString key. PdfString(String) chooses PDFDocEncoding when
    /// possible and falls back to UTF-16BE with BOM for non-encodable code
    /// points — matching how strings are stored in conformant PDF writers.
    private static PdfBase makeKey(String key) {
        return new PdfString(key);
    }

    /// Returns an unmodifiable view of [#entries()] for callers that
    /// want a [List] interface but no mutation.
    ///
    /// @return unmodifiable entry list
    public List<Map.Entry<String, PdfBase>> entriesUnmodifiable() {
        return Collections.unmodifiableList(entries());
    }
}
