package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * GoToE (Go-To-Embedded) action — navigates to a destination in an embedded PDF
 * (ISO 32000-1:2008, §12.6.4.4). Used for PDF portfolio/collection documents.
 */
public class GoToEmbeddedAction extends PdfAction {

    /**
     * Parses a GoToEmbeddedAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public GoToEmbeddedAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the destination in the embedded document (/D).
     *
     * @return the destination object
     */
    public COSBase getDestination() {
        return resolve(actionDict.get("D"));
    }

    /**
     * Returns the target specification for the embedded file (/T).
     *
     * @return the target dictionary, or {@code null}
     */
    public COSDictionary getTarget() {
        COSBase t = resolve(actionDict.get("T"));
        return (t instanceof COSDictionary) ? (COSDictionary) t : null;
    }

    /**
     * Returns whether a new window should be opened.
     *
     * @return {@code true} if /NewWindow is true
     */
    public boolean isNewWindow() {
        return actionDict.getBoolean("NewWindow", false);
    }
}
