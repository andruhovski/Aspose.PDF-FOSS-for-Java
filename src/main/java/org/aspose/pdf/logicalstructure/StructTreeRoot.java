package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/// The root of the logical structure tree (ISO 32000-1:2008, §14.7.2, Table 322).
/// Wraps the /StructTreeRoot dictionary from the document catalog.
public class StructTreeRoot {

    private final PdfDictionary dict;
    private final PDFParser parser;

    /// Creates a StructTreeRoot wrapping the given dictionary.
    ///
    /// @param dict   the /StructTreeRoot dictionary
    /// @param parser the PDF parser (may be null)
    public StructTreeRoot(PdfDictionary dict, PDFParser parser) {
        this.dict = dict != null ? dict : new PdfDictionary();
        this.parser = parser;
    }

    /// Returns the underlying PDF dictionary.
    public PdfDictionary getPdfDictionary() { return dict; }

    /// Returns the root structure element (typically the /Document element).
    /// /K is usually a single StructElem or an array with one.
    ///
    /// @return the root element, or `null`
    public StructureElement getRootElement() {
        PdfBase k = resolve(dict.get("K"));
        if (k instanceof PdfDictionary) {
            return new StructureElement((PdfDictionary) k, parser);
        }
        if (k instanceof PdfArray && ((PdfArray) k).size() > 0) {
            PdfBase first = resolve(((PdfArray) k).get(0));
            if (first instanceof PdfDictionary) {
                return new StructureElement((PdfDictionary) first, parser);
            }
        }
        return null;
    }

    /// Returns all top-level structure elements.
    ///
    /// @return the child elements
    public ElementList getChildren() {
        List<StructureElement> children = new ArrayList<>();
        PdfBase k = resolve(dict.get("K"));
        if (k instanceof PdfDictionary) {
            children.add(new StructureElement((PdfDictionary) k, parser));
        } else if (k instanceof PdfArray) {
            PdfArray arr = (PdfArray) k;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase item = resolve(arr.get(i));
                if (item instanceof PdfDictionary) {
                    children.add(new StructureElement((PdfDictionary) item, parser));
                }
            }
        }
        return new ElementList(children);
    }

    /// Returns the role map for this structure tree.
    ///
    /// @return the role map (empty if not present)
    public RoleMap getRoleMap() {
        PdfBase rm = resolve(dict.get("RoleMap"));
        return RoleMap.parse((rm instanceof PdfDictionary) ? (PdfDictionary) rm : null);
    }

    /// Returns the next available parent tree key (/ParentTreeNextKey).
    ///
    /// @return the next key value
    public int getParentTreeNextKey() {
        return dict.getInt("ParentTreeNextKey", 0);
    }

    /// Returns the `/ParentTree` number tree (§14.7.4.4) — the reverse
    /// map from a page's structural parent key (the /StructParents entry on
    /// the page) and from a content stream's marked-content identifier
    /// (MCID, indexed via the page entry) back to the structure element(s)
    /// referencing them. Returns `null` if the structure tree carries
    /// no /ParentTree.
    ///
    /// Each value in the tree is either an indirect reference to a single
    /// [StructureElement] dictionary (used for annotation- or page-level
    /// keys) or an array whose i-th entry is the structure element that owns
    /// the marked-content with MCID = i on that page. Callers can use
    /// [#lookupParentTreeEntry(int)] for a typed view.
    ///
    /// @return the parent number tree, or `null` if absent
    public NumberTree getParentTree() {
        PdfBase pt = resolve(dict.get("ParentTree"));
        return (pt instanceof PdfDictionary) ? new NumberTree((PdfDictionary) pt) : null;
    }

    /// Resolves a single `/ParentTree` key. When the value is an array
    /// (page-keyed entries — one slot per MCID), the array is returned
    /// verbatim and callers should index by MCID. When it is a single
    /// structure-element dictionary, it is returned as-is.
    ///
    /// @param key the parent-tree key (either a `/StructParents` on a
    ///            page or `/StructParent` on an annotation/object)
    /// @return the raw PDF value, or `null` if no /ParentTree exists
    ///         or the key is absent
    public PdfBase lookupParentTreeEntry(int key) {
        NumberTree tree = getParentTree();
        return tree == null ? null : tree.get(key);
    }

    /// Returns the structure element that owns the marked-content with the
    /// given MCID on a page identified by its `/StructParents` key.
    ///
    /// @param structParentsKey the page's /StructParents value
    /// @param mcid             the marked-content identifier
    /// @return the StructureElement, or `null` if not found
    public StructureElement findElementByMcid(int structParentsKey, int mcid) {
        PdfBase entry = lookupParentTreeEntry(structParentsKey);
        if (!(entry instanceof PdfArray)) return null;
        PdfArray arr = (PdfArray) entry;
        if (mcid < 0 || mcid >= arr.size()) return null;
        PdfBase target = resolve(arr.get(mcid));
        return (target instanceof PdfDictionary) ? new StructureElement((PdfDictionary) target, parser) : null;
    }

    /// Returns all structure elements in the tree (depth-first traversal).
    ///
    /// @return list of all structure elements
    public List<StructureElement> getAllElements() {
        List<StructureElement> result = new ArrayList<>();
        StructureElement root = getRootElement();
        if (root != null) {
            collectElements(root, result);
        }
        return result;
    }

    /// Finds all structure elements matching the given structure type name.
    ///
    /// @param typeName the structure type name (e.g., "P", "H1", "Table")
    /// @return list of matching elements
    public List<StructureElement> findElements(String typeName) {
        List<StructureElement> all = getAllElements();
        List<StructureElement> result = new ArrayList<>();
        for (StructureElement elem : all) {
            StructureTypeStandard type = elem.getStructureType();
            if (type != null && type.getName().equals(typeName)) {
                result.add(elem);
            }
        }
        return result;
    }

    /// Removes all child elements from the structure tree.
    public void clearChilds() {
        dict.remove(PdfName.of("K"));
    }

    private void collectElements(StructureElement elem, List<StructureElement> result) {
        result.add(elem);
        ElementList children = elem.getChildElements();
        for (int i = 0; i < children.getCount(); i++) {
            collectElements(children.get(i), result);
        }
    }

    /// Creates a new StructTreeRoot with a /Document root element.
    ///
    /// @return the new structure tree root
    public static StructTreeRoot createNew() {
        PdfDictionary root = new PdfDictionary();
        root.set(PdfName.of("Type"), PdfName.of("StructTreeRoot"));

        PdfDictionary docElem = new PdfDictionary();
        docElem.set(PdfName.of("Type"), PdfName.of("StructElem"));
        docElem.set(PdfName.of("S"), PdfName.of("Document"));
        docElem.set(PdfName.of("P"), root);

        root.set(PdfName.of("K"), docElem);
        root.setInt("ParentTreeNextKey", 0);

        return new StructTreeRoot(root, null);
    }

    private static PdfBase resolve(PdfBase obj) {
        if (obj instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
