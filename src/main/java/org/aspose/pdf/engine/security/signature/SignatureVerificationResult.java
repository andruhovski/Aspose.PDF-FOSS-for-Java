package org.aspose.pdf.engine.security.signature;

import java.security.cert.X509Certificate;
import java.util.Date;

/// Result of verifying a PDF signature.
public class SignatureVerificationResult {

    private final String fieldName;
    private final boolean valid;
    private final String signerName;
    private final Date signingTime;
    private final X509Certificate certificate;
    private final String reason;
    private final String location;

    public SignatureVerificationResult(String fieldName, boolean valid, String signerName,
                                       Date signingTime, X509Certificate certificate,
                                       String reason, String location) {
        this.fieldName = fieldName;
        this.valid = valid;
        this.signerName = signerName;
        this.signingTime = signingTime;
        this.certificate = certificate;
        this.reason = reason;
        this.location = location;
    }

    public String getFieldName() { return fieldName; }
    public boolean isValid() { return valid; }
    public String getSignerName() { return signerName; }
    public Date getSigningTime() { return signingTime; }
    public X509Certificate getCertificate() { return certificate; }
    public String getReason() { return reason; }
    public String getLocation() { return location; }
}
