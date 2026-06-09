package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.pdfobjects.NameTree;
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

    private final PdfDictionary catalog;
    private final Document doc;
    private final PDFParser parser;

    /**
     * Creates a NamedDestinations accessor.
     *
     * @param catalog the document catalog dictionary
     * @param doc     the document for page resolution
     * @param parser  the PDF parser for resolving references
     */
    public NamedDestinations(PdfDictionary catalog, Document doc, PDFParser parser) {
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

        PdfBase dest = lookupInNameTree(name);
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
            for (Map.Entry<String, PdfBase> e : tree.entries()) {
                names.add(e.getKey());
            }
        }

        PdfBase destsObj = resolve(catalog.get("Dests"));
        if (destsObj instanceof PdfDictionary) {
            for (PdfName key : ((PdfDictionary) destsObj).keySet()) {
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

        PdfDictionary names = resolveDict(catalog.get("Names"));
        if (names == null) {
            names = new PdfDictionary();
            catalog.set(PdfName.of("Names"), names);
        }
        PdfDictionary destsRoot = resolveDict(names.get("Dests"));
        if (destsRoot == null) {
            destsRoot = new PdfDictionary();
            // A fresh name tree root keeps an empty /Names array and no /Limits.
            destsRoot.set(PdfName.of("Names"), new PdfArray());
            names.set(PdfName.of("Dests"), destsRoot);
        }
        NameTree tree = new NameTree(destsRoot);
        tree.put(name, destination.toPdfArray());
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
        PdfBase destsObj = resolve(catalog.get("Dests"));
        if (destsObj instanceof PdfDictionary) {
            PdfDictionary destsDict = (PdfDictionary) destsObj;
            if (destsDict.get(name) != null) {
                destsDict.remove(PdfName.of(name));
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Name-tree access (§7.9.6) — via shared NameTree utility
    // ═══════════════════════════════════════════════════════════════

    private NameTree openNameTree() throws IOException {
        PdfDictionary namesDict = resolveDict(catalog.get("Names"));
        if (namesDict == null) return null;
        PdfBase destsTree = resolve(namesDict.get("Dests"));
        if (!(destsTree instanceof PdfDictionary)) return null;
        return new NameTree((PdfDictionary) destsTree);
    }

    private PdfBase lookupInNameTree(String name) throws IOException {
        NameTree tree = openNameTree();
        return tree == null ? null : tree.get(name);
    }

    // ═══════════════════════════════════════════════════════════════
    //  /Dests dictionary lookup (PDF 1.1)
    // ═══════════════════════════════════════════════════════════════

    private PdfBase lookupInDestsDict(String name) throws IOException {
        PdfBase destsObj = resolve(catalog.get("Dests"));
        if (!(destsObj instanceof PdfDictionary)) return null;
        return resolve(((PdfDictionary) destsObj).get(name));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Destination value resolution
    // ═══════════════════════════════════════════════════════════════

    /**
     * Resolves a destination value which can be:
     * - PdfArray → explicit destination
     * - PdfDictionary with /D → destination dictionary (§12.3.2.3)
     */
    private ExplicitDestination resolveDestValue(PdfBase dest) throws IOException {
        dest = resolve(dest);
        if (dest instanceof PdfArray) {
            return ExplicitDestination.fromPdfArray((PdfArray) dest, doc);
        }
        if (dest instanceof PdfDictionary) {
            PdfBase d = resolve(((PdfDictionary) dest).get("D"));
            if (d instanceof PdfArray) {
                return ExplicitDestination.fromPdfArray((PdfArray) d, doc);
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Utilities
    // ═══════════════════════════════════════════════════════════════

    private PdfBase resolve(PdfBase obj) throws IOException {
        if (obj instanceof PdfObjectReference) {
            return ((PdfObjectReference) obj).dereference();
        }
        return obj;
    }

    private PdfDictionary resolveDict(PdfBase obj) throws IOException {
        obj = resolve(obj);
        return (obj instanceof PdfDictionary) ? (PdfDictionary) obj : null;
    }
}
