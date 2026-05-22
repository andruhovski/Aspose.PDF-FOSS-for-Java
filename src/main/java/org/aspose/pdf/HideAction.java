package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

/**
 * Hide action — shows or hides annotations (ISO 32000-1:2008, §12.6.4.10).
 * /T identifies annotations by name (string) or array of names/references.
 * /H = true means hide (default), false means show.
 */
public class HideAction extends PdfAction {

    /**
     * Parses a HideAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public HideAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a HideAction targeting a single annotation by name.
     *
     * @param annotationName the annotation field name
     * @param hide           {@code true} to hide, {@code false} to show
     */
    public HideAction(String annotationName, boolean hide) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("Hide"));
        actionDict.set(COSName.of("T"), new COSString(annotationName));
        actionDict.setBoolean("H", hide);
    }

    /**
     * Creates a HideAction targeting multiple annotations by name.
     *
     * @param annotationNames the annotation field names
     * @param hide            {@code true} to hide, {@code false} to show
     */
    public HideAction(String[] annotationNames, boolean hide) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("Hide"));
        COSArray arr = new COSArray();
        for (String name : annotationNames) {
            arr.add(new COSString(name));
        }
        actionDict.set(COSName.of("T"), arr);
        actionDict.setBoolean("H", hide);
    }

    /**
     * Returns whether this action hides annotations (default {@code true}).
     *
     * @return {@code true} if hiding, {@code false} if showing
     */
    public boolean isHide() {
        return actionDict.getBoolean("H", true);
    }

    /**
     * Sets whether this action hides or shows annotations.
     *
     * @param hide {@code true} to hide, {@code false} to show
     */
    public void setHide(boolean hide) {
        actionDict.setBoolean("H", hide);
    }

    /**
     * Returns the annotation name(s) targeted by this action.
     *
     * @return array of annotation names (may be empty)
     */
    public String[] getAnnotationNames() {
        COSBase t = resolve(actionDict.get("T"));
        if (t instanceof COSString) {
            return new String[]{((COSString) t).getString()};
        }
        if (t instanceof COSArray) {
            COSArray arr = (COSArray) t;
            String[] names = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = resolve(arr.get(i));
                names[i] = (item instanceof COSString) ? ((COSString) item).getString() : "";
            }
            return names;
        }
        return new String[0];
    }
}
