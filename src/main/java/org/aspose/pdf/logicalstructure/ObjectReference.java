package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;

import java.io.IOException;

/**
 * Object reference — links a structure element to a PDF object such as an annotation
 * (ISO 32000-1:2008, §14.7.4.3).
 *
 * <p>Corresponds to a /Type /OBJR dictionary entry in the /K array.</p>
 */
public class ObjectReference {

    private final COSDictionary referencedObject;
    private final COSDictionary page;

    /**
     * Creates an object reference.
     *
     * @param referencedObject the referenced object dictionary
     * @param page             the page dictionary (may be null)
     */
    public ObjectReference(COSDictionary referencedObject, COSDictionary page) {
        this.referencedObject = referencedObject;
        this.page = page;
    }

    /** Returns the referenced object dictionary. */
    public COSDictionary getReferencedObject() { return referencedObject; }

    /** Returns the page dictionary, or {@code null}. */
    public COSDictionary getPage() { return page; }

    /**
     * Parses an OBJR from a COS dictionary.
     *
     * @param dict the OBJR dictionary
     * @return the object reference
     */
    public static ObjectReference fromDictionary(COSDictionary dict) {
        COSBase obj = resolve(dict.get("Obj"));
        COSBase pg = resolve(dict.get("Pg"));
        return new ObjectReference(
                (obj instanceof COSDictionary) ? (COSDictionary) obj : null,
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
