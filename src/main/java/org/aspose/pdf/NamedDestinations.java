package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.cos.NameTree;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides access to named destinations in a PDF document
 * (ISO 32000-1:2008, §12.3.2.3).
 *
 * <p>Named destinations are resolved from two possible locations:</p>
 * <ol>
 *   <li>{@code /Dests} dictionary in the catalog (PDF 1.1, deprecated but common)</li>
 *   <li>{@code /Names → /Dests} name tree in the catalog (PDF 1.2+)</li>
 * </ol>
 *
 * <p>The name-tree path delegates to {@link NameTree}, so {@code /Limits}
 * pruning works for both lookup and enumeration.</p>
 */
public class NamedDestinations {

    private static final Logger LOG = Logger.getLogger(NamedDestinations.class.getName());

    private final COSDictionary catalog;
    private final Document doc;
    private final PDFParser parser;

    /**
     * Creates a NamedDestinations accessor.
     *
     * @param catalog the document catalog dictionary
     * @param doc     the document for page resolution
     * @param parser  the PDF parser for resolving references
     */
    public NamedDestinations(COSDictionary catalog, Document doc, PDFParser parser) {
        this.catalog = catalog;
        this.doc = doc;
        this.parser = parser;
    }

    /**
     * Resolves a named destination to an explicit destination.
     * Searches {@code /Names→/Dests} name tree first, then {@code /Dests} dictionary.
     *
     * @param name the destination name
     * @return the resolved destination, or {@code null} if not found
     * @throws IOException if resolution fails
     */
    public ExplicitDestination get(String name) throws IOException {
        if (name == null) return null;

        COSBase dest = lookupInNameTree(name);
        if (dest == null) {
            dest = lookupInDestsDict(name);
        }
        if (dest == null) return null;

        return resolveDestValue(dest);
    }

    /**
     * Returns all named destination names from both sources, deduplicated and
     * in insertion order ({@code /Names → /Dests} entries first, then any
     * extra keys from the legacy {@code /Dests} dictionary).
     *
     * @return list of destination names
     * @throws IOException if resolution fails
     */
    public List<String> getNames() throws IOException {
        LinkedHashSet<String> names = new LinkedHashSet<>();

        NameTree tree = openNameTree();
        if (tree != null) {
            for (Map.Entry<String, COSBase> e : tree.entries()) {
                names.add(e.getKey());
            }
        }

        COSBase destsObj = resolve(catalog.get("Dests"));
        if (destsObj instanceof COSDictionary) {
            for (COSName key : ((COSDictionary) destsObj).keySet()) {
                names.add(key.getName());
            }
        }

        return new ArrayList<>(names);
    }

    /**
     * Returns the total number of named destinations.
     *
     * @return the count
     * @throws IOException if resolution fails
     */
    public int getCount() throws IOException {
        return getNames().size();
    }

    /**
     * Alias for {@link #getCount()} matching the C# Aspose {@code .Count} property.
     *
     * @return the count
     * @throws IOException if resolution fails
     */
    public int size() throws IOException {
        return getCount();
    }

    /**
     * Returns all named destination names as an array. Mirrors the C# Aspose
     * {@code NamedDestinations.Names} property.
     *
     * @return array of names (never null; empty if no destinations exist)
     * @throws IOException if resolution fails
     */
    public String[] getNamesArray() throws IOException {
        return getNames().toArray(new String[0]);
    }

