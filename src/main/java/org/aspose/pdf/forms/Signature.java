package org.aspose.pdf.forms;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Logger;

/// Abstract base class for PDF digital signature types (ISO 32000-1:2008, §12.8).
///
/// Subclasses represent specific signature formats:
///
///   - [PKCS7Detached] — PKCS#7 detached (adbe.pkcs7.detached)
///   - [PKCS7] — PKCS#7 SHA-1 (adbe.pkcs7.sha1)
///   - [PKCS1] — PKCS#1 RSA (adbe.x509.rsa\_sha1)
public abstract class Signature {

    private static final Logger LOG = Logger.getLogger(Signature.class.getName());

    /// The loaded KeyStore from PFX/P12.
    protected KeyStore keyStore;
    /// The alias in the KeyStore.
    protected String alias;
    /// The password for the key.
    protected char[] password;

    /// Signing reason.
    private String reason;
    /// Signing location.
    private String location;
    /// Contact info.
    private String contactInfo;
    /// Signer name override (if null, extracted from certificate).
    private String name;
    /// Signing date override.
    private Date date;

    /// Constructs a Signature by loading a PFX/P12 KeyStore from a file path.
    ///
    /// @param pfxPath  path to the PKCS#12 file
    /// @param password the keystore password
    protected Signature(String pfxPath, String password) {
        this.password = password != null ? password.toCharArray() : new char[0];
        try {
            this.keyStore = KeyStore.getInstance("PKCS12");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(pfxPath)) {
                this.keyStore.load(fis, this.password);
            }
            this.alias = this.keyStore.aliases().nextElement();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PFX file: " + pfxPath, e);
        }
    }

    /// Constructs a Signature by loading a PFX/P12 KeyStore from a stream.
    ///
    /// @param pfxStream the PKCS#12 input stream
    /// @param password  the keystore password
    protected Signature(InputStream pfxStream, String password) {
        this.password = password != null ? password.toCharArray() : new char[0];
        try {
            this.keyStore = KeyStore.getInstance("PKCS12");
            this.keyStore.load(pfxStream, this.password);
            this.alias = this.keyStore.aliases().nextElement();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PFX stream", e);
        }
    }

    /// Returns the private key from the loaded KeyStore.
    ///
    /// @return the private key
    /// @throws Exception if key extraction fails
    public PrivateKey getPrivateKey() throws Exception {
        return (PrivateKey) keyStore.getKey(alias, password);
    }

    /// Returns the signer's X.509 certificate from the loaded KeyStore.
    ///
    /// @return the certificate
    /// @throws Exception if certificate extraction fails
    public X509Certificate getCertificate() throws Exception {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    /// Returns the certificate chain from the loaded KeyStore.
    ///
    /// @return the certificate chain, or null
    /// @throws Exception if chain extraction fails
    public X509Certificate[] getCertificateChain() throws Exception {
        java.security.cert.Certificate[] chain = keyStore.getCertificateChain(alias);
        if (chain == null) return null;
        X509Certificate[] result = new X509Certificate[chain.length];
        for (int i = 0; i < chain.length; i++) {
            result[i] = (X509Certificate) chain[i];
        }
        return result;
    }

    /// Returns the PDF sub-filter name for this signature type.
    ///
    /// @return the sub-filter string
    public abstract String getSubFilter();

    /// Returns the signing reason.
    public String getReason() { return reason; }
    /// Sets the signing reason.
    public void setReason(String reason) { this.reason = reason; }

    /// Returns the signing location.
    public String getLocation() { return location; }
    /// Sets the signing location.
    public void setLocation(String location) { this.location = location; }

    /// Returns the contact info.
    public String getContactInfo() { return contactInfo; }
    /// Sets the contact info.
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    /// Returns the signer name override.
    public String getName() { return name; }
    /// Sets the signer name override.
    public void setName(String name) { this.name = name; }

    /// Returns the signing date.
    public Date getDate() { return date; }
    /// Sets the signing date.
    public void setDate(Date date) { this.date = date; }

    /// Custom appearance settings for the signature visual representation.
    private SignatureCustomAppearance customAppearance;

    /// Returns the custom appearance settings for the signature.
    ///
    /// @return the custom appearance, or null if not set
    public SignatureCustomAppearance getCustomAppearance() {
        return customAppearance;
    }

    /// Sets the custom appearance settings for the signature.
    ///
    /// @param appearance the custom appearance settings
    public void setCustomAppearance(SignatureCustomAppearance appearance) {
        this.customAppearance = appearance;
    }
}
