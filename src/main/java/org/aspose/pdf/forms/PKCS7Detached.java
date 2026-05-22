package org.aspose.pdf.forms;

import java.io.InputStream;

/**
 * PKCS#7 detached signature for PDF (ISO 32000-1:2008, §12.8.3.3.1).
 * <p>
 * Uses the /SubFilter value {@code adbe.pkcs7.detached}. This is the most
 * common and recommended signature format for PDF documents. The signed
 * data is the byte range of the PDF file excluding the /Contents value.
 * </p>
 */
public class PKCS7Detached extends Signature {

    /**
     * Creates a PKCS#7 detached signature from a PFX/P12 file.
     *
     * @param pfxPath  path to the PKCS#12 key store file
     * @param password the key store password
     */
    public PKCS7Detached(String pfxPath, String password) {
        super(pfxPath, password);
    }

    /**
     * Creates a PKCS#7 detached signature from a PFX/P12 stream.
     *
     * @param pfxStream the PKCS#12 input stream
     * @param password  the key store password
     */
    public PKCS7Detached(InputStream pfxStream, String password) {
        super(pfxStream, password);
    }

    @Override
    public String getSubFilter() {
        return "adbe.pkcs7.detached";
    }
}