    /**
     * Adds (or replaces) a named destination via the catalog's
     * {@code /Names→/Dests} name tree (PDF 1.2+). Creates the
     * {@code /Names} dictionary and {@code /Dests} subtree if they are
     * missing.
     *
     * @param name        the destination name (must not be null)
     * @param destination the explicit destination (must not be null)
     * @throws IOException if the name tree cannot be updated
     */
    public void add(String name, ExplicitDestination destination) throws IOException {
        if (name == null) throw new IllegalArgumentException("name must not be null");
        if (destination == null) throw new IllegalArgumentException("destination must not be null");

        COSDictionary names = resolveDict(catalog.get("Names"));
        if (names == null) {
            names = new COSDictionary();
            catalog.set(COSName.of("Names"), names);
        }
        COSDictionary destsRoot = resolveDict(names.get("Dests"));
        if (destsRoot == null) {
            destsRoot = new COSDictionary();
            // A fresh name tree root keeps an empty /Names array and no /Limits.
            destsRoot.set(COSName.of("Names"), new COSArray());
            names.set(COSName.of("Dests"), destsRoot);
        }
        NameTree tree = new NameTree(destsRoot);
        tree.put(name, destination.toCOSArray());
    }

    /**
     * Sets a named destination — semantically identical to
     * {@link #add(String, ExplicitDestination)}; the name tree's
     * {@code put} replaces any existing value.
     *
     * @param name        the destination name
     * @param destination the explicit destination
     * @throws IOException if the name tree cannot be updated
     */
    public void set(String name, ExplicitDestination destination) throws IOException {
        add(name, destination);
    }

    /**
     * Removes a named destination if it exists.
     *
     * @param name the destination name to remove
     * @return {@code true} if a destination was removed
     * @throws IOException if the name tree cannot be updated
     */
    public boolean remove(String name) throws IOException {
        if (name == null) return false;
        NameTree tree = openNameTree();
        if (tree != null && tree.remove(name) != null) {
            return true;
        }
        // Try the legacy /Dests dictionary.
        COSBase destsObj = resolve(catalog.get("Dests"));
        if (destsObj instanceof COSDictionary) {
            COSDictionary destsDict = (COSDictionary) destsObj;
            if (destsDict.get(name) != null) {
                destsDict.remove(COSName.of(name));
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Name-tree access (§7.9.6) — via shared NameTree utility
    // ═══════════════════════════════════════════════════════════════

    private NameTree openNameTree() throws IOException {
        COSDictionary namesDict = resolveDict(catalog.get("Names"));
        if (namesDict == null) return null;
        COSBase destsTree = resolve(namesDict.get("Dests"));
        if (!(destsTree instanceof COSDictionary)) return null;
        return new NameTree((COSDictionary) destsTree);
    }

    private COSBase lookupInNameTree(String name) throws IOException {
        NameTree tree = openNameTree();
        return tree == null ? null : tree.get(name);
    }

    // ═══════════════════════════════════════════════════════════════
    //  /Dests dictionary lookup (PDF 1.1)
    // ═══════════════════════════════════════════════════════════════

    private COSBase lookupInDestsDict(String name) throws IOException {
        COSBase destsObj = resolve(catalog.get("Dests"));
        if (!(destsObj instanceof COSDictionary)) return null;
        return resolve(((COSDictionary) destsObj).get(name));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Destination value resolution
    // ═══════════════════════════════════════════════════════════════

    /**
     * Resolves a destination value which can be:
     * - COSArray → explicit destination
     * - COSDictionary with /D → destination dictionary (§12.3.2.3)
     */
    private ExplicitDestination resolveDestValue(COSBase dest) throws IOException {
        dest = resolve(dest);
        if (dest instanceof COSArray) {
            return ExplicitDestination.fromCOSArray((COSArray) dest, doc);
        }
        if (dest instanceof COSDictionary) {
            COSBase d = resolve(((COSDictionary) dest).get("D"));
            if (d instanceof COSArray) {
                return ExplicitDestination.fromCOSArray((COSArray) d, doc);
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Utilities
    // ═══════════════════════════════════════════════════════════════

    private COSBase resolve(COSBase obj) throws IOException {
        if (obj instanceof COSObjectReference) {
            return ((COSObjectReference) obj).dereference();
        }
        return obj;
    }

    private COSDictionary resolveDict(COSBase obj) throws IOException {
        obj = resolve(obj);
        return (obj instanceof COSDictionary) ? (COSDictionary) obj : null;
    }
}
