package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

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
    public ResetFormAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a ResetFormAction that resets all fields.
     */
    public ResetFormAction() {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("ResetForm"));
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
    public PdfArray getFields() {
        PdfBase f = resolve(actionDict.get("Fields"));
        return (f instanceof PdfArray) ? (PdfArray) f : null;
    }

    /**
     * Sets the field list.
     *
     * @param fields the fields array (or {@code null} for all fields)
     */
    public void setFields(PdfArray fields) {
        actionDict.set(PdfName.of("Fields"), fields);
    }
}
