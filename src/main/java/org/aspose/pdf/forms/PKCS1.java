package org.aspose.pdf.forms;

import java.io.InputStream;

/// PKCS#1 RSA signature for PDF (ISO 32000-1:2008, §12.8.3.2).
///
/// Uses the /SubFilter value `adbe.x509.rsa_sha1`. This is a legacy
/// format where the certificate is stored separately in /Cert and the
/// /Contents holds a raw PKCS#1 RSA signature. Prefer [PKCS7Detached].
///
public class PKCS1 extends Signature {

    /// Creates a PKCS#1 RSA signature from a PFX/P12 file.
    ///
    /// @param pfxPath  path to the PKCS#12 key store file
    /// @param password the key store password
    public PKCS1(String pfxPath, String password) {
        super(pfxPath, password);
    }

    /// Creates a PKCS#1 RSA signature from a PFX/P12 stream.
    ///
    /// @param pfxStream the PKCS#12 input stream
    /// @param password  the key store password
    public PKCS1(InputStream pfxStream, String password) {
        super(pfxStream, password);
    }

    @Override
    public String getSubFilter() {
        return "adbe.x509.rsa_sha1";
    }
}
