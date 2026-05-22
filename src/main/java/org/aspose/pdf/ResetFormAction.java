package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

/**
 * ResetForm action — resets form fields to default values
 * (ISO 32000-1:2008, §12.6.4.15).
 */
public class ResetFormAction extends PdfAction {

    /** Include/Exclude flag (bit 1 of /Flags). */
    public static final int FLAG_INCLUDE_EXCLUDE = 0x01;

    /**
     * Parses a ResetFormAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public ResetFormAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a ResetFormAction that resets all fields.
     */
    public ResetFormAction() {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("ResetForm"));
    }

    /**
     * Returns the flags (bit 0 = include/exclude semantics).
     *
     * @return the flags bitmask
     */
    public int getFlags() { return actionDict.getInt("Flags", 0); }

    /**
     * Sets the flags.
     *
     * @param flags the flags bitmask
     */
    public void setFlags(int flags) { actionDict.setInt("Flags", flags); }

    /**
     * Returns the field list, or {@code null} for all fields.
     *
     * @return the fields array, or {@code null}
     */
    public COSArray getFields() {
        COSBase f = resolve(actionDict.get("Fields"));
        return (f instanceof COSArray) ? (COSArray) f : null;
    }

    /**
     * Sets the field list.
     *
     * @param fields the fields array (or {@code null} for all fields)
     */
    public void setFields(COSArray fields) {
        actionDict.set(COSName.of("Fields"), fields);
    }
}
