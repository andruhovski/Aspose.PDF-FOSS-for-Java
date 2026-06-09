package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/**
 * SetOCGState action — changes the state of Optional Content Groups
 * (ISO 32000-1:2008, §12.6.4.12).
 * /State is an array of [name ocg ocg ... name ocg ...] where name is ON/OFF/Toggle.
 */
public class SetOCGStateAction extends PdfAction {

    /**
     * Parses a SetOCGStateAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public SetOCGStateAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the state change array.
     * Entries alternate between command names (ON/OFF/Toggle) and OCG references.
     *
     * @return the state array, or {@code null}
     */
    public PdfArray getState() {
        PdfBase s = resolve(actionDict.get("State"));
        return (s instanceof PdfArray) ? (PdfArray) s : null;
    }

    /**
     * Sets the state change array.
     *
     * @param state the state array
     */
    public void setState(PdfArray state) {
        actionDict.set(PdfName.of("State"), state);
    }

    /**
     * Returns whether radio-button group relationships are preserved.
     *
     * @return {@code true} if preserved (default)
     */
    public boolean isPreserveRB() {
        return actionDict.getBoolean("PreserveRB", true);
    }
}
