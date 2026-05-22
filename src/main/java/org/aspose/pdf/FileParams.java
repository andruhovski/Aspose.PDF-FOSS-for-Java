package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;

/**
 * Embedded file parameters (ISO 32000-1:2008, §7.11.4, Table 46).
 */
public class FileParams {

    private final COSDictionary dict;

    /** Wraps an existing params dictionary. */
    public FileParams(COSDictionary dict) {
        this.dict = dict != null ? dict : new COSDictionary();
    }

    /** Creates new empty params. */
    public FileParams() { this.dict = new COSDictionary(); }

    /** /Size — uncompressed file size in bytes. */
    public long getSize() { return dict.getLong("Size", 0); }

    /** /CreationDate. */
    public String getCreationDate() {
        COSBase d = dict.get("CreationDate");
        return (d instanceof COSString) ? ((COSString) d).getString() : null;
    }

    /** /ModDate. */
    public String getModDate() {
        COSBase d = dict.get("ModDate");
        return (d instanceof COSString) ? ((COSString) d).getString() : null;
    }

    /** /CheckSum — MD5 digest. */
    public String getCheckSum() {
        COSBase cs = dict.get("CheckSum");
        return (cs instanceof COSString) ? ((COSString) cs).getString() : null;
    }

    /** Returns the underlying dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }
}
