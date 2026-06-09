package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;

/**
 * SubmitForm action — submits form data to a URL (ISO 32000-1:2008, §12.6.4.14).
 * /F = URL (file specification), /Fields = field references, /Flags = submission flags.
 */
public class SubmitFormAction extends PdfAction {

    /** Include/Exclude flag (Table 237, bit 1). */
    public static final int FLAG_INCLUDE_EXCLUDE = 0x01;
    /** Include fields with no value (bit 2). */
    public static final int FLAG_INCLUDE_NO_VALUE_FIELDS = 0x02;
    /** Export as HTML form (bit 3). */
    public static final int FLAG_EXPORT_FORMAT = 0x04;
    /** Use HTTP GET (bit 4). */
    public static final int FLAG_GET_METHOD = 0x08;
    /** Submit mouse coordinates (bit 5). */
    public static final int FLAG_SUBMIT_COORDINATES = 0x10;
    /** Submit as XFDF (bit 6). */
    public static final int FLAG_XFDF = 0x20;
    /** Include append-saves (bit 7). */
    public static final int FLAG_INCLUDE_APPEND_SAVES = 0x40;
    /** Include annotations (bit 8). */
    public static final int FLAG_INCLUDE_ANNOTATIONS = 0x80;
    /** Submit as PDF (bit 9). */
    public static final int FLAG_SUBMIT_PDF = 0x100;
    /** Canonical date format (bit 10). */
    public static final int FLAG_CANONICAL_FORMAT = 0x200;
    /** Exclude non-user annotations (bit 11). */
    public static final int FLAG_EXCL_NON_USER_ANNOTS = 0x400;
    /** Exclude F key (bit 12). */
    public static final int FLAG_EXCL_F_KEY = 0x800;
    /** Embed form in submission (bit 14). */
    public static final int FLAG_EMBED_FORM = 0x2000;

    /**
     * Parses a SubmitFormAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public SubmitFormAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a SubmitFormAction for the given URL.
     *
     * @param url the submission URL
     */
    public SubmitFormAction(String url) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("SubmitForm"));
        PdfDictionary fileSpec = new PdfDictionary();
        fileSpec.set(PdfName.of("Type"), PdfName.of("Filespec"));
        fileSpec.set(PdfName.of("F"), new PdfString(url));
        actionDict.set(PdfName.of("F"), fileSpec);
    }

    /**
     * Returns the submission URL.
     *
     * @return the URL string, or {@code null}
     */
    public String getUrl() {
        PdfBase f = resolve(actionDict.get("F"));
        if (f instanceof PdfString) return ((PdfString) f).getString();
        if (f instanceof PdfDictionary) return ((PdfDictionary) f).getString("F");
        return null;
    }

    /**
     * Returns the submission flags.
     *
     * @return the flags bitmask
     */
    public int getFlags() { return actionDict.getInt("Flags", 0); }

    /**
     * Sets the submission flags.
     *
     * @param flags the flags bitmask
     */
    public void setFlags(int flags) { actionDict.setInt("Flags", flags); }

    /**
     * Returns the field name/reference list, or {@code null} if all fields.
     *
     * @return the fields array, or {@code null}
     */
    public PdfArray getFields() {
        PdfBase fields = resolve(actionDict.get("Fields"));
        return (fields instanceof PdfArray) ? (PdfArray) fields : null;
    }
}
