package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

/**
 * Rendition action — controls multimedia renditions
 * (ISO 32000-1:2008, §12.6.4.13).
 */
public class RenditionAction extends PdfAction {

    /** Play if not playing, stop if playing. */
    public static final int OP_PLAY_STOP = 0;
    /** Stop. */
    public static final int OP_STOP = 1;
    /** Pause. */
    public static final int OP_PAUSE = 2;
    /** Resume. */
    public static final int OP_RESUME = 3;
    /** Play. */
    public static final int OP_PLAY = 4;

    /**
     * Parses a RenditionAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public RenditionAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Returns the operation code (0–4).
     *
     * @return the operation code
     */
    public int getOperation() {
        return actionDict.getInt("OP", 0);
    }

    /**
     * Returns the rendition dictionary (/R).
     *
     * @return the rendition dictionary, or {@code null}
     */
    public PdfDictionary getRendition() {
        PdfBase r = resolve(actionDict.get("R"));
        return (r instanceof PdfDictionary) ? (PdfDictionary) r : null;
    }

    /**
     * Returns the screen annotation associated with this action (/AN).
     *
     * @return the annotation dictionary, or {@code null}
     */
    public PdfDictionary getAnnotation() {
        PdfBase an = resolve(actionDict.get("AN"));
        return (an instanceof PdfDictionary) ? (PdfDictionary) an : null;
    }
}
