package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

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
    public GenericAction(PdfDictionary dict) {
        this.actionDict = dict;
    }
}
