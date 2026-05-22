package org.aspose.pdf.logicalstructure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard structure types for Tagged PDF (ISO 32000-1:2008, §14.8.4, Tables 333–338).
 * Each constant corresponds to a standard structure type name used in the
 * logical structure tree.
 */
public final class StructureTypeStandard {

    private final String name;

    private StructureTypeStandard(String name) { this.name = name; }

    /** Returns the structure type name (e.g., "P", "H1", "Table"). */
    public String getName() { return name; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureTypeStandard)) return false;
        return name.equals(((StructureTypeStandard) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    // ── §14.8.4.2 Grouping elements (Table 333) ─────────────────
    public static final StructureTypeStandard Document = new StructureTypeStandard("Document");
    public static final StructureTypeStandard Part = new StructureTypeStandard("Part");
    public static final StructureTypeStandard Art = new StructureTypeStandard("Art");
    public static final StructureTypeStandard Sect = new StructureTypeStandard("Sect");
    public static final StructureTypeStandard Div = new StructureTypeStandard("Div");
    public static final StructureTypeStandard BlockQuote = new StructureTypeStandard("BlockQuote");
    public static final StructureTypeStandard Caption = new StructureTypeStandard("Caption");
    public static final StructureTypeStandard TOC = new StructureTypeStandard("TOC");
    public static final StructureTypeStandard TOCI = new StructureTypeStandard("TOCI");
    public static final StructureTypeStandard Index = new StructureTypeStandard("Index");
    public static final StructureTypeStandard NonStruct = new StructureTypeStandard("NonStruct");
    public static final StructureTypeStandard Private = new StructureTypeStandard("Private");

    // ── §14.8.4.3 Block-level structure elements (Table 334) ────
    public static final StructureTypeStandard P = new StructureTypeStandard("P");
    public static final StructureTypeStandard H = new StructureTypeStandard("H");
    public static final StructureTypeStandard H1 = new StructureTypeStandard("H1");
    public static final StructureTypeStandard H2 = new StructureTypeStandard("H2");
    public static final StructureTypeStandard H3 = new StructureTypeStandard("H3");
    public static final StructureTypeStandard H4 = new StructureTypeStandard("H4");
    public static final StructureTypeStandard H5 = new StructureTypeStandard("H5");
    public static final StructureTypeStandard H6 = new StructureTypeStandard("H6");

    // ── List elements ────────────────────────────────────────────
    public static final StructureTypeStandard L = new StructureTypeStandard("L");
    public static final StructureTypeStandard LI = new StructureTypeStandard("LI");
    public static final StructureTypeStandard Lbl = new StructureTypeStandard("Lbl");
    public static final StructureTypeStandard LBody = new StructureTypeStandard("LBody");

    // ── §14.8.4.4 Table elements (Table 337) ────────────────────
    public static final StructureTypeStandard Table = new StructureTypeStandard("Table");
    public static final StructureTypeStandard TR = new StructureTypeStandard("TR");
    public static final StructureTypeStandard TH = new StructureTypeStandard("TH");
    public static final StructureTypeStandard TD = new StructureTypeStandard("TD");
    public static final StructureTypeStandard THead = new StructureTypeStandard("THead");
    public static final StructureTypeStandard TBody = new StructureTypeStandard("TBody");
    public static final StructureTypeStandard TFoot = new StructureTypeStandard("TFoot");

    // ── §14.8.4.4 Inline-level elements (Table 338) ─────────────
    public static final StructureTypeStandard Span = new StructureTypeStandard("Span");
    public static final StructureTypeStandard Quote = new StructureTypeStandard("Quote");
    public static final StructureTypeStandard Note = new StructureTypeStandard("Note");
    public static final StructureTypeStandard Reference = new StructureTypeStandard("Reference");
    public static final StructureTypeStandard BibEntry = new StructureTypeStandard("BibEntry");
    public static final StructureTypeStandard Code = new StructureTypeStandard("Code");
    public static final StructureTypeStandard Link = new StructureTypeStandard("Link");
    public static final StructureTypeStandard Annot = new StructureTypeStandard("Annot");

    // ── Illustration elements ────────────────────────────────────
    public static final StructureTypeStandard Figure = new StructureTypeStandard("Figure");
    public static final StructureTypeStandard Formula = new StructureTypeStandard("Formula");
    public static final StructureTypeStandard Form = new StructureTypeStandard("Form");

    // ── Ruby / Warichu ───────────────────────────────────────────
    public static final StructureTypeStandard Ruby = new StructureTypeStandard("Ruby");
    public static final StructureTypeStandard RB = new StructureTypeStandard("RB");
    public static final StructureTypeStandard RT = new StructureTypeStandard("RT");
    public static final StructureTypeStandard RP = new StructureTypeStandard("RP");
    public static final StructureTypeStandard Warichu = new StructureTypeStandard("Warichu");
    public static final StructureTypeStandard WT = new StructureTypeStandard("WT");
    public static final StructureTypeStandard WP = new StructureTypeStandard("WP");

    // ── Lookup map ───────────────────────────────────────────────

    private static final Map<String, StructureTypeStandard> BY_NAME = new LinkedHashMap<>();
    static {
        BY_NAME.put("Document", Document); BY_NAME.put("Part", Part);
        BY_NAME.put("Art", Art); BY_NAME.put("Sect", Sect);
        BY_NAME.put("Div", Div); BY_NAME.put("BlockQuote", BlockQuote);
        BY_NAME.put("Caption", Caption); BY_NAME.put("TOC", TOC);
        BY_NAME.put("TOCI", TOCI); BY_NAME.put("Index", Index);
        BY_NAME.put("NonStruct", NonStruct); BY_NAME.put("Private", Private);
        BY_NAME.put("P", P); BY_NAME.put("H", H);
        BY_NAME.put("H1", H1); BY_NAME.put("H2", H2);
        BY_NAME.put("H3", H3); BY_NAME.put("H4", H4);
        BY_NAME.put("H5", H5); BY_NAME.put("H6", H6);
        BY_NAME.put("L", L); BY_NAME.put("LI", LI);
        BY_NAME.put("Lbl", Lbl); BY_NAME.put("LBody", LBody);
        BY_NAME.put("Table", Table); BY_NAME.put("TR", TR);
        BY_NAME.put("TH", TH); BY_NAME.put("TD", TD);
        BY_NAME.put("THead", THead); BY_NAME.put("TBody", TBody);
        BY_NAME.put("TFoot", TFoot);
        BY_NAME.put("Span", Span); BY_NAME.put("Quote", Quote);
        BY_NAME.put("Note", Note); BY_NAME.put("Reference", Reference);
        BY_NAME.put("BibEntry", BibEntry); BY_NAME.put("Code", Code);
        BY_NAME.put("Link", Link); BY_NAME.put("Annot", Annot);
        BY_NAME.put("Figure", Figure); BY_NAME.put("Formula", Formula);
        BY_NAME.put("Form", Form);
        BY_NAME.put("Ruby", Ruby); BY_NAME.put("RB", RB);
        BY_NAME.put("RT", RT); BY_NAME.put("RP", RP);
        BY_NAME.put("Warichu", Warichu); BY_NAME.put("WT", WT);
        BY_NAME.put("WP", WP);
    }

    /**
     * Looks up a standard structure type by name.
     * Returns the predefined constant if it exists, otherwise creates a custom instance.
     *
     * @param name the structure type name
     * @return the standard type, or a custom instance
     */
    public static StructureTypeStandard fromName(String name) {
        StructureTypeStandard std = BY_NAME.get(name);
        return std != null ? std : new StructureTypeStandard(name);
    }

    /**
     * Returns an unmodifiable map of all standard type names.
     *
     * @return the standard types map
     */
    public static Map<String, StructureTypeStandard> getStandardTypes() {
        return java.util.Collections.unmodifiableMap(BY_NAME);
    }
}
