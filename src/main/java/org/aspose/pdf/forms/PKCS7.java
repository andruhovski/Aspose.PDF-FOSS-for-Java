package org.aspose.pdf.forms;

import java.io.InputStream;

/**
 * PKCS#7 SHA-1 signature for PDF (ISO 32000-1:2008, §12.8.3.3.2).
 * <p>
 * Uses the /SubFilter value {@code adbe.pkcs7.sha1}. In this format,
 * the SHA-1 digest of the byte range is embedded as the data content
 * within the PKCS#7 SignedData structure (non-detached).
 * This is a legacy format; prefer {@link PKCS7Detached}.
 * </p>
 */
public class PKCS7 extends Signature {

    /**
     * Creates a PKCS#7 SHA-1 signature from a PFX/P12 file.
     *
     * @param pfxPath  path to the PKCS#12 key store file
     * @param password the key store password
     */
    public PKCS7(String pfxPath, String password) {
        super(pfxPath, password);
    }

    /**
     * Creates a PKCS#7 SHA-1 signature from a PFX/P12 stream.
     *
     * @param pfxStream the PKCS#12 input stream
     * @param password  the key store password
     */
    public PKCS7(InputStream pfxStream, String password) {
        super(pfxStream, password);
    }

    @Override
    public String getSubFilter() {
        return "adbe.pkcs7.sha1";
    }
}
