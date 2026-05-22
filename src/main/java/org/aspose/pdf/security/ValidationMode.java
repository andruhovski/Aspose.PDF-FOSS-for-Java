package org.aspose.pdf.security;

/**
 * Specifies the mode used for certificate validation during signature verification.
 */
public enum ValidationMode {

    /**
     * Validate all certificates online (OCSP/CRL).
     */
    OnlineAll,

    /**
     * Only check online if certificates contain OCSP/CRL URLs.
     */
    OnlineOnlyIfCertsHaveUrls,

    /**
     * Perform only offline validation (no network access).
     */
    Offline,

    /**
     * Strict validation: check certificate chain, revocation, and trust.
     */
    Strict
}
