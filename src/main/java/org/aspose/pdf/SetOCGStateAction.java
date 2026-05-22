package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

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
    public SetOCGStateAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the state change array.
     * Entries alternate between command names (ON/OFF/Toggle) and OCG references.
     *
     * @return the state array, or {@code null}
     */
    public COSArray getState() {
        COSBase s = resolve(actionDict.get("State"));
        return (s instanceof COSArray) ? (COSArray) s : null;
    }

    /**
     * Sets the state change array.
     *
     * @param state the state array
     */
    public void setState(COSArray state) {
        actionDict.set(COSName.of("State"), state);
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
