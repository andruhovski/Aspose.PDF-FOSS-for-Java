package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;

import java.io.IOException;

/**
 * Marked content reference — links a structure element to marked content
 * in a page's content stream (ISO 32000-1:2008, §14.7.4.2).
 *
 * <p>Corresponds to a /Type /MCR dictionary entry in the /K array,
 * or a bare integer MCID.</p>
 */
public class MarkedContentReference {

    private final int mcid;
    private final COSDictionary page;

    /**
     * Creates a marked content reference.
     *
     * @param mcid the marked content identifier
     * @param page the page dictionary (may be null if inherited)
     */
    public MarkedContentReference(int mcid, COSDictionary page) {
        this.mcid = mcid;
        this.page = page;
    }

    /** Returns the marked content identifier (MCID). */
    public int getMCID() { return mcid; }

    /** Returns the page dictionary, or {@code null} if inherited from parent. */
    public COSDictionary getPage() { return page; }

    /**
     * Parses a MCR from a COS dictionary.
     *
     * @param dict the MCR dictionary
     * @return the marked content reference
     */
    public static MarkedContentReference fromDictionary(COSDictionary dict) {
        int mcid = dict.getInt("MCID", -1);
        COSBase pg = resolve(dict.get("Pg"));
        return new MarkedContentReference(mcid,
                (pg instanceof COSDictionary) ? (COSDictionary) pg : null);
    }

    private static COSBase resolve(COSBase obj) {
        if (obj instanceof COSObjectReference) {
            try { return ((COSObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
