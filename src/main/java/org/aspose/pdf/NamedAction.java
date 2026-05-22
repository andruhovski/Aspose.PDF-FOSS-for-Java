package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

/**
 * Named action — predefined action (ISO 32000-1:2008, §12.6.4.11).
 * <p>
 * Standard names: "NextPage", "PrevPage", "FirstPage", "LastPage".
 * </p>
 */
public class NamedAction extends PdfAction {

    /**
     * Creates a NamedAction with the given name.
     *
     * @param name the action name (e.g., "NextPage")
     */
    public NamedAction(String name) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("Named"));
        actionDict.set(COSName.of("N"), COSName.of(name));
    }

    /**
     * Parses a NamedAction from a dictionary.
     *
     * @param dict the action dictionary
     */
    public NamedAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the action name.
     *
     * @return the name (e.g., "NextPage")
     */
    public String getActionName() {
        return actionDict.getNameAsString("N");
    }
}
