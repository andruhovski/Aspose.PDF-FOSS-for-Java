package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfString;
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

    private final PdfDictionary dict;
    private final PDFParser parser;

    /**
     * Creates a structure element wrapping the given dictionary.
     *
     * @param dict   the /StructElem dictionary
     * @param parser the PDF parser for resolving references (may be null)
     */
    public StructureElement(PdfDictionary dict, PDFParser parser) {
        this.dict = dict;
        this.parser = parser;
    }

    /** Returns the underlying PDF dictionary. */
    public PdfDictionary getPdfDictionary() { return dict; }

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
        dict.set(PdfName.of("S"), PdfName.of(type.getName()));
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
        PdfBase lang = dict.get("Lang");
        if (lang instanceof PdfString) return ((PdfString) lang).getString();
        if (lang instanceof PdfName) return ((PdfName) lang).getName();
        return null;
    }

    /** Sets the language (/Lang). */
    public void setLanguage(String lang) {
        dict.set(PdfName.of("Lang"), new PdfString(lang));
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
        PdfBase k = resolve(dict.get("K"));

        if (k instanceof PdfDictionary) {
            addIfStructElem((PdfDictionary) k, children);
        } else if (k instanceof PdfArray) {
            PdfArray arr = (PdfArray) k;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase item = resolve(arr.get(i));
                if (item instanceof PdfDictionary) {
                    addIfStructElem((PdfDictionary) item, children);
                }
            }
        }
        return new ElementList(children);
    }

    private void addIfStructElem(PdfDictionary d, List<StructureElement> list) {
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
        PdfBase k = resolve(dict.get("K"));
        if (k == null) return kids;

        if (k instanceof PdfInteger) {
            kids.add(new MarkedContentReference(((PdfInteger) k).intValue(), null));
            return kids;
        }

        List<PdfBase> items;
        if (k instanceof PdfArray) {
            items = new ArrayList<>();
            PdfArray arr = (PdfArray) k;
            for (int i = 0; i < arr.size(); i++) items.add(resolve(arr.get(i)));
        } else {
            items = Collections.singletonList(k);
        }

        for (PdfBase item : items) {
            if (item instanceof PdfInteger) {
                kids.add(new MarkedContentReference(((PdfInteger) item).intValue(), null));
            } else if (item instanceof PdfDictionary) {
                PdfDictionary d = (PdfDictionary) item;
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
        child.dict.set(PdfName.of("P"), dict);
        PdfBase k = dict.get("K");
        if (k == null) {
            dict.set(PdfName.of("K"), child.dict);
        } else if (k instanceof PdfArray) {
            ((PdfArray) k).add(child.dict);
        } else {
            PdfArray arr = new PdfArray();
            arr.add(k);
            arr.add(child.dict);
            dict.set(PdfName.of("K"), arr);
        }
    }

    /**
     * Appends a marked content reference (MCID) as a child.
     *
     * @param mcid the marked content identifier
     * @param page the page dictionary (may be null)
     */
    public void appendMarkedContent(int mcid, PdfDictionary page) {
        PdfDictionary mcr = new PdfDictionary();
        mcr.set(PdfName.of("Type"), PdfName.of("MCR"));
        mcr.setInt("MCID", mcid);
        if (page != null) mcr.set(PdfName.of("Pg"), page);

        PdfBase k = dict.get("K");
        if (k == null) {
            dict.set(PdfName.of("K"), mcr);
        } else if (k instanceof PdfArray) {
            ((PdfArray) k).add(mcr);
        } else {
            PdfArray arr = new PdfArray();
            arr.add(k);
            arr.add(mcr);
            dict.set(PdfName.of("K"), arr);
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
        PdfBase p = resolve(dict.get("P"));
        if (p instanceof PdfDictionary) {
            PdfDictionary pd = (PdfDictionary) p;
            if (pd.get("S") != null) return new StructureElement(pd, parser);
        }
        return null;
    }

    private static PdfBase resolve(PdfBase obj) {
        if (obj instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
