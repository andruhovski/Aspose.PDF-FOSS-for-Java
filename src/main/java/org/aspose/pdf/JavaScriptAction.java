package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JavaScript action — stores a JavaScript script (ISO 32000-1:2008, §12.6.4.16).
 * The script is stored in /JS as a text string or stream.
 *
 * <p><b>NOTE:</b> This class stores and exposes the script but does NOT execute it.</p>
 */
public class JavaScriptAction extends PdfAction {

    /**
     * Parses a JavaScriptAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public JavaScriptAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a JavaScriptAction with the given script.
     *
     * @param script the JavaScript source code
     */
    public JavaScriptAction(String script) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("JavaScript"));
        actionDict.set(PdfName.of("JS"), new PdfString(script));
    }

    /**
     * Returns the JavaScript source code.
     *
     * @return the script string, or {@code null}
     */
    public String getScript() {
        PdfBase js = resolve(actionDict.get("JS"));
        if (js instanceof PdfString) return ((PdfString) js).getString();
        if (js instanceof PdfStream) {
            try {
                byte[] data = ((PdfStream) js).getDecodedData();
                return new String(data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        }
        return null;
    }

    /**
     * Sets the JavaScript source code (as a text string).
     *
     * @param script the JavaScript source code
     */
    public void setScript(String script) {
        actionDict.set(PdfName.of("JS"), new PdfString(script));
    }
}
