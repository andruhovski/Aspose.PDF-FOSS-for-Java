package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * Transition action — controls page transitions during presentations
 * (ISO 32000-1:2008, §12.6.4.14).
 */
public class TransitionAction extends PdfAction {

    /**
     * Parses a TransitionAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public TransitionAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the transition dictionary (/Trans).
     *
     * @return the transition dictionary, or {@code null}
     */
    public COSDictionary getTransition() {
        COSBase t = resolve(actionDict.get("Trans"));
        return (t instanceof COSDictionary) ? (COSDictionary) t : null;
    }
}
