package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.7.2, Table 323).
 *
 * <p>Key dictionary entries:</p>
 * <ul>
 *   <li>/S — structure type (e.g., P, H1, Table)</li>
 *   <li>/P — parent structure element or StructTreeRoot</li>
 *   <li>/K — children: StructElem dicts, MCR dicts, OBJR dicts, or integers (MCIDs)</li>
 *   <li>/Pg — default page for this element's content</li>
 *   <li>/T — title, /Lang — language, /Alt — alternate description</li>
 *   <li>/ActualText — replacement text, /E — expanded form</li>
 *   <li>/ID — unique identifier, /A — attributes</li>
 * </ul>
 */
public class StructureElement {

    private final COSDictionary dict;
    private final PDFParser parser;

    /**
     * Creates a structure element wrapping the given dictionary.
     *
     * @param dict   the /StructElem dictionary
     * @param parser the PDF parser for resolving references (may be null)
     */
    public StructureElement(COSDictionary dict, PDFParser parser) {
        this.dict = dict;
        this.parser = parser;
    }

    /** Returns the underlying COS dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }

    // ═══════════════════════════════════════════════════════════════
    //  Structure Type
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the structure type (e.g., P, H1, Table, Div).
     *
     * @return the type, or {@code null} if /S is missing
     */
    public StructureTypeStandard getStructureType() {
        String s = dict.getNameAsString("S");
        return s != null ? StructureTypeStandard.fromName(s) : null;
    }

    /**
     * Sets the structure type.
     *
     * @param type the structure type
     */
    public void setStructureType(StructureTypeStandard type) {
        dict.set(COSName.of("S"), COSName.of(type.getName()));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Metadata
    // ═══════════════════════════════════════════════════════════════

    /** Returns the title (/T). */
    public String getTitle() { return dict.getString("T"); }

    /** Sets the title (/T). */
    public void setTitle(String title) { dict.setString("T", title); }

    /** Returns the language (/Lang). */
    public String getLanguage() {
        COSBase lang = dict.get("Lang");
        if (lang instanceof COSString) return ((COSString) lang).getString();
        if (lang instanceof COSName) return ((COSName) lang).getName();
        return null;
    }

    /** Sets the language (/Lang). */
    public void setLanguage(String lang) {
        dict.set(COSName.of("Lang"), new COSString(lang));
    }

    /** Returns the alternate description (/Alt). */
    public String getAlternateDescription() { return dict.getString("Alt"); }

    /** Sets the alternate description (/Alt). */
    public void setAlternateDescription(String alt) { dict.setString("Alt", alt); }

    /** Returns the actual text (/ActualText). */
    public String getActualText() { return dict.getString("ActualText"); }

    /** Sets the actual text (/ActualText). */
    public void setActualText(String text) { dict.setString("ActualText", text); }

    /** Returns the expanded form (/E). */
    public String getExpandedForm() { return dict.getString("E"); }

    /** Sets the expanded form (/E). */
    public void setExpandedForm(String expanded) { dict.setString("E", expanded); }

    /** Returns the element ID (/ID). */
    public String getID() { return dict.getString("ID"); }

    /** Sets the element ID (/ID). */
    public void setID(String id) { dict.setString("ID", id); }

    // ═══════════════════════════════════════════════════════════════
    //  Children
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns only StructureElement children (filters out MCR, OBJR, integers).
     *
     * @return the child structure elements
     */
    public ElementList getChildElements() {
        List<StructureElement> children = new ArrayList<>();
        COSBase k = resolve(dict.get("K"));

        if (k instanceof COSDictionary) {
            addIfStructElem((COSDictionary) k, children);
        } else if (k instanceof COSArray) {
            COSArray arr = (COSArray) k;
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = resolve(arr.get(i));
                if (item instanceof COSDictionary) {
                    addIfStructElem((COSDictionary) item, children);
                }
            }
        }
        return new ElementList(children);
    }

    private void addIfStructElem(COSDictionary d, List<StructureElement> list) {
        // A StructElem has /S and is not a MCR (/MCID) or OBJR (/Obj)
        if (d.get("S") != null && d.get("MCID") == null && d.get("Obj") == null) {
            list.add(new StructureElement(d, parser));
        } else if ("StructElem".equals(d.getNameAsString("Type"))) {
            list.add(new StructureElement(d, parser));
        }
    }

    /**
     * Returns ALL child items: StructureElements, MarkedContentReferences,
     * ObjectReferences, and bare MCIDs.
     *
     * @return list of mixed child objects
     */
    public List<Object> getAllKids() {
        List<Object> kids = new ArrayList<>();
        COSBase k = resolve(dict.get("K"));
        if (k == null) return kids;

        if (k instanceof COSInteger) {
            kids.add(new MarkedContentReference(((COSInteger) k).intValue(), null));
            return kids;
        }

        List<COSBase> items;
        if (k instanceof COSArray) {
            items = new ArrayList<>();
            COSArray arr = (COSArray) k;
            for (int i = 0; i < arr.size(); i++) items.add(resolve(arr.get(i)));
        } else {
            items = Collections.singletonList(k);
        }

        for (COSBase item : items) {
            if (item instanceof COSInteger) {
                kids.add(new MarkedContentReference(((COSInteger) item).intValue(), null));
            } else if (item instanceof COSDictionary) {
                COSDictionary d = (COSDictionary) item;
                String type = d.getNameAsString("Type");
                if ("MCR".equals(type) || d.get("MCID") != null) {
                    kids.add(MarkedContentReference.fromDictionary(d));
                } else if ("OBJR".equals(type)) {
                    kids.add(ObjectReference.fromDictionary(d));
                } else {
                    kids.add(new StructureElement(d, parser));
                }
            }
        }
        return kids;
    }

    /**
     * Appends a child structure element.
     *
     * @param child the child element to append
     */
    public void appendChild(StructureElement child) {
        child.dict.set(COSName.of("P"), dict);
        COSBase k = dict.get("K");
        if (k == null) {
            dict.set(COSName.of("K"), child.dict);
        } else if (k instanceof COSArray) {
            ((COSArray) k).add(child.dict);
        } else {
            COSArray arr = new COSArray();
            arr.add(k);
            arr.add(child.dict);
            dict.set(COSName.of("K"), arr);
        }
    }

    /**
     * Appends a marked content reference (MCID) as a child.
     *
     * @param mcid the marked content identifier
     * @param page the page dictionary (may be null)
     */
    public void appendMarkedContent(int mcid, COSDictionary page) {
        COSDictionary mcr = new COSDictionary();
        mcr.set(COSName.of("Type"), COSName.of("MCR"));
        mcr.setInt("MCID", mcid);
        if (page != null) mcr.set(COSName.of("Pg"), page);

        COSBase k = dict.get("K");
        if (k == null) {
            dict.set(COSName.of("K"), mcr);
        } else if (k instanceof COSArray) {
            ((COSArray) k).add(mcr);
        } else {
            COSArray arr = new COSArray();
            arr.add(k);
            arr.add(mcr);
            dict.set(COSName.of("K"), arr);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Parent
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the parent element, or {@code null} for root-level elements.
     *
     * @return the parent structure element, or null
     */
    public StructureElement getParent() {
        COSBase p = resolve(dict.get("P"));
        if (p instanceof COSDictionary) {
            COSDictionary pd = (COSDictionary) p;
            if (pd.get("S") != null) return new StructureElement(pd, parser);
        }
        return null;
    }

    private static COSBase resolve(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try { return ((COSObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
