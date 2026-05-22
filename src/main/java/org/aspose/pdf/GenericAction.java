package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSDictionary;

/**
 * Represents a PDF action of an unknown or unsupported type.
 * Preserves the raw dictionary for round-tripping.
 */
public class GenericAction extends PdfAction {

    /**
     * Creates a GenericAction wrapping the given dictionary.
     *
     * @param dict the action dictionary
     */
    public GenericAction(COSDictionary dict) {
        this.actionDict = dict;
    }
}
