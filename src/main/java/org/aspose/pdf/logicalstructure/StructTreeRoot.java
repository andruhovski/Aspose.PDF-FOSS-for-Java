package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.NumberTree;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The root of the logical structure tree (ISO 32000-1:2008, §14.7.2, Table 322).
 * Wraps the /StructTreeRoot dictionary from the document catalog.
 */
public class StructTreeRoot {

    private final COSDictionary dict;
    private final PDFParser parser;

    /**
     * Creates a StructTreeRoot wrapping the given dictionary.
     *
     * @param dict   the /StructTreeRoot dictionary
     * @param parser the PDF parser (may be null)
     */
    public StructTreeRoot(COSDictionary dict, PDFParser parser) {
        this.dict = dict != null ? dict : new COSDictionary();
        this.parser = parser;
    }

    /** Returns the underlying COS dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }

    /**
     * Returns the root structure element (typically the /Document element).
     * /K is usually a single StructElem or an array with one.
     *
     * @return the root element, or {@code null}
     */
    public StructureElement getRootElement() {
        COSBase k = resolve(dict.get("K"));
        if (k instanceof COSDictionary) {
            return new StructureElement((COSDictionary) k, parser);
        }
        if (k instanceof COSArray && ((COSArray) k).size() > 0) {
            COSBase first = resolve(((COSArray) k).get(0));
            if (first instanceof COSDictionary) {
                return new StructureElement((COSDictionary) first, parser);
            }
        }
        return null;
    }

    /**
     * Returns all top-level structure elements.
     *
     * @return the child elements
     */
    public ElementList getChildren() {
        List<StructureElement> children = new ArrayList<>();
        COSBase k = resolve(dict.get("K"));
        if (k instanceof COSDictionary) {
            children.add(new StructureElement((COSDictionary) k, parser));
        } else if (k instanceof COSArray) {
            COSArray arr = (COSArray) k;
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = resolve(arr.get(i));
                if (item instanceof COSDictionary) {
                    children.add(new StructureElement((COSDictionary) item, parser));
                }
            }
        }
        return new ElementList(children);
    }

    /**
     * Returns the role map for this structure tree.
     *
     * @return the role map (empty if not present)
     */
    public RoleMap getRoleMap() {
        COSBase rm = resolve(dict.get("RoleMap"));
        return RoleMap.parse((rm instanceof COSDictionary) ? (COSDictionary) rm : null);
    }

    /**
     * Returns the next available parent tree key (/ParentTreeNextKey).
     *
     * @return the next key value
     */
    public int getParentTreeNextKey() {
        return dict.getInt("ParentTreeNextKey", 0);
    }

    /**
     * Returns the {@code /ParentTree} number tree (§14.7.4.4) — the reverse
     * map from a page's structural parent key (the /StructParents entry on
     * the page) and from a content stream's marked-content identifier
     * (MCID, indexed via the page entry) back to the structure element(s)
     * referencing them. Returns {@code null} if the structure tree carries
     * no /ParentTree.
     *
     * <p>Each value in the tree is either an indirect reference to a single
     * {@link StructureElement} dictionary (used for annotation- or page-level
     * keys) or an array whose i-th entry is the structure element that owns
     * the marked-content with MCID = i on that page. Callers can use
     * {@link #lookupParentTreeEntry(int)} for a typed view.</p>
     *
     * @return the parent number tree, or {@code null} if absent
     */
    public NumberTree getParentTree() {
        COSBase pt = resolve(dict.get("ParentTree"));
        return (pt instanceof COSDictionary) ? new NumberTree((COSDictionary) pt) : null;
    }

    /**
     * Resolves a single {@code /ParentTree} key. When the value is an array
     * (page-keyed entries — one slot per MCID), the array is returned
     * verbatim and callers should index by MCID. When it is a single
     * structure-element dictionary, it is returned as-is.
     *
     * @param key the parent-tree key (either a {@code /StructParents} on a
     *            page or {@code /StructParent} on an annotation/object)
     * @return the raw COS value, or {@code null} if no /ParentTree exists
     *         or the key is absent
     */
    public COSBase lookupParentTreeEntry(int key) {
        NumberTree tree = getParentTree();
        return tree == null ? null : tree.get(key);
    }

    /**
     * Returns the structure element that owns the marked-content with the
     * given MCID on a page identified by its {@code /StructParents} key.
     *
     * @param structParentsKey the page's /StructParents value
     * @param mcid             the marked-content identifier
     * @return the StructureElement, or {@code null} if not found
     */
    public StructureElement findElementByMcid(int structParentsKey, int mcid) {
        COSBase entry = lookupParentTreeEntry(structParentsKey);
        if (!(entry instanceof COSArray)) return null;
        COSArray arr = (COSArray) entry;
        if (mcid < 0 || mcid >= arr.size()) return null;
        COSBase target = resolve(arr.get(mcid));
        return (target instanceof COSDictionary) ? new StructureElement((COSDictionary) target, parser) : null;
    }

    /**
     * Returns all structure elements in the tree (depth-first traversal).
     *
     * @return list of all structure elements
     */
    public List<StructureElement> getAllElements() {
        List<StructureElement> result = new ArrayList<>();
        StructureElement root = getRootElement();
        if (root != null) {
            collectElements(root, result);
        }
        return result;
    }

    /**
     * Finds all structure elements matching the given structure type name.
     *
     * @param typeName the structure type name (e.g., "P", "H1", "Table")
     * @return list of matching elements
     */
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

    /**
     * Removes all child elements from the structure tree.
     */
    public void clearChilds() {
        dict.remove(COSName.of("K"));
    }

    private void collectElements(StructureElement elem, List<StructureElement> result) {
        result.add(elem);
        ElementList children = elem.getChildElements();
        for (int i = 0; i < children.getCount(); i++) {
            collectElements(children.get(i), result);
        }
    }

    /**
     * Creates a new StructTreeRoot with a /Document root element.
     *
     * @return the new structure tree root
     */
    public static StructTreeRoot createNew() {
        COSDictionary root = new COSDictionary();
        root.set(COSName.of("Type"), COSName.of("StructTreeRoot"));

        COSDictionary docElem = new COSDictionary();
        docElem.set(COSName.of("Type"), COSName.of("StructElem"));
        docElem.set(COSName.of("S"), COSName.of("Document"));
        docElem.set(COSName.of("P"), root);

        root.set(COSName.of("K"), docElem);
        root.setInt("ParentTreeNextKey", 0);

        return new StructTreeRoot(root, null);
    }

    private static COSBase resolve(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try { return ((COSObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
